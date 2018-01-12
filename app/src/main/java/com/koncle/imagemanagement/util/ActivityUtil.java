package com.koncle.imagemanagement.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.koncle.imagemanagement.activity.ImageListViewer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/12.
 */

public class ActivityUtil {
    public static void showImageList(Context context, List<String> data) {
        Intent intent = new Intent(context, ImageListViewer.class);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("paths", (ArrayList<String>) data);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }
}
