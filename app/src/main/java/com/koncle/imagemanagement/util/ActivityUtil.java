package com.koncle.imagemanagement.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.koncle.imagemanagement.R;
import com.koncle.imagemanagement.activity.MapActivity;
import com.koncle.imagemanagement.activity.MultiColumnImagesActivity;
import com.koncle.imagemanagement.activity.SingleImageActivity;
import com.koncle.imagemanagement.bean.Image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class ActivityUtil {

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

    public static void showImageList(Context context, List<Image> images) {
        Intent intent = new Intent(context, MultiColumnImagesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("images", (ArrayList<Image>) images);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void showSingleImageWithPos(Context context, List<Image> images, int currentPos) {
        Intent intent = new Intent(context, SingleImageActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("images", (ArrayList<Image>) images);
        bundle.putInt("pos", currentPos);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, 1); // lager than 0
    }

    public static void showPopup(final Context context, View view) {
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.getMenuInflater().inflate(R.menu.event_op, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(context, "delete...", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
            }
        });
        popupMenu.show();
    }

    public static void showMap(Context context, List<Image> images) {
        Intent intent = new Intent(context, MapActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("images", (ArrayList<Image>) images);
        intent.putExtras(bundle);
        ((Activity) context).startActivityForResult(intent, 1);
    }
}
