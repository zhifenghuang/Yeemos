package com.yeemos.app.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.gbsocial.BeansBase.PushMessageBean;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.main.GBSocial;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gigabud.core.activity.SplashGigabudActivity;
import com.gigabud.core.util.FileUtil;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.fragment.FirstFragment;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.manager.YemmosDataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;
import java.io.File;


/**
 * 应用首界面
 *
 * @author xiangwei.ma
 */
public class SplashActivity extends SplashGigabudActivity {

    private PushMessageBean mPushMessageBean;


    private static final String[] APP_NEED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    /**
     * 返回要加载的layout
     *
     * @return
     */
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    /**
     * 在初始化线程结束后(在主线程中)，去下一个activity前执行
     */
    protected void doBeforeGoNext() {

    }

    /**
     * 第一次安装后第一次启动时调用，该方法在异步线程中执行
     *
     * @return 出错时返回false
     */
    protected boolean onAppCreate() {
        return true;
    }

    /**
     * 覆盖安装后第一次启动时调用，该方法在异步线程中执行
     *
     * @return 出错时返回false
     */
    protected boolean onAppUpgrade(int oldVersion, int newVersion) {
        return false;
    }

    /**
     * 这个方法每次应用启动时都会在异步线程中调用，但在onAppCreate及onAppUpgrade之后
     */
    protected void doInThread() {
        initdatabase();
        //初始化数据库
        DatabaseFactory.getDBOper().initDBData();
    }

    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GBSMemberShipManager.setClassName(MemberShipManager.class.getName());
        GBSDataManager.setClassName(YemmosDataManager.class.getName());
        Preferences.getInstacne().setValues(Constants.TUTORIAL_SEARCH_FRIEND, true);
        LanguagePreferences.setLanguage(this.getApplicationContext(),
                GBSocial.getGBSocialConfiguration().getLanguage(), GBSocial.getGBSocialConfiguration().getLanguageFlag());

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.get("google.message_id") != null) {
            mPushMessageBean = new PushMessageBean();
            mPushMessageBean.setId(bundle.getString("id"));
            mPushMessageBean.setCuid(bundle.getString("cuid"));
            mPushMessageBean.setTuid(bundle.getString("tuid"));
            mPushMessageBean.setType(bundle.getString("type"));
            mPushMessageBean.setBadge(bundle.getString("badge"));
        }
    }


    @Override
    protected Intent getToNextIntent() {
        if (!MemberShipManager.getInstance().needToLogin()) { // 已经登录
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            if (mPushMessageBean != null) {
                intent.putExtra(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN, mPushMessageBean);
            }
            // intent
            return intent;
        } else {
            Preferences.getInstacne().setLastCloseAppTime();
            Intent intent = new Intent(SplashActivity.this, EmptyActivity.class);
            intent.putExtra("FRAGMENT_NAME", FirstFragment.class.getName());
            return intent;
        }
    }

    protected String[] getAppNeedPermissions() {
        return APP_NEED_PERMISSIONS;
    }

    @Override
    protected void errorOnCreate() {
    }

    @Override
    protected void errorOnUpgrade() {
    }


    /**
     * 初始化数据库
     *
     * @return
     */
    private void initdatabase() {
//		boolean temp = deleteDatabase(Constants.DBNAME);
//		LogUtils.i(" deleteDatabase ="+temp);
        int result = FileUtil.initDatabaseFile(getApplicationContext(), false,
                Constants.DB_NAME, R.raw.chat, 200000);
        if (result != 5) {
            //没出现异常
            // TODO  Del it (This test fucntion)
            FileUtil.databaseToSD(getApplicationContext(), Constants.DB_NAME, "original" + File.separator + getApplicationContext().getPackageName());
        } else {
            Log.e("SplashActivity", "DB init failed");
        }
    }
}
