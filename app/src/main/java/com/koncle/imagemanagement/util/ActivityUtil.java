package com.koncle.imagemanagement.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.SharedElementCallback;
import android.transition.Explode;
import android.util.Log;
import android.view.View;

import com.koncle.imagemanagement.activity.MapActivity;
import com.koncle.imagemanagement.activity.MultiColumnImagesActivity;
import com.koncle.imagemanagement.activity.SelectImageActivity;
import com.koncle.imagemanagement.activity.SingleImageActivity;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 10976 on 2018/1/12.
 */

public class ActivityUtil {
    public static final String ACTIVITY_MUL_IMAGE_TAG = "singleImages";
    public static final String ACTIVITY_MUL_IMAGE_TITLE_TAG = "title";

    public static final String ACTIVITY_POS_TAG = "pos";
    public static final String ACTIVITY_MUL_IMAGE_DATA = "mul";
    public static final String DATA_TYPE = "database";
    public static final String DESC_ORDER = "descOrder";

    public static void shareImages(Context context, List<Image> images) {

        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (Image image : images) {
            imageUris.add(Uri.fromFile(new File(image.getPath())));
        }

        Intent intent = new Intent();
                /*
                // share to wechat moment
                ComponentName comp = new ComponentName("com.tencent.mm",
                            "com.tencent.mm.ui.tools.ShareToTimeLineUI");
                intent.setComponent(comp);
                */
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        context.startActivity(intent);
    }

    public static void shareImage(Context context, Image image) {
        Uri uri = Uri.fromFile(new File(image.getPath()));
        Intent intent = new Intent();

        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(Intent.EXTRA_STREAM, uri);

        context.startActivity(intent);
    }

    /*
    * reset Images and send it to mul activity
    * */
    public static void showImageList(Context context, @Nullable Parcelable obj, String title) {
        Intent intent = new Intent(context, MultiColumnImagesActivity.class);
        Bundle bundle = new Bundle();
        ImageService.resetImages(obj);
        bundle.putParcelable(ACTIVITY_MUL_IMAGE_DATA, obj);
        bundle.putString(ACTIVITY_MUL_IMAGE_TITLE_TAG, title);
        bundle.putBoolean(DATA_TYPE, true);
        intent.putExtras(bundle);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
        ActivityCompat.startActivityForResult((Activity) context, intent, 1, options.toBundle());
        //((Activity) context).startActivityForResult(intent, 1);
    }

    /*
    * reset Images and send it to mul activity
    * */
    public static void showImageList(Context context, List<Image> images, String title) {
        Intent intent = new Intent(context, MultiColumnImagesActivity.class);
        Bundle bundle = new Bundle();

        bundle.putParcelableArrayList(ACTIVITY_MUL_IMAGE_DATA, (ArrayList<? extends Parcelable>) images);
        bundle.putString(ACTIVITY_MUL_IMAGE_TITLE_TAG, title);
        bundle.putBoolean("database", false);
        intent.putExtras(bundle);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
        ActivityCompat.startActivityForResult((Activity) context, intent, 1, options.toBundle());
        //((Activity) context).startActivityForResult(intent, 1);
    }

    public static void showSingleImageWithPos(final Context context, Parcelable obj, int currentPos, boolean desOrder, View view) {
        Intent intent = new Intent(context, SingleImageActivity.class);
        Bundle bundle = new Bundle();
        ImageService.resetImages(obj);

        bundle.putParcelable(ACTIVITY_MUL_IMAGE_DATA, obj);
        bundle.putBoolean(DATA_TYPE, true);
        bundle.putBoolean(DESC_ORDER, desOrder);
        bundle.putInt(ACTIVITY_POS_TAG, currentPos);
        intent.putExtras(bundle);

        //ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, view, context.getString(R.string.m2s_transition));
        //SharedElementCallback s = new ECallback();
        //ActivityCompat.setEnterSharedElementCallback((Activity) context, s);
        //ActivityCompat.setExitSharedElementCallback((Activity) context, s);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
        ActivityCompat.startActivityForResult((Activity) context, intent, 1, options.toBundle());

        //((Activity) context).startActivityForResult(intent, 1); // lager than 0
    }

    public static void showSingleImageWithPos(final Context context, List<Image> images, int currentPos, boolean desOrder, View view) {
        Intent intent = new Intent(context, SingleImageActivity.class);
        Bundle bundle = new Bundle();

        bundle.putBoolean(DATA_TYPE, false);
        bundle.putParcelableArrayList(ACTIVITY_MUL_IMAGE_DATA, (ArrayList<? extends Parcelable>) images);
        bundle.putBoolean(DESC_ORDER, desOrder);
        bundle.putInt(ACTIVITY_POS_TAG, currentPos);
        intent.putExtras(bundle);

        //ActivityOptionsCompat compat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, view, context.getString(R.string.m2s_transition));
        //SharedElementCallback s = new ECallback();
        //ActivityCompat.setEnterSharedElementCallback((Activity) context, s);
        //ActivityCompat.setExitSharedElementCallback((Activity) context, s);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
        ActivityCompat.startActivityForResult((Activity) context, intent, 1, options.toBundle());

        //((Activity) context).startActivityForResult(intent, 1); // lager than 0
    }

    public static void showMap(Context context, List<Image> images) {

        Intent intent = new Intent(context, MapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ACTIVITY_MUL_IMAGE_TAG, (ArrayList<Image>) images);
        intent.putExtras(bundle);

        //((Activity) context).startActivityForResult(intent, 1);

        ((Activity) context).getWindow().setExitTransition(new Explode());
        ((Activity) context).getWindow().setEnterTransition(new Explode());
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
        context.startActivity(intent, options.toBundle());
    }

    public static void selectImages(Context context) {
        Intent intent = new Intent(context, SelectImageActivity.class);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation((Activity) context);
        ActivityCompat.startActivityForResult((Activity) context, intent, 1, options.toBundle());
    }

    static class ECallback extends SharedElementCallback {
        @Override
        public void onSharedElementStart(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
            super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots);
            Log.w("Shared", "start share");
        }

        @Override
        public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
            super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
            Log.w("Shared", "end share");
        }

        @Override
        public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
            super.onMapSharedElements(names, sharedElements);
            Log.w("Shared", "map share");
            for (String s : names) {
                Log.w("Shared", s + sharedElements.get(s));
            }
        }

        @Override
        public void onRejectSharedElements(List<View> rejectedSharedElements) {
            super.onRejectSharedElements(rejectedSharedElements);
        }
    }
}
