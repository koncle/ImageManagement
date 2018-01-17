package com.koncle.imagemanagement.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
        //image.resetTags();
        image.resetTags();// refresh();
        super.setData(image.getTags());
        this.image = image;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

        StringBuilder sb = new StringBuilder();
        for (Tag tag : getSelectedTags().values()) {
            sb.append(tag.getTag().toString());
            ImageService.addTag2Image(image, tag);
        }
        Toast.makeText(getContext(), " " + sb.toString(), Toast.LENGTH_SHORT).show();
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "Single");
    }
}
