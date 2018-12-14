package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.gbsocial.constants.GBSConstants;
import com.gbsocial.preferences.GBSPreferences;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.view.MenuRadioGroup;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-14.
 */
public class NotificationSettingFragment extends BaseFragment implements View.OnClickListener, MenuRadioGroup.MenuRadioGroupListener {

    private ArrayList<String> titlesFourOption = null;
    private ArrayList<String> titlesTwoOption1 = null;
    private ArrayList<String> titlesTwoOption2 = null;
//    private boolean isChangeCheck = false;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);

        getMenuRadioGroup(R.id.rgLike).setMenuRadioGroupListener(this);
        getMenuRadioGroup(R.id.rgComment).setMenuRadioGroupListener(this);
        getMenuRadioGroup(R.id.rgAcptFoloRsqt).setMenuRadioGroupListener(this);
        getMenuRadioGroup(R.id.rgFoloRqst).setMenuRadioGroupListener(this);
        getMenuRadioGroup(R.id.rgMsgNotification).setMenuRadioGroupListener(this);
        if(GBSPreferences.getInstacne().getPrivateAccountStateNum()==0){
            getMenuRadioGroup(R.id.rgFoloRqst).setVisibility(View.GONE);
            view.findViewById(R.id.llFoloRqst).setVisibility(View.GONE);
        }
        //    getSmartManager();
        view.post(new Runnable() {
            public void run() {
                int padding = 0;
                getMenuRadioGroup(R.id.rgLike).setRadioButtonTitles(getTitlesFourOptArr(),
                        getTitlesFourOptArr().size() == 0 ? 0 : getIndexByRecordValue(Preferences.getInstacne().getReciveNotificationOfLikeStateNum()), padding, padding);
                getMenuRadioGroup(R.id.rgComment).setRadioButtonTitles(getTitlesFourOptArr(),
                        getTitlesFourOptArr().size() == 0 ? 0 : getIndexByRecordValue(Preferences.getInstacne().getReciveNotificationOfCmtStateNum()), padding, padding);
                getMenuRadioGroup(R.id.rgAcptFoloRsqt).setRadioButtonTitles(getTitlesTwoOptArr1(),
                        getTitlesTwoOptArr1().size() == 0 ? 0 : Preferences.getInstacne().getReciveNotificationOfAcptFoloRqstStateNum(), padding, padding);
                getMenuRadioGroup(R.id.rgFoloRqst).setRadioButtonTitles(getTitlesTwoOptArr1(),
                        getTitlesTwoOptArr1().size() == 0 ? 0 : Preferences.getInstacne().getReciveNotificationOfFoloRqstStateNum(), padding, padding);
                getMenuRadioGroup(R.id.rgMsgNotification).setRadioButtonTitles(getTitlesTwoOptArr2(),
                        getTitlesTwoOptArr2().size() == 0 ? 0 : Preferences.getInstacne().getFriendsSendMsgStateNum(), padding, padding);
            }
        });
    }
    /**
     * 因为服务器那边　      //0 关闭通知  1所有的人      2我follow的人  3我的好友
     * radioGroup这边index //0 关闭通知  1我follow的人  2我的好友      3所有的人
     * 根据 Value 转成 Index
     * @param value
     * @return
     */
    private int getIndexByRecordValue(int value) {
        switch (value) {
            case 1:
                return 3;
            case 2:
                return 1;
            case 3:
                return 2;
            default:
                return 0;
        }
    }

    private ArrayList<String> getTitlesFourOptArr() {
        if (titlesFourOption == null) {
            titlesFourOption = new ArrayList<String>();
            titlesFourOption.add(ServerDataManager.getTextFromKey("ntfctn_btn_off"));
            titlesFourOption.add(ServerDataManager.getTextFromKey("ntfctn_btn_frompeopleifollow"));
            titlesFourOption.add(ServerDataManager.getTextFromKey("ntfctn_btn_fromfriend"));
            titlesFourOption.add(ServerDataManager.getTextFromKey("ntfctn_btn_fromeveryone"));
        }
        return titlesFourOption;
    }

    private ArrayList<String> getTitlesTwoOptArr1() {
        if (titlesTwoOption1 == null) {
            titlesTwoOption1 = new ArrayList<String>();
            titlesTwoOption1.add(ServerDataManager.getTextFromKey("ntfctn_btn_off"));
            titlesTwoOption1.add(ServerDataManager.getTextFromKey("ntfctn_btn_fromeveryone"));
        }
        return titlesTwoOption1;
    }

    private ArrayList<String> getTitlesTwoOptArr2() {
        if (titlesTwoOption2 == null) {
            titlesTwoOption2 = new ArrayList<String>();
            titlesTwoOption2.add(ServerDataManager.getTextFromKey("ntfctn_btn_off"));
            titlesTwoOption2.add(ServerDataManager.getTextFromKey("ntfctn_btn_fromfriend"));
        }
        return titlesTwoOption2;
    }

    private MenuRadioGroup getMenuRadioGroup(int id) {
        return (MenuRadioGroup) getView().findViewById(id);
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {

        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvTitle, "ntfctn_ttl_notifications");
        setOnlineText(R.id.tvLike, "ntfctn_txt_likes");
        setOnlineText(R.id.tvLikeDes, "ntfctn_txtonpg_johnlikedyourphoto");
        setOnlineText(R.id.tvComments, "ntfctn_txt_comment");
        setOnlineText(R.id.tvCommentsDes, "ntfctn_txtonpg_johncommented");
        setOnlineText(R.id.tvFoloRqst, "ntfctn_txt_followrequests");
        setOnlineText(R.id.tvFoloRqstDes, "ntfctn_txtonpg_userrequeststofollowyou");
        setOnlineText(R.id.tvAcptFoloRsqt, "ntfctn_txt_acceptedfollowrequests");
        setOnlineText(R.id.tvAcptFoloRsqtDes, "ntfctn_txtonpg_usernameacceptedyourfollowrequests");
        setOnlineText(R.id.tvMsgNotification, "ntfctn_txt_messagenotification");
        setOnlineText(R.id.tvMsgNotificationDes, "ntfctn_txtonpg_johnsentyoumessage");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_notification_setting;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    /**
     * 因为服务器那边　      //0 关闭通知  1所有的人      2我follow的人  3我的好友
     * radioGroup这边index //0 关闭通知  1我follow的人  2我的好友      3所有的人
     * 根据 Index 转成 Value
     * @param value
     * @return
     */
    public int settingValueToRadioIndex(int value) {
        switch (value) {
            case 1:
                return 2;
            case 2:
                return 3;
            case 3:
                return 1;
            default:
                return 0;
        }
    }

    @Override
    public void onClicked(MenuRadioGroup radioGroup, int index) {
        String key = null;
        switch (radioGroup.getId()) {
            case R.id.rgLike:
                key = GBSConstants.APP_RECIVENOTIFICATION_LIKE;
                index = settingValueToRadioIndex(index);
                break;
            case R.id.rgComment:
                key = GBSConstants.APP_RECIVENOTIFICATION_COMMENTS;
                index = settingValueToRadioIndex(index);
                break;
            case R.id.rgFoloRqst:
                key = GBSConstants.APP_RECIVENOTIFICATION_FOLLOWREQUESTS;
                break;
            case R.id.rgAcptFoloRsqt:
                key = GBSConstants.APP_RECIVENOTIFICATION_ACCPETEDFOLLOWREQUEST;
                break;
            case R.id.rgMsgNotification:
                key = GBSConstants.APP_RECIVENOTIFICATION_FRIENDSSENDMSG;
                break;
            default:
                break;
        }
        if (key != null) {
            //点击后,立即更改本地设置,请求在过后再统一请求
            Preferences.getInstacne().setValues(key, index);
        }

        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                updateUserSetting();
            }
        });
    }
    private synchronized void updateUserSetting() {
        DataManager.getInstance().updateUserSetting();
    }
}
