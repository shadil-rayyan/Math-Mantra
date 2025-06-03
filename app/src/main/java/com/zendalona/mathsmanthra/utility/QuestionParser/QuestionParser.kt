package com.zendalona.mathsmanthra.utility.QuestionParser

import com.zendalona.mathsmanthra.utility.QuestionParser.parserimplemention.*


object QuestionParser {

    fun parseExpression(expr: String): Pair<String, Int> {
        val strategy: ParserStrategy = when {
//            expr.startsWith("mean:", true) -> MeanParser()
//            expr.startsWith("mode:", true) -> ModeParser()
//            expr.startsWith("prob:", true) -> ProbabilityParser()
            else -> ArithmeticParser()
        }
        return strategy.parseQuestion(expr)
    }
}
