package com.koncle.imagemanagement.dialog;


import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10976 on 2018/1/17.
 */

public class TagSelectDialog extends BaseDialogFragment {
    private List<Image> images;

    public static TagSelectDialog newInstance(Image image) {
        Bundle args = new Bundle();

        TagSelectDialog fragment = new TagSelectDialog();
        fragment.setArguments(args);
        List<Image> images = new ArrayList<>();
        images.add(image);
        fragment.setImagesData(images);
        return fragment;
    }

    public static TagSelectDialog newInstance(List<Image> images) {
        Bundle args = new Bundle();

        TagSelectDialog fragment = new TagSelectDialog();
        fragment.setArguments(args);
        fragment.setImagesData(images);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("images", (ArrayList<? extends Parcelable>) images);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            images = savedInstanceState.getParcelableArrayList("images");
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void setImagesData(List<Image> images) {
        this.images = images;
        if (images.size() == 1) {
            Image image = images.get(0);
            // ensure correct data
            image.resetTags();
            super.setData(image.getTags());
        } else {
            super.setData(null);
        }
    }

    @Override
    public void onClick(View v) {
        List<Tag> tags = new ArrayList<>();
        tags.addAll(getSelectedTags().values());
        for (Image image : images) {
            ImageService.overwriteImageTag(image, tags);
        }
    }
}
