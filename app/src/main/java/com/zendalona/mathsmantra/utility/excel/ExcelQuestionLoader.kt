package com.zendalona.mathsmantra.utility.excel

import android.content.Context
import android.util.Log
import com.zendalona.mathsmantra.model.GameQuestion
import net.objecthunter.exp4j.ExpressionBuilder
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.*
import kotlin.random.Random

object ExcelQuestionLoader {

    private const val TAG = "ExcelQuestionLoader"

    private fun extractVariables(input: String): List<String> {
        val set = mutableSetOf<String>()
        for (c in input) {
            if (c.isLetter()) set.add(c.toString())
        }
        Log.d(TAG, "Extracted variables: $set from input: $input")
        return set.toList()
    }

    private fun parseInputRange(inputRange: String): List<Int> {
        Log.d(TAG, "Parsing input range: $inputRange")
        val operands = mutableListOf<Int>()
        var current = ""

        for (c in inputRange) {
            if (c == '*') {
                operands.add(parseSingleOperand(current))
                current = ""
            } else {
                current += c
            }
        }

        if (current.isNotEmpty()) {
            operands.add(parseSingleOperand(current))
        }

        Log.d(TAG, "Parsed operands: $operands")
        return operands
    }

    private fun parseSingleOperand(input: String): Int {
        // Remove alphabetic characters before parsing
        val cleanedInput = input.filter { it.isDigit() || it == ',' || it == ':' || it == ';' }
        return try {
            when {
                cleanedInput.contains(",") -> {
                    val options = cleanedInput.split(",").map { it.trim().toInt() }
                    options.random().also { Log.d(TAG, "Random from list $options: $it") }
                }
                cleanedInput.contains(":") -> {
                    val (start, end) = cleanedInput.split(":").map { it.trim().toInt() }
                    Random.nextInt(start, end + 1).also {
                        Log.d(TAG, "Random from range $start:$end → $it")
                    }
                }
                cleanedInput.contains(";") -> {
                    val parts = cleanedInput.split(";").map { it.trim().toInt() }
                    if (parts.size == 3) {
                        val (a, b, c) = parts
                        val value = a * Random.nextInt(b, c + 1)
                        Log.d(TAG, "Parsed and computed ($a * random($b to $c)) → $value")
                        value
                    } else {
                        Log.w(TAG, "Invalid format for semicolon input: $cleanedInput")
                        0
                    }
                }
                else -> cleanedInput.trim().toIntOrNull()?.also {
                    Log.d(TAG, "Parsed single int: $it")
                } ?: 0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing operand: $input (cleaned: $cleanedInput)", e)
            0
        }
    }

    private fun replaceVariables(template: String, variables: List<String>, values: List<Int>): String {
        var updated = template
        for (i in variables.indices) {
            updated = updated.replace("{${variables[i]}}", values[i].toString())
        }
        Log.d(TAG, "Template: $template → Updated: $updated")
        return updated
    }

    private fun evaluateEquation(equation: String): Int {
        return try {
            val result = ExpressionBuilder(equation)
                .build()
                .evaluate()
            Log.d(TAG, "Evaluating: $equation = $result")
            result.toInt()
        } catch (e: Exception) {
            Log.e(TAG, "Evaluation failed for: $equation", e)
            0
        }
    }

    fun loadQuestionsFromExcel(
        context: Context,
        lang: String,
        mode: String,
        difficulty: String
    ): List<GameQuestion> {
        val questions = mutableListOf<GameQuestion>()
        val fileName = "questions/${lang.lowercase(Locale.ROOT)}.xlsx"
        Log.d(TAG, "Loading questions from: $fileName [mode: $mode, difficulty: $difficulty]")

        try {
            context.assets.open(fileName).use { inputStream ->
                val workbook = WorkbookFactory.create(inputStream)
                val sheet = workbook.getSheetAt(0)

                for (row in sheet) {
                    if (row.rowNum == 0) {
                        Log.d(TAG, "Skipping header row ${row.rowNum}")
                        // no continue, just skip processing rest of loop body by if/else
                    } else {
                        Log.d(TAG, "Processing row ${row.rowNum}")

                        val questionTemplate = row.getCell(0)?.toString()
                        val rowModeRaw = row.getCell(1)?.toString()
                        val operandRaw = row.getCell(2)?.toString()
                        val rowDifficultyNum = row.getCell(3)?.toString()
                        val answerExpressionTemplate = row.getCell(4)?.toString()
                        val timeLimitNum = row.getCell(5)?.numericCellValue ?: 20.0

                        if (questionTemplate == null || rowModeRaw == null || operandRaw == null
                            || rowDifficultyNum == null || answerExpressionTemplate == null) {
                            Log.w(TAG, "Row ${row.rowNum} has null/missing required fields. Skipping.")
                        } else {
                            val rowMode = rowModeRaw.trim().lowercase(Locale.ROOT)
                            val rowDifficulty = rowDifficultyNum.toDoubleOrNull()?.toInt()?.toString()
                            val timeLimit = timeLimitNum.toInt()

                            Log.d(TAG, "Row $row rowMode=$rowMode, rowDifficulty=$rowDifficulty")

                            if (rowMode == mode.lowercase(Locale.ROOT) && rowDifficulty == difficulty) {
                                Log.d(TAG, "✅ Matching row found at rowNum=${row.rowNum}")

                                val variables = extractVariables(operandRaw)
                                val operands = parseInputRange(operandRaw)

                                if (variables.size != operands.size) {
                                    Log.w(TAG, "Variable and operand count mismatch at row ${row.rowNum}: variables=${variables.size}, operands=${operands.size}")
                                } else {
                                    val renderedQuestion = replaceVariables(questionTemplate, variables, operands)
                                    val renderedEquation = replaceVariables(answerExpressionTemplate, variables, operands)
                                    val answer = evaluateEquation(renderedEquation)

                                    questions.add(GameQuestion(renderedQuestion, answer, timeLimit))
                                    Log.d(TAG, "Added question from row ${row.rowNum}")
                                }
                            } else {
                                Log.d(TAG, "❌ Skipping row ${row.rowNum} due to mode/difficulty mismatch: mode=$rowMode vs $mode, difficulty=$rowDifficulty vs $difficulty")
                            }
                        }
                    }
                }

                workbook.close()
                Log.d(TAG, "Loaded total ${questions.size} valid questions")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading questions from Excel", e)
        }

        return questions
    }
}