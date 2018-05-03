package com.xoteev.githubtest;

import android.app.Application;

/*
 * Класс глобальных данных
 * */

public class GlobalData extends Application {
    private String userName;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}