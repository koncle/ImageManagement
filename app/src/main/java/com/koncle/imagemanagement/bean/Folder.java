package com.koncle.imagemanagement.bean;

import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

/**
 * Created by Koncle on 2018/1/25.
 */

public class Folder {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String name;

    private String coverPath;

    @ToMany(referencedJoinProperty = "folder_id")
    @OrderBy("time DESC")
    private List<Image> images;
}
