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

    private fun evalExpression(expression: String): Int {
        val tokens = mutableListOf<String>()
        var current = ""
        for (ch in expression) {
            if (ch in "+-*/") {
                if (current.isNotEmpty()) tokens.add(current)
                tokens.add(ch.toString())
                current = ""
            } else current += ch
        }
        if (current.isNotEmpty()) tokens.add(current)

        var result = tokens[0].toIntOrNull() ?: 0
        var index = 1
        while (index < tokens.size) {
            val op = tokens[index]
            val num = tokens[index + 1].toIntOrNull() ?: 0
            when (op) {
                "+" -> result += num
                "-" -> result -= num
                "*" -> result *= num
                "/" -> if (num != 0) result /= num
            }
            index += 2
        }
        return result
    }

    override fun parseQuestion(expr: String): Pair<String, Int> {
        val operators = setOf('+', '-', '*', '/')
        val builder = StringBuilder()
        var token = ""
        for (ch in expr) {
            if (ch in operators) {
                builder.append(parseToken(token)).append(ch)
                token = ""
            } else token += ch
        }
        if (token.isNotEmpty()) builder.append(parseToken(token))

        val question = builder.toString()
        val answer = evalExpression(question)
        return question to answer
    }
}
