package com.zendalona.zmantra.core.utility.common;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.zendalona.zmantra.core.utility.settings.LocaleHelper;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;

public class TTSUtility {

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private float speechRate = 1.0f; // default
    private final Queue<String> speechQueue = new LinkedList<>();

    private final Map<String, Locale> languageMap = Map.of(
            "en", new Locale("en", "IN"), // English (India)
            "ml", new Locale("ml"), // Malayalam
            "hi", new Locale("hi"), // Hindi
            "ar", new Locale("ar"), // Arabic
            "sa", new Locale("sa"), // Sanskrit
            "ta", new Locale("ta")  // Tamil
    );

    public TTSUtility(Context context) {
        Log.d("TTSUtility", "Initializing TTS");

        // Get the selected language from LocaleHelper
        String selectedLanguage = LocaleHelper.getLanguage(context);
        Locale selectedLocale = languageMap.getOrDefault(selectedLanguage, Locale.forLanguageTag("en-IN"));

        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Set the language based on LocaleHelper or fall back to default (English)
                int result = tts.setLanguage(selectedLocale);

                isInitialized = (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED);
                if (isInitialized) {
                    tts.setSpeechRate(speechRate);
                    Log.d("TTSUtility", "TTS initialized successfully with language: " + selectedLanguage + " and speech rate " + speechRate);

                    // Speak queued texts
                    while (!speechQueue.isEmpty()) {
                        speakInternal(speechQueue.poll());
                    }
                } else {
                    Log.e("TTSUtility", "TTS language not supported, falling back to default (English)");
                    tts.setLanguage(Locale.ENGLISH); // Fallback to English if the selected language is not supported
                }
            } else {
                Log.e("TTSUtility", "TTS initialization failed");
            }
        });
    }

    public void setSpeechRate(float rate) {
        speechRate = Math.max(0.5f, Math.min(rate, 3.0f)); // clamp between 0.5 and 3.0
        if (isInitialized) {
            tts.setSpeechRate(speechRate);
            Log.d("TTSUtility", "Speech rate set to " + speechRate);
        } else {
            Log.d("TTSUtility", "Speech rate will be set after initialization: " + speechRate);
        }
    }

    public float getSpeechRate() {
        return speechRate;
    }

    public void speak(String text) {
        if (isInitialized) {
            speakInternal(text);
        } else {
            Log.w("TTSUtility", "TTS not initialized yet, queuing text: " + text);
            speechQueue.add(text);
        }
    }

    private void speakInternal(String text) {
        tts.setSpeechRate(speechRate);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId");
        Log.d("TTSUtility", "Speaking: " + text + " at rate " + speechRate);
    }

    public void stop() {
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
            Log.d("TTSUtility", "Stopped speaking");
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            Log.d("TTSUtility", "TTS shut down");
        }
    }
}
