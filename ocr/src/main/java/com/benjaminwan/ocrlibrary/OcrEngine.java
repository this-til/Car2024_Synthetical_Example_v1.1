package com.benjaminwan.ocrlibrary;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class OcrEngine {
    public static final int numThread = 4;

    int padding = 50;
    float boxScoreThresh = 0.6f;
    float boxThresh = 0.3f;
    float unClipRatio = 2.0f;
    boolean doAngle = true;
    boolean mostAngle = true;

    static {
        System.loadLibrary("OcrLite");
    }
    
    public OcrEngine(Context context) {
        boolean ret = init(context.getAssets(), numThread);
        if (!ret) {
            throw new IllegalArgumentException();
        }
    }

    public OcrResult detect(Bitmap input, Bitmap output, int maxSideLen) {
        return detect(input, output, padding, maxSideLen,
                boxScoreThresh, boxThresh,
                unClipRatio, doAngle, mostAngle);
    }

    // Native methods
    private native boolean init(AssetManager assetManager, int numThread);

    public native OcrResult detect(
            Bitmap input, Bitmap output, int padding, int maxSideLen,
            float boxScoreThresh, float boxThresh,
            float unClipRatio, boolean doAngle, boolean mostAngle
    );

    public native double benchmark(Bitmap input, int loop);
}
