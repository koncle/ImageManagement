package com.koncle.imagemanagement.message;

import com.koncle.imagemanagement.bean.Folder;

/**
 * Created by Koncle on 2018/2/5.
 */

public interface FolderChangeObserver {
    void onFolderDeleted(Folder folder);

    void onFolderAdded(Folder folder);
}
