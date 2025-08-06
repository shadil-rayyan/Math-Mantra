package com.zendalona.zmantra.presentation.features.userguide

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zendalona.zmantra.domain.usecase.userguide.GetUserGuideHtmlUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserGuideViewModel @Inject constructor(
    private val getUserGuideHtmlUseCase: GetUserGuideHtmlUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(UserGuideUiState())
    val uiState: LiveData<UserGuideUiState> = _uiState

    fun loadUserGuide(language: String, themeColors: Pair<String, String>) {
        viewModelScope.launch {
            try {
                val html = getUserGuideHtmlUseCase(language)
                val styledHtml = applyThemeToHtml(html, themeColors)
                _uiState.value = UserGuideUiState(styledHtml = styledHtml)
            } catch (e: Exception) {
                _uiState.value = UserGuideUiState(isError = true)
            }
        }
    }

    private fun applyThemeToHtml(htmlBody: String, themeColors: Pair<String, String>): String {
        val (backgroundColor, textColor) = themeColors
        return """
            <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                <style>
                    body {
                        background-color: $backgroundColor;
                        color: $textColor;
                        font-size: 18px;
                        font-family: sans-serif;
                        padding: 16px;
                    }
                    h2 { margin-top: 20px; }
                    a { color: #64B5F6; }
                </style>
            </head>
            <body>
                $htmlBody
            </body>
            </html>
        """.trimIndent()
    }
}
