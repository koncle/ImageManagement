package com.koncle.imagemanagement.dataManagement;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.ImageAndEvent;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.bean.TagAndImage;
import com.koncle.imagemanagement.dao.DaoMaster;
import com.koncle.imagemanagement.dao.DaoSession;
import com.koncle.imagemanagement.dao.EventDao;
import com.koncle.imagemanagement.dao.ImageAndEventDao;
import com.koncle.imagemanagement.dao.ImageDao;
import com.koncle.imagemanagement.dao.TagAndImageDao;
import com.koncle.imagemanagement.dao.TagDao;
import com.koncle.imagemanagement.util.ImageUtils;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.koncle.imagemanagement.util.TagUtil.DEBUG;

/**
 * Created by 10976 on 2018/1/11.
 */

public class ImageService {
    private static DaoManager daoManager;
    private static String TAG = ImageService.class.getSimpleName();

    public static void init(Context context) {
        daoManager = DaoManager.getInstance();
        daoManager.init(context);
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    public static void close() {
        daoManager.closeConnection();
        daoManager = null;
    }

    public static void createTables() {
        DaoSession daoSession = daoManager.getDaoSession();
        DaoMaster.createAllTables(daoSession.getDatabase(), true);
    }

    public static void dropTables() {
        DaoSession daoSession = daoManager.getDaoSession();
        DaoMaster.dropAllTables(daoSession.getDatabase(), true);
    }

    public static boolean insertImage(Image image, boolean ifExist) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        if (imageDao.queryRawCreate("where T.path = ?", image.getPath()).list().size() == 0) {
            Log.w(TAG, "insert " + image.getPath());
            imageDao.insertInTx(image);
        }
        return true;
    }

    public static void deleteImage(Image image) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        imageDao.delete(image);
        if (DEBUG) {
            Log.i(TAG, "delete image : " + image.getPath());
        }
    }

    public static boolean deleteImages(final List<Image> deletedImages) {
        boolean flag = false;
        final DaoSession daoSession = daoManager.getDaoSession();
        try {
            daoSession.runInTx(new Runnable() {
                @Override
                public void run() {
                    for (Image image : deletedImages) {
                        daoSession.delete(image);
                    }
                }
            });
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DEBUG) {
            Log.i(TAG, "delete images : " + deletedImages.size());
        }
        return flag;
    }

    public static List<Image> getImages() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryBuilder().orderDesc(ImageDao.Properties.Time).build().list();
        List<Image> images2 = imageDao.queryBuilder().orderAsc(ImageDao.Properties.Time).build().list();
        if (DEBUG) {
            Log.i(TAG, "get all images : " + images.size());
        }
        return images;
    }

    public static List<Image> getImagesWithLoc() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryRawCreate("where T.lat !=? ", "0.0").list();
        if (DEBUG) {
            Log.i(TAG, "get images with loc : " + images.size());
        }
        return images;
    }

    public static List<Image> getFolders() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryRawCreate("where T._id!=? GROUP BY T.folder ORDER BY T.folder ASC ", "0").list();
        if (DEBUG) {
            Log.i(TAG, "get folders : " + images.size());
        }
        return images;
    }

    public static List<Image> getImagesFromSameFolders(String folder) {
        List<Image> images = daoManager.getDaoSession().getImageDao()
                .queryBuilder()
                .where(ImageDao.Properties.Folder.eq(folder))
                .orderDesc(ImageDao.Properties.Time)
                .build().list();
        if (DEBUG) {
            Log.i(TAG, "get images from folder : " + images.size());
        }
        return images;
    }

    public static void refreshTables() {
        DaoSession daoSession = daoManager.getDaoSession();
        DaoMaster.dropAllTables(daoSession.getDatabase(), true);
        DaoMaster.createAllTables(daoSession.getDatabase(), true);
    }

    public static boolean deleteImage(Context context, Image image, boolean invalidImage) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        imageDao.delete(image);
        boolean ret = ImageUtils.deleteFile(image.getPath());
        // if the action of delete is successful
        // or meet invalid image
        if (invalidImage || ret) {
            MediaScannerConnection.scanFile(context, new String[]{image.getPath()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
        }
        return ret;
    }

    public static boolean deleteImageByPath(String path) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryBuilder().where(new WhereCondition.StringCondition("T.path = ?", path)).list();
        Log.w(TAG, "delete file from path : " + path);
        Log.w(TAG, "find file : " + images.size());
        if (images.size() > 0) {
            imageDao.delete(images.get(0));
            return true;
        } else {
            return false;
        }
    }

    /*
    *
    *       TAGS
    * */

    public static Tag addTagIfNotExists(String tagString) {
        DaoSession daoSession = daoManager.getDaoSession();
        TagDao tagDao = daoSession.getTagDao();
        List<Tag> tags = tagDao.queryRawCreate(" where T.tag = ?", tagString).list();
        Tag tag;
        if (tags.size() > 0) {
            tag = tags.get(0);
        } else {
            tag = new Tag(null, tagString);
            tagDao.insertInTx(tag);
        }
        return tag;
    }

    public static List<Tag> getTags() {
        DaoSession daoSession = daoManager.getDaoSession();
        TagDao tagDao = daoSession.getTagDao();
        return tagDao.loadAll();
    }

    public static Tag addTag2Images(List<Image> images, Tag tag) {
        Long tagId = tag.getId();
        List<TagAndImage> tagAndImages = new ArrayList<>();
        for (Image image : images) {
            if (!image.getTags().contains(tag)) {
                TagAndImage tagAndImage = new TagAndImage(null, tagId, image.getId());
                tagAndImages.add(tagAndImage);
            }
        }
        TagAndImageDao tagAndImageDao = daoManager.getDaoSession().getTagAndImageDao();
        tagAndImageDao.insertInTx(tagAndImages);
        tag.resetImages();
        return tag;
    }

    public static void addTag2Image(Image image, Tag tag) {
        Long tagId = tag.getId();
        TagAndImage tagAndImage = new TagAndImage(null, tagId, image.getId());
        TagAndImageDao tagAndImageDao = daoManager.getDaoSession().getTagAndImageDao();
        tagAndImageDao.insertInTx(tagAndImage);
    }

    public static void addTags(List<Image> images, String tagString) {
        tagString = tagString.trim();
        Tag tag = addTagIfNotExists(tagString);
        addTag2Images(images, tag);
    }

    public static void addTags2Images(List<Image> images, List<Tag> tags) {
        for (Tag tag : tags) {
            addTag2Images(images, tag);
        }
    }

    public static void clearTag(Image image) {
        DaoSession daoSession = daoManager.getDaoSession();
        ImageDao imageDao = daoSession.getImageDao();
        image.resetTags();
        imageDao.save(image);
    }

    public static void clearTags(List<Image> images) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        for (Image image : images) {
            image.resetTags();
            ;
        }
        imageDao.saveInTx(images);
    }

    public static void recoverDaoSession(Image image) {
        DaoSession daoSession = daoManager.getDaoSession();
        image.__setDaoSession(daoSession);
    }

    /*
    *
    *       EVENTs
    * */

    public static List<Event> getEvents() {
        EventDao eventDao = daoManager.getDaoSession().getEventDao();
        return eventDao.loadAll();
    }

    public static Event addEvent(String eventName) {
        eventName = eventName.trim();
        EventDao eventDao = daoManager.getDaoSession().getEventDao();
        List<Event> events = eventDao.queryRawCreate(" where T.name = ?", eventName).list();
        Event event;
        if (events.size() > 0) {
            event = events.get(0);
        } else {
            event = new Event(null, eventName);
            eventDao.insert(event);
        }
        return event;
    }

    public static void updateEvent(Event event, String eventName) {
        event.setName(eventName);
        EventDao eventDao = daoManager.getDaoSession().getEventDao();
        eventDao.save(event);
    }

    public static void deleteEvent(Event event) {
        EventDao eventDao = daoManager.getDaoSession().getEventDao();
        eventDao.delete(event);
    }

    public static Event addImages2Event(Event event, List<Image> images) {
        Long eventId = event.getId();
        List<ImageAndEvent> imageAndEvents = new ArrayList<>();
        for (Image image : images) {
            if (!image.getEvents().contains(event)) {
                ImageAndEvent imageAndEvent = new ImageAndEvent(null, eventId, image.getId());
                imageAndEvents.add(imageAndEvent);
            }
        }
        ImageAndEventDao imageAndEventDao = daoManager.getDaoSession().getImageAndEventDao();
        imageAndEventDao.insertInTx(imageAndEvents);
        event.resetImageList();
        return event;
    }

    public static void deleteImageFromEvent(Image image, Event event) {
        ImageAndEventDao imageAndEventDao = daoManager.getDaoSession().getImageAndEventDao();
        List<ImageAndEvent> imageAndEvents = imageAndEventDao
                .queryRawCreate(" where T.image_id = ? and T.event_id = ?", image.getId(), event.getId()).list();
        if (imageAndEvents.size() > 0) {
            imageAndEventDao.deleteInTx(imageAndEvents);
        }
    }

    public static void recoverDaoSession(List<Image> images) {
        DaoSession daoSession = daoManager.getDaoSession();
        for (Image image : images) {
            image.__setDaoSession(daoSession);
        }
    }

    public static void getSystemPhotoList(Context context) {

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(uri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png"}, MediaStore.Images.Media.DATE_MODIFIED);

        if (cursor == null || cursor.getCount() <= 0) return; // 没有图片

        String path;
        Image image;
        DaoSession daoSession = DaoManager.getInstance().getDaoSession();

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

            int timeIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
            int latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
            int lngIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
            int despIndex = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);

            path = cursor.getString(index); // 文件地址
            String name = cursor.getString(nameIndex);
            long time = cursor.getLong(timeIndex);
            double lat = cursor.getDouble(latIndex);
            double lng = cursor.getDouble(lngIndex);
            String desp = cursor.getString(despIndex);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String folder;
            try {
                String[] f = path.split("/");
                folder = f[f.length - 2];

                image = new Image();
                image.setName(name);
                image.setFolder(folder);
                image.setPath(path);
                image.setTime(new Date(time));
                image.setLat(String.valueOf(lat));
                image.setLng(String.valueOf(lng));
                image.setDesc(desp);
                ImageService.insertImage(image, true);
                // daoSession.insert(image);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("path", "failed path name : " + path);
            }
        }
        cursor.close();
    }

}
