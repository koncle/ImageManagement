package com.koncle.imagemanagement.dao;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.ImageAndEvent;
import com.koncle.imagemanagement.bean.Location;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.bean.TagAndImage;

import com.koncle.imagemanagement.dao.EventDao;
import com.koncle.imagemanagement.dao.FolderDao;
import com.koncle.imagemanagement.dao.ImageDao;
import com.koncle.imagemanagement.dao.ImageAndEventDao;
import com.koncle.imagemanagement.dao.LocationDao;
import com.koncle.imagemanagement.dao.TagDao;
import com.koncle.imagemanagement.dao.TagAndImageDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig eventDaoConfig;
    private final DaoConfig folderDaoConfig;
    private final DaoConfig imageDaoConfig;
    private final DaoConfig imageAndEventDaoConfig;
    private final DaoConfig locationDaoConfig;
    private final DaoConfig tagDaoConfig;
    private final DaoConfig tagAndImageDaoConfig;

    private final EventDao eventDao;
    private final FolderDao folderDao;
    private final ImageDao imageDao;
    private final ImageAndEventDao imageAndEventDao;
    private final LocationDao locationDao;
    private final TagDao tagDao;
    private final TagAndImageDao tagAndImageDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        eventDaoConfig = daoConfigMap.get(EventDao.class).clone();
        eventDaoConfig.initIdentityScope(type);

        folderDaoConfig = daoConfigMap.get(FolderDao.class).clone();
        folderDaoConfig.initIdentityScope(type);

        imageDaoConfig = daoConfigMap.get(ImageDao.class).clone();
        imageDaoConfig.initIdentityScope(type);

        imageAndEventDaoConfig = daoConfigMap.get(ImageAndEventDao.class).clone();
        imageAndEventDaoConfig.initIdentityScope(type);

        locationDaoConfig = daoConfigMap.get(LocationDao.class).clone();
        locationDaoConfig.initIdentityScope(type);

        tagDaoConfig = daoConfigMap.get(TagDao.class).clone();
        tagDaoConfig.initIdentityScope(type);

        tagAndImageDaoConfig = daoConfigMap.get(TagAndImageDao.class).clone();
        tagAndImageDaoConfig.initIdentityScope(type);

        eventDao = new EventDao(eventDaoConfig, this);
        folderDao = new FolderDao(folderDaoConfig, this);
        imageDao = new ImageDao(imageDaoConfig, this);
        imageAndEventDao = new ImageAndEventDao(imageAndEventDaoConfig, this);
        locationDao = new LocationDao(locationDaoConfig, this);
        tagDao = new TagDao(tagDaoConfig, this);
        tagAndImageDao = new TagAndImageDao(tagAndImageDaoConfig, this);

        registerDao(Event.class, eventDao);
        registerDao(Folder.class, folderDao);
        registerDao(Image.class, imageDao);
        registerDao(ImageAndEvent.class, imageAndEventDao);
        registerDao(Location.class, locationDao);
        registerDao(Tag.class, tagDao);
        registerDao(TagAndImage.class, tagAndImageDao);
    }
    
    public void clear() {
        eventDaoConfig.clearIdentityScope();
        folderDaoConfig.clearIdentityScope();
        imageDaoConfig.clearIdentityScope();
        imageAndEventDaoConfig.clearIdentityScope();
        locationDaoConfig.clearIdentityScope();
        tagDaoConfig.clearIdentityScope();
        tagAndImageDaoConfig.clearIdentityScope();
    }

    public EventDao getEventDao() {
        return eventDao;
    }

    public FolderDao getFolderDao() {
        return folderDao;
    }

    public ImageDao getImageDao() {
        return imageDao;
    }

    public ImageAndEventDao getImageAndEventDao() {
        return imageAndEventDao;
    }

    public LocationDao getLocationDao() {
        return locationDao;
    }

    public TagDao getTagDao() {
        return tagDao;
    }

    public TagAndImageDao getTagAndImageDao() {
        return tagAndImageDao;
    }

}
