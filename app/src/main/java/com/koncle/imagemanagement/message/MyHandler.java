package com.koncle.imagemanagement.message;

/**
 * Created by Koncle on 2018/1/24.
 */

public class MyHandler {

    public static final int SCAN_OK = 1;
    public static final int SCAN_OK_SHOW = 2;
    public static final int IMAGE_ADDED = 3;
    public static final int IMAGE_DELETED = 4;

    public static final int FOLDER_DELETED = 5;
    public static final int FOLDER_ADDED = 6;

    public static final int IMAGE_MOVED = -10;

    public static final int IMAGE_ADD_TO_EVENT = -21;

    public static final int IMAGE_TAG_CHANGED = -22;

    public static final int IMAGE_DELETED_BY_SELF = -23;
}