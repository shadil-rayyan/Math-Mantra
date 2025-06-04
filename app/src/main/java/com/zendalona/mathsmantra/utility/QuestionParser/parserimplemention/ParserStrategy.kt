package com.zendalona.mathsmantra.utility.QuestionParser.parserimplemention


interface ParserStrategy {
    fun parseQuestion(expression: String): Pair<String, Int>
}
