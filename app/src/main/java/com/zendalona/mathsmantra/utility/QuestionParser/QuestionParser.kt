package com.zendalona.mathsmantra.utility.QuestionParser

import com.zendalona.mathsmantra.utility.QuestionParser.parserimplemention.*

object QuestionParser {

    /**
     * Delegates parsing to appropriate parser implementation based on prefix.
     * If none matched, uses ArithmeticParser by default.
     */
    fun parseExpression(expr: String): Pair<String, Int> {
        // Step 1: Strip type if present, e.g., add?3+4===10===1
        val cleanedExpr = expr.substringAfter('?')

        // Step 2: Remove ===time===bell part if present
        val questionPart = cleanedExpr.substringBefore("===")

        // Step 3: Choose strategy (can extend more prefixes later)
        val strategy: ParserStrategy = when {
            questionPart.startsWith("ratio:", true) -> RatioProportionParser()
            questionPart.startsWith("convert:", true) -> UnitConversionParser()
            else -> ArithmeticParser()
        }

        return strategy.parseQuestion(questionPart)
    }

}
