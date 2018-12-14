package com.yeemos.app;


import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;

import com.bumptech.glide.Glide;
import com.bumptech.glide.MemoryCategory;
import com.flurry.android.FlurryAgent;
import com.gbsocial.main.GBSocial;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tencent.bugly.crashreport.CrashReport;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.manager.YemmosSocialConfiguration;

public class BaseApplication extends Application {


    private static Context appContext = null;
    private static BaseFragment curFragment = null;

    public BaseApplication() {
        super();
    }

    public static void setAppContext(Context context) {
        appContext = context;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public static void setCurFragment(BaseFragment baseFragment) {
        curFragment = baseFragment;
    }

    public static BaseFragment getCurFragment() {
        return curFragment;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        BaseApplication.setAppContext(getApplicationContext());
        GBSocial.setGBSocialConfigurationClassName(YemmosSocialConfiguration.class.getName());
        MemberShipManager.getInstance();
        DataManager.getInstance();
        Glide.get(this).setMemoryCategory(MemoryCategory.HIGH); //动态设置内存缓存size
//        FlurryAgent.setLogEnabled(false);
//        FlurryAgent.init(this, "D56KMG9Q9DTFPJBCFCXC");
        CrashReport.initCrashReport(getApplicationContext(), "61d0f84108", false);



//        FirebaseOptions firebaseOptions = new FirebaseOptions.Builder()
//                .setDatabaseUrl("https://yeemos-148203.firebaseio.com")
//                .setApiKey("AIzaSyCANgVCdqzCsdXHdlXKVp5EfykvmGUJ1YU")
//                .setApplicationId("yeemos-148203").build();
//
//        FirebaseApp.initializeApp(getApplicationContext(),firebaseOptions,
//                "yeemos");
    }


}

