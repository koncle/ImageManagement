package com.koncle.imagemanagement.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.koncle.imagemanagement.dao.DaoSession;
import com.koncle.imagemanagement.dao.ImageDao;
import com.koncle.imagemanagement.dao.TagDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.JoinEntity;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

/**
 * Created by 10976 on 2018/1/11.
 */

@Entity
public class Image implements Parcelable {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String path;
    @NotNull
    private String folder;
    @NotNull
    private String name;

    private String desc;

    private String time;

    @ToMany
    @JoinEntity(
            entity = TagAndImage.class,
            sourceProperty = "image_id",
            targetProperty = "tag_id"
    )
    private List<Tag> tags;

    private Long event_id;

    private Long loc_id;

    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 1428462909)
    private transient ImageDao myDao;

    @Generated(hash = 1065913437)
    public Image(Long id, @NotNull String path, @NotNull String folder,
                 @NotNull String name, String desc, String time, Long event_id,
                 Long loc_id) {
        this.id = id;
        this.path = path;
        this.folder = folder;
        this.name = name;
        this.desc = desc;
        this.time = time;
        this.event_id = event_id;
        this.loc_id = loc_id;
    }

    @Generated(hash = 1590301345)
    public Image() {
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

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Long getEvent_id() {
        return this.event_id;
    }

    public void setEvent_id(Long event_id) {
        this.event_id = event_id;
    }

    public Long getLoc_id() {
        return this.loc_id;
    }

    public void setLoc_id(Long loc_id) {
        this.loc_id = loc_id;
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

    /**
     * called by internal mechanisms, do not call yourself.
     */
    @Generated(hash = 1859334423)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getImageDao() : null;
    }


    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null)
            dest.writeLong(-1);
        else
            dest.writeLong(id);

        dest.writeString(path);
        dest.writeString(folder);
        dest.writeString(name);
        dest.writeString(desc);
        dest.writeString(time);

        dest.writeList(tags);
        if (event_id == null)
            dest.writeLong(-1);
        if (loc_id == null)
            dest.writeLong(-1);
    }

    public static final Parcelable.Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {

            Image image = new Image();

            long tmp = source.readLong();
            //image.id=source.readLong();
            if (tmp == -1)
                image.id = null;
            else
                image.id = tmp;

            image.path = source.readString();
            image.folder = source.readString();
            image.name = source.readString();
            image.desc = source.readString();
            image.time = source.readString();

            source.readList(image.tags, Tag.class.getClass().getClassLoader());

            tmp = source.readLong();
            if (tmp == -1)
                image.event_id = null;
            else
                image.event_id = tmp;

            tmp = source.readLong();
            if (tmp == -1)
                image.loc_id = null;
            else
                image.loc_id = tmp;

            return image;
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
