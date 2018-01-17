package com.koncle.imagemanagement.dataManagement;

import android.content.Context;
import android.util.Log;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.bean.Tag;
import com.koncle.imagemanagement.bean.TagAndImage;
import com.koncle.imagemanagement.dao.DaoMaster;
import com.koncle.imagemanagement.dao.DaoSession;
import com.koncle.imagemanagement.dao.ImageDao;
import com.koncle.imagemanagement.dao.TagAndImageDao;
import com.koncle.imagemanagement.dao.TagDao;

import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
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

    public static List<Image> getAllImages() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.loadAll();
        if (DEBUG) {
            Log.i(TAG, "get all images : " + images.size());
        }
        return images;
    }

    public static Image getImageFromPath(String path) {
        String folder, name, time, loc;

        String[] f = path.split("/");
        folder = f[f.length - 2];
        name = f[f.length - 1].split("\\.")[0];

        Image image = new Image();
        image.setName(name);
        image.setFolder(folder);
        image.setPath(path);

        time = ImageAttribute.getTime(path);
        image.setTime(time);

        double[] tmp = ImageAttribute.getLocation(path);
        if (tmp != null) {
            image.setLat(String.valueOf(tmp[0]));
            image.setLng(String.valueOf(tmp[1]));
        }
        return image;
    }

    public static List<Image> getImagesWithLoc() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryRawCreate("where T.lat !=? ", "0").list();
        if (DEBUG) {
            Log.i(TAG, "get images with loc : " + images.size());
        }
        return images;
    }

    public static List<Image> getFolders() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryRawCreate("where T._id!=? GROUP BY FOLDER", "0").list();
        if (DEBUG) {
            Log.i(TAG, "get folders : " + images.size());
        }
        return images;
    }

    public static List<Image> getImagesFromSameFolders(String folder) {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.queryRawCreate("where T.folder = ?", folder).list();
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

    public static List<Image> getEvents() {
        return null;
    }

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

    public static void addTag2Images(List<Image> images, Tag tag) {
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

    public static void clearTags(Image image) {
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

    public static void recoverDaoSession(List<Image> images) {
        DaoSession daoSession = daoManager.getDaoSession();
        for (Image image : images) {
            image.__setDaoSession(daoSession);
        }
    }
}
