package com.yeemos.app.manager;

/**
 * Created by gigabud on 15-12-10.
 */

import android.content.Context;
import android.graphics.Bitmap.CompressFormat;

import com.gbsocial.main.GBSocialConfiguration;
import com.gbsocial.server.YeemosTask;
import com.gigabud.core.http.RequestBean;
import com.gigabud.core.util.DeviceUtil;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;

import java.util.Locale;

public class YemmosSocialConfiguration implements GBSocialConfiguration {
    @Override
    public Context getApplicationContext() {
        return BaseApplication.getAppContext();
    }

    @Override
    public String getToken() {
        return MemberShipManager.getInstance().getToken();
    }

    @Override
    public String getLanguage() {
        Locale systemLocale = Locale.getDefault();
        if (systemLocale.equals(Locale.SIMPLIFIED_CHINESE) || systemLocale.equals(Locale.CHINA)
                || systemLocale.equals(Locale.CHINESE) || systemLocale.getCountry().contains("CN")) {
            return "zh_CN";
        } else if (systemLocale.equals(Locale.TRADITIONAL_CHINESE) || systemLocale.equals(Locale.TAIWAN)
                || systemLocale.getCountry().contains("HK") || systemLocale.getCountry().contains("hk")) {
            return "zh_TW";
        } else if (systemLocale.equals(Locale.JAPAN) || systemLocale.equals(Locale.JAPANESE)) {
            return "ja_JP";
        } else if (systemLocale.equals(Locale.KOREA) || systemLocale.equals(Locale.KOREAN)) {
            return "ko_KR";
        }
        return "en_US";
    }

    @Override
    public String getLanguageFlag() {
        if (getLanguage().startsWith("zh_CN")) {
            return "_zh_CN";
        }else if(getLanguage().startsWith("zh-TW")){
            return "_zh_TW";
        }else if (getLanguage().startsWith("ja_JP")) {
            return "_ja_JP";
        } else if (getLanguage().startsWith("ko_KR")) {
            return "_ko_KR";
        }
        return "_en";
    }

    @Override
    public String getServerURL() {
        return Preferences.getInstacne().getServerURL();
    }

    @Override
    public String getAppID() {

        return "yeemos";
    }

    @Override
    public String getAppVersion() {

        return "10000";
    }

    @Override
    public String getDeviceType() {

        return "android";
    }

    @Override
    public CompressFormat getUploadImageCompressFormat() {

        return CompressFormat.JPEG;
    }

    @Override
    public int getUploadImageCompressQuality() {

        return 50;
    }

    @Override
    public String getDeviceUUID() {
        return DeviceUtil.getDeviceId(getApplicationContext());
    }

    @Override
    public int getRequestTimeOut() {
        return 20;
    }

    @Override
    public int getApiVer() {
        return 10000;
    }

    @Override
    public int getDefaultIconDrawble() {
        return R.drawable.default_avater;
    }

    @Override
    public int getDefaultCoverDrawble() {
        return 0;
    }

    @Override
    public int getDefaultPostImageDrawble() {
        return 0;
    }


    //网络请求任务
    public YeemosTask getDefaultUploadTask(RequestBean rqst) {
        return new YeemosTask(rqst);
    }
}

