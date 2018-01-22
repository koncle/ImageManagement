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

public class FolderDialogFragment extends BaseDialogFragment {
    private String folder;

    public static FolderDialogFragment newInstance(List<Tag> tags, String folder) {
        Bundle args = new Bundle();

        FolderDialogFragment fragment = new FolderDialogFragment();
        fragment.setArguments(args);
        fragment.setData(tags, folder);
        return fragment;
    }

    public void setData(List<Tag> tags, String folder) {
        super.setData(tags);
        this.folder = folder;
    }

    public void show(FragmentManager fragmentManager) {
        super.show(fragmentManager, "Folder");
    }

    @Override
    public void onClick(View v) {
        StringBuilder sb = new StringBuilder();
        for (Tag tag : getSelectedTags().values()) {
            sb.append(tag.getTag().toString());
            sb.append(" ");
            List<Image> images = ImageService.getImagesFromFolder(folder);
            ImageService.addTag2Images(images, tag);
        }
        Toast.makeText(getContext(), " " + sb.toString(), Toast.LENGTH_SHORT).show();
    }
}
