package com.zendalona.mathsmantra.utility.QuestionParser

import com.zendalona.mathsmantra.utility.QuestionParser.parserimplemention.*

object QuestionParser {

    /**
     * Delegates parsing to appropriate parser implementation based on prefix.
     * If none matched, uses ArithmeticParser by default.
     */
    fun parseExpression(expr: String): Pair<String, Int> {
        val strategy: ParserStrategy = when {
            expr.startsWith("ratio:", true) -> RatioProportionParser()
            expr.startsWith("convert:", true) -> UnitConversionParser()
            else -> ArithmeticParser()
        }
        return strategy.parseQuestion(expr)
    }
}
