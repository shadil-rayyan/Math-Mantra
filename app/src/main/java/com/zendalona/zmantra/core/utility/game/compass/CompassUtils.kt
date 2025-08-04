package com.zendalona.zmantra.core.utility.game.compass

import kotlin.math.abs

object CompassUtils {

    fun directionToDegrees(dir: String, directions: Array<String>): Float {
        val index = directions.indexOfFirst { it.equals(dir, ignoreCase = true) }
        return if (index != -1) index * 22.5f else 0f
    }

    fun getCompassDirection(degrees: Float, directions: Array<String>): String {
        val index = ((degrees + 11.25) / 22.5).toInt() % 16
        return directions[index]
    }

    fun angleDifference(a: Float, b: Float): Float {
        val normA = (a + 360) % 360
        val normB = (b + 360) % 360
        val diff = abs(normA - normB)
        return if (diff > 180) 360 - diff else diff
    }
}
