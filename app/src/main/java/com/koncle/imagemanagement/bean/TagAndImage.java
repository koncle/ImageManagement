package com.koncle.imagemanagement.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by 10976 on 2018/1/11.
 */
@Entity
public class TagAndImage {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private Long tag_id;

    @NotNull
    private Long image_id;

    @Generated(hash = 1137636668)
    public TagAndImage(Long id, @NotNull Long tag_id, @NotNull Long image_id) {
        this.id = id;
        this.tag_id = tag_id;
        this.image_id = image_id;
    }

    @Generated(hash = 1880183631)
    public TagAndImage() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTag_id() {
        return this.tag_id;
    }

    public void setTag_id(Long tag_id) {
        this.tag_id = tag_id;
    }

    public Long getImage_id() {
        return this.image_id;
    }

    public void setImage_id(Long image_id) {
        this.image_id = image_id;
    }

}
