package com.zendalona.mathsmanthra.utility.QuestionParser.parserimplemention


interface ParserStrategy {
    fun parseQuestion(expression: String): Pair<String, Int>
}
