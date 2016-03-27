package com.investigatorsapp.app;

import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.investigatorsapp.BuildConfig;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.utils.CrashHandler;

/**
 * Created by fenglei on 15/12/21.
 */
public class MyApp extends Application {

    private static final String TAG = MyApp.class.getSimpleName();

    public static Context app;

//    public static Map<String, Integer> polycountHashMap = new ConcurrentHashMap<String, Integer>();

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");
        super.onCreate();
        Logger.setDebug(BuildConfig.DEBUG);
        CrashHandler.getInstance().init(this);
        VolleySingleton.getInstance().init(this);
        SDKInitializer.initialize(this);
        app = this;
    }

//    public static void updatePolycountHashMap(String polyno) {
//        Integer polycount = polycountHashMap.get(polyno);
//        if(polycount != null) {
//            polycountHashMap.put(polyno, polycount + 1);
//        }
//    }

}
