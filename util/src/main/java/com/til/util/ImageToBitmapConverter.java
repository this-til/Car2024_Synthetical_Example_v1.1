package com.til.util;

import android.graphics.*;
import android.media.Image;
import android.renderscript.*;

import java.nio.ByteBuffer;

public class ImageToBitmapConverter {
    private static final String TAG = "ImageToBitmapConverter";

    public static Bitmap jpegToBitmap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        int W = image.getWidth();
        int H = image.getHeight();
        Bitmap inBitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inBitmap = inBitmap;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
    }
}
