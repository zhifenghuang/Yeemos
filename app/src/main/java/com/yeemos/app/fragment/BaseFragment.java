package com.yeemos.app.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.interfaces.DataChangeListener;
import com.yeemos.app.interfaces.IUpdateUI;
import com.yeemos.app.manager.DataChangeManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.manager.YemmosDataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;

import acplibrary.ACProgressBaseDialog;
import acplibrary.ACProgressConstant;
import acplibrary.ACProgressFlower;

/**
 * Fragment基类提供公共的页面跳转方面，公共弹窗等方法
 *
 * @author xiangwei.ma
 */
public abstract class BaseFragment extends Fragment implements IUpdateUI, DataChangeListener {

    // 标示是否第一次执行onStart页面
    private boolean mIsFirstOnStart = true;

    private boolean mIsContentViewPrepared = false;
    private boolean mIsFragmentVisible = false;
    private DisplayMetrics mDisplaymetrics;

    //  private Intent intentBroadCast = null;

    private ACProgressBaseDialog mDlgLoading;

    protected abstract int getLayoutId();

    /**
     * 得到当前Fragment的位置 默认返回 PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_NONE;
     * PHONE_FRAGMENT_UI_RIGHT_POSITION(0), //替换前一个Fragment
     * PHONE_FRAGMENT_UI_ALONE_POSITION(1); //单独一个页面 使用EmptyActivity
     *
     * @return
     */
    public abstract Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion();

    protected View mBaseView;

    /**
     * 构造函数，不能使用带有参数的构造函数，因为系统自动回收后，会调用没有参数的构造函数
     */
    public BaseFragment() {

        super();
    }


    /**
     * Log专用
     *
     * @return
     */
    protected void Logs(String str) {
        Log.v(this.getClass().getName(), str);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        GBSMemberShipManager.setClassName(MemberShipManager.class.getName());
        GBSDataManager.setClassName(YemmosDataManager.class.getName());
        DataChangeManager.getInstance().addDataChangeListener(this);
    }

    public void toPageTop() {

    }

    public void showKeyBoard() {
        //Log.i("BaseFragment", "hideKeyBoard");

    }

    public void setViewPadding(boolean isFull) {
        if (getView() == null) {
            return;
        }
        int top = 0;
        if (isFull) {
            top = Utils.getStatusBarHeight(getActivity());
        }
        getView().setPadding(0, top, 0, 0);
        getView().requestLayout();
    }


    public void hideKeyBoard() {
        InputMethodManager in = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (getActivity() != null && getActivity().getCurrentFocus() != null) {
            in.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }

        //Log.i("BaseFragment", "hideKeyBoard");
    }

    public Handler handler = new Handler(Looper.getMainLooper(),
            new Handler.Callback() {

                @Override
                public boolean handleMessage(Message msg) {
                    refreshUIview(UI_SHOW_TYPE.GetObject(msg.what));
                    return true;
                }
            });

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!isHidden()) {
            if (BaseApplication.getCurFragment() == null || !BaseApplication.getCurFragment().getClass().getName().equals(getClass().getName())) {
                BaseApplication.setCurFragment(this);
            }
        } else {
            View rlTourial = getActivity().findViewById(R.id.rlTourial);
            if (rlTourial != null) {
                rlTourial.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 通知包含此Fragment的Acitivy，是否支持物理键盘返回键，如果支持，用户点击返回按键，则返回。
     *
     * @return
     */
    public boolean isSupportPhysicBackKey() {
        return true;
    }


    public void closeKeyBoard() {
        if (getContext() != null) {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0); //强制隐藏键盘
        }
    }

    public void onEditKeyListener(EditText et) {
        et.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyEvent.KEYCODE_DPAD_UP:
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            return true;
                    }
                }
                return false;
            }
        });
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //setIntentBroadCast(null);
        mIsFirstOnStart = true;
    }


    public int[] getInScreen(View v) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        return location;
    }

    /**
     * 跳转到新的界面
     *
     * @param pagerClass
     * @param bundle
     */
    public void gotoPager(final Class<?> pagerClass, final Bundle bundle) {

        if (getActivity() instanceof BaseActivity) {

            ((BaseActivity) getActivity()).gotoPager(pagerClass, bundle);
        }
    }

    public void sendBroadcastMessage(int brocastType, BasicUser user) {
        if (getActivity() != null) {
            ((BaseActivity) getActivity()).sendBroadcastMessage(brocastType, user);
        }
    }

    /**
     * 返回，如果stack中还有Fragment的话，则返回stack中的fragment，否则 finish当前的Activity
     */
    public void goBack() {
        closeKeyBoard();
        if (getActivity() != null) {
            ((BaseActivity) getActivity()).goBack();
        }
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
    public void showPublicDialog(final String strTitle,
                                 final String strBody, final String strLeftBtn,
                                 final String strRightBtn, final Handler callback) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showPublicDialog(strTitle, strBody, strLeftBtn, strRightBtn, callback);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        //registerBroadcastReceiver();
        initFilterForBroadcast();
        mBaseView = inflater.inflate(getLayoutId(), null);

        mIsContentViewPrepared = true;

        return mBaseView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

    }

    protected View getBaseView() {
        return mBaseView;
    }

    protected abstract void initFilterForBroadcast();

    @Override
    public void onDestroyView() {
        mIsContentViewPrepared = false;
        super.onDestroyView();
    }


    @Override
    public void onStop() {
        mIsFirstOnStart = true;
        super.onStop();

    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsContentViewPrepared && mIsFragmentVisible) {
            onLazyLoad();
        }
        if (mIsFirstOnStart) {
            updateUIText();
            mIsFirstOnStart = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (BaseApplication.getCurFragment() == null || !BaseApplication.getCurFragment().getClass().getName().equals(getClass().getName())) {
            BaseApplication.setCurFragment(this);
        }
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    /**
     * 视图出现,只针对ViewPager中的fragment有效
     *
     * @author Damon
     * @version 创建时间：2015-10-15  上午11:55:03
     */
    public void onViewDidAppeared() {

    }

    /**
     * 懒加载,如果初始化后视图出现就会调用,只针对ViewPager中的fragment有效
     *
     * @attention
     * @author Damon
     * @version 创建时间：2015-10-15  下午2:35:29
     */
    public void onLazyLoad() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        // TODO Auto-generated method stub
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisible = isVisibleToUser;
        if (isVisibleToUser && mIsContentViewPrepared) {
            onViewDidAppeared();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DataChangeManager.getInstance().removeDataChangeListener(this);
    }


    /**
     * 广播过滤
     *
     * @param intentFilter
     */
    protected void addReceiverAction(IntentFilter intentFilter) {

        intentFilter.addAction(Constants.BROADCAST_RECEIVE_PUSH_TYPE);
        intentFilter.addAction(Constants.BROADCAST_REFRESHUI_CATEGORY);
    }


    public void onKeyDown() {

    }

    /**
     * 设置线上文字
     *
     * @param WidgetID
     * @param textKey
     * @author Damon
     * @version 创建时间：2015-10-16  下午4:01:57
     */
    public void setOnlineText(int WidgetID, String textKey) {
        String text = ServerDataManager.getTextFromKey(textKey);
        TextView view = ((TextView) getView().findViewById(WidgetID));
        if (view == null) {
            return;
        }
        if (view instanceof EditText) {
            view.setHint(text);
        } else
            view.setText(text);
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
            mDlgLoading = new ACProgressFlower.Builder(getActivity())
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
     * 错误码处理
     *
     * @param errorCode
     * @version 创建时间：2015-9-22  下午4:43:59
     */
    public void errorCodeDo(String errorCode) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).errorCodeDo(errorCode);
        }
    }

    public void refreshFromNextFragment(Object obj) {

    }

    public void onBackKeyClick() {
    }

    public void onDataChange(int dataType, Object data, int oprateType) {   //dataType为0时,表示data为User，dataType为１时,表示data为Post

    }

    public DisplayMetrics getDisplaymetrics() {
        if (mDisplaymetrics == null) {
            mDisplaymetrics = new DisplayMetrics();
            ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplaymetrics);
        }
        return mDisplaymetrics;
    }

    public void loadMoreData() {

    }
}


