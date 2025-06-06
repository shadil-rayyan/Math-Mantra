package com.zendalona.mathsmantra.utility.story

import android.content.Context
import android.util.Log
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import com.zendalona.mathsmantra.utility.settings.LocaleHelper
import kotlin.random.Random

object StoryQuestionGenerator {

    private const val TAG = "StoryQuestionGenerator"

    /**
     * Input example: "add:4+3"
     * Steps:
     * - Extract operationKey = "add"
     * - Extract expression = "4+3"
     * - Extract numbers for story formatting (4 and 3)
     * - Load story template file path from StoryConfigLoader using operationKey
     * - Read story lines from assets, pick a random one
     * - Format it with numbers
     * - Parse expression with QuestionParser to get correct answer
     * - Return Pair(questionText, correctAnswer)
     */
    fun generateStoryQuestion(context: Context, rawLine: String): Pair<String, Int> {
        Log.d(TAG, "Generating question for rawLine: $rawLine")

        val parts = rawLine.split("?")
        if (parts.size < 2) {
            Log.w(TAG, "Invalid rawLine format: $rawLine")
            return rawLine to 0
        }

        val operationKey = parts[0]
        val expression = parts[1]
        Log.d(TAG, "OperationKey: $operationKey, Expression: $expression")

        // ✅ Parse using QuestionParser
        val (parsedExpression, correctAnswer) = QuestionParser.parseExpression(expression)
        Log.d(TAG, "Parsed expression: $parsedExpression, Correct answer: $correctAnswer")

        // ✅ Extract numbers from parsed expression for formatting the story
        val numbers = parsedExpression.split(Regex("[^\\d]+")).mapNotNull { it.toIntOrNull() }
        val digits = numbers.size
        Log.d(TAG, "Extracted numbers: $numbers, digits count: $digits")

        val lang = LocaleHelper.getLanguage(context) ?: "en"
        Log.d(TAG, "Locale language: $lang")

        val templatePath = StoryConfigLoader.getStoryPathTemplate(context, operationKey)
        Log.d(TAG, "Template path from StoryConfigLoader: $templatePath")

        val storyPath = templatePath?.let { String.format(it, lang, digits) }
        Log.d(TAG, "Final story path: $storyPath")

        val storyLines = try {
            context.assets.open(storyPath!!).bufferedReader().readLines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading story asset file: $storyPath", e)
            listOf("$parsedExpression = ?")
        }
        Log.d(TAG, "Loaded story lines count: ${storyLines.size}")

        val storyTemplate = if (storyLines.isNotEmpty()) {
            storyLines[Random.nextInt(storyLines.size)]
        } else {
            "$parsedExpression = ?"
        }
        Log.d(TAG, "Selected story template line: $storyTemplate")

        val questionText = try {
            String.format(storyTemplate, *numbers.toTypedArray())
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting story template with numbers: $numbers", e)
            "$parsedExpression = ?"
        }
        Log.d(TAG, "Final question text: $questionText")

        return questionText to correctAnswer
    }

}
