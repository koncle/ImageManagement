package com.koncle.imagemanagement.dialog;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;

/**
 * Created by 10976 on 2018/1/17.
 */

public class SingleImageDIalogFragment extends BaseDialogFragment {
    private Image image;

    public static SingleImageDIalogFragment newInstance(Image image) {
        Bundle args = new Bundle();

        SingleImageDIalogFragment fragment = new SingleImageDIalogFragment();
        fragment.setArguments(args);
        fragment.setData(image);
        return fragment;
    }

    public void setData(Image image) {
        // in case the tag related to the images has been added,
        // this image has to be refreshed to get new data;
        image.resetTags();// refresh();
        super.setData(image.getTags());
        this.image = image;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "Single");
    }

    @Override
    public void onClick(View v) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : getSelectedTags().values()) {
            sb.append(tag.getTag().toString());
            ImageService.addTag2Image(image, tag);
        }
        Toast.makeText(getContext(), " " + sb.toString(), Toast.LENGTH_SHORT).show();
    }
}
