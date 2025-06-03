package com.zendalona.mathsmanthra.utility.QuestionParser.parserimplemention

class RatioProportionParser : ParserStrategy {

    override fun parseQuestion(expr: String): Pair<String, Int> {
        // assume expr format is "3:5=x:20" or "3:5=?:20"

        val cleanExpr = expr.removePrefix("ratio:")
        val sides = cleanExpr.split("=")
        if (sides.size != 2) return "Invalid ratio format" to 0

        val left = sides[0].split(":")
        val right = sides[1].split(":")

        if (left.size != 2 || right.size != 2) return "Invalid ratio parts" to 0

        val a = left[0].toDoubleOrNull() ?: return "Invalid left ratio" to 0
        val b = left[1].toDoubleOrNull() ?: return "Invalid left ratio" to 0

        val xStr = right[0]
        val d = right[1].toDoubleOrNull() ?: return "Invalid right ratio" to 0

        val questionText = "Find the missing value in the ratio $a:$b = $xStr:$d"

        val answer = if (xStr == "x" || xStr == "?") {
            ((b * d) / a).toInt()
        } else {
            // if x is known, maybe calculate other way
            0
        }

        return questionText to answer
    }
}
