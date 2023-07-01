package com.example.diary;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.FileOutputStream;
import java.io.IOException;

public class ImageHelper {
    public static String saveImageToInternalStorage(Context context, Bitmap bitmap, String filename) {
        try {
            FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            return context.getFileStreamPath(filename).getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
