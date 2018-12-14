package com.yeemos.app.services;

import android.content.Intent;
import android.util.Log;

import com.gigabud.core.upgrade.BaseUpgradeDialog;
import com.gigabud.core.upgrade.BaseUpgradeService;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.utils.Preferences;

import java.util.Locale;


public class UpgradeAppService extends BaseUpgradeService {

    @Override
    protected String getAppConfigURL() {
        return Preferences.getInstacne().getAppConfigURL();

    }


    @Override
    protected String getUpgradeURL() {
        return Preferences.getInstacne().getUpgradeURL();
    }


    protected String getI18NURL() {
        return Preferences.getInstacne().getI18NURL();
    }

    @Override
    protected String getAppId() {
        return "yeemos";
    }

    @Override
    protected String getDeviceLang() {
        Locale systemLocale = Locale.getDefault();
        if (systemLocale.equals(Locale.SIMPLIFIED_CHINESE)
                || systemLocale.equals(Locale.CHINA)
                || systemLocale.equals(Locale.CHINESE)
                || systemLocale.getCountry().contains("CN")) {
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
    protected String getLanguageFlag() {
        if (getDeviceLang().startsWith("zh_CN")) {
            return "_zh_CN";
        } else if (getDeviceLang().startsWith("zh_TW")) {
            return "_zh_TW";
        } else if (getDeviceLang().startsWith("ja_JP")) {
            return "_ja_JP";
        } else if (getDeviceLang().startsWith("ko_KR")) {
            return "_ko_KR";
        }
        return "_en";
    }

    @Override
    protected Class<?> getDialogClass() {
        return UpgradeDialog.class;
    }

    @Override
    protected long getUpdateStringLastTime() {
        return 1544091567970l;
    }

    @Override
    public boolean isMustUpgrade() {
        return false;
    }

    @Override
    public void showDialog(int type, String message, final String newPackageName, String btnOK, String btnCancel, String title) {
        if (type == 0) {
            btnOK = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pub_btn_nor_updatenow");
            btnCancel = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pblc_btn_cancel");
            title = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pub_txt_newversiontitle");
            message = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pub_txt_newversioncontentandroid");
        } else {
            btnOK = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pub_btn_nor_updatenow");
            btnCancel = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pblc_btn_cancel");
            title = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pub_txt_newversiontitle");
            message = LanguagePreferences.getInstanse(getApplicationContext())
                    .getPreferenceStringValue("pub_txt_whatsnew");
        }
        Intent intent = new Intent(this, getDialogClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BaseUpgradeDialog.DIALOG_TYPE, type);
        intent.putExtra(BaseUpgradeDialog.DIALOG_MESSAGE, message);
        intent.putExtra(BaseUpgradeDialog.DIALOG_NEWPACKAGENAME, newPackageName);
        intent.putExtra(BaseUpgradeDialog.DIALOG_BTNOK, btnOK);
        intent.putExtra(BaseUpgradeDialog.DIALOG_BTNCANCEL, btnCancel);
        intent.putExtra(BaseUpgradeDialog.DIALOG_TITLE, title);
        startActivity(intent);
        stopSelf();

    }
}

