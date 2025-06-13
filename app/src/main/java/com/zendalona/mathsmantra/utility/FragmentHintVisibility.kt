package com.zendalona.mathsmantra.utility;


object HintVisibilityUtil {
    var isHintVisible: Boolean = true

    fun showHint(show: Boolean) {
        isHintVisible = show
    }

    fun shouldShowHint(): Boolean = isHintVisible
}


