package com.zendalona.mathsmantra.utility.drawing;

import android.content.Context;
import android.graphics.PointF;

import com.google.mlkit.common.MlKitException;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionCandidate;
import com.google.mlkit.vision.digitalink.RecognitionResult;

import java.util.List;

public class InkRecognitionHelper {
    private final DigitalInkRecognizer recognizer;

    public InkRecognitionHelper(Context context) {
        DigitalInkRecognitionModelIdentifier modelIdentifier = null;
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        } catch (MlKitException e) {
            // Handle the exception (e.g., log the error, show a message to the user)
            modelIdentifier = null;
        }

        if (modelIdentifier != null) {
            DigitalInkRecognitionModel model =
                    DigitalInkRecognitionModel.builder(modelIdentifier).build();
            DigitalInkRecognizerOptions options =
                    DigitalInkRecognizerOptions.builder(model).build();
            recognizer = DigitalInkRecognition.getClient(options);
        } else {
            recognizer = null;
        }
    }

    public void recognize(List<List<PointF>> strokeList, OnShapeRecognized callback) {
        if (recognizer == null) {
            callback.onResult("Recognizer not initialized");
            return;
        }

        Ink.Builder inkBuilder = Ink.builder();
        for (List<PointF> stroke : strokeList) {
            Ink.Stroke.Builder strokeBuilder = Ink.Stroke.builder();
            for (PointF point : stroke) {
                strokeBuilder.addPoint(Ink.Point.create(point.x, point.y, System.currentTimeMillis()));
            }
            inkBuilder.addStroke(strokeBuilder.build());
        }

        recognizer.recognize(inkBuilder.build())
                .addOnSuccessListener(result -> {
                    String label = "unknown";
                    if (!result.getCandidates().isEmpty()) {
                        RecognitionCandidate candidate = result.getCandidates().get(0);
                        label = candidate.getText();
                    }
                    callback.onResult(label);
                })
                .addOnFailureListener(e -> callback.onResult("Recognition failed"));
    }

    public interface OnShapeRecognized {
        void onResult(String label);
    }
}
