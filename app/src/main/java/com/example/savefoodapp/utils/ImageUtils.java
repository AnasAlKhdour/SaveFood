package com.example.savefoodapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

public class ImageUtils {

    public static Bitmap decodeSampled(String path, int reqW, int reqH) {
        if (path == null) return null;

        File file = new File(path);
        if (!file.exists()) return null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(path, options);

        int height = options.outHeight;
        int width = options.outWidth;
        int sampleSize = 1;

        while ((height / sampleSize) > reqH ||
                (width / sampleSize) > reqW) {
            sampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        return BitmapFactory.decodeFile(path, options);
    }

    public static void deleteImage(String path) {
        if (path == null) return;

        File file = new File(path);

        if (file.exists()) {
            file.delete();
        }
    }
}