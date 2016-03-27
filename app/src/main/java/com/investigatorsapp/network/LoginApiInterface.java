package com.investigatorsapp.network;

import com.android.volley.Response;
import com.investigatorsapp.model.User;

/**
 * Created by fenglei on 15/12/21.
 */
public interface LoginApiInterface {
    public void login(String userName, String pwd, Response.Listener<User> listener, Response.ErrorListener errorListener);
}
