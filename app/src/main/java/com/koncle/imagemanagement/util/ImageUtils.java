package com.koncle.imagemanagement.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by 10976 on 2018/1/9.
 */

public class ImageUtils {
    public static final String TAG = ImageUtils.class.getSimpleName();

    public static Bitmap drawableToBitmap(Drawable drawable) {

        Bitmap bitmap = Bitmap.createBitmap(
                drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static boolean copyFile(String srcFilePath, String desDirPath) {
        File srcFile = new File(srcFilePath);
        if (!srcFile.exists()) {
            return false;
        }
        try {
            InputStream streamFrom = new FileInputStream(srcFile);
            OutputStream streamTo = new FileOutputStream(desDirPath);
            byte buffer[] = new byte[1024];
            int len;
            while ((len = streamFrom.read(buffer)) > 0) {
                streamTo.write(buffer, 0, len);
            }
            streamFrom.close();
            streamTo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        } else {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    deleteFile(f);
                }
            } else {
                file.delete();
            }
        }
        return true;
    }

    public static boolean deleteFile(String path) {
        if (path == null) return false;
        File file = new File(path);
        Log.w(TAG, "delete 1 file");
        return deleteFile(file);
    }

    public static int deleteFiles(List<String> pathes) {
        int count = 0;
        for (String p : pathes) {
            count += deleteFile(p) ? 1 : 0;
        }
        Log.w(TAG, "delete " + count + " file");
        return count;
    }

    public static boolean deleteImages(List<String> paths) {
        for (String path : paths) {
            if (!deleteFile(path))
                return false;
        }
        return true;
    }

    public static Bitmap loadBitmap(String path, int reqWidth, int reqHeight) {
        // get parameters of image without loading it
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;
        String imageType = options.outMimeType;
        int inSampleSize = 1;

        // calculate the final width and height
        if (reqWidth < imageWidth || reqHeight < imageHeight) {
            int widthRatio = Math.round((float) reqWidth / reqWidth);
            int heightRatio = Math.round((float) imageHeight / reqHeight);
            inSampleSize = widthRatio > heightRatio ? widthRatio : heightRatio;
        }

        // scale the image with the parameters and load it
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }
}
