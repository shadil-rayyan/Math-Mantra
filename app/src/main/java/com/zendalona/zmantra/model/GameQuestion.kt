package com.zendalona.zmantra.model

//used in all screen that load quesiton from excel sheet it basically contain the columns type
data class GameQuestion(
    val expression: String,
    val answer: Int,
    val timeLimit: Int = 20,
    val celebration: Boolean = false
)
