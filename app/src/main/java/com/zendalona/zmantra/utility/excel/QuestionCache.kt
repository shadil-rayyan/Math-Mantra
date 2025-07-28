package com.zendalona.zmantra.utility.excel

import android.content.Context
import android.util.Log
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.excel.ExcelQuestionLoader
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object QuestionCache {

    private val cache = mutableMapOf<String, List<GameQuestion>>() // key = "$lang-$mode-$difficulty"
    private const val TAG = "QuestionCache"

    suspend fun preloadAllQuestions(context: Context, lang: String) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "Starting to preload all questions...")

        val currentDifficulty = DifficultyPreferences.getDifficulty(context).toString()
        Log.d(TAG, "Detected current difficulty: $currentDifficulty")

        val workbook = ExcelQuestionLoader.loadWorkbook(context, lang)
        val sheet = workbook.getSheetAt(0)

        val allDifficulties = listOf("1", "2", "3", "4", "5")
        val sortedDifficulties = listOf(currentDifficulty) + allDifficulties.filter { it != currentDifficulty }

        // Dynamically extract all unique modes from the sheet
        val modeColumnIndex = 1 // Assuming mode is in first column; change if needed
        val detectedModes = mutableSetOf<String>()

        for (i in 1..sheet.lastRowNum) { // Skip header row
            val row = sheet.getRow(i) ?: continue
            val cell = row.getCell(modeColumnIndex) ?: continue
            val mode = cell.stringCellValue.trim().lowercase(Locale.ROOT)
            if (mode.isNotEmpty()) detectedModes.add(mode)
        }

        Log.d(TAG, "Detected modes in sheet: $detectedModes")

        // First load ALL modes for CURRENT difficulty
        for (mode in detectedModes) {
            val key = "$lang-$mode-$currentDifficulty"
            val questions = ExcelQuestionLoader.loadQuestionsFromSheet(sheet, mode, currentDifficulty)
            if (questions.isNotEmpty()) {
                cache[key] = questions
                Log.d(TAG, "✅ Cached $mode-$currentDifficulty questions (${questions.size})")
            }
        }

        // Then load ALL modes for OTHER difficulties
        val otherDifficulties = allDifficulties.filter { it != currentDifficulty }
        for (difficulty in otherDifficulties) {
            for (mode in detectedModes) {
                val key = "$lang-$mode-$difficulty"
                val questions = ExcelQuestionLoader.loadQuestionsFromSheet(sheet, mode, difficulty)
                if (questions.isNotEmpty()) {
                    cache[key] = questions
                    Log.d(TAG, "✅ Cached $mode-$difficulty questions (${questions.size})")
                }
            }
        }

        workbook.close()

        val duration = System.currentTimeMillis() - startTime
        Log.d(TAG, "✅ Finished preloading all questions in $duration ms (${duration / 1000.0} seconds)")
    }

    fun getQuestions(lang: String, mode: String, difficulty: String): List<GameQuestion> {
        val key = "$lang-$mode-$difficulty"
        val result = cache[key]
        if (result == null) {
            Log.w(TAG, "⚠️ Cache miss for key: $key")
        }
        return result ?: emptyList()
    }

    fun putQuestions(lang: String, mode: String, difficulty: String, questions: List<GameQuestion>) {
        val key = "$lang-$mode-$difficulty"
        cache[key] = questions
    }

    fun clearCache() {
        cache.clear()
    }
}
