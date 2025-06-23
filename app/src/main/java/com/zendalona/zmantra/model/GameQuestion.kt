package com.zendalona.zmantra.model


data class GameQuestion(
    val expression: String,
    val answer: Int,
    val timeLimit: Int = 20,
    val celebration: Boolean = false
)
