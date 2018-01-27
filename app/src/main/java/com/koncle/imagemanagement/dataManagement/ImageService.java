package com.koncle.imagemanagement.dataManagement;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import com.koncle.imagemanagement.activity.DrawerActivity;
import com.koncle.imagemanagement.activity.MsgCenter;
import com.koncle.imagemanagement.activity.MultiColumnImagesActivity;
import com.koncle.imagemanagement.activity.MyHandler;
import com.koncle.imagemanagement.bean.Event;
import com.koncle.imagemanagement.bean.Folder;
import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.ImageAndEvent;
import com.koncle.imagemanagement.bean.MySearchSuggestion;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.bean.TagAndImage;
import com.koncle.imagemanagement.bean.VideoInfo;
import com.koncle.imagemanagement.dao.DaoMaster;
import com.koncle.imagemanagement.dao.DaoSession;
import com.koncle.imagemanagement.dao.EventDao;
import com.koncle.imagemanagement.dao.FolderDao;
import com.koncle.imagemanagement.dao.ImageAndEventDao;
import com.koncle.imagemanagement.dao.ImageDao;
import com.koncle.imagemanagement.dao.TagAndImageDao;
import com.koncle.imagemanagement.dao.TagDao;
import com.koncle.imagemanagement.util.ImageUtils;

import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.koncle.imagemanagement.activity.MyHandler.IMAGE_ADD_TO_EVENT;
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
        MsgCenter.init(context);

        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    public static void close(Context context) {
        daoManager.closeConnection();
        daoManager = null;
        MsgCenter.close(context);
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
            Log.w("IMAGE SERVICE", "insert a image : " + image);
        }
        return imageInDatabase;
    }

    public static void deleteImageInDataBase(Image image) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        imageDao.delete(image);
        if (DEBUG) {
            Log.i(TAG, "delete image : " + image);
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
            Log.i(TAG, "delete singleImages : " + deletedImages.size());
        }
        return flag;
    }

    public static List<Image> getImages() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryBuilder().orderDesc(ImageDao.Properties.Time).build().list();
        if (DEBUG) {
            Log.w(TAG, "get all singleImages : " + images.size());
        }
        return images;
    }

    public static List<Image> getImagesWithLoc() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryRawCreate("where T.lat !=? ", "0.0").list();
        if (DEBUG) {
            Log.i(TAG, "get singleImages with loc : " + images.size());
        }
        return images;
    }

    public static Image getLatestImage() {
        return daoManager.getDaoSession().getImageDao().queryBuilder()
                .orderDesc(ImageDao.Properties.Time)
                .limit(1)
                .list().get(0);
    }

    public static Image getCoverFromFolder(long folderId) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryBuilder()
                .where(ImageDao.Properties.Folder_id.eq(folderId))
                .orderDesc(ImageDao.Properties.Time)
                .build().list();
        return images.size() > 0 ? images.get(0) : null;
    }

    public static Long getImagesCount() {
        Long count = daoManager.getDaoSession().getImageDao()
                .queryBuilder()
                .count();
        return count;
    }

    public static Long getImageCountByFolder(String folder) {
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
            Log.i(TAG, "get singleImages from folder : " + images.size());
        }
        return images;
    }

    public static void refreshTables() {
        DaoSession daoSession = daoManager.getDaoSession();
        DaoMaster.dropAllTables(daoSession.getDatabase(), true);
        DaoMaster.createAllTables(daoSession.getDatabase(), true);
    }

    public static boolean deleteImageInFileSystemAndBroadcast(Context context, Image image, boolean deleteSystemFile) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        imageDao.delete(image);
        // if the action of delete is successful
        // or meet invalid image
        //
        boolean ret = true;
        if (deleteSystemFile) {
            ret = ImageUtils.deleteFile(image.getPath());
        }

        // this function would cause memory leak when the activity finished and the service is
        // still scanning file

        // MediaScannerConnection.scanFile(context, new String[]{image.getPath()}, null,null);
        broadcastToNotifySystem(context, image.getPath());

        MsgCenter.notifyDataDeletedInner(image);

        // refresh Cover
        refreshFolderCovers();
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

    public static void overwriteImageTagInThread(final List<Image> images, final List<Tag> tags) {
        daoManager.getDaoSession().runInTx(new Runnable() {
            @Override
            public void run() {
                TagAndImageDao tagAndImageDao = daoManager.getDaoSession().getTagAndImageDao();
                List<TagAndImage> tagAndImages = null;

                // delete
                for (Image image : images) {
                    tagAndImages = tagAndImageDao.queryBuilder()
                            .where(TagAndImageDao.Properties.Image_id.eq(image.getId()))
                            .build().list();
                    tagAndImageDao.deleteInTx(tagAndImages);
                }
                // add
                tagAndImages.clear();
                for (Image image : images) {
                    for (Tag tag : tags) {
                        Long tagId = tag.getId();
                        TagAndImage tagAndImage = new TagAndImage(null, tagId, image.getId());
                        tagAndImages.add(tagAndImage);
                    }
                }
                // insert
                for (TagAndImage tagAndImage : tagAndImages) {
                    tagAndImageDao.insert(tagAndImage);
                }
                MsgCenter.sendEmptyMessage(MyHandler.IMAGE_TAG_ADDED, null, DrawerActivity.className);
            }
        });
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

    public static Event addImages2Event(final Event event, final List<Image> images) {
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

    public static void addImages2EventInThread(final Event event, final List<Image> images) {
        daoManager.getDaoSession().runInTx(new Runnable() {
            @Override
            public void run() {
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
                MsgCenter.sendEmptyMessage(IMAGE_ADD_TO_EVENT, null, DrawerActivity.className);
            }
        });
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

    // load all singleImages
    public static int getSystemPhotoList(Context context) {
        int count = 0;
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();

        Cursor cursor = contentResolver.query(uri, null,
                MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=? or "
                        + MediaStore.Images.Media.MIME_TYPE + "=?",
                new String[]{"image/jpeg", "image/png", "image/gif"}, MediaStore.Images.Media.DATE_MODIFIED);

        if (cursor == null || cursor.getCount() <= 0) return count;


        int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        //int thumnailIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MINI_THUMB_MAGIC);
        int timeIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
        int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
        int latIndex = cursor.getColumnIndex(MediaStore.Images.Media.LATITUDE);
        int lngIndex = cursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE);
        int descIndex = cursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION);
        int mineTypeIndex = cursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE);

        while (cursor.moveToNext()) {
            Image image = new Image();
            String path = cursor.getString(pathIndex); // 文件地址
            image.setPath(path);
            image = ifExistImage(image);
            if (image == null) {
                createImage(cursor, pathIndex, timeIndex, nameIndex, latIndex, lngIndex, descIndex, mineTypeIndex);
                count += 1;
            }
            /*
            int thumnailId = cursor.getInt(thumnailIndex);
            Cursor thumbCursor = contentResolver.query(
                    MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    null, MediaStore.Images.Thumbnails.IMAGE_ID
                            + "=" + thumnailId, null, null);
            String thumbnail = path;
            if (thumbCursor != null){
                if (thumbCursor.moveToNext()) {
                    thumbnail = thumbCursor.getString(thumbCursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
                }
                thumbCursor.close();
            }
            */
        }
        refreshFolderCovers();
        cursor.close();
        return count;
    }

    public static Image createImage(Cursor cursor, int pathIndex, int timeIndex, int nameIndex, int latIndex,
                                    int lngIndex, int descIndex, int mineTypeIndex) {

        String path = cursor.getString(pathIndex); // 文件地址
        String folderName = ImageUtils.getFolderNameFromPath(path);

        String name = cursor.getString(nameIndex);
        long time = cursor.getLong(timeIndex);
        double lat = cursor.getDouble(latIndex);
        double lng = cursor.getDouble(lngIndex);
        String desc = cursor.getString(descIndex);
        String mineType = cursor.getString(mineTypeIndex);

        Image image = new Image();
        image.setName(name);
        image.setFolder(folderName);

        List<Folder> folders = ifExistFolder(folderName);
        Folder f;
        if (folders.size() == 0) {
            f = insertFolder(ImageUtils.getFolderPathFromPath(path), folderName);
        } else {
            f = folders.get(0);
        }
        image.setFolder_id(f.getId());
        image.setPath(path);
        image.setThumbnailPath("");
        image.setTime(new Date(time));
        image.setLat(String.valueOf(lat));
        image.setLng(String.valueOf(lng));
        image.setDesc(desc);

        if ("image/gif".equals(mineType))
            image.setType(Image.TYPE_GIF);
        else
            image.setType(Image.TYPE_NORNAL);

        insertImageWithOutCheck(image);
        return image;
    }


    public static void refreshFolderCovers() {
        List<Folder> folders = getNewFolders();
        for (Folder folder : folders) {
            refreshFolderCover(folder);
        }
    }

    public static void refreshFolderCover(Image newImage) {
        Folder folder = daoManager.getDaoSession().getFolderDao().load(newImage.getFolder_id());
        refreshFolderCover(folder);
    }

    public static void refreshFolderCover(Folder folder) {
        Image image = getCoverFromFolderId(folder.getId());
        if (image != null) {
            folder.setCoverPath(image.getPath());
        } else {
            folder.setCoverPath(null);
        }
    }

    public static Image getCoverFromFolderId(long folderId) {
        List<Image> images = daoManager.getDaoSession().getImageDao().queryBuilder()
                .where(ImageDao.Properties.Folder_id.eq(folderId))
                .orderDesc(ImageDao.Properties.Time)
                .limit(1)
                .list();
        return images.size() > 0 ? images.get(0) : null;
    }

    public static List<Folder> getNewFolders() {
        return daoManager.getDaoSession().getFolderDao().loadAll();
    }

    public static Folder getAllNameFolder() {

        Folder folder = new Folder();
        Image image = getLatestImage();
        folder.setCoverPath(image.getPath());
        folder.setName(MultiColumnImagesActivity.ALL_FOLDER_NAME);
        return folder;
    }

    public static List<Folder> getNewAllFolders() {
        List<Folder> folders = daoManager.getDaoSession().getFolderDao().loadAll();
        folders.add(0, getAllNameFolder());
        return folders;
    }

    public static List<Folder> ifExistFolder(String folder) {
        return daoManager.getDaoSession().getFolderDao()
                .queryBuilder()
                .where(FolderDao.Properties.Name.eq(folder))
                .build().list();
    }

    public static Folder insertFolder(String folderPath, String name) {
        Folder folder = new Folder();
        folder.setName(name);
        folder.setPath(folderPath);
        daoManager.getDaoSession().getFolderDao().insert(folder);
        Log.w(TAG, "insert a folder " + folder.getPath());
        return folder;
    }

    public static void testFile(Context context) {
        // MediaStore.Video.Thumbnails.DATA:视频缩略图的文件路径
        String[] thumbColumns = {MediaStore.Video.Thumbnails.DATA,
                MediaStore.Video.Thumbnails.VIDEO_ID};

        // MediaStore.Video.Media.DATA：视频文件路径；
        // MediaStore.Video.Media.DISPLAY_NAME : 视频文件名，如 testVideo.mp4
        // MediaStore.Video.Media.TITLE: 视频标题 : testVideo
        String[] mediaColumns = {MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA, MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.MIME_TYPE,
                MediaStore.Video.Media.DISPLAY_NAME};

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, mediaColumns,
                null, null, MediaStore.Images.Media.DATE_MODIFIED);

        if (cursor == null || cursor.getCount() <= 0) return; // 没有图片;

        while (cursor.moveToNext()) {
            VideoInfo info = new VideoInfo();
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.Video.Media._ID));

            Cursor thumbCursor = contentResolver.query(
                    MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                    thumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID
                            + "=" + id, null, null);

            if (thumbCursor != null && thumbCursor.moveToFirst()) {
                info.setThumbPath(thumbCursor.getString(thumbCursor
                        .getColumnIndex(MediaStore.Video.Thumbnails.DATA)));
            }

            info.setPath(cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA)));
            info.setTitle(cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));

            info.setDisplayName(cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)));

            info.setMimeType(cursor
                    .getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)));

            //System.out.println(info.toString());
            Log.w("TEST", info.toString());
        }
    }

    public static boolean moveFileAndSendMsg(Context context, Image image, Folder folder) {
        String des = folder.getPath() + "/" + image.getName();
        String src = image.getPath();
        // delete from file system
        boolean b = ImageUtils.copyFile(src, des);
        if (b && ImageUtils.deleteFile(src)) {
            // set old image
            Image preImage = new Image();
            preImage.setPath(image.getPath());
            preImage.setFolder(image.getFolder());
            preImage.setName(image.getName());
            preImage.setType(image.getType());
            preImage.setDesc(image.getDesc());

            // update database
            ImageService.updateImagePath(context, image, folder);

            // notify
            MsgCenter.notifyDataMovedInner(preImage, image);

            // refreshCover
            refreshFolderCovers();
            return true;
        }
        return false;
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

    public static void updateImagePath(Context context, Image image, Folder folder) {
        broadcastToNotifySystem(context, image.getPath());
        String path = folder.getPath() + "/" + image.getName();
        image.setPath(path);
        image.setFolder(ImageUtils.getFolderNameFromPath(path));
        image.setFolder_id(folder.getId());
        daoManager.getDaoSession().getImageDao().save(image);
        broadcastToNotifySystem(context, image.getPath());
    }

    public static void deleteTag(final Tag tag) {
        Long tag_id = tag.getId();
        // delete
        TagAndImageDao tagAndImageDao = daoManager.getDaoSession().getTagAndImageDao();
        List<TagAndImage> tagAndImages = tagAndImageDao.queryBuilder()
                .where(TagAndImageDao.Properties.Tag_id.eq(tag_id))
                .build().list();
        tagAndImageDao.deleteInTx(tagAndImages);

        daoManager.getDaoSession().getTagDao().delete(tag);
    }

    public static List<Image> getImagesFromParcelable(Parcelable obj) throws IllegalArgumentException {
        if (obj == null) return null;
        if (obj instanceof Folder) {
            Folder folder = (Folder) obj;
            // All folder
            if (MultiColumnImagesActivity.ALL_FOLDER_NAME.equals(folder.getName())) {
                return getImages();
            } else {
                folder.__setDaoSession(daoManager.getDaoSession());
                return ((Folder) obj).getImages();
            }
        } else if (obj instanceof Event) {
            Event event = (Event) obj;
            event.__setDaoSession(daoManager.getDaoSession());
            return (event).getImageList();
        } else if (obj instanceof Tag) {
            Tag tag = (Tag) obj;
            tag.__setDaoSession(daoManager.getDaoSession());
            return (tag).getImages();
        } else {
            throw new IllegalArgumentException("wrong argument to extract images!");
        }
    }

    public static void recoverDaoSession(Parcelable obj) {
        if (obj instanceof Folder) {
            Folder folder = (Folder) obj;
            folder.__setDaoSession(daoManager.getDaoSession());
        } else if (obj instanceof Event) {
            Event event = (Event) obj;
            event.__setDaoSession(daoManager.getDaoSession());
        } else if (obj instanceof Tag) {
            Tag tag = (Tag) obj;
            tag.__setDaoSession(daoManager.getDaoSession());
        } else {
            throw new IllegalArgumentException("wrong argument to extract images!");
        }
    }

    public static void resetImages(Parcelable obj) {
        if (obj == null) return;
        if (obj instanceof Folder) {
            Folder folder = (Folder) obj;
            folder.resetImages();
        } else if (obj instanceof Event) {
            Event event = (Event) obj;
            event.resetImageList();
        } else if (obj instanceof Tag) {
            Tag tag = (Tag) obj;
            tag.resetImages();
        } else {
            throw new IllegalArgumentException("wrong argument to extract images!");
        }
    }
}
