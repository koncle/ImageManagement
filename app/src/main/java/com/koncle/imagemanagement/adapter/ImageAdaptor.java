package com.koncle.imagemanagement.adapter;

import android.content.Context;
import android.os.Parcelable;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;

import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dataManagement.ImageService;
import com.koncle.imagemanagement.util.ActivityUtil;
import com.koncle.imagemanagement.util.FileChangeObserver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 10976 on 2018/1/8.
 */

public class ImageAdaptor extends AbstractImageAdapter{
    private static final String TAG = ImageAdaptor.class.getSimpleName();
    private boolean descOrder = true;

    public ImageAdaptor(Context context, GridLayoutManager gridLayoutManager, List<Image> images) {
        super(context, gridLayoutManager);
        if (images == null) {
            this.images = new ArrayList<>();
        } else {
            this.images = images;
        }
    }

    @Override
    protected boolean imageClickInSelectMode(int pos, ImageViewHolder holder) {
        return false;
    }

    @Override
    protected boolean imageClickNotInSelectMode(int pos, ImageViewHolder holder) {
        Parcelable obj = modeListener.getObj();
        if (obj != null) {
            ActivityUtil.showSingleImageWithPos(ImageAdaptor.this.context, modeListener.getObj(), pos, descOrder, holder.image);
        } else {
            ActivityUtil.showSingleImageWithPos(ImageAdaptor.this.context, images, pos, descOrder, holder.image);
        }
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

    public int moveSelectedImages(Folder folder) {
        int count = 0;
        FileChangeObserver.lock();
        for (Image image : selectedImages.values()) {
            count += ImageService.moveFileAndSendMsg(context, image, folder) ? 1 : 0;
        }
        FileChangeObserver.unlock();
        Log.w(TAG, "delete " + count + " files");
        return count;
    }

    public int deleteSelectedImages() {
        int count = 0;

        FileChangeObserver.lock();
        for (Image image : selectedImages.values()) {
            count += ImageService.deleteImageInFileSystemAndBroadcast(context, image, true) ? 1 : 0;
        }
        FileChangeObserver.unlock();
        Log.w(TAG, "delete " + count + " files");
        return count;
    }

    public void removeSelectedItems() {
        for (Image image : selectedImages.values()) {
            // can't directly delete image from images with pos
            // cause the pos is relative to original images
            removeItem(image);
        }
    }

    // delete one item from single view
    public void removeItem(Image image) {
        int pos = images.indexOf(image);
        if (pos == -1) return;
        images.remove(pos);
        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, images.size() - pos);
        Log.w(TAG, "remove a image item " + image.getPath());
    }

    public void setData(List<Image> images) {
        this.images = images;
        notifyDataSetChangedWithoutFlash();
    }

    // image added from other apps
    public void addNewImage(Image image) {
        if (descOrder) {
            images.add(0, image);
            // prevent wrong position for views
            // because when call notifyItemInserted() method, the Items below this
            // item will be moved, thus position changed and the most important
            // point is the method : onBindView will not be called,
            // so that listener that were added to these items will not be
            // updated.
            // If use notifyItemRangeChanged, the animation will not displayed
            // but the position is correct.
            // There use both them to ensure the animation and position
            notifyItemInserted(0);
        } else {
            images.add(image);
            notifyItemInserted(images.size());
        }
        notifyItemRangeChanged(0, images.size());
    }

    public void addImage(Image image) {
        int size = images.size();
        // desc order
        if (descOrder) {
            // insert into the first place
            int i = 0;
            for (; i < images.size(); ++i)
                if (image.getTime().getTime() >= images.get(i).getTime().getTime())
                    break;
            images.add(i, image);

            notifyItemInserted(0);
            notifyItemRangeChanged(0, images.size());
        } else {
            int i = 0;
            for (; i < images.size(); ++i)
                if (image.getTime().getTime() <= images.get(i).getTime().getTime())
                    break;
            // append the list
            images.add(i, image);
            notifyItemInserted(size);
        }
    }


    public void toggleImagesOrder() {
        descOrder = !descOrder;
        Collections.reverse(images);
        notifyDataSetChangedWithoutFlash();
    }
}
