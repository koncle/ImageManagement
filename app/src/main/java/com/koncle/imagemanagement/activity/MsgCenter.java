package com.koncle.imagemanagement.activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.util.FileChangeObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.koncle.imagemanagement.activity.MyHandler.IMAGE_DELETED_BY_SELF;
import static com.koncle.imagemanagement.activity.MyHandler.IMAGE_MOVED;

/**
 * Created by Koncle on 2018/1/22.
 */

// Life cycle managed by ImageService
public class MsgCenter {

    public static final String MOVE_PREIMAGE = "preImage";
    public static final String MOVE_REARIMAGE = "rearImage";

    public static void notifyDataDeletedInner(List<Image> deletedImages) {
        // send msg to all other activities
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("singleImages", (ArrayList<? extends Parcelable>) deletedImages);
        msg.setData(bundle);
        msg.what = IMAGE_DELETED_BY_SELF;
        MsgCenter.sendMsg(msg);
    }

    public static void notifyDataMovedInner(Image preImage, Image rearImage) {
        Message msg = new Message();
        msg.what = IMAGE_MOVED;
        Bundle bundle = new Bundle();
        bundle.putParcelable(MOVE_REARIMAGE, rearImage);
        bundle.putParcelable(MOVE_PREIMAGE, preImage);
        msg.setData(bundle);
        MsgCenter.sendMsg(msg);
    }

    public static class MyHandler extends Handler {
        public void handleMessage(Message msg) {
            sendMsg(msg);
        }
    }

    public static void sendEmptyMessage(int what, String from, String to) {
        Message msg1 = Message.obtain();
        msg1.what = what;
        sendMsg(msg1, from, to);
    }

    public static void sendMsg(Message msg) {
        sendMsg(msg, null, null);
    }

    public static void sendMsg(Message msg, String from) {
        sendMsg(msg, from, null);
    }

    public static void sendMsg(Message msg, String from, String to) {
        Message newMsg;
        // send to target handler
        if (to != null) {
            Handler target = handlerMap.get(to);
            if (target != null) {
                newMsg = Message.obtain(msg);
                newMsg.setTarget(target);
                newMsg.sendToTarget();
                return;
            }
        }

        // target is null, or wrong target
        if (from != null) {
            for (String name : handlerMap.keySet()) {
                if (!name.equals(from)) {
                    newMsg = Message.obtain(msg);
                    handlerMap.get(name).sendMessage(newMsg);
                }
            }
        } else {
            for (Handler handler : handlerMap.values()) {
                newMsg = Message.obtain(msg);
                handler.sendMessage(newMsg);
            }
        }
    }

    private static Map<String, Handler> handlerMap = new HashMap<>();
    private static FileChangeObserver observer;

    public static void init(Context context) {
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        observer = new FileChangeObserver(new MyHandler(), context);
        context.getContentResolver().registerContentObserver(uri, false, observer);
    }

    public static void close(Context context) {
        context.getContentResolver().unregisterContentObserver(observer);
        handlerMap.clear();
    }

    public static void addHandler(Handler handler, String name) {
        handlerMap.put(name, handler);
    }

    public static void removeHandler(String name) {
        handlerMap.remove(name);
    }
}
