package com.koncle.imagemanagement.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.ImageAndEvent;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "EVENT".
 */
public class EventDao extends AbstractDao<Event, Long> {

    public static final String TABLENAME = "EVENT";
    private DaoSession daoSession;
    private Query<Event> image_EventsQuery;

    public EventDao(DaoConfig config) {
        super(config);
    }

    public EventDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"EVENT\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"NAME\" TEXT NOT NULL );"); // 1: name
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"EVENT\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, Event entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getName());
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, Event entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
        stmt.bindString(2, entity.getName());
    }

    @Override
    protected final void attachEntity(Event entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    public Event readEntity(Cursor cursor, int offset) {
        Event entity = new Event( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.getString(offset + 1) // name
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, Event entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setName(cursor.getString(offset + 1));
    }
     
    @Override
    protected final Long updateKeyAfterInsert(Event entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(Event entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }
    
    @Override
    public boolean hasKey(Event entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

    /** Internal query to resolve the "events" to-many relationship of Image. */
    public List<Event> _queryImage_Events(Long image_id) {
        synchronized (this) {
            if (image_EventsQuery == null) {
                QueryBuilder<Event> queryBuilder = queryBuilder();
                queryBuilder.join(ImageAndEvent.class, ImageAndEventDao.Properties.Event_id)
                        .where(ImageAndEventDao.Properties.Image_id.eq(image_id));
                image_EventsQuery = queryBuilder.build();
            }
        }
        Query<Event> query = image_EventsQuery.forCurrentThread();
        query.setParameter(0, image_id);
        return query.list();
    }

    /**
     * Properties of entity Event.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Name = new Property(1, String.class, "name", false, "NAME");
    }

}
