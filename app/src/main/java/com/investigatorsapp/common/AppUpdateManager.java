package com.investigatorsapp.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.GsonRequest;
import com.investigatorsapp.logger.Logger;
import com.investigatorsapp.model.UpdateInfo;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.MD5;
import com.investigatorsapp.utils.UrlWrapper;
import com.investigatorsapp.utils.Util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by fenglei on 15/12/22.
 */
public class AppUpdateManager {

    private static final String TAG = AppUpdateManager.class.getSimpleName();

    private Context context;
    private Dialog dialog;

    public interface CheckUpdateCallback {
        public void check(boolean isNew);
    }

//    private class DownloadAsyncTask extends AsyncTask<UpdateInfo, Void, String> {
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected String doInBackground(UpdateInfo... updateInfos) {
//            File apkFile = new File(getApkPath(updateInfos[0].md5));
//            if(apkFile.exists() && apkFile.isFile()) {
//                return updateInfos[0].md5;
//            }
//            boolean result = downloadApk(updateInfos[0].filepath, updateInfos[0].md5);
//            if(result) {
//                return updateInfos[0].md5;
//            }
//            return "";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            if(!TextUtils.isEmpty(result)) {
//                Util.installApk(context, getApkPath(result));
//            }else {
//                Toast.makeText(context, "下载失败", Toast.LENGTH_LONG).show();
//            }
//        }
//
//    }
//    String testUrl = "http://m.shouji.360tpcdn.com/151215/891b33eebf0eecd1015cab66e4282559/com.soku.videostore_29.apk";

    public AppUpdateManager(Context context) {
        this.context = context;
    }

    public void checkAppUpdate(final CheckUpdateCallback callback) {
        final int versionCode = Util.getVersionCode(context);
        GsonRequest<UpdateInfo> gsonRequest = new GsonRequest<UpdateInfo>(Request.Method.POST,
                UrlWrapper.getUpdateUrl(), UpdateInfo.class,
                UrlWrapper.getUpdateUrlParams(versionCode),
                new Response.Listener<UpdateInfo>() {
                    @Override
                    public void onResponse(UpdateInfo response) {
                        try {
//                            if(response == null) {
//                                response = new UpdateInfo();
//                            }
//                            response.retcode = Constant.RET_SUCCESS_CODE;
//                            response.newversion = "2";
//                            response.updatetype = "1";
//                            response.filepath = testUrl;
//                            response.md5 = "891B33EEBF0EECD1015CAB66E4282559";
                            if(response != null && Constant.RET_SUCCESS_CODE.equals(response.retcode)
                                    && Integer.parseInt(response.newversion) > versionCode) {
                                if(callback != null) {
                                    showDialog(response, callback);
                                    return;
                                }
                            }
                            if(callback != null){
                                callback.check(false);
                            }
                        }catch (Exception e) {
                            e.printStackTrace();
                            if(callback != null){
                                callback.check(false);
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Logger.d(TAG, "check App update error, error = " + error);
                if(callback != null) {
                    callback.check(false);
                }
            }
        });
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

    private void showDialog(final UpdateInfo updateInfo, final CheckUpdateCallback callback) {
        if(dialog == null || !dialog.isShowing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("发现新版本");
            builder.setTitle("软件更新");
            builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    new DownloadAsyncTask().execute(updateInfo);
                    dialogInterface.dismiss();
                    if (callback != null) {
                        callback.check(true);
                    }
                }
            });
            if(Integer.parseInt(updateInfo.updatetype) != 0) {
                builder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        if(callback != null) {
                            callback.check(true);
                        }
                    }
                });
            }
            dialog = builder.create();
            dialog.setCancelable(false);
            dialog.show();
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    if (callback != null) {
                        callback.check(true);
                    }
                }
            });
        }
    }

    private class DownloadAsyncTask extends AsyncTask<UpdateInfo, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(UpdateInfo... updateInfos) {
            File apkFile = new File(getApkPath(updateInfos[0].md5));
            if(apkFile.exists() && apkFile.isFile()) {
                return updateInfos[0].md5;
            }
            boolean result = downloadApk(updateInfos[0].filepath, updateInfos[0].md5);
            if(result) {
                return updateInfos[0].md5;
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if(!TextUtils.isEmpty(result)) {
                Util.installApk(context, getApkPath(result));
            }else {
                Toast.makeText(context, "下载失败", Toast.LENGTH_LONG).show();
            }
        }

    }

    private boolean downloadApk(String downloadUrl, String md5) {
        try {
            String tempFileName = context.getCacheDir().getAbsolutePath() + "/temp";
            File apkFile = new File(tempFileName);
            if(apkFile.exists()) {
                apkFile.delete();
            }
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();
            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(apkFile);
            byte data[] = new byte[1000];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
//                Bundle resultData = new Bundle();
//                resultData.putInt("progress" ,(int) (total * 100 / fileLength));
//                receiver.send(UPDATE_PROGRESS, resultData);
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            String fileMD5 = MD5.md5sum(tempFileName);
            if(md5 != null && md5.equalsIgnoreCase(fileMD5)) {
//                String command = "chmod 777 " + apkFile.getAbsolutePath();
//                Runtime runtime = Runtime.getRuntime();
//                runtime.exec(command);
                return apkFile.renameTo(new File(getApkPath(md5)));
            }else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getApkPath(String md5) {
        return context.getCacheDir().getAbsolutePath() + "/" + md5;
    }


//    public void checkAppUpdate() {
//        checkAppUpdate(null);
//    }

//    public void checkAppUpdate(final CheckUpdateCallback callback) {
//        GsonRequest<UpdateInfo> gsonRequest = new GsonRequest<UpdateInfo>(Request.Method.POST,
//                UrlWrapper.getUpdateUrl(), UpdateInfo.class,
//                UrlWrapper.getUpdateUrlParams(Util.getVersionCode(context)),
//                new Response.Listener<UpdateInfo>() {
//            @Override
//            public void onResponse(UpdateInfo response) {
//                try {
//                    if(response != null && Integer.parseInt(response.newversion) > Util.getVerCode(context)) {
//                        if(callback != null) {
//                            callback.check(true, response);
//                        }
//                    }else if(callback != null){
//                        callback.check(false, null);
//                    }
//                }catch (Exception e) {
//                    e.printStackTrace();
//                    if(callback != null){
//                        callback.check(false, null);
//                    }
//                }
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Logger.d(TAG, "check App update error, error = " + error);
//                if(callback != null) {
//                    callback.check(false, null);
//                }
//            }
//        });
//        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
//    }

//    private void showDialog(final UpdateInfo updateInfo) {
//        if(dialog == null || !dialog.isShowing()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(context);
//            builder.setMessage("发现新版本");
//            builder.setTitle("软件更新");
//            builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    new DownloadAsyncTask().execute(updateInfo);
//                    dialogInterface.dismiss();
//                }
//            });
//            if(updateInfo.updatetype != 1) {
//                builder.setNegativeButton("暂不更新", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//            }
//            dialog = builder.create();
//            dialog.show();
//        }
//    }
//
//    private boolean downloadApk(String downloadUrl, String md5) {
//        try {
//            String tempFileName = context.getCacheDir().getAbsolutePath() + "/temp";
//            File apkFile = new File(tempFileName);
//            if(apkFile.exists()) {
//                apkFile.delete();
//            }
//            URL url = new URL(downloadUrl);
//            URLConnection connection = url.openConnection();
//            connection.connect();
//            // this will be useful so that you can show a typical 0-100% progress bar
//            int fileLength = connection.getContentLength();
//            // download the file
//            InputStream input = new BufferedInputStream(connection.getInputStream());
//            OutputStream output = new FileOutputStream(apkFile);
//            byte data[] = new byte[1000];
//            long total = 0;
//            int count;
//            while ((count = input.read(data)) != -1) {
//                total += count;
//                // publishing the progress....
////                Bundle resultData = new Bundle();
////                resultData.putInt("progress" ,(int) (total * 100 / fileLength));
////                receiver.send(UPDATE_PROGRESS, resultData);
//                output.write(data, 0, count);
//            }
//            output.flush();
//            output.close();
//            input.close();
//            String fileMD5 = MD5.md5sum(tempFileName);
//            if(md5 != null && md5.equals(fileMD5)) {
//                return apkFile.renameTo(new File(getApkPath(md5)));
//            }else {
//                return false;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private String getApkPath(String md5) {
//        return context.getCacheDir().getAbsolutePath() + "/" + md5;
//    }

}
