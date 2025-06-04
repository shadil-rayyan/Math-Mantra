package com.zendalona.mathsmantra.utility.QuestionParser.parserimplemention

import java.util.Locale

class UnitConversionParser : ParserStrategy {

    private val conversionRates = mapOf(
        Pair("miles", "km") to 1.60934,
        Pair("km", "miles") to 0.621371,
        // add more units here
    )

    override fun parseQuestion(expr: String): Pair<String, Int> {
        val cleanExpr = expr.removePrefix("convert:")
        // example: "5 miles to km"
        val parts = cleanExpr.split(" ")
        if (parts.size < 4 || parts[2] != "to") return "Invalid conversion format" to 0

        val amount = parts[0].toDoubleOrNull() ?: return "Invalid amount" to 0
        val fromUnit = parts[1].lowercase(Locale.ROOT)
        val toUnit = parts[3].lowercase(Locale.ROOT)

        val rate = conversionRates[Pair(fromUnit, toUnit)] ?: return "Conversion not supported" to 0

        val answer = (amount * rate).toInt()

        val questionText = "Convert $amount $fromUnit to $toUnit"

        return questionText to answer
    }
}
