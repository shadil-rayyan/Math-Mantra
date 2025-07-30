package com.zendalona.zmantra

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.zendalona.zmantra.utility.excel.QuestionCache
import com.zendalona.zmantra.utility.settings.LocaleHelper
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    private val announceInterval = 1500L
    private val handler = Handler(Looper.getMainLooper())
    private var announceRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val gifImageView: ImageView = findViewById(R.id.gifImageView)

        // Load loading GIF
        Glide.with(this)
            .asGif()
            .load(R.drawable.dialog_welcome_1)
            .into(gifImageView)

        // Accessibility announcement
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        if (accessibilityManager.isEnabled) {
            announceRunnable = object : Runnable {
                override fun run() {
                    gifImageView.announceForAccessibility("Loading questions, please wait")
                    handler.postDelayed(this, announceInterval)
                }
            }
            handler.postDelayed(announceRunnable!!, announceInterval)
        }

        // Preload current difficulty, then go to MainActivity, then load rest
        lifecycleScope.launch {
            val lang = LocaleHelper.getLanguage(this@SplashScreen).ifEmpty { "en" }

            Log.d("SplashScreen", "⏳ Preloading current difficulty...")
            QuestionCache.preloadCurrentDifficultyModes(this@SplashScreen, lang)

            Log.d("SplashScreen", "✅ Current difficulty preload complete")

            announceRunnable?.let { handler.removeCallbacks(it) }
            startActivity(Intent(this@SplashScreen, MainActivity::class.java))
            finish()

            launch {
                Log.d("SplashScreen", "⏳ Preloading other difficulties in background...")
                QuestionCache.preloadOtherDifficultyModes(this@SplashScreen, lang)
                Log.d("SplashScreen", "✅ Background preload complete")
            }
        }
    }
}
