package com.koncle.imagemanagement.bean;

/**
 * Created by Koncle on 2018/1/24.
 */

public class VideoInfo {
    String thumbPath;
    String path;
    String title;
    String displayName;
    String mimeType;

    @Override
    public String toString() {
        return title + " " + displayName + " " + thumbPath + " " + path + " " + mimeType;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
