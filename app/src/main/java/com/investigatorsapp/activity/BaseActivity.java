package com.investigatorsapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

/**
 * Created by fenglei on 15/12/21.
 */
public class BaseActivity extends Activity {

    protected ProgressDialog progressDialog;

    public void dissmissProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void createProgress(String title, String message) {
        Context activity = this.getParent() != null ? this.getParent() : this;
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(activity, AlertDialog.THEME_HOLO_LIGHT);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
        }
        if (progressDialog != null && progressDialog.isShowing())
            return;
        if (!progressDialog.isShowing()) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

}
