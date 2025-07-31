package com.zendalona.zmantra.utility.excel

import android.content.Context
import android.util.Log
import com.zendalona.zmantra.model.GameQuestion
import com.zendalona.zmantra.utility.settings.DifficultyPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

object QuestionCache {

    private val cache = mutableMapOf<String, List<GameQuestion>>() // key = "$lang-$mode-$difficulty"
    private const val TAG = "QuestionCache"

    suspend fun preloadCurrentDifficultyModes(
        context: Context,
        lang: String,
        onProgress: (Int) -> Unit = {} // Optional progress callback
    ) = withContext(Dispatchers.IO) {
        val currentDifficulty = DifficultyPreferences.getDifficulty(context).toString()
        val workbook = ExcelQuestionLoader.loadWorkbook(context, lang)
        val sheet = workbook.getSheetAt(0)

        val detectedModes = extractModes(sheet)

        val total = detectedModes.size
        var loaded = 0

        for (mode in detectedModes) {
            val key = "$lang-$mode-$currentDifficulty"
            val questions = ExcelQuestionLoader.loadQuestionsFromSheet(sheet, mode, currentDifficulty)
            if (questions.isNotEmpty()) {
                cache[key] = questions
                Log.d(TAG, "✅ Cached $mode-$currentDifficulty (${questions.size})")
            }
            loaded++
            val progress = (loaded * 100) / total
            onProgress(progress)
        }

        workbook.close()
    }

    suspend fun preloadOtherDifficultyModes(context: Context, lang: String) = withContext(Dispatchers.IO) {
        val currentDifficulty = DifficultyPreferences.getDifficulty(context).toString()
        val allDifficulties = listOf("1", "2", "3", "4", "5")
        val otherDifficulties = allDifficulties.filter { it != currentDifficulty }

        val workbook = ExcelQuestionLoader.loadWorkbook(context, lang)
        val sheet = workbook.getSheetAt(0)

        val detectedModes = extractModes(sheet)

        for (difficulty in otherDifficulties) {
            for (mode in detectedModes) {
                val key = "$lang-$mode-$difficulty"
                val questions = ExcelQuestionLoader.loadQuestionsFromSheet(sheet, mode, difficulty)
                if (questions.isNotEmpty()) {
                    cache[key] = questions
                    Log.d(TAG, "✅ Cached $mode-$difficulty (${questions.size})")
                }
            }
        }

        workbook.close()
    }

    private fun extractModes(sheet: org.apache.poi.ss.usermodel.Sheet): Set<String> {
        val modeColumnIndex = 1
        val detectedModes = mutableSetOf<String>()
        for (i in 1..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            val cell = row.getCell(modeColumnIndex) ?: continue
            val mode = cell.stringCellValue.trim().lowercase(Locale.ROOT)
            if (mode.isNotEmpty()) detectedModes.add(mode)
        }
        return detectedModes
    }

    fun getQuestions(lang: String, mode: String, difficulty: String): List<GameQuestion> {
        val key = "$lang-$mode-$difficulty"
        return cache[key] ?: emptyList()
    }

    fun putQuestions(lang: String, mode: String, difficulty: String, questions: List<GameQuestion>) {
        val key = "$lang-$mode-$difficulty"
        cache[key] = questions
    }

    fun clearCache() {
        cache.clear()
    }
}
