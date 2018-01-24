package com.koncle.imagemanagement.util;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.io.File;
import java.util.Date;

import static com.koncle.imagemanagement.activity.MyHandler.IMAGE_ADDED;

/**
 * Created by Koncle on 2018/1/22.
 */

public class FileChangeObserver extends ContentObserver {
    private static final String TAG = "FILE OBSERVER";
    private final Context context;
    private final Handler handler;

    private Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public FileChangeObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
        this.handler = handler;
    }

    @Override
    public void onChange(boolean selfChange) {
        Cursor cursor = context.getContentResolver().query(uri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED + " desc limit 1");

        if (cursor == null) return;

        int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        int timeIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
        int latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
        int lngIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
        int despIndex = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
        int mineTypeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

        while (cursor.moveToNext()) {
            String path = cursor.getString(pathIndex);
            Image image = new Image();
            image.setPath(path);

            // test if exist
            Image imageInDatabase = ImageService.ifExistImage(image);
            // if return false, which means it exists in the database.
            // else it should be added to the database
            if (imageInDatabase != null) {

                File f = new File(path);
                // if not exits in the file system,
                // which means the image has been deleted
                if (!f.exists()) {
                    // delete the image from database
                    ImageService.deleteImageInDataBase(imageInDatabase);
                    Log.w(TAG, "delete image from database : " + path);
                } else {
                    Log.w(TAG, "did nothing to : " + path);
                    break;
                }
                // not exist, insert the image
            } else {
                String folder = ImageUtils.getFolderNameFromPath(path);
                String name = cursor.getString(nameIndex);
                long time = cursor.getLong(timeIndex);
                double lat = cursor.getDouble(latIndex);
                double lng = cursor.getDouble(lngIndex);
                String desp = cursor.getString(despIndex);
                String mineType = cursor.getString(mineTypeIndex);

                image.setName(name);
                image.setFolder(folder);
                image.setTime(new Date(time));
                image.setLat(String.valueOf(lat));
                image.setLng(String.valueOf(lng));
                image.setDesc(desp);

                if ("image/gif".equals(mineType))
                    image.setType(Image.TYPE_GIF);
                else
                    image.setType(Image.TYPE_NORNAL);

                // if the image is not in database
                ImageService.insertImageWithOutCheck(image);

                // send msg to DrawerActivity handler
                Message msg = new Message();
                msg.what = IMAGE_ADDED;
                Bundle bundle = new Bundle();
                bundle.putParcelable("image", image);
                msg.setData(bundle);
                handler.sendMessage(msg);

                Log.w(TAG, "insert image into database : " + path);
            }
            break;
        }
        cursor.close();
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.w(TAG, uri.toString());
        onChange(selfChange);
    }
}
