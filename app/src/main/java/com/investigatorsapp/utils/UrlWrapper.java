package com.investigatorsapp.utils;

import com.google.gson.Gson;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.model.User;

import java.net.URLEncoder;

/**
 * Created by fenglei on 15/12/21.
 */
public class UrlWrapper {

    private static final String ENCODE = "utf-8";

    private static String BASE_URL = "http://101.201.152.134/api.php/Api";

    private static String BASE_UPLOADFILE_URL = "http://101.201.152.134/api.php/File";

    public static String getLoginUrl() {
        return BASE_URL + "/logon";
    }

    static class LoginJson {
        public String type;
        public String username;
        public String password;
    }

    public static String getLoginParams(String username, String password) {
        try {
            LoginJson loginJson = new LoginJson();
            loginJson.type = "logon";
            loginJson.username = username;
            loginJson.password = password;
            Gson gson = new Gson();
            return gson.toJson(loginJson);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("type=logon").append("&username=")
//                    .append(URLEncoder.encode(userid, ENCODE))
//                    .append("&password=").append(URLEncoder.encode(password, ENCODE));
//            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    static class UpdateJson {
        public String type;
        public String version;
    }

    public static String getUpdateUrl() {
        return BASE_URL + "/update";
    }

    public static String getUpdateUrlParams(int versionCode) {
        try {
            UpdateJson updateJson = new UpdateJson();
            updateJson.type = "update";
            updateJson.version = String.valueOf(versionCode);
            Gson gson = new Gson();
            return gson.toJson(updateJson);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("type=update").append("&version=")
//                    .append(String.valueOf(versionCode));
//            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    static class BlockJson {
        public String type;
        public String userid;
        public String jobid;
        public String token;
    }

    public static String getBlockUrl() {
        return BASE_URL + "/getPol";
    }

    public static String getBlockParams() {
        User user = UserSingleton.getInstance().getUser();
        try {
            BlockJson blockJson = new BlockJson();
            blockJson.type = "getPol";
            blockJson.userid = user.userid;
            blockJson.jobid = user.jobid;
            blockJson.token = user.token;
            Gson gson = new Gson();
            return gson.toJson(blockJson);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("type=getPol&")
//                    .append("userid=").append(URLEncoder.encode(user.userid, ENCODE))
//                    .append("&jobid=").append(URLEncoder.encode(user.jobid, ENCODE))
//                    .append("&token=").append(URLEncoder.encode(user.token, ENCODE));
//            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getSalerNoUrl() {
        return "";
    }

    public static String getSalerNoParams(String polygonid, String lat, String lng) {
        User user = UserSingleton.getInstance().getUser();
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("userid=").append(URLEncoder.encode(user.userid, ENCODE))
                    .append("&jobid=").append(URLEncoder.encode(user.jobid, ENCODE))
                    .append("&tokenid=").append(URLEncoder.encode(user.token, ENCODE))
                    .append("&polygonid=").append(URLEncoder.encode(polygonid, ENCODE))
                    .append("&lat=").append(URLEncoder.encode(lat, ENCODE))
                    .append("&lng=").append(URLEncoder.encode(lng, ENCODE));
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPostStoreUrl() {
        return BASE_URL + "/postQst";
    }

    public static String getPostStoreParams(String storeJson) {
        try {
            return storeJson;
//            return URLEncoder.encode(storeJson, ENCODE);
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("data=").append(URLEncoder.encode(storeJson, ENCODE));
//            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPhotoUploadUrl() {
        return BASE_UPLOADFILE_URL + "/uploadfile";
    }

    public static String getAuidoUploadUrl() {
        return BASE_UPLOADFILE_URL + "/uploadfile";
    }

    public static String getLocReportUrl() {
        return BASE_URL + "/postTrace";
    }

//    public static String getLocReportParams(Context context) {
//        LocReport locReport = new LocReport();
//        locReport.type = "postTrace";
//        locReport.userid = UserSingleton.getInstance().getUser().userid;
//        locReport.jobid = UserSingleton.getInstance().getUser().jobid;
//        locReport.token = UserSingleton.getInstance().getUser().token;
//        locReport.date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//        TraceDao traceDao = DaoSessionInstance.getDaoSession(context).getTraceDao();
//        List<Trace> traceList = traceDao.queryBuilder().list();
//        if(traceList != null) {
//            Map<String, ArrayList<Trace>> traceMap = new HashMap<>();
//            for(int i = 0; i < traceList.size(); i++) {
//                Trace trace = traceList.get(i);
//                if(traceMap.get(trace.getPointno()) == null) {
//                    traceMap.put(trace.getPointno(), new ArrayList<Trace>());
//                }
//                traceMap.get(trace.getPointno()).add(trace);
//            }
//            for(Map.Entry<String, ArrayList<Trace>> entry : traceMap.entrySet()) {
//                locReport.traceno =  entry.getKey();
//                locReport.points = entry.getValue();
//                locReport.pointnum = String.valueOf(locReport.points.size());
//            }
//        }
//        return BASE_URL + "/postTrace";
//    }

//    public static String getFileUploadParams(String fileName, String md5, long fileSize) {
//        try {
//            StringBuilder stringBuilder = new StringBuilder();
//            stringBuilder.append("filename=").append(URLEncoder.encode(fileName, ENCODE))
//                    .append("&md5=").append(URLEncoder.encode(md5, ENCODE))
//                    .append("&fileSize=").append(URLEncoder.encode(String.valueOf(fileSize), ENCODE));
//            return stringBuilder.toString();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }


}
