package com.koncle.imagemanagement.dataManagement;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.ImageAndEvent;
import com.koncle.imagemanagement.bean.MySearchSuggestion;
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

import java.io.File;
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

    public static Image ifExistImage(Image image) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images;
        if (image.getId() == null) {
            images = imageDao.queryRawCreate("where T.path = ?", image.getPath()).list();
        } else {
            images = imageDao.queryBuilder().where(ImageDao.Properties.Id.eq(image.getId())).build().list();
        }
        if (images.size() > 0) {
            return images.get(0);
        } else {
            return null;
        }
    }

    public static Image insertImageWithOutCheck(Image image) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        Log.w(TAG, "insert " + image.getPath());
        imageDao.insertInTx(image);
        return image;
    }

    public static Image insertImage(Image image) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        Image imageInDatabase = ifExistImage(image);
        if (imageInDatabase == null) {
            imageDao.insertInTx(image);
            imageInDatabase = image;
        }
        return imageInDatabase;
    }

    public static void deleteImageInDataBase(Image image) {
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

    public static Image getCoverFromFolder(String folder) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryBuilder()
                .where(ImageDao.Properties.Folder.eq(folder))
                .orderDesc(ImageDao.Properties.Time)
                .build().list();
        return images.size() > 0 ? images.get(0) : null;
    }

    public static Long getImageCountFromFolder(String folder) {
        Long count = daoManager.getDaoSession().getImageDao()
                .queryBuilder()
                .where(ImageDao.Properties.Folder.eq(folder))
                .count();
        return count;
    }

    public static List<Image> getImagesFromFolder(String folder) {
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

    public static boolean deleteImageInFileSystem(Context context, Image image, boolean invalidImage) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        imageDao.delete(image);
        boolean ret = ImageUtils.deleteFile(image.getPath());
        // if the action of delete is successful
        // or meet invalid image
        //
        if (invalidImage || ret) {

            broadcastToNotifySystem(context, image.getPath());
            // this function would cause memory leak when the activity finished and the service is
            // still scanning file

            // MediaScannerConnection.scanFile(context, new String[]{image.getPath()}, null,null);
        }
        return ret;
    }

    public static void broadcastToNotifySystem(Context context, String path) {
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(new File(path));
        mediaScannerIntent.setData(uri);
        context.sendBroadcast(mediaScannerIntent);
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

    public static void overwriteImageTag(Image image, List<Tag> tags) {
        // delete
        TagAndImageDao tagAndImageDao = daoManager.getDaoSession().getTagAndImageDao();
        List<TagAndImage> tagAndImages = tagAndImageDao.queryBuilder()
                .where(TagAndImageDao.Properties.Image_id.eq(image.getId()))
                .build().list();
        tagAndImageDao.deleteInTx(tagAndImages);

        // add
        tagAndImages.clear();
        for (Tag tag : tags) {
            Long tagId = tag.getId();
            TagAndImage tagAndImage = new TagAndImage(null, tagId, image.getId());
            tagAndImages.add(tagAndImage);
        }
        tagAndImageDao.insertInTx(tagAndImages);
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
        if (images.size() == 0) return;
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

        int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        int timeIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
        int latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
        int lngIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
        int despIndex = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);

        while (cursor.moveToNext()) {
            path = cursor.getString(index); // 文件地址
            String name = cursor.getString(nameIndex);
            long time = cursor.getLong(timeIndex);
            double lat = cursor.getDouble(latIndex);
            double lng = cursor.getDouble(lngIndex);
            String desp = cursor.getString(despIndex);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String folder;
            try {
                folder = ImageUtils.getFolderNameFromPath(path);
                image = new Image();
                image.setName(name);
                image.setFolder(folder);
                image.setPath(path);
                image.setTime(new Date(time));
                image.setLat(String.valueOf(lat));
                image.setLng(String.valueOf(lng));
                image.setDesc(desp);
                ImageService.insertImage(image);
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("path", "failed path name : " + path);
            }
        }
        cursor.close();
    }

    public static boolean moveFile(Image image, String folder) {
        String des = folder + "/" + image.getName();
        String src = image.getPath();
        boolean b = ImageUtils.copyFile(src, des);
        return b && ImageUtils.deleteFile(src);
    }

    public static Tag searchTagByName(String name) {
        List<Tag> tags = daoManager.getDaoSession().getTagDao()
                .queryBuilder()
                .where(TagDao.Properties.Tag.eq(name))
                .build()
                .list();
        if (tags.size() > 0) {
            return tags.get(0);
        } else {
            return null;
        }
    }

    public static Event searchEventByName(String name) {
        List<Event> events = daoManager.getDaoSession().getEventDao()
                .queryBuilder()
                .where(EventDao.Properties.Name.eq(name))
                .build()
                .list();
        if (events.size() > 0) {
            return events.get(0);
        } else {
            return null;
        }
    }

    public static List<MySearchSuggestion> findSuggestions(String query) {
        List<MySearchSuggestion> suggestions = new ArrayList<>();
        if ("".equals(query)) return suggestions;

        DaoSession daoSession = daoManager.getDaoSession();
        TagDao tagDao = daoSession.getTagDao();
        EventDao eventDao = daoSession.getEventDao();

        List<Tag> tags = tagDao.queryBuilder()
                .where(TagDao.Properties.Tag.like("%" + query + "%"))
                .build().list();

        List<Event> events = eventDao.queryBuilder()
                .where(EventDao.Properties.Name.like("%" + query + "%"))
                .build().list();

        for (Tag tag : tags) {
            suggestions.add(new MySearchSuggestion(tag.getTag(), MySearchSuggestion.TYPE_TAG));
        }

        for (Event event : events) {
            suggestions.add(new MySearchSuggestion(event.getName(), MySearchSuggestion.TYPE_EVENT));
        }

        return suggestions;
    }

    public static void addImageDesc(Image image, String remark) {
        image.setDesc(remark);
        daoManager.getDaoSession().getImageDao().save(image);
    }

    public static void updateImagePath(Context context, Image image, String folderPath) {
        broadcastToNotifySystem(context, image.getPath());
        String path = folderPath + "/" + image.getName();
        image.setPath(path);
        image.setFolder(ImageUtils.getFolderNameFromPath(path));
        daoManager.getDaoSession().getImageDao().save(image);
        broadcastToNotifySystem(context, image.getPath());
    }
}
