package com.koncle.imagemanagement.dialog;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.dataManagement.ImageService;

import java.util.List;

/**
 * Created by 10976 on 2018/1/17.
 */

public class MultipleImageDialogFragment extends BaseDialogFragment {
    private List<Image> images;

    public static MultipleImageDialogFragment newInstance(List<Image> images) {
        Bundle args = new Bundle();

        MultipleImageDialogFragment fragment = new MultipleImageDialogFragment();
        fragment.setArguments(args);
        fragment.setImagesData(images);
        return fragment;
    }

    public void setImagesData(List<Image> images) {
        super.setData(null);
        this.images = images;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "Multi");
    }

    @Override
    public void onClick(View v) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : getSelectedTags().values()) {
            sb.append(tag.getTag());
            ImageService.addTag2Images(images, tag);
        }
        Toast.makeText(getContext(), " " + sb.toString(), Toast.LENGTH_SHORT).show();
    }
}
