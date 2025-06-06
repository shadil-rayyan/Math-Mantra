package com.zendalona.mathsmantra.utility.QuestionParser.parserimplemention

import kotlin.random.Random

class ArithmeticParser : ParserStrategy {

    private fun parseToken(token: String): Int {
        return when {
            ',' in token -> token.split(",").mapNotNull { it.toIntOrNull() }.randomOrNull() ?: 0
            ':' in token -> {
                val bounds = token.split(":").mapNotNull { it.toIntOrNull() }
                if (bounds.size == 2) Random.nextInt(bounds[0], bounds[1] + 1) else 0
            }
            ';' in token -> {
                val parts = token.split(";")
                if (parts.size == 3) {
                    val digit = parts[0].toIntOrNull() ?: 0
                    val start = parts[1].toIntOrNull() ?: 1
                    val end = parts[2].toIntOrNull() ?: 1
                    digit * Random.nextInt(start, end + 1)
                } else 0
            }
            else -> token.toIntOrNull() ?: 0
        }
    }

    override fun parseQuestion(expr: String): Pair<String, Int> {
        val operators = setOf('+', '-', '*', '/')
        val tokens = mutableListOf<String>()
        var currentToken = ""

        for (ch in expr) {
            if (ch in operators) {
                tokens.add(currentToken)
                tokens.add(ch.toString())
                currentToken = ""
            } else currentToken += ch
        }
        if (currentToken.isNotEmpty()) tokens.add(currentToken)

        val values = mutableListOf<Int>()
        val ops = mutableListOf<Char>()

        for ((index, token) in tokens.withIndex()) {
            if (index % 2 == 0) {
                values.add(parseToken(token))
            } else {
                ops.add(token[0])
            }
        }

        var result = values[0]
        val builder = StringBuilder()
        builder.append(values[0])

        for (i in ops.indices) {
            val op = ops[i]
            val nextVal = values[i + 1]
            builder.append(op).append(nextVal)

            result = when (op) {
                '+' -> result + nextVal
                '-' -> result - nextVal
                '*' -> result * nextVal
                '/' -> if (nextVal != 0) result / nextVal else result
                else -> result
            }
        }

        val questionStr = builder.toString()
        return questionStr to result
    }
}
