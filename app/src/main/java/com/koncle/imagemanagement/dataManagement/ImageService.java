package com.koncle.imagemanagement.dataManagement;

import android.content.Context;
import android.util.Log;

import com.koncle.imagemanagement.bean.Image;
import com.koncle.imagemanagement.dao.DaoMaster;
import com.koncle.imagemanagement.dao.ImageDao;

import java.util.List;

import static com.koncle.imagemanagement.util.TagUtil.DEBUG;

/**
 * Created by 10976 on 2018/1/11.
 */

public class ImageService {
    private static DaoManager daoManager;
    private static DaoMaster daoMaster;
    private static String TAG = ImageService.class.getSimpleName();

    public static void init(Context context) {
        daoManager = DaoManager.getInstance();
        daoManager.init(context);
    }

    public static boolean insertImage(Image image) {
        return true;
    }

    public static List<Image> getAllImages() {
        ImageDao imageDao = daoManager.getDaoSession().getImageDao();
        List<Image> images = imageDao.loadAll();
        if (DEBUG) {
            Log.i(TAG, "get all images : " + images.size());
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
}
