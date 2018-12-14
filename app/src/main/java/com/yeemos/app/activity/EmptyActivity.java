package com.yeemos.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.fragment.EditPostFragment;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;

/**
 * 空的Activty，会替换你传进来的Fragment
 *
 * @author xiangwei.ma
 */
public class EmptyActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty);
        onNewIntent(getIntent());
    }

    @Override
    public void onNewIntent(Intent intent) {
        String fragmentName = intent.getStringExtra("FRAGMENT_NAME");
        if (!TextUtils.isEmpty(fragmentName)) {
            try {
                BaseFragment fragment = (BaseFragment) Fragment.instantiate(this,
                        fragmentName);
                Bundle b = intent.getExtras();
                fragment.setArguments(b);
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        Fragment currentFragment = getVisibleFragment();
//        if (currentFragment != null) {
//            ft.hide(currentFragment);
//        }
                ft.add(R.id.container, fragment, fragmentName);
                ft.addToBackStack(null);
                ft.commitAllowingStateLoss();
                BaseApplication.setCurFragment(fragment);
            }catch (Exception e){

            }
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        BaseFragment baseFragment = getVisibleFragment();
        if (baseFragment instanceof ShowPostViewPagerFragment) {
            ((ShowPostViewPagerFragment) baseFragment).dispatchTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    protected void onFromBackground() {
        super.onFromBackground();
        if (!MemberShipManager.getInstance().needToLogin()) { // 已经登录
            long lastCloseAppTime = Preferences.getInstacne()
                    .getLastCloseAppTime();
            if (lastCloseAppTime > 0 && System.currentTimeMillis() - lastCloseAppTime > 60 * 60 * 1000) {
                finish();
                DataManager.getInstance().setSelectObject(true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (getVisibleFragment() == null || getVisibleFragment().getActivity() == null) {
                finish();
            } else {
                if (getVisibleFragment() instanceof EditPostFragment) {
                    finish();
                } else {
                    getVisibleFragment().goBack();
                }
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}

