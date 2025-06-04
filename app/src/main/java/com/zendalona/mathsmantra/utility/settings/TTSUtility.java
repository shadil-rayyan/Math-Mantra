package com.zendalona.mathsmantra.utility.settings;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;

public class TTSUtility {

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private float speechRate = 1.0f; // default
    private final Queue<String> speechQueue = new LinkedList<>();

    public TTSUtility(Context context) {
        Log.d("TTSUtility", "Initializing TTS");
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = tts.setLanguage(Locale.forLanguageTag("en-IN"));
                isInitialized = (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED);
                if (isInitialized) {
                    tts.setSpeechRate(speechRate);
                    Log.d("TTSUtility", "TTS initialized successfully with speech rate " + speechRate);

                    // Speak queued texts
                    while (!speechQueue.isEmpty()) {
                        speakInternal(speechQueue.poll());
                    }
                } else {
                    Log.e("TTSUtility", "TTS language not supported");
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
