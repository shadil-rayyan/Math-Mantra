package com.zendalona.mathsmanthra.utility.common


object TTSHelper {
    fun formatMathText(input: String): String {
        return input.replace("+", " plus ")
            .replace("-", " minus ")
            .replace("*", " multiplied by ")
            .replace("/", " divided by ")
            .replace("%", " percentage of ")
            .replace(",", " and ")
    }
}
