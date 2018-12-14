package com.yeemos.app.utils;

import android.content.SharedPreferences;

import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.preferences.GBSPreferences;
import com.gigabud.core.http.DownloadFileManager;
import com.gigabud.core.util.PreferencesWrapper;
import com.yeemos.app.BaseApplication;

import java.util.ArrayList;

/**
 * 数据存储类 使用sharePreferences来快速存储和取数据；登出时要清空此文件
 * @author Damon
 *
 */
public class Preferences extends GBSPreferences {

    static Preferences mConfig = null;
    protected SharedPreferences mSettings;
    public String FIRST_TIME_TOUSE_ANONYMOUS = "FIRST_TIME_TOUSE_ANONYMOUS";

    public static Preferences getInstacne() {
        if (mConfig == null || mConfig.mSettings == null) {
            // 如果文件存在
            mConfig = new Preferences();
        }
        return mConfig;
    }

    private Preferences() {
        super();
    }

    /**
     * 得到Membership URL
     *
     * @return
     */
    public String getServerURL() {

        String strURLBase = null;

        /**
         * 取服务器返回的url
         */

        PreferencesWrapper pref = PreferencesWrapper.getInstanse(BaseApplication.getAppContext());
        strURLBase = pref.getPreferenceStringValue("commonURL", strURLBase);  // “commonURL” 不能随意改此key
        if(strURLBase == null){

            if (Constants.DEBUG_MODE)
                strURLBase = Constants.DEBUG_MEMBERSHIP_URL;
            else
                strURLBase = Constants.PRODUCT_MEMBERSHIP_URL;

        }
        return strURLBase;
    }

    /**
     * 获取app配置信息的 URL
     *
     * @return
     */
    public String getAppConfigURL() {
        if (Constants.DEBUG_MODE)
            return Constants.DEBUG_APPCONFIG_URL;
        else
            return Constants.PRODUCT_APPCONFIG_URL;

    }

    /**
     * 获取app配置信息的 URL
     *
     * @return
     */
    public String getUpgradeURL() {
        if (Constants.DEBUG_MODE)
            return Constants.DEBUG_UPGRADE_URL;
        else
            return Constants.PRODUCT_UPGRADE_URL;
    }



    /**
     * 得到下载文件的路径
     *
     * @return
     */
    public String getDownloadFilePathByName(String fileName)
    {
        return DownloadFileManager.getInstance().getFilePath(BaseApplication.getAppContext(), fileName);
    }




    /**
     * 获取聊天部分（文件下载的URL）
     *
     * @return
     */
    public String getChatFileDownloadURLByName(String fileName)
    {
        if (Constants.DEBUG_MODE)
        {
            return Constants.DEBUG_FILE_DOWND_URL + fileName;
        }
        else
        {
            return Constants.PRODUCT_FILE_DOWND_URL + fileName;
        }
    }



    /**
     * 获取Post（文件下载的URL）
     *
     * @return
     */
    public String getPostFileDownloadURLByName(String fileName,String token)
    {
        if (Constants.DEBUG_MODE)
        {
            return String.format(Constants.DEBUG_DOWNLOAD_POST_FILE_URL,fileName,token);
        }
        else
        {
            return String.format(Constants.PRODUCT_DOWNLOAD_POST_FILE_URL,fileName,token);
        }
    }



    /**
     * 获取文件 URL
    *
            * @return
            */
    public String getFileHttpURL() {
        if (Constants.DEBUG_MODE)
            return Constants.DEBUG_FILE_HTTP_URL;
        else
            return Constants.PRODUCT_FILE_HTTP_URL;
    }



    /**
     * 获取app配置信息的 URL
     *
     * @return
     */
    public String getI18NURL() {
        String strURLBase = null;

        /**
         * 取服务器返回的url
         */
        PreferencesWrapper pref = PreferencesWrapper.getInstanse(BaseApplication.getAppContext());
        strURLBase = pref.getPreferenceStringValue("sourceManagerURL", strURLBase);
        if(strURLBase == null){
            if (Constants.DEBUG_MODE)
                strURLBase = Constants.DEBUG_i18n_URL;
            else
                strURLBase = Constants.PRODUCT_i18n_URL;
        }
        return String.format("%s/message/ws/tools/messages/i18nErrcode", strURLBase);
    }

    /**
     * 返回当前的主页  0：列表 1：日历 2：最爱页面
     * @return
     */

    public int getHomePageType()
    {
        // TODO 103
        return getValues("HOME_PAGE_TYPE", 0);
    }


    /**
     * 设置当前的主页  0：列表 1：日历 2：最爱页面
     * @return
     */

    public void setHomePageType(int nIndex)
    {
        setValues("HOME_PAGE_TYPE", nIndex);

    }

    /**
     * 设置是否第一次打开APP
     * @param bFlag
     */
    public void setIsFirstOpenApp(boolean bFlag)
    {
        setValues("IS_FIRST_OPEN_APP", bFlag);
    }

    /**
     * 设置离开发送邮件页面的时间
     * 2015-10-23 上午11:47:19
     * @return void
     */
    public void setLeaveSendEmailPageTime() {
        setValues("Leave_SendEmailPage_Time", System.currentTimeMillis());
    }

    public long getLeaveSendEmailPageTime() {
        return getValues("Leave_SendEmailPage_Time", (long)0);
    }

    /**
     * 是否第一次打开APP
     */
    public boolean getIsFirstOpenApp()
    {
        return getValues("IS_FIRST_OPEN_APP", true);
    }


    /**
     * App是否正处于打开状态
     */
    public boolean isRunning(){
        return  getValues(Constants.APP_IS_RUNNING,false);
    }
    public void setIsRunning(boolean isRunning){
        setValues(Constants.APP_IS_RUNNING, isRunning);
    }

    public boolean isFirstAnonymous() {
        return getValues(FIRST_TIME_TOUSE_ANONYMOUS, true);
    }

    public void setFirstAnonymous(boolean firstAnonymous) {
        setValues(FIRST_TIME_TOUSE_ANONYMOUS, firstAnonymous);
    }

    public void setLastSharedID(ArrayList<FriendGroup> groupList, ArrayList<Integer> userIdList) {
        String recordID = "";
        for (FriendGroup friendGroup : groupList) {
            recordID += friendGroup.getId() + ",";
        }
        for (Integer userId : userIdList) {
            recordID += userId + ",";
        }
        setValues(Constants.LAST_SELECT_GROUP, recordID);
    }

    public String getLastSharedID() {
        return getValues(Constants.LAST_SELECT_GROUP, "");
    }
}

