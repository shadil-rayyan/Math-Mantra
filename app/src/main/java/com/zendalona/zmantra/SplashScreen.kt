package com.zendalona.zmantra

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import androidx.lifecycle.lifecycleScope
import com.zendalona.zmantra.utility.excel.QuestionCache
import com.zendalona.zmantra.utility.settings.LocaleHelper
import kotlinx.coroutines.launch
class SplashScreen : AppCompatActivity() {

    private val totalSplashTime = 4000L  // 3 seconds
    private val announceInterval = 1500L // 1.5 seconds
    private val handler = Handler(Looper.getMainLooper())
    private var announceRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val gifImageView: ImageView = findViewById(R.id.gifImageView)

        // Load GIF with Glide
        Glide.with(this)
            .asGif()
            .load(R.drawable.dialog_welcome_1) // your GIF name
            .into(gifImageView)

        // Accessibility Manager
        val accessibilityManager = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager

        // Announce "Loading" if accessibility (TalkBack) is enabled
        if (accessibilityManager.isEnabled) {
            announceRunnable = object : Runnable {
                override fun run() {
                    gifImageView.announceForAccessibility("Loading")
                    handler.postDelayed(this, announceInterval)
                }
            }
            handler.postDelayed(announceRunnable!!, announceInterval)
        }

        // Move to MainActivity after 3 seconds
        handler.postDelayed({
            announceRunnable?.let { handler.removeCallbacks(it) }
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, totalSplashTime)

        lifecycleScope.launch {
            Log.d("SplashScreen", "Starting preload coroutine...")  // ✅ Log before preload starts

            val lang = LocaleHelper.getLanguage(this@SplashScreen).ifEmpty { "en" }
            Log.d("SplashScreen", "Detected language: $lang")       // ✅ Log detected language

            QuestionCache.preloadAllQuestions(this@SplashScreen, lang)

            Log.d("SplashScreen", "Preload coroutine finished.")


        }
    }
}