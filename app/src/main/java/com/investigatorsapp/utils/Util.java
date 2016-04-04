package com.investigatorsapp.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.investigatorsapp.app.MyApp;
import com.investigatorsapp.model.Gps;
import com.investigatorsapp.model.LatLng;
import com.investigatorsapp.network.FileUploaderAsyncHttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fenglei on 15/12/21.
 */
public class Util {

    public static final String TAG = "Util";

    public static String getUserName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_name", "");
    }

    public static String getUserPwd(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getString("user_pwd", "");
    }

    public static void saveUserName(Context context, String userName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("user_name", userName).commit();
    }

    public static void saveUserPwd(Context context, String pwd) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("user_pwd", pwd).commit();
    }

    public static void saveRemmberUser(Context context, boolean remmberUser) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("remmber_user", remmberUser).commit();
    }

    public static boolean getRemmberUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean("remmber_user", false);
    }

    public static String getPronvice(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        return sharedPreferences.getString("pronvice", "");
    }

    public static void savePronvice(Context context, String pronvice) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("pronvice", pronvice).commit();
    }

    public static String getCity(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        return sharedPreferences.getString("city", "");
    }

    public static void saveCtiy(Context context, String city) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("city", city).commit();
    }

    public static String getArea(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        return sharedPreferences.getString("area", "");
    }

    public static void saveArea(Context context, String area) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("address", Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("area", area).commit();
    }

    public static void saveLoginTime(Context context, String userName, long time) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userName + "_login", Context.MODE_PRIVATE);
        sharedPreferences.edit().putLong(userName + "_login", time).commit();
    }

    public static long getLoginTime(Context context, String userName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userName + "_login", Context.MODE_PRIVATE);
        return sharedPreferences.getLong(userName + "_login", -1);
    }

    public static void saveTraceNo(Context context, String userName, int id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userName + "_trace", Context.MODE_PRIVATE);
        sharedPreferences.edit().putInt(userName + "_trace", id).commit();
    }

    public static int getTraceNo(Context context, String userName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(userName + "_trace", Context.MODE_PRIVATE);
        return sharedPreferences.getInt(userName + "_trace", 0);
    }

//    public static void saveAutoLogin(Context context, boolean autoLogin) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
//        sharedPreferences.edit().putBoolean("user_auto_login", autoLogin).commit();
//    }
//
//    public static boolean getAutoLogin(Context context) {
//        SharedPreferences sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
//        return sharedPreferences.getBoolean("user_auto_login", false);
//    }



    public static void installApk(Context cxt, String apkPath) {
        // 为应用授权777权限
        try {
            File apkFile = new File(apkPath);
            String command = "chmod 777 " + apkFile.getAbsolutePath();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec(command);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
            cxt.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(cxt, "安装失败", Toast.LENGTH_LONG).show();
        }
    }

    private static String dataStringConvert(String dateString) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time;
        try {
            Date date = sdFormat.parse(dateString);
            time = new SimpleDateFormat("MMddHHmmss").format(date);
        } catch(Exception e) {
            e.printStackTrace();
            time = new SimpleDateFormat("MMddHHmmss").format(new Date());
        }
        return time;
    }

    public static String getPhotoFilePath(String salerNo, String dateString) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                "/" + salerNo + "_" + dataStringConvert(dateString) + ".jpg";
    }

    public static String getAudioFilePath(String salerNo, String dateString) {
//        return Environment.getExternalStorageDirectory().getAbsolutePath()
//                + "/" + salerNo + "_" + dataStringConvert(dateString) + ".3gp";
        return MyApp.app.getFilesDir() + "/" + salerNo + "_" + dataStringConvert(dateString) + ".3gp";
    }

    public static void uploadPhotoFile(String salerNo, String time,
                                       FileUploaderAsyncHttp.UpLoaderCallback upLoaderCallback) {
        File file = new File(getPhotoFilePath(salerNo, time));
        uploadFile(UrlWrapper.getPhotoUploadUrl(), file, upLoaderCallback);
    }

    public static void uploadAudioFile(String salerNo, String dateString,
                                       FileUploaderAsyncHttp.UpLoaderCallback upLoaderCallback) {
        File file = new File(getAudioFilePath(salerNo, dateString));
        uploadFile(UrlWrapper.getAuidoUploadUrl(), file, upLoaderCallback);
    }

    public static void uploadFile(String url, File file,
                                  final FileUploaderAsyncHttp.UpLoaderCallback upLoaderCallback) {
//        if(file != null && file.exists()) {
//            FileUploaderAsyncHttp fileUploader = new FileUploaderAsyncHttp(url, file);
//            fileUploader.uploader(upLoaderCallback);
//        }else {
//            if(upLoaderCallback != null) {
//                upLoaderCallback.onFailed(-1, "file does not exist");
//            }
//        }
        FileUploaderAsyncHttp fileUploader = new FileUploaderAsyncHttp(url, file);
        fileUploader.uploader(upLoaderCallback);
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static double pi = 3.1415926535897932384626;
    public static double a = 6378245.0;
    public static double ee = 0.00669342162296594323;

    public static Gps bd09_To_Gps84(double bd_lat, double bd_lon) {
        Gps gcj02 = bd09_To_Gcj02(bd_lat, bd_lon);
        Gps map84 = gcj_To_Gps84(gcj02.getWgLat(),
                gcj02.getWgLon());
        return map84;
    }

    public static Gps bd09_To_Gcj02(double bd_lat, double bd_lon) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new Gps(gg_lat, gg_lon);
    }

    public static Gps gcj_To_Gps84(double lat, double lon) {
        Gps gps = transform(lat, lon);
        double lontitude = lon * 2 - gps.getWgLon();
        double latitude = lat * 2 - gps.getWgLat();
        return new Gps(latitude, lontitude);
    }

    public static boolean outOfChina(double lat, double lon) {
        if (lon < 72.004 || lon > 137.8347)
            return true;
        if (lat < 0.8293 || lat > 55.8271)
            return true;
        return false;
    }

    public static Gps transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new Gps(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLat, mgLon);
    }

    public static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    public static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }


    public static void getGPSLatLng(Context context) {
        String contextService=Context.LOCATION_SERVICE;
        LocationManager locationManager=(LocationManager) context.getSystemService(contextService);
        String provider=LocationManager.GPS_PROVIDER;
        Location location = locationManager.getLastKnownLocation(provider);
        if(location != null) {
            LatLng latLng = new LatLng();
            latLng.lat = location.getLatitude();
            latLng.lng = location.getLongitude();
        }
    }

    public static void setGPS(Context context, boolean on_off) {
        boolean gpsEnabled = android.provider.Settings.Secure.isLocationProviderEnabled(
                context.getContentResolver(), LocationManager.GPS_PROVIDER);
        Intent gpsIntent = new Intent();
        gpsIntent.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
        gpsIntent.addCategory("android.intent.category.ALTERNATIVE");

        if (on_off == true) {
            if (!gpsEnabled) {
                gpsIntent.setData(Uri.parse("custom:3"));
                try {
                    PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            if (gpsEnabled) {
                gpsIntent.setData(Uri.parse("custom:3"));
                try {
                    PendingIntent.getBroadcast(context, 0, gpsIntent, 0).send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String readFileToString(File file) {
        if(file.exists()) {
            BufferedReader bufferReader = null;
            try {
                bufferReader = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                return stringBuilder.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(bufferReader != null) {
                    try {
                        bufferReader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return "";
    }

    public static Map<String, String> stringToMap(String content) {
        Map<String, String> map = new HashMap<>();
        if(!TextUtils.isEmpty(content)) {
            String[] strings = content.split(",");
            for(int i = 0; i < strings.length; i++) {
                String[] inStrings = strings[i].split(":");
                if(inStrings.length == 2) {
                    String key = inStrings[0].substring(1, inStrings[0].length() - 1);
                    String value = inStrings[1].substring(1, inStrings[1].length() - 1);
                    map.put(key, value);
                }
            }
        }
        return map;
    }



    public static String geoJsonString = "{\n" +
            "    \"retcode\": \"0000\",\n" +
            "    \"retmessage\": \"查询成功\",\n" +
            "    \"userid\": \"10001\",\n" +
            "    \"jobid\": \"00001\",\n" +
            "    \"polygons\": [\n" +
            "        {\n" +
            "            \"polygonno\": \"90001\",\n" +
            "            \"polygonname\": \"一号区块\",\n" +
            "            \"polygon\": [\n" +
            "                {\n" +
            "                    \"lat\": \"39.93923\",\n" +
            "                    \"lng\": \"116.357428\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.91923\",\n" +
            "                    \"lng\": \"116.327428\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.89923\",\n" +
            "                    \"lng\": \"116.347428\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.89923\",\n" +
            "                    \"lng\": \"116.367428\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.91923\",\n" +
            "                    \"lng\": \"116.387428\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"polygonno\": \"90002\",\n" +
            "            \"polygonname\": \"二号区块\",\n" +
            "            \"polygon\": [\n" +
            "                {\n" +
            "                    \"lat\": \"39.915291\",\n" +
            "                    \"lng\": \"116.403857\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915391\",\n" +
            "                    \"lng\": \"116.403957\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915391\",\n" +
            "                    \"lng\": \"116.403757\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915191\",\n" +
            "                    \"lng\": \"116.403757\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915191\",\n" +
            "                    \"lng\": \"116.403957\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"polygonno\": \"90003\",\n" +
            "            \"polygonname\": \"三号区块\",\n" +
            "            \"polygon\": [\n" +
            "                {\n" +
            "                    \"lat\": \"39.915291\",\n" +
            "                    \"lng\": \"116.403857\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915391\",\n" +
            "                    \"lng\": \"116.403957\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915391\",\n" +
            "                    \"lng\": \"116.403757\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915191\",\n" +
            "                    \"lng\": \"116.403757\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"lat\": \"39.915191\",\n" +
            "                    \"lng\": \"116.403957\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    public static String uploadJsonString = "{\n" +
            "    \"type\": \"postQst\",\n" +
            "    \"userid\": \"10001\",\n" +
            "    \"jobid\": \"00001\",\n" +
            "    \"tokenid\": \"1000123456789\",\n" +
            "    \"polygonid\": \"90001\",\n" +
            "    \"salerno\": \"9000100021\",\n" +
            "    \"time\": \"2015-12-12 10:10:09\",\n" +
            "   \"status\": \"0\",\n" +
            "    \"custname\": \"剑哥汽车养护行\",\n" +
            "    \"custperson\": \"王剑\",\n" +
            "    \"fphone\": \"01062898888\",\n" +
            "    \"telephone\": \"18911112222\",\n" +
            "    \"email\": \"test@126.com\",\n" +
            "    \"fax\": \"01082899999\",\n" +
            "    \"country\": \"中国\",\n" +
            "    \"province\": \"浙江省\",\n" +
            "    \"city\": \"杭州市\",\n" +
            "    \"district\": \"拱墅区\",\n" +
            "    \"address\": \"蔡马东路中环106号\",\n" +
            "    \"zipcode\": \"200211\",\n" +
            "    \"custtype\": \"B2C\",\n" +
            "    \"producttype\": \"汽机油\",\n" +
            "    \"channeltype\": \"快修店\",\n" +
            "    \"stationnum\": \"3\",\n" +
            "    \"workernum\": \"6\",\n" +
            "    \"lat1\": \"39.915291\", \n" +
            "    \"lng1\": \"116.403857\",\n" +
            "    \"lat2\": \"39.915281\", \n" +
            "    \"lng2\": \"116.403847\",\n" +
            "    \"photoname\": \"9000100021_1.png\",\n" +
            "    \"isshell\": \"1\",\n" +
            "    \"iscastrol\": \"1\",\n" +
            "    \"ismobil\": \"0\",\n" +
            "    \"monthoil\": \"2\",\n" +
            "    \"audioname\": \"9000100021_1.wav\"\n" +
            "}";

    public static String uploadString2 = "{\"type\":\"postQst\",\"userid\":\"10001\",\"jobid\":\"00001\",\"tokenid\":\"1000123456789\",\"polygonid\":\"90001\",\"salerno\":\"9000100021\",\"time\":\"2015-12-12 10:10:09\",\"status\":\"0\",\"custname\":\"\\u5251\\u54e5\\u6c7d\\u8f66\\u517b\\u62a4\\u884c\",\"custperson\":\"\\u738b\\u5251\",\"fphone\":\"01062898888\",\"telephone\":\"18911112222\",\"email\":\"test@126.com\",\"fax\":\"01082899999\",\"country\":\"\\u4e2d\\u56fd\",\"province\":\"\\u6d59\\u6c5f\\u7701\",\"city\":\"\\u676d\\u5dde\\u5e02\",\"district\":\"\\u62f1\\u5885\\u533a\",\"address\":\"\\u8521\\u9a6c\\u4e1c\\u8def\\u4e2d\\u73af106\\u53f7\",\"zipcode\":\"200211\",\"custtype\":\"B2C\",\"producttype\":\"\\u6c7d\\u673a\\u6cb9\",\"channeltype\":\"\\u5feb\\u4fee\\u5e97\",\"stationnum\":\"3\",\"workernum\":\"6\",\"lat1\":\"39.915291\",\"lng1\":\"116.403857\",\"lat2\":\"39.915281\",\"lng2\":\"116.403847\",\"photoname\":\"9000100021_1.png\",\"isshell\":\"1\",\"iscastrol\":\"1\",\"ismobil\":\"0\",\"monthoil\":\"2\",\"audioname\":\"9000100021_1.wav\"}";

}
