package com.zendalona.mathsmantra.utility.story

import android.content.Context
import com.zendalona.mathsmantra.utility.QuestionParser.QuestionParser
import com.zendalona.mathsmantra.utility.settings.LocaleHelper

object StoryQuestionGenerator {

    fun generateStoryQuestion(context: Context, rawLine: String): Pair<String, Int> {
        val metadata = rawLine.split(",")[0].split(":")
        val expression = rawLine.split(",")[1]
        val operationKey = metadata[0]

        val numbers = expression.split(Regex("[^\\d]+")).mapNotNull { it.toIntOrNull() }
        val digits = numbers.size

        val lang = LocaleHelper.getLanguage(context) ?: "en"
        val template = StoryConfigLoader.getStoryPathTemplate(context, operationKey)
        val storyPath = template?.let { String.format(it, lang, digits) }

        val storyLines = try {
            context.assets.open(storyPath!!).bufferedReader().readLines()
        } catch (e: Exception) {
            listOf("$expression = ?")
        }

        val storyTemplate = storyLines.random()
        val questionText = String.format(storyTemplate, *numbers.toTypedArray())

        val (_, correctAnswer) = QuestionParser.parseExpression(expression) // ✅ This works

        return questionText to correctAnswer
    }
}
