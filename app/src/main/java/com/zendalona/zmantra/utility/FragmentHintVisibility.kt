package com.zendalona.zmantra.utility;


object HintVisibilityUtil {
    var isHintVisible: Boolean = true

    fun showHint(show: Boolean) {
        isHintVisible = show
    }

    fun shouldShowHint(): Boolean = isHintVisible
}


