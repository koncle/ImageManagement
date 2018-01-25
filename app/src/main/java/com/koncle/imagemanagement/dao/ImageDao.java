package com.koncle.imagemanagement.dao;

import java.util.List;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import com.koncle.imagemanagement.bean.ImageAndEvent;
import com.koncle.imagemanagement.bean.TagAndImage;

import com.koncle.imagemanagement.bean.Image;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "IMAGE".
 */
public class ImageDao extends AbstractDao<Image, Long> {

    public static final String TABLENAME = "IMAGE";

    /**
     * Properties of entity Image.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Path = new Property(1, String.class, "path", false, "PATH");
        public final static Property ThumbnailPath = new Property(2, String.class, "thumbnailPath", false, "THUMBNAIL_PATH");
        public final static Property Folder = new Property(3, String.class, "folder", false, "FOLDER");
        public final static Property Name = new Property(4, String.class, "name", false, "NAME");
        public final static Property Desc = new Property(5, String.class, "desc", false, "DESC");
        public final static Property Time = new Property(6, java.util.Date.class, "time", false, "TIME");
        public final static Property Loc_id = new Property(7, Long.class, "loc_id", false, "LOC_ID");
        public final static Property Lat = new Property(8, String.class, "lat", false, "LAT");
        public final static Property Lng = new Property(9, String.class, "lng", false, "LNG");
        public final static Property Type = new Property(10, int.class, "type", false, "TYPE");
    }

    private DaoSession daoSession;

    private Query<Image> event_ImageListQuery;
    private Query<Image> location_ImagesQuery;
    private Query<Image> tag_ImagesQuery;

    public ImageDao(DaoConfig config) {
        super(config);
    }

    public ImageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"IMAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"PATH\" TEXT NOT NULL ," + // 1: path
                "\"THUMBNAIL_PATH\" TEXT," + // 2: thumbnailPath
                "\"FOLDER\" TEXT NOT NULL ," + // 3: folder
                "\"NAME\" TEXT NOT NULL ," + // 4: name
                "\"DESC\" TEXT," + // 5: desc
                "\"TIME\" INTEGER," + // 6: time
                "\"LOC_ID\" INTEGER," + // 7: loc_id
                "\"LAT\" TEXT," + // 8: lat
                "\"LNG\" TEXT," + // 9: lng
                "\"TYPE\" INTEGER NOT NULL );"); // 10: type
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"IMAGE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Image entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getPath());
 
        String thumbnailPath = entity.getThumbnailPath();
        if (thumbnailPath != null) {
            stmt.bindString(3, thumbnailPath);
        }
        stmt.bindString(4, entity.getFolder());
        stmt.bindString(5, entity.getName());
 
        String desc = entity.getDesc();
        if (desc != null) {
            stmt.bindString(6, desc);
        }
 
        java.util.Date time = entity.getTime();
        if (time != null) {
            stmt.bindLong(7, time.getTime());
        }
 
        Long loc_id = entity.getLoc_id();
        if (loc_id != null) {
            stmt.bindLong(8, loc_id);
        }
 
        String lat = entity.getLat();
        if (lat != null) {
            stmt.bindString(9, lat);
        }
 
        String lng = entity.getLng();
        if (lng != null) {
            stmt.bindString(10, lng);
        }
        stmt.bindLong(11, entity.getType());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Image entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getPath());
 
        String thumbnailPath = entity.getThumbnailPath();
        if (thumbnailPath != null) {
            stmt.bindString(3, thumbnailPath);
        }
        stmt.bindString(4, entity.getFolder());
        stmt.bindString(5, entity.getName());
 
        String desc = entity.getDesc();
        if (desc != null) {
            stmt.bindString(6, desc);
        }
 
        java.util.Date time = entity.getTime();
        if (time != null) {
            stmt.bindLong(7, time.getTime());
        }
 
        Long loc_id = entity.getLoc_id();
        if (loc_id != null) {
            stmt.bindLong(8, loc_id);
        }
 
        String lat = entity.getLat();
        if (lat != null) {
            stmt.bindString(9, lat);
        }
 
        String lng = entity.getLng();
        if (lng != null) {
            stmt.bindString(10, lng);
        }
        stmt.bindLong(11, entity.getType());
    }

    @Override
    protected final void attachEntity(Image entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public Image readEntity(Cursor cursor, int offset) {
        Image entity = new Image( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getString(offset + 1), // path
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // thumbnailPath
                cursor.getString(offset + 3), // folder
                cursor.getString(offset + 4), // name
                cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // desc
                cursor.isNull(offset + 6) ? null : new java.util.Date(cursor.getLong(offset + 6)), // time
                cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7), // loc_id
                cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8), // lat
                cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9), // lng
                cursor.getInt(offset + 10) // type
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, Image entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setPath(cursor.getString(offset + 1));
        entity.setThumbnailPath(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setFolder(cursor.getString(offset + 3));
        entity.setName(cursor.getString(offset + 4));
        entity.setDesc(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setTime(cursor.isNull(offset + 6) ? null : new java.util.Date(cursor.getLong(offset + 6)));
        entity.setLoc_id(cursor.isNull(offset + 7) ? null : cursor.getLong(offset + 7));
        entity.setLat(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
        entity.setLng(cursor.isNull(offset + 9) ? null : cursor.getString(offset + 9));
        entity.setType(cursor.getInt(offset + 10));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(Image entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Image entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(Image entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
    /** Internal query to resolve the "imageList" to-many relationship of Event. */
    public List<Image> _queryEvent_ImageList(Long event_id) {
        synchronized (this) {
            if (event_ImageListQuery == null) {
                QueryBuilder<Image> queryBuilder = queryBuilder();
                queryBuilder.join(ImageAndEvent.class, ImageAndEventDao.Properties.Image_id)
                        .where(ImageAndEventDao.Properties.Event_id.eq(event_id));
                queryBuilder.orderRaw("T.'TIME' ASC");
                event_ImageListQuery = queryBuilder.build();
            }
        }
        Query<Image> query = event_ImageListQuery.forCurrentThread();
        query.setParameter(0, event_id);
        return query.list();
    }

    /** Internal query to resolve the "images" to-many relationship of Location. */
    public List<Image> _queryLocation_Images(Long loc_id) {
        synchronized (this) {
            if (location_ImagesQuery == null) {
                QueryBuilder<Image> queryBuilder = queryBuilder();
                queryBuilder.where(Properties.Loc_id.eq(null));
                location_ImagesQuery = queryBuilder.build();
            }
        }
        Query<Image> query = location_ImagesQuery.forCurrentThread();
        query.setParameter(0, loc_id);
        return query.list();
    }

    /** Internal query to resolve the "images" to-many relationship of Tag. */
    public List<Image> _queryTag_Images(Long tag_id) {
        synchronized (this) {
            if (tag_ImagesQuery == null) {
                QueryBuilder<Image> queryBuilder = queryBuilder();
                queryBuilder.join(TagAndImage.class, TagAndImageDao.Properties.Image_id)
                        .where(TagAndImageDao.Properties.Tag_id.eq(tag_id));
                queryBuilder.orderRaw("T.'TIME' DESC");
                tag_ImagesQuery = queryBuilder.build();
            }
        }
        Query<Image> query = tag_ImagesQuery.forCurrentThread();
        query.setParameter(0, tag_id);
        return query.list();
    }

}
