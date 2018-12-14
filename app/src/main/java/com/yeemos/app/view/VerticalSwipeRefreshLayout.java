package com.yeemos.app.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.yeemos.app.R;


public class VerticalSwipeRefreshLayout extends SwipeRefreshLayout {

    private int mTouchSlop;
    // 上一次触摸时的X坐标
    private float mPrevX;

    private boolean mIsCanRefrsh;

    private DirectionalViewPager mParentDirectionalViewPager;

    public VerticalSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 触发移动事件的最短距离，如果小于这个距离就不触发移动控件
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setColorSchemeResources(R.color.color_45_223_227, R.color.color_45_223_227, R.color.color_45_223_227,
                R.color.color_45_223_227);
    }

    public void setCanRefresh(boolean isCanRefrsh) {
        mIsCanRefrsh = isCanRefrsh;
    }

    public void setParentDirectionalViewPager(DirectionalViewPager parentDirectionalViewPager) {
        mParentDirectionalViewPager = parentDirectionalViewPager;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action=event.getAction();
        if (MotionEvent.ACTION_DOWN == action) {
            mIsCanRefrsh = true;
        }else if(MotionEvent.ACTION_UP == action){
            if(mParentDirectionalViewPager!=null){
                mParentDirectionalViewPager.setCanScroll(true);
            }
        }
        if (!mIsCanRefrsh) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - mPrevX);
                if (xDiff > mTouchSlop) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }

}
