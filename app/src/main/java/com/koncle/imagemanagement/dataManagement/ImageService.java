package com.koncle.imagemanagement.dataManagement;

import android.content.Context;
import android.util.Log;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dao.DaoMaster;
import com.koncle.imagemanagement.dao.DaoSession;
import com.koncle.imagemanagement.dao.ImageDao;

import org.greenrobot.greendao.query.WhereCondition;

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
            imageDao.insert(image);
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
}
