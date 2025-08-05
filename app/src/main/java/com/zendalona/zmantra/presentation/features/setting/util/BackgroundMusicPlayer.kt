package com.zendalona.zmantra.presentation.features.setting.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.zendalona.zmantra.R


object BackgroundMusicPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    private var volume = 0.5f

    fun initialize(context: Context) {
        if (!isInitialized) {
            mediaPlayer = MediaPlayer.create(context.applicationContext, R.raw.drums_sound).apply {
                isLooping = true
                setVolume(volume, volume)
            }
            isInitialized = true
            Log.d("BackgroundMusicPlayer", "Initialized media player with volume $volume")
        }
    }

    fun startMusic() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            Log.d("BackgroundMusicPlayer", "Music started")
        }
    }

    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            Log.d("BackgroundMusicPlayer", "Music paused")
        }
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isInitialized = false
        Log.d("BackgroundMusicPlayer", "Music stopped and released")
    }

    fun setVolume(vol: Float) {
        volume = vol.coerceIn(0.0f, 1.0f)
        mediaPlayer?.setVolume(volume, volume)
        Log.d("BackgroundMusicPlayer", "Volume set to $volume")
    }

    fun getVolume(): Float = volume

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false
}
