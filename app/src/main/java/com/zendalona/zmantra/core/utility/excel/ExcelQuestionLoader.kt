package com.zendalona.zmantra.core.utility.excel

import android.content.Context
import android.util.Log
import com.zendalona.zmantra.domain.model.GameQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.objecthunter.exp4j.ExpressionBuilder
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.*
import kotlin.random.Random
import kotlin.text.iterator

object ExcelQuestionLoader {

    private const val TAG = "ExcelQuestionLoader"

    private fun extractVariables(input: String): List<String> {
        val regex = Regex("""([a-zA-Z])[^*]*\*""")
        val matches = regex.findAll(input)
        return matches.map { it.groupValues[1] }.distinct().toList()
    }

    private fun parseInputRange(inputRange: String, mode: String): List<Any> {
        val operands = mutableListOf<Any>()
        var current = ""
        for (c in inputRange) {
            if (c == '*') {
                if (current.isNotBlank()) {
                    operands.add(parseSingleOperand(current.trim(), mode))
                    current = ""
                }
            } else {
                current += c
            }
        }
        if (current.isNotBlank()) operands.add(parseSingleOperand(current.trim(), mode))
        return operands
    }

    private fun parseSingleOperand(input: String, mode: String): Any {
        return try {
            if (mode in listOf("direction", "drawing")) {
                input.drop(1).split(",").random().trim()
            } else {
                val cleaned = input.filter { it.isDigit() || it == ',' || it == ':' || it == ';' }
                when {
                    ";" in cleaned -> {
                        val (left, right) = cleaned.split(";").map { it.trim() }
                        val leftVal = parseValue(left)
                        val rightVal = parseValue(right)
                        leftVal * rightVal
                    }
                    "," in cleaned -> cleaned.split(",").map { it.toInt() }.random()
                    ":" in cleaned -> {
                        val (start, end) = cleaned.split(":").map { it.toInt() }
                        Random.nextInt(start, end + 1)
                    }
                    else -> cleaned.toIntOrNull() ?: 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing operand: $input", e)
            0
        }
    }

    private fun parseValue(value: String): Int {
        return when {
            "," in value -> value.split(",").map { it.toInt() }.random()
            ":" in value -> {
                val (start, end) = value.split(":").map { it.toInt() }
                Random.nextInt(start, end + 1)
            }
            else -> value.toIntOrNull() ?: 0
        }
    }

    private fun replaceVariables(template: String, variables: List<String>, values: List<Any>): String {
        var updated = template
        for (i in variables.indices) {
            updated = updated.replace("{${variables[i]}}", values[i].toString())
        }
        return updated
    }

    private fun evaluateEquation(equation: String): Int {
        return try {
            ExpressionBuilder(equation).build().evaluate().toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to evaluate: $equation", e)
            0
        }
    }

    /** ✅ Called by BaseGameFragment */
    suspend fun loadQuestionsFromExcel(
        context: Context,
        lang: String,
        mode: String,
        difficulty: String
    ): List<GameQuestion> = withContext(Dispatchers.IO) {
        val fileName = "questions/${lang.lowercase(Locale.ROOT)}.xlsx"
        try {
            context.assets.open(fileName).use { stream ->
                val workbook = WorkbookFactory.create(stream)
                val sheet = workbook.getSheetAt(0)
                val questions = loadQuestionsFromSheet(sheet, mode, difficulty)
                workbook.close()
                questions
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading from Excel", e)
            emptyList()
        }
    }

    /** ✅ Called from splash screen preload cache */
    fun loadQuestionsFromSheet(
        sheet: Sheet,
        mode: String,
        difficulty: String
    ): List<GameQuestion> {
        val questions = mutableListOf<GameQuestion>()
        for (row in sheet) {
            if (row.rowNum == 0) continue

            val template = row.getCell(0)?.toString()
            val rowMode = row.getCell(1)?.toString()?.trim()?.lowercase(Locale.ROOT)
            val operand = row.getCell(2)?.toString()
            val diff = row.getCell(3)?.toString()?.toDoubleOrNull()?.toInt()?.toString()
            val answerTemplate = row.getCell(4)?.toString()
            val timeLimit = row.getCell(5)?.numericCellValue?.toInt() ?: 20

            if (template == null || rowMode == null || operand == null || diff == null || answerTemplate == null) {
                continue
            }

            if (rowMode == mode.lowercase(Locale.ROOT) && diff == difficulty) {
                val variables = extractVariables(operand)
                val values = parseInputRange(operand, mode)
                if (variables.size != values.size) continue

                val renderedQ = replaceVariables(template, variables, values)
                val renderedExpr = replaceVariables(answerTemplate, variables, values)
                val answer = if (mode in listOf("direction", "drawing")) 0 else evaluateEquation(renderedExpr)

                questions.add(GameQuestion(renderedQ, answer, timeLimit))
            }
        }
        return questions
    }

    /** ✅ Used once in splash preload */
    suspend fun loadWorkbook(context: Context, lang: String): Workbook {
        val fileName = "questions/${lang.lowercase(Locale.ROOT)}.xlsx"
        return withContext(Dispatchers.IO) {
            val inputStream = context.assets.open(fileName)
            WorkbookFactory.create(inputStream)
        }
    }
}
