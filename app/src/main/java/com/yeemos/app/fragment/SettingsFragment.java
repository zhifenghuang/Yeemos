package com.yeemos.app.fragment;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;

import com.bumptech.glide.Glide;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.preferences.GBSPreferences;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.EmptyActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomToggleImageButton;

import rx.Subscriber;

/**
 * Created by gigabud on 15-12-29.
 */
public class SettingsFragment extends BaseFragment implements View.OnClickListener {

    private Dialog dialog;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.tvLogout).setOnClickListener(this);
        view.findViewById(R.id.tvEditProfile).setOnClickListener(this);
        view.findViewById(R.id.tvChangePsw).setOnClickListener(this);
        view.findViewById(R.id.tvLinkedAccount).setOnClickListener(this);
        view.findViewById(R.id.tvNotificationSetting).setOnClickListener(this);
        view.findViewById(R.id.tvBlackList).setOnClickListener(this);
        view.findViewById(R.id.tvAboutUs).setOnClickListener(this);
        view.findViewById(R.id.tvContactUs).setOnClickListener(this);
        view.findViewById(R.id.tvTermsOfService).setOnClickListener(this);
        view.findViewById(R.id.tvPrivacyPolicy).setOnClickListener(this);
        view.findViewById(R.id.tvCommunity).setOnClickListener(this);
        view.findViewById(R.id.tvHelp).setOnClickListener(this);
        view.findViewById(R.id.tvClearCache).setOnClickListener(this);

        getBtnNotificationSound().setKey(GBSConstants.APP_RECIVENOTIFICATION_SOUND, GBSPreferences.getInstacne().getSoundStateNum());
        getBtnSaveOriginalPhoto().setKey(GBSConstants.APP_RECIVENOTIFICATION_SAVE_ORIGINAL_PHONE, GBSPreferences.getInstacne().getSaveOriginalStateNum());
        getBtnPrivateAccount().setKey(GBSConstants.APP_PRIVATEACCOUNT, GBSPreferences.getInstacne().getPrivateAccountStateNum());
        getBtnPrivateFollowList().setKey(GBSConstants.APP_RECIVENOTIFICATION_PRIVATEFOLLOW, GBSPreferences.getInstacne().getPrivateFollowListStateNum());
    }

    private CustomToggleImageButton getBtnNotificationSound() {
        return (CustomToggleImageButton) getView().findViewById(R.id.btnNotificationSound);
    }

    private CustomToggleImageButton getBtnPrivateAccount() {
        return (CustomToggleImageButton) getView().findViewById(R.id.btnPrivateAccount);
    }

    private CustomToggleImageButton getBtnPrivateFollowList() {
        return (CustomToggleImageButton) getView().findViewById(R.id.btnPrivateFollowList);
    }

    private CustomToggleImageButton getBtnSaveOriginalPhoto() {
        return (CustomToggleImageButton) getView().findViewById(R.id.btnSaveOriginalPhoto);
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvSetting, "sttngs_ttl_settings");
        setOnlineText(R.id.tvAccount, "sttngs_txt_account");
        setOnlineText(R.id.tvEditProfile, "sttngs_btn_editprofile");
        setOnlineText(R.id.tvChangePsw, "sttngs_btn_changepassword");
        setOnlineText(R.id.tvLinkedAccount, "sttngs_btn_linkedaccounts");
        setOnlineText(R.id.tvNotificationSetting, "sttngs_btn_notificationset");
        setOnlineText(R.id.tvBlackList, "sttngs_btn_blacklist");
        setOnlineText(R.id.tvFunction, "sttngs_txt_function");
        setOnlineText(R.id.tvNotificationSound, "sttngs_btn_notificationsound");
        setOnlineText(R.id.tvPrivateAccount, "sttngs_btn_privateaccount");
        setOnlineText(R.id.tvPrivateAccountDetail, "sttngs_txt_privateaccountexplain");
        setOnlineText(R.id.tvPrivateFollowList, "sttngs_btn_privatefollowlist");
        setOnlineText(R.id.tvPrivateFollowListDetail, "sttngs_txt_privatefollowlistexplain");
        setOnlineText(R.id.tvSaveOriginalPhoto, "sttngs_btn_saveoriginal");
        setOnlineText(R.id.tvYeemos, "sttngs_txt_yeemos");
        setOnlineText(R.id.tvAboutUs, "sttngs_btn_aboutus");
        setOnlineText(R.id.tvContactUs, "sttngs_btn_contactus");
        setOnlineText(R.id.tvHelp, "sttngs_btn_help");
        setOnlineText(R.id.tvPrivacyPolicy, "sttngs_btn_privacypolicy");
        setOnlineText(R.id.tvTermsOfService, "sttngs_btn_terms");
        setOnlineText(R.id.tvCommunity, "sgn_up_txt_communityguidelines");
        setOnlineText(R.id.tvClearCache, "sttngs_btn_clearcache");
        setOnlineText(R.id.tvLogout, "sttngs_btn_logout");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.tvLogout:
                showPublicDialog(null,
                        ServerDataManager.getTextFromKey("pblc_txt_areyousure"),
                        ServerDataManager.getTextFromKey("pblc_btn_cancel"),
                        ServerDataManager.getTextFromKey("pub_btn_ok"),
                        new Handler(new Handler.Callback() {
                            public boolean handleMessage(Message msg) {
                                if (msg.what == Constants.DIALOG_RIGHY_BTN) {
                                    BadgeUtil.setBadgeCount(getActivity(), 0);
                                    MemberShipManager.getInstance().logout(new GBSMemberShipManager.memberShipCallBack<Object>() {
                                        public void success(Object obj) {
                                            toInFramgent();
                                        }

                                        public void timeOut() {
                                            toInFramgent();
                                        }

                                        public void fail(String errorStr) {
                                            toInFramgent();
                                        }

                                        public void cancel() {

                                        }
                                    });

                                }
                                return false;
                            }
                        }));
                break;
            case R.id.tvEditProfile:
                gotoPager(EditProfileFragment.class, null);
                break;
            case R.id.tvChangePsw:
                gotoPager(ChangePasswordFragment.class, null);
                break;
            case R.id.tvLinkedAccount:
                gotoPager(LinkedAccountFragment.class, null);
                break;
            case R.id.tvNotificationSetting:
                gotoPager(NotificationSettingFragment.class, null);
                break;
            case R.id.tvBlackList:
                gotoPager(BlackListFragment.class, null);
                break;
            case R.id.tvAboutUs:
                gotoPager(AboutFragment.class, null);
                break;
            case R.id.tvContactUs:
                Uri uri = Uri.parse("mailto:" + ServerDataManager.getTextFromKey("abtus_txt_supportemail"));
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(it);
                break;
            case R.id.tvTermsOfService:
                Bundle b = new Bundle();
                b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_TERMS_OF_SERVICE);
                gotoPager(TermsOrPrivacyFragment.class, b);
                break;
            case R.id.tvPrivacyPolicy:
                b = new Bundle();
                b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_PRIVACY_POLICY);
                gotoPager(TermsOrPrivacyFragment.class, b);
                break;
            case R.id.tvCommunity:
                b = new Bundle();
                b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_COMMUNITY);
                gotoPager(TermsOrPrivacyFragment.class, b);
                break;
            case R.id.tvHelp:
                gotoPager(HelpFragment.class, null);
                break;
            case R.id.tvClearCache:
                String content = ServerDataManager.getTextFromKey("sttngs_txt_clearcachetext");
                String okay = ServerDataManager.getTextFromKey("pub_btn_ok");
                String cancel = ServerDataManager.getTextFromKey("pblc_btn_cancel");
                showPublicDialog(null, content, cancel, okay, clearCacheDialog);
                break;
        }
    }

    private Handler clearCacheDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }

                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    showLoadingDialog(null, null, true);
                    clearCache();
                    break;
            }
        }
    };

    private void clearCache() {
        Glide.get(getActivity()).clearMemory();
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Glide.get(getActivity()).clearDiskCache();
                    Utils.deleteCache(getActivity());
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                            String content = ServerDataManager.getTextFromKey("sttngs_txt_clearcachefinish");
                            String okay = ServerDataManager.getTextFromKey("pub_btn_ok");
                            showPublicDialog(null, content, okay, null, afterDeleteCache);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                        }
                    });
                }
            }
        });
    }

    private Handler afterDeleteCache = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    goBack();
                    break;
            }
        }
    };

    private void toInFramgent() {

        ((BaseActivity) getActivity()).logoutApp();
//        //      getActivity().finish();
//        Preferences.getInstacne().setValues(
//                HomeActivity.HAD_SET_PUSH_TOKEN, false);
//        Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, "");
//        Preferences.getInstacne().setValues(EditPostFragment.LAST_ANONYMOUS_TIME, 0l);
//        //      MemberShipManager.getInstance().logout(null);
//        IChat.getInstance().logOut();
//        MemberShipManager.getInstance().logout(new GBSMemberShipManager.memberShipCallBack<Object>() {
//        });
//        GBSDataManager.reset();
//        if (getActivity() == null) {
//            return;
//        }
//        Intent intent = new Intent(getActivity(), EmptyActivity.class);
//        intent.putExtra("FRAGMENT_NAME", FirstFragment.class.getName());
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//
//        NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
//        notificationManager.cancelAll();
    }
}
