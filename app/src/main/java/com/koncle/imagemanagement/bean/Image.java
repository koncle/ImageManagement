package com.koncle.imagemanagement.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.amap.api.maps.model.LatLng;
import com.koncle.imagemanagement.dao.DaoSession;
import com.koncle.imagemanagement.dao.EventDao;
import com.koncle.imagemanagement.dao.ImageDao;
import com.koncle.imagemanagement.dao.TagDao;
import com.koncle.imagemanagement.markerClusters.ClusterItem;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Date;
import java.util.List;

/**
 * Created by 10976 on 2018/1/11.
 */

@Entity
public class Image implements Parcelable, Comparable<Image>, ClusterItem {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String path;
    @NotNull
    private String folder;
    @NotNull
    private String name;

    private String desc;

    private Date time;

    @ToMany
    @JoinEntity(
            entity = TagAndImage.class,
            sourceProperty = "image_id",
            targetProperty = "tag_id"
    )
    private List<Tag> tags;

    @ToMany
    @JoinEntity(
            entity = ImageAndEvent.class,
            sourceProperty = "image_id",
            targetProperty = "event_id"
    )
    private List<Event> events;

    private Long loc_id;

    private String lat;

    private String lng;

    @Transient
    private LatLng pos;

    public void setPos(LatLng pos) {
        this.pos = pos;
    }

    @Override
    public LatLng getPosition() {
        return pos;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Image && ((Image) obj).getPath().equals(path);
    }

    @Override
    public int compareTo(@NonNull Image image) {
        if (image.getTime().getTime() > time.getTime()) {
            return -1;
        } else if (image.getTime().getTime() == time.getTime()) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.path);
        dest.writeString(this.folder);
        dest.writeString(this.name);
        dest.writeString(this.desc);
        dest.writeLong(this.time != null ? this.time.getTime() : -1);
        dest.writeTypedList(this.tags);
        dest.writeTypedList(this.events);
        dest.writeValue(this.loc_id);
        dest.writeString(this.lat);
        dest.writeString(this.lng);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFolder() {
        return this.folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Date getTime() {
        return this.time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Long getLoc_id() {
        return this.loc_id;
    }

    public void setLoc_id(Long loc_id) {
        this.loc_id = loc_id;
    }

    public String getLat() {
        return this.lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return this.lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1124026004)
    public List<Tag> getTags() {
        if (tags == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            TagDao targetDao = daoSession.getTagDao();
            List<Tag> tagsNew = targetDao._queryImage_Tags(id);
            synchronized (this) {
                if (tags == null) {
                    tags = tagsNew;
                }
            }
        }
        return tags;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 404234)
    public synchronized void resetTags() {
        tags = null;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 737283601)
    public List<Event> getEvents() {
        if (events == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            EventDao targetDao = daoSession.getEventDao();
            List<Event> eventsNew = targetDao._queryImage_Events(id);
            synchronized (this) {
                if (events == null) {
                    events = eventsNew;
                }
            }
        }
        return events;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 1830105409)
    public synchronized void resetEvents() {
        events = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1859334423)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getImageDao() : null;
    }

    public Image() {
    }

    protected Image(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.path = in.readString();
        this.folder = in.readString();
        this.name = in.readString();
        this.desc = in.readString();
        long tmpTime = in.readLong();
        this.time = tmpTime == -1 ? null : new Date(tmpTime);
        this.tags = in.createTypedArrayList(Tag.CREATOR);
        this.events = in.createTypedArrayList(Event.CREATOR);
        this.loc_id = (Long) in.readValue(Long.class.getClassLoader());
        this.lat = in.readString();
        this.lng = in.readString();
    }

    @Generated(hash = 550789930)
    public Image(Long id, @NotNull String path, @NotNull String folder,
                 @NotNull String name, String desc, Date time, Long loc_id, String lat,
                 String lng) {
        this.id = id;
        this.path = path;
        this.folder = folder;
        this.name = name;
        this.desc = desc;
        this.time = time;
        this.loc_id = loc_id;
        this.lat = lat;
        this.lng = lng;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1428462909)
    private transient ImageDao myDao;

}
