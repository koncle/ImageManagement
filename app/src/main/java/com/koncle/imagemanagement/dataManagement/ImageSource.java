package com.koncle.imagemanagement.dataManagement;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dao.DaoSession;

import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageSource {

    private static final String IMAGE_UNSPECIFIED = "image/*";
    private static final int IMAGE_CODE = 0; // 这里的IMAGE_CODE是自己任意定义的

    public static Intent setImage1(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_UNSPECIFIED);
        startActivityForResult(activity, intent, IMAGE_CODE, null);
        return intent;
    }

    public static void getSystemPhotoList(Context context) {

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(uri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

        if (cursor == null || cursor.getCount() <= 0) return; // 没有图片

        String path;
        Image image;
        DaoSession daoSession = DaoManager.getInstance().getDaoSession();

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            path = cursor.getString(index); // 文件地址
            try {
                image = ImageService.getImageFromPath(path);
                daoSession.insert(image);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("path", "failed path name : " + path);
            }
        }
    }

}
