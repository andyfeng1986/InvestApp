package com.investigatorsapp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.investigatorsapp.R;
import com.investigatorsapp.activity.LoginActivity;
import com.investigatorsapp.common.AppUpdateManager;
import com.investigatorsapp.common.UserSingleton;
import com.investigatorsapp.utils.Util;

/**
 * Created by fenglei on 15/12/22.
 */
public class MeFragment extends Fragment implements View.OnClickListener{

    private Button exitBtn;
    private Button checkUpdateBtn;
    private TextView versionTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_me, container, false);
        exitBtn = (Button)rootView.findViewById(R.id.exit_btn);
        checkUpdateBtn = (Button)rootView.findViewById(R.id.checkupdate_btn);
        versionTV = (TextView) rootView.findViewById(R.id.version);
        exitBtn.setOnClickListener(this);
        checkUpdateBtn.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String versionName = Util.getVersionName(getActivity());
        if(!TextUtils.isEmpty(versionName)) {
            versionTV.setText("当前版本: " + versionName);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View view) {
        try {
            if(view.equals(exitBtn)) {
//                UserSingleton.getInstance().setLogin(false);
                UserSingleton.getInstance().setUser(null);
                Intent intent = new Intent(this.getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }else if(view.equals(checkUpdateBtn)) {
                //TODO
                AppUpdateManager appUpdateManager = new AppUpdateManager(getActivity());
                appUpdateManager.checkAppUpdate(new AppUpdateManager.CheckUpdateCallback() {
                    @Override
                    public void check(boolean isNew) {
                        if(!isNew) {
                            Toast.makeText(getActivity(), "已是最新版本", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

}
