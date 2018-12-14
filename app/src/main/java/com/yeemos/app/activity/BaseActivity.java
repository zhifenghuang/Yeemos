package com.yeemos.app.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.main.GBSocial;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.gigabud.core.activity.BaseGigabudActivity;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.GBExecutionPool;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.bean.BroadcastMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.manager.CRabbitMQChat;
import com.yeemos.app.dialogs.TwoBtnsDialog;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.fragment.EditPostFragment;
import com.yeemos.app.fragment.FirstFragment;
import com.yeemos.app.interfaces.IDismissListener;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.services.UpgradeAppService;
import com.yeemos.app.services.UpgradeDialog;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.List;

import acplibrary.ACProgressBaseDialog;
import acplibrary.ACProgressConstant;
import acplibrary.ACProgressFlower;

/**
 * 应用首界面
 *
 * @author xiangwei.ma
 */
@SuppressLint({"InlinedApi", "NewApi"})
public abstract class BaseActivity extends BaseGigabudActivity {

    static public TwoBtnsDialog mErrorDialog = null;
    static public TwoBtnsDialog mTwoBtnsDialog = null;
    static public TwoBtnsDialog mOneBtnsDialog = null;

    private ACProgressBaseDialog mDlgLoading;

    private DisplayMetrics mDisplaymetrics;

    private boolean mIsFullScreen;

    public static final int PERMISSION_ACCESS_FINE_LOCATION_REQ_CODE = 0;
    public static final int PERMISSION_RECORD_AUDIO_REQ_CODE = 1;
    public static final int PERMISSION_READ_PHONE_STATE_REQ_CODE = 2;
    public static final int PERMISSION_READ_CONTACTS_REQ_CODE = 3;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE_CODE = 4;
    public static final int PERMISSION_CAMERA_CODE = 5;


    @Override
    protected void onStart() {
        super.onStart();
        RelativeLayout rlTourial = (RelativeLayout) findViewById(R.id.rlTourial);
        if (rlTourial != null) {
            int childCount = rlTourial.getChildCount();
            for (int i = 0; i < childCount; ++i) {
                rlTourial.getChildAt(i).setVisibility(View.GONE);
            }
            rlTourial.setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermission(0, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public Class<?> getSplashActivityClass() {
        return SplashActivity.class;
    }

    @Override
    protected void popupRateDialog() {

    }

    @Override
    protected Class<?> getUpgradeService() {
        return UpgradeAppService.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
//        }
        LanguagePreferences.setLanguage(this.getApplicationContext(),
                GBSocial.getGBSocialConfiguration().getLanguage(), GBSocial.getGBSocialConfiguration().getLanguageFlag());

    }

    public void requestPermission(int permissionReqCode, String... permissions) {
        ArrayList<String> uncheckPermissions = null;
        for (String permission : permissions) {
            if (!BaseUtils.isGrantPermission(this, permission)) {
                //进行权限请求
                if (uncheckPermissions == null) {
                    uncheckPermissions = new ArrayList<>();
                }
                uncheckPermissions.add(permission);
            }
        }
        if (uncheckPermissions != null && !uncheckPermissions.isEmpty()) {
            String[] array = new String[uncheckPermissions.size()];
            ActivityCompat.requestPermissions(this, uncheckPermissions.toArray(array), permissionReqCode);
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

    }


    public DisplayMetrics getDisplaymetrics() {
        if (mDisplaymetrics == null) {
            mDisplaymetrics = new DisplayMetrics();
            ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplaymetrics);
        }
        return mDisplaymetrics;
    }


    /**
     * 页面跳转，如果返回true,则基类已经处理，否则没有处理
     *
     * @param pagerClass
     * @param bundle
     * @return
     */
    public boolean gotoPager(Class<?> pagerClass, Bundle bundle) {

        if (Activity.class.isAssignableFrom(pagerClass)) {
            Intent intent = new Intent(this, pagerClass);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            startActivity(intent);
            return true;
        } else {
            if (BaseFragment.class.isAssignableFrom(pagerClass)) {
                String name = pagerClass.getName();
                BaseFragment newFragment = getFragmentByName(name);

                Constants.PHONE_FRAGMENT_UI_POSITION enCurFragmentPosition = newFragment
                        .getFragmentPhoneUIPostion();

                if (enCurFragmentPosition == Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION) // 使用EmptyActiviy
                {
                    Intent intent = new Intent(this, EmptyActivity.class);
                    if (bundle != null) {
                        intent.putExtras(bundle);
                    }
                    intent.putExtra("FRAGMENT_NAME", name);
                    startActivity(intent);
                    return true;
                } else if (enCurFragmentPosition == Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_TWO) {
                    Intent intent = new Intent(this, EmptyTwoActivity.class);
                    if (bundle != null) {
                        intent.putExtras(bundle);
                    }
                    intent.putExtra("FRAGMENT_NAME", name);
                    startActivity(intent);
                    return true;
                } else if (enCurFragmentPosition == Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_THREE) {
                    Intent intent = new Intent(this, EmptyThreeActivity.class);
                    if (bundle != null) {
                        intent.putExtras(bundle);
                    }
                    intent.putExtra("FRAGMENT_NAME", name);
                    startActivity(intent);
                    return true;
                }
            }
            return false;

        }
    }

    public void sendBroadcastMessage(int brocastType, final BasicUser user) {
        BroadcastMessage msg = new BroadcastMessage();
        msg.setBtype(brocastType);
        msg.setsUID(Long.parseLong(MemberShipManager.getInstance().getUserID()));
        msg.setrUID(Long.parseLong(user.getUserId()));
        IChat.getInstance().sendMsg(msg);

        if (brocastType == 3) {
            if (user.getFollowedStatus() == 1) {
                ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
                if (allFriends == null) {
                    allFriends = new ArrayList<>();
                }
                boolean isHad = false;
                for (BasicUser basicUser : allFriends) {
                    if (basicUser.getUserId().equals(user.getUserId())) {
                        isHad = true;
                        break;
                    }
                }
                if (!isHad) {
                    allFriends.add(user);
                    GBExecutionPool.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            ((CRabbitMQChat) CRabbitMQChat.getInstance()).getRabbitMQManager().getPeerOfflineMessage(Long.parseLong(user.getUserId()), null);
                        }
                    });
                }
            }
        } else {
            ArrayList<Long> arrayList = new ArrayList<Long>();
            arrayList.add(Long.parseLong(user.getUserId()));
            IChat.getInstance().deleteMsgsRecordByUserIds(arrayList,
                    Long.parseLong(MemberShipManager.getInstance().getUserID()),
                    MemberShipManager.getInstance().getToken());
            if (user.getFollowedStatus() == 1) {
                ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
                if (allFriends == null || allFriends.isEmpty()) {
                    return;
                }
                for (BasicUser basicUser : allFriends) {
                    if (basicUser.getUserId().equals(user.getUserId())) {
                        allFriends.remove(basicUser);
                        break;
                    }
                }
            }
        }
        int unreadMsgNum = IMsg.getUnReadMsgFriendNum(Long.parseLong(MemberShipManager.getInstance().getUserID()));
        BadgeUtil.setBadgeCount(this, (int) unreadMsgNum);
    }


    public void setScreenFull(boolean isFull) {
        if (mIsFullScreen == isFull) {
            return;
        }

        if (isFull) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(params);
            //          getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mIsFullScreen = true;
        } else {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(params);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            mIsFullScreen = false;
        }

    }

    /**
     * 通过设置全屏，设置状态栏透明
     */
    public void fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //5.x开始需要把颜色设置透明，否则导航栏会呈现系统默认的浅灰色
                Window window = getWindow();
                View decorView = window.getDecorView();
                //两个 flag 要结合使用，表示让应用的主体内容占用系统状态栏的空间
                int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
                decorView.setSystemUiVisibility(option);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(Color.TRANSPARENT);
                //导航栏颜色也可以正常设置
//                window.setNavigationBarColor(Color.TRANSPARENT);
            } else {
                Window window = getWindow();
                WindowManager.LayoutParams attributes = window.getAttributes();
                int flagTranslucentStatus = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
                int flagTranslucentNavigation = WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
                attributes.flags |= flagTranslucentStatus;
//                attributes.flags |= flagTranslucentNavigation;
                window.setAttributes(attributes);
            }
        }
    }

    private void hideSystemUI() {
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_FULLSCREEN
//                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        View decorView = getWindow().getDecorView();
        decorView.setFitsSystemWindows(false);
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }


    private void showSystemUI() {
//        getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        View decorView = getWindow().getDecorView();
//        decorView.setFitsSystemWindows(true);
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(getResources().getColor(R.color.color_45_223_227));


//        View decorView = getWindow().getDecorView();
//        int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
//        decorView.setSystemUiVisibility(option);
//        getWindow().setStatusBarColor(Color.TRANSPARENT); //也可以设置成灰色透明的，比较符合Material Design的风格
    }


    public boolean isFullScreen() {
        return mIsFullScreen;
    }

    /**
     * 根据name获取fragment
     *
     * @param name
     * @return
     */
    public BaseFragment getFragmentByName(String name) {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager()
                .findFragmentByTag(name);
        if (fragment == null) {
            fragment = (BaseFragment) Fragment.instantiate(this, name);
        }
        return fragment;
    }

    /**
     * 返回，如果stack中还有Fragment的话，则返回stack中的fragment，否则 finish当前的Activity
     */
    public void goBack() {

        try {
            getSupportFragmentManager().executePendingTransactions();
            int nSize = getSupportFragmentManager().getBackStackEntryCount();
            if (nSize > 1) {
                getSupportFragmentManager().popBackStack();
            } else {
                finish();
            }
        } catch (Exception e) {
            finish();
        }
    }


    public BaseFragment getVisibleFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        if (fragments == null) {
            return null;
        }
        for (Fragment fragment : fragments) {
            if (fragment != null && fragment.isVisible())
                return (BaseFragment) fragment;
        }
        return BaseApplication.getCurFragment();
    }


    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {

        super.onActivityResult(arg0, arg1, arg2);
        MemberShipManager.getInstance().onActivityResult(arg0, arg1, arg2);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    protected void onToBackground() {

        super.onToBackground();
        Preferences.getInstacne().setIsRunning(false);
        Preferences.getInstacne().setLastCloseAppTime();

    }

    @Override
    protected void onFromBackground() {
        super.onFromBackground();
        Preferences.getInstacne().setIsRunning(true);
        if ((System.currentTimeMillis() - Preferences.getInstacne()
                .getLastCloseAppTime()) / 1000 / 60 >= 30) {
            DataManager.getInstance().setHomePostListNeedRefresh(true);
        }
    }

    /**
     * 显示Loading 页面， listener可为空
     *
     * @param strTitle
     * @param listener
     * @param isCancelByUser:用户是否可点击屏幕，或者Back键关掉对话框
     */
    public void showLoadingDialog(String strTitle, final DialogInterface.OnCancelListener listener, boolean isCancelByUser) {
        if (mDlgLoading == null) {
            //mDlgLoading = new ProgressDialog(getActivity());
            mDlgLoading = new ACProgressFlower.Builder(this)
                    .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                    .themeColor(Color.WHITE)  // loading花瓣颜色
                    .text(strTitle)
                    .fadeColor(Color.DKGRAY).build(); // loading花瓣颜色
        }

        if (listener != null) {
            mDlgLoading.setOnCancelListener(listener);
        }

        if (isCancelByUser) {
            mDlgLoading.setCanceledOnTouchOutside(true);
            mDlgLoading.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    return false;
                }
            });
        } else {
            mDlgLoading.setCanceledOnTouchOutside(false);
            //防止用户点击Back键，关掉此对话框
            mDlgLoading.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK)
                        return true;
                    return false;
                }
            });
        }

        mDlgLoading.setMessage(strTitle);
        mDlgLoading.show();
    }


    /**
     * 关闭loading的页面
     */
    public void hideLoadingDialog() {

        if (mDlgLoading != null) {
            mDlgLoading.dismiss();
        }
    }

    /**
     * 处理错误码
     *
     * @param errorCode
     * @version 创建时间：2015-9-24 上午10:07:20
     */
    public void errorCodeDo(final String errorCode) {
        if (errorCode == null || errorCode.equals(PlatformErrorKeys.CODE_FOLLOW_BUT_BLOCKED)
                || errorCode.equals(PlatformErrorKeys.CODE_THIRDPARTY_ACCESSTOKEN_INVAID)) {
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                if ((errorCode.equals(PlatformErrorKeys.CODE_TOKEN_FORMAT_ERROR)
                        || errorCode.equals(PlatformErrorKeys.CODE_TOKEN_TIME_EXPIRATION)
                        || errorCode.equals(PlatformErrorKeys.CODE_ACCOUNT_BLOCKED)
                        || errorCode.equals(PlatformErrorKeys.CODE_TOKEN_SERVER_NOT_MATCH))
                        && Preferences.getInstacne().isAppLogged()) {
                    //App已经登录,才处理这些报错
                    //token过期,密码无效之类的要强制登出,弹出的框为背景点击无效
                    //Back点击不取消弹框
                    showErrorDialog(getErrorCodeContent(LanguagePreferences.getInstanse(
                            BaseActivity.this.getApplication()).getPreferenceStringValue(errorCode), errorCode),
                            null, ServerDataManager.getTextFromKey("pub_btn_ok"), false, false, new Handler(new Handler.Callback() {
                                public boolean handleMessage(Message msg) {
                                    switch (msg.what) {
                                        case Constants.DIALOG_RIGHY_BTN:
                                            logoutApp();
                                            break;
                                        default:
                                            break;
                                    }
                                    return false;
                                }
                            }));
                    mErrorDialog.setOnDismissListener(new IDismissListener() {
                        @Override
                        public void onDismiss() {
                            logoutApp();
                        }
                    });
                } else {
                    String newErrorCode = errorCode;
                    if (newErrorCode.equals(PlatformErrorKeys.CONNECTTION_OFFLINE) || newErrorCode.equals(PlatformErrorKeys.CONNECTTION_EXCEPTION)
                            || newErrorCode.equals(PlatformErrorKeys.CONNECTTION_ERROR) || newErrorCode.equals(PlatformErrorKeys.CONNECTTION_TIMEOUT)) {
                        showNoNetWorkError(newErrorCode);
                        return;
                    } else if (newErrorCode.equals(PlatformErrorKeys.USER_BE_BLOCKED_ERROR)) {
                        showErrorDialog(getErrorCodeContent(LanguagePreferences.getInstanse(
                                BaseActivity.this.getApplication()).getPreferenceStringValue(newErrorCode), newErrorCode),
                                null, ServerDataManager.getTextFromKey("pub_btn_ok"), new Handler(new Handler.Callback() {
                                    public boolean handleMessage(Message msg) {
                                        switch (msg.what) {
                                            case Constants.DIALOG_RIGHY_BTN:
                                                goBack();
                                                break;
                                            default:
                                                break;
                                        }
                                        return false;
                                    }
                                }));
                        return;
                    }
                    showErrorDialog(getErrorCodeContent(LanguagePreferences.getInstanse(BaseActivity.this.getApplication()).getPreferenceStringValue(newErrorCode), newErrorCode),
                            null, ServerDataManager.getTextFromKey("pub_btn_ok"), null);

//                    String content = getErrorCodeContent(LanguagePreferences.getInstanse(BaseActivity.this.getApplication()).getPreferenceStringValue(newErrorCode), newErrorCode);
//                    if (TextUtils.isEmpty(content)) {
//                        return;
//                    }
//                    Toast.makeText(BaseActivity.this, content, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean mIsErrorViewShow = false;

    public void showNoNetWorkError(final String errorCode) {
        String contentStr = getErrorCodeContent(LanguagePreferences.getInstanse(BaseActivity.this.getApplication()).getPreferenceStringValue(errorCode), errorCode);
        if (contentStr != null && !mIsErrorViewShow) {
            mIsErrorViewShow = true;
            ViewGroup view = (ViewGroup) getWindow().getDecorView();
            FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);
            final ViewGroup viewGroup = (ViewGroup) content.getChildAt(0);
            final View errorView = LayoutInflater.from(this).inflate(R.layout.error_fleeting_view, null);
            ((TextView) errorView.findViewById(R.id.tv)).setText(contentStr);
            viewGroup.addView(errorView);
            errorView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewGroup.removeView(errorView);
                    mIsErrorViewShow = false;
                }
            }, 2000);
        }
    }

    public void logoutApp() {
        BadgeUtil.setBadgeCount(this, 0);
        MemberShipManager.getInstance().logout(new GBSMemberShipManager.memberShipCallBack<Object>() {
            public void success(Object obj) {

            }

            public void timeOut() {

            }

            public void fail(String errorStr) {

            }

            public void cancel() {

            }
        });
        Preferences.getInstacne().setValues(
                HomeActivity.HAD_SET_FIREBASE_PUSH_TOKEN, false);
        Preferences.getInstacne().setValues(EditPostFragment.LAST_ANONYMOUS_TIME, 0l);
        IChat.getInstance().logOut();
        GBSDataManager.reset();
        Intent intent = new Intent(BaseActivity.this, EmptyActivity.class);
        intent.putExtra("FRAGMENT_NAME", FirstFragment.class.getName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }


    public DialogFragment showErrorDialog(final String strBody, final String strLeftBtn, final String strRightBtn, final boolean isBackgroundClick, final boolean isBackKeyEventValid, final Handler callback) {
        if (mErrorDialog == null) {
            mErrorDialog = (TwoBtnsDialog) getSupportFragmentManager()
                    .findFragmentByTag(TwoBtnsDialog.class.getName());
            if (mErrorDialog == null) {
                mErrorDialog = new TwoBtnsDialog();
            }
        }

        mErrorDialog.setTextInfo(null, strBody, strLeftBtn, strRightBtn, isBackgroundClick, isBackKeyEventValid, callback);
        if (mErrorDialog.isShow()) {
            mErrorDialog.setOnDismissListener(new IDismissListener() {
                public void onDismiss() {
                    showErrorDialog(strBody, strLeftBtn, strRightBtn, isBackgroundClick, isBackKeyEventValid, callback);
                }
            });
        } else {
            if (!mErrorDialog.isAdded()) {
                FragmentTransaction ft = getSupportFragmentManager()
                        .beginTransaction();
                //               ft.remove(mErrorDialog);
                //mErrorDialog.show(ft, "errorDialog");
                ft.add(mErrorDialog, "errorDialog"); // for fix
                ft.commitAllowingStateLoss();
            }
            mErrorDialog.setIsShow(true);
        }
        return mErrorDialog;
    }

    public DialogFragment showErrorDialog(final String strBody, final String strLeftBtn, final String strRightBtn, final Handler callback) {
        return showErrorDialog(strBody, strLeftBtn, strRightBtn, true, true, callback);
    }

    public DialogFragment showErrorDialog(final String strBody, final String strLeftBtn, final String strRightBtn, final boolean isBackgroundClick, final Handler callback) {
        return showErrorDialog(strBody, strLeftBtn, strRightBtn, isBackgroundClick, true, callback);
    }

    /**
     * 错误码提示内容
     *
     * @param des
     * @param errorCode
     * @return
     * @version 创建时间：2015-9-24 上午10:07:28
     */
    private String getErrorCodeContent(String des, String errorCode) {
        if (des == null) {
            if (PlatformErrorKeys.CONNECTTION_TIMEOUT.equals(errorCode)) {
                des = getString(R.string.GB2411059);
            } else
                des = getString(R.string.pub_unknownerror);
        }
        return des;
        //return des + "(" + errorCode + ")";
    }

    /**
     * 弹通用的对话框，如果strRightBtn为空时，则只会弹出有1个按钮的对话框，否则就2个按钮的对话框
     *
     * @param strTitle
     * @param strBody
     * @param strLeftBtn
     * @param strRightBtn
     * @param callback
     * @return
     */
    public DialogFragment showPublicDialog(final String strTitle,
                                           final String strBody, final String strLeftBtn,
                                           final String strRightBtn, final Handler callback) {
        return showPublicDialog(strTitle, strBody, strLeftBtn, strRightBtn, true, true, callback);
    }

    public DialogFragment showPublicDialogNoExclamation(final String strTitle,
                                                        final String strBody, final String strLeftBtn,
                                                        final String strRightBtn, final Handler callback) {
        return showPublicDialog(strTitle, strBody, strLeftBtn, strRightBtn, true, false, callback);
    }

    /**
     * 显示弹框
     *
     * @param strTitle
     * @param strBody
     * @param strLeftBtn
     * @param strRightBtn
     * @param isBackgroundClickable
     * @param callback
     * @return
     */
    public DialogFragment showPublicDialog(final String strTitle,
                                           final String strBody, final String strLeftBtn,
                                           final String strRightBtn, final boolean isBackgroundClickable, final boolean isNeedExclamation, final Handler callback) {
        if (strLeftBtn != null && strRightBtn != null
                && strLeftBtn.length() > 0 && strRightBtn.length() > 0) {

            if (mTwoBtnsDialog == null) {
                mTwoBtnsDialog = (TwoBtnsDialog) getSupportFragmentManager()
                        .findFragmentByTag(TwoBtnsDialog.class.getName());
                if (mTwoBtnsDialog == null) {
                    // mTwoBtnsDialog = new TwoBtnsDialog();
                    mTwoBtnsDialog = (TwoBtnsDialog) DialogFragment
                            .instantiate(this, TwoBtnsDialog.class.getName());
                }
            }
            // mTwoBtnsDialog.setStyle(FilterDialogFragment.STYLE_NO_TITLE, 0);
            mTwoBtnsDialog.setTextInfo(strTitle, strBody, strLeftBtn,
                    strRightBtn, isBackgroundClickable, isNeedExclamation, callback);
            if (mTwoBtnsDialog.isShow()) {
                mTwoBtnsDialog.setOnDismissListener(new IDismissListener() {
                    @Override
                    public void onDismiss() {
                        showPublicDialog(strTitle, strBody, strLeftBtn,
                                strRightBtn, isBackgroundClickable, isNeedExclamation, callback);
                    }
                });
            } else {

                if (!mTwoBtnsDialog.isAdded()) {
                    FragmentTransaction ft = getSupportFragmentManager()
                            .beginTransaction();
                    ft.remove(mTwoBtnsDialog);
                    ft.add(mTwoBtnsDialog, TwoBtnsDialog.class.getName()); // for
                    ft.commitAllowingStateLoss();
                }
                mTwoBtnsDialog.setIsShow(true);

                // mTwoBtnsDialog.show(getSupportFragmentManager(),
                // TwoBtnsDialog.class.getName());
            }

            return mTwoBtnsDialog;
        } else {

            if (mOneBtnsDialog == null) {
                mOneBtnsDialog = (TwoBtnsDialog) getSupportFragmentManager()
                        .findFragmentByTag(TwoBtnsDialog.class.getName());
                if (mOneBtnsDialog == null) {
                    mOneBtnsDialog = new TwoBtnsDialog();
                }
            }

            if (mTwoBtnsDialog != null && mTwoBtnsDialog.isShow()) {
                mTwoBtnsDialog.setOnDismissListener(new IDismissListener() {
                    @Override
                    public void onDismiss() {
                        showPublicDialog(strTitle, strBody, strLeftBtn,
                                strRightBtn, isBackgroundClickable, isNeedExclamation, callback);
                    }
                });

            } else {
                mOneBtnsDialog.setTextInfo(strTitle, strBody, strLeftBtn,
                        strRightBtn, isBackgroundClickable, isNeedExclamation, callback);
                if (!mOneBtnsDialog.isAdded()) {
                    FragmentTransaction ft = getSupportFragmentManager()
                            .beginTransaction();
                    ft.remove(mOneBtnsDialog);
                    ft.add(mOneBtnsDialog, TwoBtnsDialog.class.getName()); // for
                    ft.commitAllowingStateLoss();
                }

                mOneBtnsDialog.setIsShow(true);
            }
            return mOneBtnsDialog;
        }

    }

    @Override
    protected boolean extraCondition() {
        boolean extraCondition = Preferences.getInstacne().getValues(UpgradeDialog.UPGRADE_DIALOG_TO_BACKGROUND, false);
        Preferences.getInstacne().setValues(UpgradeDialog.UPGRADE_DIALOG_TO_BACKGROUND, false);
        return extraCondition;
    }
}
