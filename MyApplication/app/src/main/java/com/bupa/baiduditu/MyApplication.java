package com.bupa.baiduditu;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

/**
 * 作者: l on 2017/2/4 21:28
 * 邮箱: xjs250@163.com
 * 描述:
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化sdk
        SDKInitializer.initialize(getApplicationContext());
    }
}
