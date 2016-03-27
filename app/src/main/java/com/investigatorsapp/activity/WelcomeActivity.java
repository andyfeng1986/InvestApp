package com.investigatorsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.investigatorsapp.R;
import com.investigatorsapp.common.AppUpdateManager;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.model.User;
import com.investigatorsapp.network.impl.LoginApiImpl;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.Util;

import java.lang.ref.WeakReference;

/**
 * Created by fenglei on 15/12/21.
 */
public class WelcomeActivity extends BaseActivity {

    private static final int LOGIN = 0;
    private static final int MAIN = 1;
    private static final int DELAY = 1000;

    private MyHandler myHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        TextView versionTV = (TextView) findViewById(R.id.version);
        String versionName = Util.getVersionName(this);
        if(!TextUtils.isEmpty(versionName)) {
            versionTV.setText("v" + versionName);
        }
        myHandler = new MyHandler(this);
        AppUpdateManager appUpdateManager = new AppUpdateManager(this);
        appUpdateManager.checkAppUpdate(new AppUpdateManager.CheckUpdateCallback() {
            @Override
            public void check(boolean isNew) {
                login();
            }
        });
    }

    private void login() {
//        boolean isLogin = UserSingleton.getInstance().isLogin();
        User user = UserSingleton.getInstance().getUser();
        if(user != null) {
            myHandler.sendEmptyMessageDelayed(MAIN, DELAY);
        }else {
            String userName = Util.getUserName(WelcomeActivity.this);
            String userPwd = Util.getUserPwd(WelcomeActivity.this);
            if(!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPwd)) {
                new LoginApiImpl().login(userName, userPwd, new Response.Listener<User>() {
                    @Override
                    public void onResponse(User response) {
                        if(response != null && Constant.RET_SUCCESS_CODE.equals(response.retcode)) {
//                            UserSingleton.getInstance().setLogin(true);
                            UserSingleton.getInstance().setUser(response);
                            myHandler.sendEmptyMessageDelayed(MAIN, DELAY);
                        }else {
                            String s = getString(R.string.login_error);
                            if(response != null) {
                                try {
                                    s = new String(response.retmessage.getBytes(), "utf-8");
                                }catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            Toast.makeText(WelcomeActivity.this, s, Toast.LENGTH_LONG).show();
                            myHandler.sendEmptyMessageDelayed(LOGIN, DELAY);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(WelcomeActivity.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
                        myHandler.sendEmptyMessageDelayed(LOGIN, DELAY);
                    }
                });
            }else {
                myHandler.sendEmptyMessageDelayed(LOGIN, DELAY);
            }
        }
    }

    private static class MyHandler extends Handler {

        private WeakReference<WelcomeActivity> weakReference;

        MyHandler(WelcomeActivity welcomeActivity) {
            weakReference = new WeakReference<WelcomeActivity>(welcomeActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            WelcomeActivity welcomeActivity = weakReference.get();
            if(welcomeActivity != null) {
                switch (msg.what) {
                    case LOGIN:
                        welcomeActivity.jumpToLoginActivity();
                        break;
                    case MAIN:
                        welcomeActivity.jumpToMainActivity();
                        break;
                    default:
                        break;
                }
            }
        }
    }

//    private void showDialog(final UpdateInfo updateInfo) {
//        if(dialog == null || !dialog.isShowing()) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("发现新版本");
//            builder.setTitle("软件更新");
//            builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    new DownloadAsyncTask().execute(updateInfo);
//                    dialogInterface.dismiss();
//                }
//            });
//            if(Integer.parseInt(updateInfo.updatetype) != 0) {
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
//                Util.installApk(WelcomeActivity.this, getApkPath(result));
//            }else {
//                Toast.makeText(WelcomeActivity.this, "下载失败", Toast.LENGTH_LONG).show();
//            }
//        }
//
//    }
//
//    private boolean downloadApk(String downloadUrl, String md5) {
//        try {
//            String tempFileName = getCacheDir().getAbsolutePath() + "/temp";
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
//        return getCacheDir().getAbsolutePath() + "/" + md5;
//    }

    private void jumpToLoginActivity() {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void jumpToMainActivity() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
