package com.investigatorsapp.common;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.google.gson.Gson;
import com.investigatorsapp.db.greendao.Trace;
import com.investigatorsapp.db.greendao.TraceDao;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.LocReport;
import com.investigatorsapp.model.LocReportInfo;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.UrlWrapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by fenglei on 16/1/13.
 */
public class LocationReport {

    private static final String TAG = "LocationReport";

//    public synchronized static void updateTraceNo(Context context) {
//        String userId = UserSingleton.getInstance().getUser().userid;
//        int traceNo = Util.getTraceNo(context, userId);
//        long lastLoginTime = Util.getLoginTime(context, userId);
//        if(System.currentTimeMillis() - lastLoginTime >= Constant.LOC_REPORT_INTERVAL) {
//            traceNo++;
//            Util.saveTraceNo(context, userId, traceNo);
//        }
//        Util.saveLoginTime(context, userId, System.currentTimeMillis());
//    }

    public synchronized static void saveLocation(Context context, double lat, double lng) {
        TraceDao traceDao = DaoSessionInstance.getDaoSession(context).getTraceDao();
//        List<Trace> traceList = traceDao.queryBuilder().list();
//        int pointNo = 0;
//        if(traceList != null && traceList.size() > 0) {
//            for(Trace trace : traceList) {
//                if(Integer.parseInt(trace.getPointno()) > pointNo) {
//                    pointNo = Integer.parseInt(trace.getPointno());
//                }
//            }
//        }
//        pointNo = pointNo + 1;
        Trace trace = new Trace();
//        trace.setTraceno(String.valueOf(Util.getTraceNo(context,
//                UserSingleton.getInstance().getUser().userid)));
        trace.setLat(String.valueOf(lat));
        trace.setLng(String.valueOf(lng));
//        trace.setPointno(String.valueOf(pointNo));
        trace.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        traceDao.insert(trace);
    }

    public synchronized static void reportLocation(Context context) {
        TraceDao traceDao = DaoSessionInstance.getDaoSession(context).getTraceDao();
        ArrayList<Trace> traceList = (ArrayList<Trace>)traceDao.queryBuilder().list();
        if(traceList != null && traceList.size() > 0) {
//            Map<String, ArrayList<Trace>> traceMap = new HashMap<>();
//            for(int i = 0; i < traceList.size(); i++) {
//                Trace trace = traceList.get(i);
//                if(traceMap.get(trace.getTraceno()) == null) {
//                    traceMap.put(trace.getTraceno(), new ArrayList<Trace>());
//                }
//                traceMap.get(trace.getTraceno()).add(trace);
//            }
            LocReport locReport = new LocReport();
            locReport.type = "postTrace";
            locReport.userid = UserSingleton.getInstance().getUser().userid;
            locReport.jobid = UserSingleton.getInstance().getUser().jobid;
            locReport.token = UserSingleton.getInstance().getUser().token;
            locReport.date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            locReport.points = traceList;
            locReport.pointnum = String.valueOf(traceList.size());
            updateLocToNetwork(locReport, context);
//            for(Map.Entry<String, ArrayList<Trace>> entry : traceMap.entrySet()) {
//                locReport.traceno =  entry.getKey();
//                locReport.points = entry.getValue();
//                locReport.pointnum = String.valueOf(locReport.points.size());
//                updateLocToNetwork(locReport, context);
//            }
        }
    }

    private static void updateLocToNetwork(final LocReport locReport, final Context context) {
        Gson gson = new Gson();
        String locReportGsonString = gson.toJson(locReport);
        GsonRequest<LocReportInfo> gsonRequest = new GsonRequest<LocReportInfo>(Request.Method.POST,
                UrlWrapper.getLocReportUrl(), LocReportInfo.class,
                locReportGsonString, new Response.Listener<LocReportInfo>() {
            @Override
            public void onResponse(LocReportInfo response) {
                if(response != null && response.retcode.equals(Constant.RET_SUCCESS_CODE)) {
                    TraceDao traceDao = DaoSessionInstance.getDaoSession(context).getTraceDao();
                    traceDao.deleteAll();
                }
                if(response != null) {
                    Logger.d(TAG, "updateLocToNetwork onResponse, response = " + response.retmessage);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.d(TAG, "updateLocToNetwork onErrorResponse, error = " + error.toString());
            }
        });
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

}
