package com.koncle.imagemanagement.activity;

import android.util.Log;

import com.koncle.imagemanagement.bean.Image;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Koncle on 2018/1/24.
 */

// Controlled by DrawerActivity
public class WeakReference {
    static final String TAG = WeakReference.class.getSimpleName();
    public static List<Image> singleImages;
    public static List<Image> multi_images;
    public static List<Image> selections;

    public static void removeSelections() {
        if (selections != null) {
            selections.clear();
        }
    }

    public static List<Image> getSelections() {
        return selections;
    }

    public static void putSelections(List<Image> selections) {
        WeakReference.selections = new ArrayList<>(selections.size());
        Collections.copy(WeakReference.selections, selections);
    }


    public static void putMulImages(List<Image> imageList) {
        Log.w(TAG, "put mul images ");
        multi_images = new ArrayList<>(imageList.size());
        Collections.copy(multi_images, imageList);
    }

    public static void removeMulImages() {
        Log.w(TAG, "remove mul images ");
        if (multi_images != null) {
            multi_images.clear();
        }
    }

    public static List<Image> getMulImages() {
        return multi_images;
    }

    public static void putSingleImages(List<Image> imageList) {
        Log.w(TAG, "put single images ");
        singleImages = new ArrayList<>(imageList.size());
        Collections.copy(singleImages, imageList);
    }

    public static void removeSingleImages() {
        Log.w(TAG, "remove single images ");
        if (singleImages != null) {
            singleImages.clear();
        }
    }

    public static List<Image> getSingleImages() {
        return singleImages;
    }

    public static void clear() {
        removeSingleImages();
        removeMulImages();
        removeSelections();
    }
}