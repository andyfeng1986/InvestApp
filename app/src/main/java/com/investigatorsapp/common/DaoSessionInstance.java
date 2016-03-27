package com.investigatorsapp.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.investigatorsapp.db.greendao.DaoMaster;
import com.investigatorsapp.db.greendao.DaoSession;

/**
 * Created by fenglei on 15/12/24.
 */
public class DaoSessionInstance {

    private static final String DB_NAME = "invest_app";

    private static DaoSession daoSession;

    private DaoSessionInstance() {

    }

    public synchronized static DaoSession getDaoSession(Context context) {
        if(daoSession == null) {
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context.getApplicationContext(), DB_NAME, null);
            SQLiteDatabase db = helper.getWritableDatabase();
            DaoMaster daoMaster = new DaoMaster(db);
            daoSession = daoMaster.newSession();
        }
        return daoSession;
    }


}
