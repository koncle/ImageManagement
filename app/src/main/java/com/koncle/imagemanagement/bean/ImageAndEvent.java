package com.koncle.imagemanagement.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by 10976 on 2018/1/18.
 */

@Entity
public class ImageAndEvent {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private Long event_id;

    @NotNull
    private Long image_id;

    @Generated(hash = 1022863805)
    public ImageAndEvent(Long id, @NotNull Long event_id, @NotNull Long image_id) {
        this.id = id;
        this.event_id = event_id;
        this.image_id = image_id;
    }

    @Generated(hash = 189040897)
    public ImageAndEvent() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEvent_id() {
        return this.event_id;
    }

    public void setEvent_id(Long event_id) {
        this.event_id = event_id;
    }

    public Long getImage_id() {
        return this.image_id;
    }

    public void setImage_id(Long image_id) {
        this.image_id = image_id;
    }
}
