package com.zendalona.mathsmantra.utility;

import androidx.fragment.app.Fragment

object HintVisibilityUtil {
    var isHintVisible: Boolean = false

    fun showHint(show: Boolean) {
        isHintVisible = show
    }

    fun shouldShowHint(): Boolean = isHintVisible
}


