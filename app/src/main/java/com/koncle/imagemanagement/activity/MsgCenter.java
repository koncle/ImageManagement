package com.koncle.imagemanagement.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;

import com.koncle.imagemanagement.util.FileChangeObserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Koncle on 2018/1/22.
 */

public class MsgCenter {

    public static class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            for (Handler handler : handlers) {
                Message newMsg = new Message();
                newMsg.what = msg.what;
                Bundle bundle = new Bundle();
                bundle.putParcelable("image", msg.getData().getParcelable("image"));
                newMsg.setData(bundle);
                handler.sendMessage(newMsg);
            }
        }
    }

    private static List<Handler> handlers = new ArrayList<>();
    private static FileChangeObserver observer;

    public static void init(Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        observer = new FileChangeObserver(new MyHandler(), context);
        context.getContentResolver().registerContentObserver(uri, false, observer);
    }

    public static void close(Context context) {
        context.getContentResolver().unregisterContentObserver(observer);
        handlers.clear();
    }

    public static void addHandler(Handler handler) {
        handlers.add(handler);
    }

    public static void removeHandler(Handler handler) {
        handlers.remove(handler);
    }


}
