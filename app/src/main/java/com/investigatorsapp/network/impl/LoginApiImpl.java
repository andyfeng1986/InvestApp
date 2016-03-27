package com.investigatorsapp.network.impl;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.GsonRequest;
import com.investigatorsapp.common.VolleySingleton;
import com.investigatorsapp.model.User;
import com.investigatorsapp.network.LoginApiInterface;
import com.investigatorsapp.utils.UrlWrapper;

/**
 * Created by fenglei on 15/12/21.
 */
public class LoginApiImpl implements LoginApiInterface {

    @Override
    public void login(String userName, String pwd, Response.Listener<User> listener,
                      Response.ErrorListener errorListener) {
        String url = UrlWrapper.getLoginUrl();
        GsonRequest<User> gsonRequest = new GsonRequest<User>(Request.Method.POST, url, User.class,
                UrlWrapper.getLoginParams(userName, pwd), listener, errorListener);
        VolleySingleton.getInstance().addToRequestQueue(gsonRequest);
    }

}
