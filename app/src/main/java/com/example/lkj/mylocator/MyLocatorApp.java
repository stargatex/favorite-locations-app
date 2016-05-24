package com.example.lkj.mylocator;

import android.app.Application;

import com.firebase.client.Firebase;

/**
 * Created by LKJ on 5/18/2016.
 */
public class MyLocatorApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
