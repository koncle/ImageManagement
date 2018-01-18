package com.koncle.imagemanagement.service;

import android.app.Service;
import android.content.Intent;
import android.os.FileObserver;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.koncle.imagemanagement.activity.MainActivity;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Thread.sleep;

public class ImageListenerService extends Service {
    private static final String TAG = ImageListener.class.getSimpleName();

    private List<ImageListener> imageListeners;

    public ImageListenerService() {
    }

    @Override
    public void onCreate() {
        imageListeners = new ArrayList<>();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<String> folders = intent.getExtras().getStringArrayList(MainActivity.WATCH_TAG);
        ImageListener imageListener;
        Log.w(TAG, "start watching file...");
        for (int i = 0; i < folders.size(); ++i) {
            Log.w(TAG, "watching file..." + folders.get(i));
            imageListener = new ImageListener(folders.get(i));
            imageListener.startWatching();
            imageListeners.add(imageListener);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true)
                    try {
                        sleep(1000);
                        Log.e("thread", "Thread running ");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }).start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (int i = 0; i < imageListeners.size(); ++i) {
            imageListeners.get(i).stopWatching();
        }
        Log.w(TAG, "stop watching file...");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public class ImageListener extends FileObserver {
        private String folder;

        public ImageListener(String path) {
            super(path);
            this.folder = path;
        }

        @Override
        public void onEvent(int event, @Nullable String path) {
            event &= ALL_EVENTS;
            Log.w(TAG, "event : " + event + " " + folder + "/" + path);
            switch (event) {
                case FileObserver.CREATE:
                    Image image = ImageUtils.getImageFromPath(folder + "/" + path);
                    ImageService.insertImage(image, true);
                    Log.w(TAG, "create file, path : " + path);
                    break;
                case FileObserver.DELETE:
                    ImageService.deleteImageByPath(folder + "/" + path);
                    Log.w(TAG, "delete file, path : " + path);
                    break;
                case FileObserver.MOVED_TO:
                    Log.w(TAG, "move file to path : " + path);
                    break;
                case FileObserver.MOVED_FROM:
                    Log.w(TAG, "move file from path : " + path);
                    break;
            }
        }
    }
}
