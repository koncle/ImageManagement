package com.koncle.imagemanagement;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.koncle.imagemanagement.bean.VideoInfo;
import com.koncle.imagemanagement.dataManagement.ImageService;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    public static String TAG = "test database";
    public static Context context;

    @BeforeClass
    public static void init() {
        context = InstrumentationRegistry.getTargetContext();
        ImageService.init(context);
    }

    @Test
    public void testFile() throws Exception {
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID};

        // MediaStore.Video.Media.DATA：视频文件路径；
        // MediaStore.Video.Media.DISPLAY_NAME : 视频文件名，如 testVideo.mp4
        // MediaStore.Video.Media.TITLE: 视频标题 : testVideo
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DISPLAY_NAME};

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, mediaColumns,
                null, null, MediaStore.Images.Media.DATE_MODIFIED);

        if (cursor == null || cursor.getCount() <= 0) return; // 没有图片;

        while (cursor.moveToNext()) {
            VideoInfo info = new VideoInfo();
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Video.Media._ID));

            Cursor thumbCursor = contentResolver.query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
                            + "=" + id, null, null);

            if (thumbCursor != null && thumbCursor.moveToFirst()) {
                info.setThumbPath(thumbCursor.getString(thumbCursor
                        .getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
            }

            info.setPath(cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
            info.setTitle(cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));

            info.setDisplayName(cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));

            info.setMimeType(cursor
                    .getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));

            //System.out.println(info.toString());
            Log.w("TEST", info.toString());
        }
    }

    @AfterClass
    public static void destroy() {
        ImageService.close(context);
    }
}
