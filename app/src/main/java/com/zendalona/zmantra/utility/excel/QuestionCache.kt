package com.zendalona.zmantra.utility.excel

import android.content.Context
import android.util.Log
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object QuestionCache {

    private val cache = mutableMapOf<String, List<GameQuestion>>() // key = "$lang|$mode|$difficulty"
    private const val TAG = "QuestionCache"

    suspend fun preloadAllQuestions(context: Context, lang: String) {
        val workbook = ExcelQuestionLoader.loadWorkbook(context, lang)
        val sheet = workbook.getSheetAt(0)

        val supportedModes = listOf("addition", "angle", "currency", "day", "direction", "distance",
            "division", "drawing", "mental", "mode", "multiplication",
            "numberline", "percentage", "quickplay", "remainder", "shake",
            "sterio", "story", "subtraction", "tap", "time", "touch")
        val supportedDifficulties = listOf("1", "2", "3","4","5")

        for (mode in supportedModes) {
            for (difficulty in supportedDifficulties) {
                val key = "$lang|$mode|$difficulty"
                val questions = ExcelQuestionLoader.loadQuestionsFromSheet(sheet, mode, difficulty)
                if (questions.isNotEmpty()) {
                    cache[key] = questions
                    Log.d(TAG, "Cached $mode-$difficulty questions (${questions.size})")
                }
            }
        }
        workbook.close()
    }

    fun getQuestions(lang: String, mode: String, difficulty: String): List<GameQuestion> {
        val key = "$lang|$mode|$difficulty"
        return cache[key] ?: emptyList()
    }
}
