package com.example.icst;

import android.content.Context;

import com.example.icst.dao.DaoMaster;
import com.example.icst.dao.DaoSession;

/**
 * Created by 大杨编 on 2016/8/18.
 */

public class DBUtil {
    public static String dbName = "icstdb";
    private static DaoMaster daoMaster;
    private static DaoSession daoSession;
    /**
     * @param context
     * @return DaoMaster
     */
    public static DaoMaster getDaoMaster(Context context) {
        if (daoMaster == null) {
            DaoMaster.OpenHelper helper = new DaoMaster.DevOpenHelper(context,dbName, null);
            daoMaster = new DaoMaster(helper.getWritableDatabase());
            daoSession = daoMaster.newSession();
        }
        return daoMaster;
    }

    /**
     * @param context
     * @return DaoSession
     */
    public static DaoSession getDaoSession(Context context) {
        if (daoSession == null) {
            if (daoMaster == null) {
                daoMaster = getDaoMaster(context);
            }
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }
}