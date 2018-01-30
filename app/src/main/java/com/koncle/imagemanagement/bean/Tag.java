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
import org.greenrobot.greendao.annotation.OrderBy;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

/**
 * Created by 10976 on 2018/1/11.
 */
@Entity
public class Tag implements Parcelable {
    @Id(autoincrement = true)
    private Long id;

    @NotNull
    private String tag;

    @ToMany
    @JoinEntity(
            entity = TagAndImage.class,
            sourceProperty = "tag_id",
            targetProperty = "image_id"
    )
    @OrderBy("time DESC")
    private List<Image> images;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Tag && ((Tag) obj).getId().equals(id);
    }
    /**
     * Used to resolve relations
     */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /**
     * Used for active entity operations.
     */
    @Generated(hash = 2076396065)
    private transient TagDao myDao;

    @Generated(hash = 917620460)
    public Tag(Long id, @NotNull String tag) {
        this.id = id;
        this.tag = tag;
    }

    @Generated(hash = 1605720318)
    public Tag() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return this.tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 1435243796)
    public List<Image> getImages() {
        if (images == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            ImageDao targetDao = daoSession.getImageDao();
            List<Image> imagesNew = targetDao._queryTag_Images(id);
            synchronized (this) {
                if (images == null) {
                    images = imagesNew;
                }
            }
        }
        return images;
    }

    /**
     * Resets a to-many relationship, making the next get call to query for a fresh result.
     */
    @Generated(hash = 604059028)
    public synchronized void resetImages() {
        images = null;
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
    @Generated(hash = 441429822)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTagDao() : null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.tag);
        dest.writeTypedList(this.images);
    }

    protected Tag(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.tag = in.readString();
        this.images = in.createTypedArrayList(Image.CREATOR);
    }

    public static final Creator<Tag> CREATOR = new Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel source) {
            return new Tag(source);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}
