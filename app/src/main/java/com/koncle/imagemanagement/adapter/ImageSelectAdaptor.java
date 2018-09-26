package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.widget.Toast;

import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageSelectAdaptor extends AbstractImageAdapter {
    private static final String TAG = ImageSelectAdaptor.class.getSimpleName();
    private static final int MAX_SELECT_NUM = 200;

    public HashMap<String, Image> getSelectedImages() {
        return totalSelectedImages;
    }

    public void restoreState(List<Image> values) {
        totalSelectedImages.clear();
        for (Image image : values) {
            totalSelectedImages.put(image.getPath(), image);
        }
        restoreSelectState(values);
    }

    // can't use position as key for there are so many different folders
    // and you can't tell this position belongs to which folder
    private HashMap<String, Image> totalSelectedImages = new HashMap<>();

    public ImageSelectAdaptor(Context context, GridLayoutManager gridLayoutManager, Folder folder) {
        super(context, gridLayoutManager);
        setData(folder);
        this.enterSelectMode();
    }

    public void warnSelectedNum() {
        Toast.makeText(context, "select at most 200 images", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void selectAll() {
        for (int i = 0; i < getItemCount(); ++i) {
            if (totalSelectedImages.size() == MAX_SELECT_NUM) {
                warnSelectedNum();
                break;
            } else {
                Image image = getItem(i);
                if (!totalSelectedImages.containsKey(image.getPath())){
                    totalSelectedImages.put(image.getPath(), image);
                    selectImage(i);
                }
            }
        }
        finishSelectImage();
    }

    @Override
    public List<Image> getSelections(){
        List<Image> selectedImages = new ArrayList<>();
        selectedImages.addAll(totalSelectedImages.values());
        return selectedImages;
    }

    @Override
    protected boolean imageClickInSelectMode(int pos, ImageViewHolder holder) {
        Image image = getItem(pos);
        if (totalSelectedImages.containsKey(image.getPath())) {
            totalSelectedImages.remove(image.getPath());
        }else {
            totalSelectedImages.put(image.getPath(), image);
        }
        return false;
    }

    @Override
    protected boolean imageClickNotInSelectMode(int pos, ImageViewHolder holder) {
        return false;
    }

    @Override
    protected boolean imageLongClickInSelectMode(int pos, ImageViewHolder holder) {
        return false;
    }

    @Override
    protected boolean imageLongClickNotInSelectMode(int pos, ImageViewHolder holder) {
        return false;
    }

    public void setData(Folder folder) {
        if (folder != null) {
            super.setData(folder.getImages());
        }else{
            super.setData(null);
        }
        List<Image> values = new ArrayList<>();
        values.addAll(totalSelectedImages.values());
        restoreSelectState(values);
    }
}
