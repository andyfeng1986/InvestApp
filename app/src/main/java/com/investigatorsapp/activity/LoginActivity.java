package com.investigatorsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.investigatorsapp.R;
import com.investigatorsapp.common.LocationReport;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.model.User;
import com.investigatorsapp.network.impl.LoginApiImpl;
import com.investigatorsapp.utils.Constant;
import com.investigatorsapp.utils.Util;

/**
 * Created by fenglei on 15/12/22.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = LoginActivity.class.getSimpleName();

    private EditText userEt;
    private EditText pwdEt;
    private CheckBox userCb;
    //private CheckBox loginCb;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userEt = (EditText)findViewById(R.id.username_et);
        userEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userCb.isChecked()) {
                    Util.saveUserName(LoginActivity.this, s.toString());
                }
            }
        });
        pwdEt = (EditText)findViewById(R.id.pwd_et);
        pwdEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (userCb.isChecked()) {
                    Util.saveUserPwd(LoginActivity.this, s.toString());
                }
            }
        });
        userCb = (CheckBox)findViewById(R.id.username_cb);
        userCb.setChecked(Util.getRemmberUser(this));
        userCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Util.saveRemmberUser(LoginActivity.this, userCb.isChecked());
                if(isChecked) {
                    if(!TextUtils.isEmpty(userEt.getText().toString().trim())) {
                        Util.saveUserName(LoginActivity.this, userEt.getText().toString());
                    }
                    if(!TextUtils.isEmpty(pwdEt.getText().toString().trim())) {
                        Util.saveUserPwd(LoginActivity.this, pwdEt.getText().toString());
                    }
                }
            }
        });
//        loginCb = (CheckBox)findViewById(R.id.login_cb);
//        loginCb.setChecked(Util.getAutoLogin(this));
//        loginCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                Util.saveAutoLogin(LoginActivity.this, loginCb.isChecked());
//            }
//        });
        loginBtn = (Button)findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        if(Util.getRemmberUser(this)) {
            userEt.setText(Util.getUserName(this));
            pwdEt.setText(Util.getUserPwd(this));
        }
    }

    @Override
    public void onClick(View v) {
        if(v.equals(loginBtn)) {
            String userName = userEt.getText().toString().trim();
            String pwd = pwdEt.getText().toString().trim();
            if(TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
                Toast.makeText(this, R.string.login_input_error, Toast.LENGTH_LONG).show();
            }else if(userName.length() < 6) {
                Toast.makeText(this, R.string.username_input_error, Toast.LENGTH_LONG).show();
            }else if(pwd.length() < 6) {
                Toast.makeText(this, R.string.pwd_input_error, Toast.LENGTH_LONG).show();
            }else {
                login(userName, pwd);
            }
        }
    }

    private void login(final String userName, final String pwd) {
        new LoginApiImpl().login(userName, pwd, new Response.Listener<User>() {
            @Override
            public void onResponse(User response) {
                if(response != null && Constant.RET_SUCCESS_CODE.equals(response.retcode)) {
//                    UserSingleton.getInstance().setLogin(true);
                    UserSingleton.getInstance().setUser(response);
                    //updateTraceNo(response.userid);
                    LocationReport.reportLocation(LoginActivity.this);
                    jumpToMainActivity();
                }else {
                    if(response != null) {
                        Toast.makeText(LoginActivity.this, response.retmessage, Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(LoginActivity.this, getString(R.string.login_error), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void jumpToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

//    private void updateTraceNo(String usereId) {
//        int traceNo = Util.getTraceNo(this, usereId);
//        long lastLoginTime = Util.getLoginTime(this, usereId);
//        if(System.currentTimeMillis() - lastLoginTime >= Constant.LOC_REPORT_INTERVAL) {
//            traceNo++;
//            Util.saveTraceNo(this, usereId, traceNo);
//        }
//        Util.saveLoginTime(LoginActivity.this, usereId, System.currentTimeMillis());
//    }

}
