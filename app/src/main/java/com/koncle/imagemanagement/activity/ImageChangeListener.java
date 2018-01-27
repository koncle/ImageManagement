package com.koncle.imagemanagement.activity;

import com.koncle.imagemanagement.bean.Image;

/**
 * Created by Koncle on 2018/1/25.
 */

public interface ImageChangeListener {
    void onImageAdded(Image image);

    void onImageMoved(Image oldImage, Image newImage);

    void onImageDeleted(Image image);
}
