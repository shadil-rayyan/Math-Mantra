package com.zendalona.mathsmantra.utility;

import androidx.fragment.app.Fragment

object FragmentHintVisibility {
val fragmentsWithoutHint = setOf(
        "UserGuideFragment",
        "GameFragment",
        "HintFragment",
        "SettingFragment",
        "LearningFragment",
        "ScorePageFragment",



    )

fun shouldShowHint(fragment: Fragment?): Boolean {
    return fragment?.javaClass?.simpleName !in fragmentsWithoutHint
}
}
