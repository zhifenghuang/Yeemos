package com.yeemos.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.R;

public class ViewDragLayout extends LinearLayout {

    private ViewDragHelper mDragHelper;
    private View mDragView1;
    private View mDragView2;
    public static boolean mIsMinLeft;
    private boolean mIsEnable;
    private int mViewDragLayout;

    public ViewDragLayout(Context context) {
        this(context, null);
    }

    public ViewDragLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDragHelper = ViewDragHelper.create(this, 1.0f,
                new DragHelperCallback());
        mIsMinLeft = false;
        mIsEnable = true;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ViewDragLayout);
            mViewDragLayout = array.getInteger(R.styleable.ViewDragLayout_drag_layout_view_type, 0);
            array.recycle();
        }
    }


    public void setDragEnable(boolean isEnable) {
        mIsEnable = isEnable;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView1 = findViewById(R.id.drag1);
        mDragView2 = findViewById(R.id.drag2);
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child == mDragView1 || child == mDragView2;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            if (changedView == mDragView1) {
                mDragView2.offsetLeftAndRight(dx);
            } else {
                mDragView1.offsetLeftAndRight(dx);
            }
            if (mDragView2.getVisibility() == View.GONE) {
                mDragView2.setVisibility(View.VISIBLE);
            }
            invalidate();
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            mDragHelper.captureChildView(mDragView1, pointerId);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (mViewDragLayout == 0) {
                DataManager.getInstance().setCurOtherUser((BasicUser) ViewDragLayout.this.getTag());
            }

            if (child == mDragView1) {
                int minLeft = -getPaddingLeft() - mDragView2.getMeasuredWidth();
                if (mViewDragLayout == 0) {
                    if (left <= minLeft) {
                        mIsMinLeft = true;
                    }
                    if (mIsMinLeft) {
                        return minLeft;
                    }
                }
                return Math.min(Math.max(minLeft, left), 0);
            } else {
                int minLeft = mDragView1.getMeasuredWidth() + getPaddingRight()
                        - mDragView2.getMeasuredWidth();
                int maxLeft = mDragView1.getMeasuredWidth() + getPaddingRight();
                return Math.min(Math.max(left, minLeft), maxLeft);
            }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            mDragHelper.smoothSlideViewTo(mDragView1, 0, 0);
            ViewCompat.postInvalidateOnAnimation(ViewDragLayout.this);
            mIsMinLeft = false;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (!mIsEnable) {
            return super.onInterceptTouchEvent(ev);
        }
        mDragHelper.shouldInterceptTouchEvent(ev);
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsEnable) {
            return super.onTouchEvent(event);
        }
        mDragHelper.processTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

}
