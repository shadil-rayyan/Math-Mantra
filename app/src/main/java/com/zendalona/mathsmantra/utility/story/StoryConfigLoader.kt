package com.zendalona.mathsmantra.utility.story
import android.content.Context
import org.json.JSONObject

object StoryConfigLoader {
    private var configMap: Map<String, String>? = null

    fun getStoryPathTemplate(context: Context, key: String): String? {
        if (configMap == null) {
            val json = context.assets.open("story_path_config.json")
                .bufferedReader().use { it.readText() }
            configMap = JSONObject(json).let { obj ->
                obj.keys().asSequence().associateWith { obj.getString(it) }
            }
        }
        return configMap?.get(key)
    }
}
