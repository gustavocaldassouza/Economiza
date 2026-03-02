package com.example.economiza.domain.usecase;

import android.graphics.Bitmap;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class ScanReceiptUseCase {
    public interface Callback {
        void onSuccess(String text);

        void onFailure(Exception e);
    }

    public void execute(Bitmap bitmap, Callback callback) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(visionText -> callback.onSuccess(visionText.getText()))
                .addOnFailureListener(callback::onFailure);
    }
}
