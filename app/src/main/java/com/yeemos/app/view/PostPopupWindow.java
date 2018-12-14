package com.yeemos.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yeemos.app.R;

/**
 * Created by gigabud on 16-12-28.
 */

public class PostPopupWindow extends PopupWindow implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    protected RelativeLayout popLayout;
    protected View viewG;
    protected Activity context;
    private boolean mHasData = false;
    private boolean mIsGetingData;

    public PostPopupWindow(Activity context) {
        super(context);
        this.context = context;
        View morePop = LayoutInflater.from(context).inflate(getContentViewID(), null);
        setContentView(morePop);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        // setWindowLayoutMode(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT);
        initView();
        setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        ViewGroup.LayoutParams lp = getMoreMenuView().getLayoutParams();
        lp.height = displayMetrics.heightPixels * 4 / 5;

        // //设置SelectPicPopupWindow弹出窗体可点击
        // this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        // 实例化一个ColorDrawable颜色为半透明
        // ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        setAnimationStyle(R.style.PopupWindowAnimation);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.setBackgroundDrawable(new ColorDrawable(0x00000000));
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        morePop.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = getMoreMenuView().getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        morePop.setFocusable(true);
        morePop.setFocusableInTouchMode(true); // 设置view能够接听事件 标注2
        morePop.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK) {
                    if (this != null) {
                        dismiss();
                    }
                }
                return false;
            }
        });

    }

    protected int getContentViewID() {
        return R.layout.post_view_popupwindow;
    }

    protected void getData() {
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
        }
        setmHasData(true);
        setmIsGetingData(true);
    }

    public void loadMoreData() {

    }

    private void initView(){
        viewG = getContentView().findViewById(R.id.view_bg);
        getBtnClose().setOnClickListener(this);
        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);

    }


    @Override
    public void dismiss() {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(viewG, "alpha",0f);
        fadeAnim.setDuration(250);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
            public void onAnimationEnd(Animator animation) {
                PostPopupWindow.super.dismiss();
            }
        });
        fadeAnim.start();
    }
    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        viewShowLocation();
        getData();
    }
    protected void viewShowLocation() {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(viewG, "alpha",0.15f).setDuration(250);
        fadeAnim.setStartDelay(250);
        fadeAnim.start();
    }
    @Override
    public void update() {
        super.update();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                dismiss();
                break;
        }
    }

    @Override
    public void onRefresh() {
        getData();
    }

    private RelativeLayout getMoreMenuView() {
        if (popLayout == null && getContentView() != null) {
            popLayout = (RelativeLayout) getContentView().findViewById(R.id.pop_layout);
        }
        return popLayout;
    }

    private ImageButton getBtnClose() {
        return (ImageButton) getContentView().findViewById(R.id.btnClose);
    }

    protected TextView getTvPostView() {
        return (TextView) getContentView().findViewById(R.id.tvPostView);
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getContentView().findViewById(R.id.swipeRefreshLayout);
    }

    protected ListView getListView() {
        return (ListView) getContentView().findViewById(R.id.listView);
    }

    protected View getNoFillLayout() {
        return getContentView().findViewById(R.id.noFillLayout);
    }

    protected ImageView getNoFeelLog() {
        return (ImageView) getContentView().findViewById(R.id.noFeelLog);
    }

    protected TextView getNoFeelText() {
        return (TextView) getContentView().findViewById(R.id.noFeelText);
    }

    public TextView getTopButton() {
        return (TextView) getContentView().findViewById(R.id.topButton);
    }

    public boolean ismHasData() {
        return mHasData;
    }

    public void setmHasData(boolean mHasData) {
        this.mHasData = mHasData;
    }

    public boolean ismIsGetingData() {
        return mIsGetingData;
    }

    public void setmIsGetingData(boolean mIsGetingData) {
        this.mIsGetingData = mIsGetingData;
    }
}
