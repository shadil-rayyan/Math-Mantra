package com.zendalona.zmantra.model;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class AudioPlayerUtility {

    private TextToSpeech tts;

    public void playNumberWithStereo(Context context, int number, boolean isRight) {
        if (tts == null) {
            tts = new TextToSpeech(context, status -> {
                if (status == TextToSpeech.SUCCESS) {
                    tts.setLanguage(Locale.ENGLISH);
//                    tts.setVolume(isRight ? 0f : 1f, isRight ? 1f : 0f);
                    tts.speak("The number is " + number, TextToSpeech.QUEUE_FLUSH, null, "stereo");
                }
            });
        } else {
//            tts.setVolume(isRight ? 0f : 1f, isRight ? 1f : 0f);
            tts.speak("The number is " + number, TextToSpeech.QUEUE_FLUSH, null, "stereo");
        }
    }
}

