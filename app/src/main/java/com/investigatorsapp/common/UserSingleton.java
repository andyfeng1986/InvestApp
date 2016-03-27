package com.investigatorsapp.common;

import com.investigatorsapp.model.User;

/**
 * Created by fenglei on 15/12/21.
 */
public class UserSingleton {

    private static final UserSingleton instance = new UserSingleton();

//    private boolean isLogin;

    private User user;

    private UserSingleton() {

    }

    public static UserSingleton getInstance() {
        return instance;
    }

//    public synchronized void setLogin(boolean isLogin) {
//        this.isLogin = isLogin;
//    }
//
//    public synchronized boolean isLogin() {
//        return isLogin;
//    }

    public synchronized void setUser(User user) {
        this.user = user;
    }

    public synchronized User getUser() {
        return user;
    }

}
