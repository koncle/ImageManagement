package com.koncle.imagemanagement.message;

import com.koncle.imagemanagement.bean.Image;

/**
 * Created by Koncle on 2018/1/25.
 */

public interface ImageChangeObserver {
    void onImageAdded(Image image);

    void onImageMoved(Image oldImage, Image newImage);

    void onImageDeleted(Image image);
}
