package com.zendalona.zmantra.utility.excel

import android.content.Context
import android.util.Log
import com.zendalona.zmantra.model.GameQuestion
import net.objecthunter.exp4j.ExpressionBuilder
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.util.*
import kotlin.random.Random

object ExcelQuestionLoader {

    private const val TAG = "ExcelQuestionLoader"

    // Extract variable names like "a", "b", etc., before * (e.g., a1:9*, b1:9*)
    private fun extractVariables(input: String): List<String> {
        val regex = Regex("""([a-zA-Z])[^*]*\*""")
        val matches = regex.findAll(input)
        val variables = matches.map { it.groupValues[1] }.distinct().toList()
        Log.d(TAG, "Extracted variables: $variables from input: $input")
        return variables
    }

    // Parse operands based on mode (some use words, others use numbers)
    private fun parseInputRange(inputRange: String, mode: String): List<Any> {
        Log.d(TAG, "Parsing input range: $inputRange")
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

        if (current.isNotBlank()) {
            operands.add(parseSingleOperand(current.trim(), mode))
        }

        Log.d(TAG, "Parsed operands: $operands")
        return operands
    }

    // Parse individual operand string (e.g., a1:9 or aRed,Blue)
    private fun parseSingleOperand(input: String, mode: String): Any {
        return try {
            if (mode in listOf("direction", "drawing")) {
                val parts = input.drop(1)
                val options = parts.split(",").map { it.trim() }
                options.random()
            } else {
                val cleanedInput = input.filter { it.isDigit() || it == ',' || it == ':' || it == ';' }

                if (cleanedInput.contains(";")) {
                    // Split by semicolon
                    val parts = cleanedInput.split(";").map { it.trim() }
                    if (parts.size == 2) {
                        // Left part: parse range or fixed number
                        val leftValue = if (parts[0].contains(":")) {
                            val (start, end) = parts[0].split(":").map { it.toInt() }
                            Random.nextInt(start, end + 1)
                        } else if (parts[0].contains(",")) {
                            parts[0].split(",").map { it.toInt() }.random()
                        } else {
                            parts[0].toInt()
                        }

                        // Right part: parse fixed number or random from commas
                        val rightValue = if (parts[1].contains(",")) {
                            parts[1].split(",").map { it.toInt() }.random()
                        } else {
                            parts[1].toInt()
                        }

                        leftValue * rightValue
                    } else {
                        Log.w(TAG, "Invalid semicolon input (expect 2 parts): $cleanedInput")
                        0
                    }
                } else if (cleanedInput.contains(",")) {
                    cleanedInput.split(",").map { it.toInt() }.random()
                } else if (cleanedInput.contains(":")) {
                    val (start, end) = cleanedInput.split(":").map { it.toInt() }
                    Random.nextInt(start, end + 1)
                } else {
                    cleanedInput.toIntOrNull() ?: 0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing operand: $input (mode: $mode)", e)
            0
        }
    }

    // Replace {a}, {b}, etc. with values
    private fun replaceVariables(template: String, variables: List<String>, values: List<Any>): String {
        var updated = template
        for (i in variables.indices) {
            updated = updated.replace("{${variables[i]}}", values[i].toString())
        }
        Log.d(TAG, "Template: $template → Updated: $updated")
        return updated
    }

    // Evaluate numeric expression (only for numeric modes)
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

    // Main loader function
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
                    if (row.rowNum == 0) continue

                    val questionTemplate = row.getCell(0)?.toString()
                    val rowModeRaw = row.getCell(1)?.toString()
                    val operandRaw = row.getCell(2)?.toString()
                    val rowDifficultyNum = row.getCell(3)?.toString()
                    val answerExpressionTemplate = row.getCell(4)?.toString()
                    val timeLimitNum = row.getCell(5)?.numericCellValue ?: 20.0

                    if (questionTemplate == null || rowModeRaw == null || operandRaw == null ||
                        rowDifficultyNum == null || answerExpressionTemplate == null
                    ) {
                        Log.w(TAG, "Row ${row.rowNum} has null/missing required fields. Skipping.")
                        continue
                    }

                    val rowMode = rowModeRaw.trim().lowercase(Locale.ROOT)
                    val rowDifficulty = rowDifficultyNum.toDoubleOrNull()?.toInt()?.toString()
                    val timeLimit = timeLimitNum.toInt()

                    if (rowMode == mode.lowercase(Locale.ROOT) && rowDifficulty == difficulty) {
                        Log.d(TAG, "✅ Matching row found at rowNum=${row.rowNum}")

                        val variables = extractVariables(operandRaw)
                        val operands = parseInputRange(operandRaw, mode)

                        if (variables.size != operands.size) {
                            Log.w(TAG, "Variable and operand count mismatch at row ${row.rowNum}: variables=${variables.size}, operands=${operands.size}")
                            continue
                        }

                        val renderedQuestion = replaceVariables(questionTemplate, variables, operands)
                        val renderedEquation = replaceVariables(answerExpressionTemplate, variables, operands)

                        val answer = if (mode in listOf("direction", "drawing")) 0 else evaluateEquation(renderedEquation)

                        questions.add(GameQuestion(renderedQuestion, answer, timeLimit))
                        Log.d(TAG, "➕ Added question from row ${row.rowNum}")
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
