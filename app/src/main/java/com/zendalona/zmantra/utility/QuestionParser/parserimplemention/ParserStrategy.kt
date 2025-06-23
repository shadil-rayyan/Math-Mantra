package com.zendalona.zmantra.utility.QuestionParser.parserimplemention


interface ParserStrategy {
    fun parseQuestion(expression: String): Pair<String, Int>
}
