package com.zendalona.mathsmantra.utility.story

import android.content.Context
import android.util.Log
import org.json.JSONObject

object StoryConfigLoader {

    private const val TAG = "StoryConfigLoader"

    private var configMap: Map<String, String>? = null

    fun getStoryPathTemplate(context: Context, key: String): String? {
        if (configMap == null) {
            try {
                Log.d(TAG, "Loading story_path_config.json from assets...")
                val json = context.assets.open("story_path_config.json")
                    .bufferedReader().use { it.readText() }
                Log.d(TAG, "Raw JSON content: $json")

                val jsonObject = JSONObject(json)
                configMap = jsonObject.keys().asSequence().associateWith { jsonObject.getString(it) }

                Log.d(TAG, "Parsed config map keys: ${configMap?.keys}")
                Log.d(TAG, "Full config map: $configMap")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading or parsing story_path_config.json", e)
            }
        } else {
            Log.d(TAG, "Config map already loaded, keys: ${configMap?.keys}")
        }

        val value = configMap?.get(key)
        Log.d(TAG, "getStoryPathTemplate() for key='$key' -> '$value'")
        return value
    }
}
