package com.koncle.imagemanagement.util;

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

    public static boolean deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        } else {
            Log.i(TAG, "delete file : " + path);
            return file.delete();
        }
    }

    public static boolean deleteImages(List<String> paths) {
        for (String path : paths) {
            if (!deleteFile(path))
                return false;
        }
        return true;
    }
}
