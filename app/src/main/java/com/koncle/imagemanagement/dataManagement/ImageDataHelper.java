package com.koncle.imagemanagement.dataManagement;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 10976 on 2018/1/11.
 */

public class ImageDataHelper extends SQLiteOpenHelper {
    public static final String name = "image_manager";
    public static final int version = 1;

    private final String CREATE_TABLES_SQL = "create table `tag`(\n" +
            "id long primary key auto_increment,\n" +
            "tag varchar(100)\n" +
            ");\n" +
            "\n" +
            "create table folder(" +
            "id long primary key auto_increment," +
            "path varchar(100)" +
            ")" +
            "create table location(\n" +
            "id long primary key auto_increment,\n" +
            "country varchar(100) ,\n" +
            "province varchar(100),\n" +
            "city varchar(100) \n" +
            ");\n" +
            "\n" +
            "create table Event(\n" +
            "id long primary key auto_increment,\n" +
            "`name` varchar(50)\n" +
            ");\n" +
            "\n" +
            "create table image(\n" +
            "id long primary key auto_increment,\n" +
            "path varchar(100) ,\n" +
            "folder varchar(100) ,\n" +
            "`name` varchar(50),\n" +
            "desc varchar(200),\n" +
            "tag_id long ,\n" +
            "loc_id long ,\n" +
            "event_id long ,\n" +
            "constraint `fk_tag` foreign key(`tag_id`) references `tag` (id),\n" +
            "constraint `fk_loc` foreign key(`loc_id`) references `location`(`id`),\n" +
            "constraint `fk_event` foreign key(`event_id`) references `Event`(`id`)\n" +
            "constraint `fk_folder` foreign key(`folder_id`) references `folder`(`id`)\n" +
            ");\n";

    private final String DROP_TABLES_SQL = "drop table if exists image;" +
            "drop table if exists location;" +
            "drop table if exists Event;" +
            "drop table if exists tag;";

    public ImageDataHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public ImageDataHelper(Context context) {
        this(context, ImageDataHelper.name, null, ImageDataHelper.version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLES_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLES_SQL);
    }
}
