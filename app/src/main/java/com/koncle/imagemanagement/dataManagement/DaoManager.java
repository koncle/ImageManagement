package com.koncle.imagemanagement.dataManagement;

import android.content.Context;

import com.koncle.imagemanagement.dao.DaoMaster;
import com.koncle.imagemanagement.dao.DaoSession;

import org.greenrobot.greendao.query.QueryBuilder;

/**
 * Created by 10976 on 2018/1/11.
 */

public class DaoManager {
    private static final String TAG = DaoManager.class.getSimpleName();
    private static final String DB_NAME = "image_management";
    private Context context;

    //多线程中要被共享的使用volatile关键字修饰
    private volatile static DaoManager mManager = new DaoManager();
    private static DaoMaster mDaoMaster;
    private static DaoMaster.DevOpenHelper mHelper;
    private static DaoSession mDaoSession;

    /**
     * 单例模式获得操作数据库对象
     *
     * @return
     */
    public static DaoManager getInstance() {
        return mManager;
    }

    public void init(Context context) {
        this.context = context;
    }

    /**
     * 判断是否有存在数据库，如果没有则创建
     *
     * @return
     */
    public DaoMaster getDaoMaster() {
        if (mDaoMaster == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, DB_NAME, null);
            mDaoMaster = new DaoMaster(helper.getWritableDb());//getWritableDatabase());
        }
        return mDaoMaster;
    }

    /**
     * 完成对数据库的添加、删除、修改、查询操作，仅仅是一个接口
     *
     * @return
     */
    public DaoSession getDaoSession() {
        if (mDaoSession == null) {
            if (mDaoMaster == null) {
                mDaoMaster = getDaoMaster();
            }
            mDaoSession = mDaoMaster.newSession();
        }
        return mDaoSession;
    }

    /**
     * 打开输出日志，默认关闭
     */
    public void setDebug() {
        QueryBuilder.LOG_SQL = true;
        QueryBuilder.LOG_VALUES = true;
    }

    /**
     * 关闭所有的操作，数据库开启后，使用完毕要关闭
     */
    public void closeConnection() {
        closeHelper();
        closeDaoSession();
    }

    public void closeHelper() {
        if (mHelper != null) {
            mHelper.close();
            mHelper = null;
        }
    }

    public void closeDaoSession() {
        if (mDaoSession != null) {
            mDaoSession.clear();
            mDaoSession = null;
        }
    }

}
