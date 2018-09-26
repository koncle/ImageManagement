package com.koncle.imagemanagement.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.koncle.imagemanagement.bean.TagAndImage;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "TAG_AND_IMAGE".
*/
public class TagAndImageDao extends AbstractDao<TagAndImage, Long> {

    public static final String TABLENAME = "TAG_AND_IMAGE";

    /**
     * Properties of entity TagAndImage.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Tag_id = new Property(1, Long.class, "tag_id", false, "TAG_ID");
        public final static Property Image_id = new Property(2, Long.class, "image_id", false, "IMAGE_ID");
    }


    public TagAndImageDao(DaoConfig config) {
        super(config);
    }
    
    public TagAndImageDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"TAG_AND_IMAGE\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"TAG_ID\" INTEGER NOT NULL ," + // 1: tag_id
                "\"IMAGE_ID\" INTEGER NOT NULL );"); // 2: image_id
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"TAG_AND_IMAGE\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, TagAndImage entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getTag_id());
        stmt.bindLong(3, entity.getImage_id());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, TagAndImage entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindLong(2, entity.getTag_id());
        stmt.bindLong(3, entity.getImage_id());
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public TagAndImage readEntity(Cursor cursor, int offset) {
        TagAndImage entity = new TagAndImage( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.getLong(offset + 1), // tag_id
            cursor.getLong(offset + 2) // image_id
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, TagAndImage entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTag_id(cursor.getLong(offset + 1));
        entity.setImage_id(cursor.getLong(offset + 2));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(TagAndImage entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(TagAndImage entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(TagAndImage entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
