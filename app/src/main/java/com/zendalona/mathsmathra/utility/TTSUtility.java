package com.zendalona.mathsmathra.utility;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.preference.PreferenceManager;

import java.util.Locale;

public class TTSUtility {

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private final SharedPreferences prefs;

    public TTSUtility(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        tts = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.forLanguageTag("en-IN"));
                isInitialized = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED;

                if (isInitialized) {
                    // Apply initial settings
                    applyTTSPreferences();
                }
            }
        });
    }

    private void applyTTSPreferences() {
        float speed = prefs.getFloat("tts_speed", 1.0f);
        float volume = prefs.getFloat("tts_volume", 1.0f); // 0.0 to 1.0

        tts.setSpeechRate(speed);
        // Volume is set during speak, not here
    }

    public void speak(String text) {
        if (isInitialized) {
            float volume = prefs.getFloat("tts_volume", 1.0f);
            Bundle params = new Bundle();
            params.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, null);
        }
    }

    public void stopSpeaking() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
    }

    public void setSpeechRate(float rate) {
        if (tts != null) {
            tts.setSpeechRate(rate);
            prefs.edit().putFloat("tts_speed", rate).apply();
        }
    }

    public void setVolume(float volume) {
        // Note: TTS volume is not persistent across `speak()` calls unless set during speak
        prefs.edit().putFloat("tts_volume", volume).apply();
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
