package com.zendalona.mathsmathra.utility.settings;

import android.content.Context
import android.media.MediaPlayer
import android.preference.PreferenceManager
import com.zendalona.mathsmathra.R

class BackgroundMusicPlayer(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private val prefs = PreferenceManager.getDefaultSharedPreferences(context)

    fun startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.drums_sound) // Your music file here
            mediaPlayer?.isLooping = true
            setVolume(getVolume())
            mediaPlayer?.start()
        } else if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
        }
    }

    fun pauseMusic() {
        mediaPlayer?.pause()
    }

    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
        prefs.edit().putFloat("music_volume", volume).apply()
    }

    fun getVolume(): Float {
        return prefs.getFloat("music_volume", 0.5f)
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }
}
