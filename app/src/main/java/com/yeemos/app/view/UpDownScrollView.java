package com.yeemos.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by gigabud on 16-1-27.
 */
public class UpDownScrollView extends ViewGroup {
    private Scroller mScroller;
    private VelocityTracker mVelocityTracker;
    private int mCurrentScreen;
    private int mDefaultScreen = 0;
    private static final int TOUCH_STATE_REST = 0;
    private static final int TOUCH_STATE_SCROLLING = 1;
    private static final int SNAP_VELOCITY = 600;
    private int mTouchState = TOUCH_STATE_REST;
    private int mTouchSlop;
    private float mLastMotionY;
    private int mMaxHeight;
    private boolean mIsEnable;
    private boolean mIsPullRefresh;
    private OnScreenChangeListener mOnScreenChangeListener;

    public UpDownScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context);
        mCurrentScreen = mDefaultScreen;
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mIsEnable = true;
    }

    public UpDownScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOnScreenChangeListener(OnScreenChangeListener onScreenChangeListener) {
        mOnScreenChangeListener = onScreenChangeListener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childTop = 0;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            if ( childView.getVisibility() != View.GONE ) {
                int childHeight = childView.getMeasuredHeight();
                childView.layout(0, childTop, childView.getMeasuredHeight(),
                        childTop + childHeight);
                childTop += childHeight;
            }
        }
    }

    public void setUpDownEnable(boolean enable) {
        mIsEnable = enable;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMaxHeight = 0;
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if ( widthMode != MeasureSpec.EXACTLY ) {
            throw new IllegalStateException(
                    "ScrollLayout only canmCurScreen run at EXACTLY mode!");
        }

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if ( heightMode != MeasureSpec.EXACTLY ) {
            throw new IllegalStateException(
                    "ScrollLayout only can run at EXACTLY mode!");
        }
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
            if ( i < count - 1 )
                mMaxHeight += height;
        }
        scrollTo(0, mCurrentScreen * height);
    }

    public void snapToDestination() {
        int screenHeight = getHeight();
        int destScreen = (getScrollY() + screenHeight / 2) / screenHeight;
        snapToScreen(destScreen);
    }

    public void setIsPullRefresh(boolean isPullRefresh) {
        mIsPullRefresh = isPullRefresh;
    }

    /**
     * @param whichScreen
     */
    public void snapToScreen(int whichScreen) {
        if ( !mIsEnable ) {
            return;
        }
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        if ( getScrollY() != (whichScreen * getHeight()) ) {
            int delta = whichScreen * getHeight() - getScrollY();
            mScroller.startScroll(0, getScrollY(), 0, delta,
                    Math.abs(delta) / 2);
            mCurrentScreen = whichScreen;
            invalidate();
            if ( mOnScreenChangeListener != null ) {
                mOnScreenChangeListener.onScreenChange(whichScreen);
            }
        }
    }

    public int getCurScreen() {
        return mCurrentScreen;
    }

    public void setToScreen(int whichScreen) {
        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        mCurrentScreen = whichScreen;
        scrollTo(0, whichScreen * getHeight());
        if ( mOnScreenChangeListener != null ) {
            mOnScreenChangeListener.onScreenChange(whichScreen);
        }
    }

    @Override
    public void computeScroll() {
        if ( mScroller.computeScrollOffset() ) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ( !mIsEnable ) {
            return super.onTouchEvent(event);
        }
        if ( mVelocityTracker == null ) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int action = event.getAction();
        float y = event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if ( !mScroller.isFinished() ) {
                    mScroller.abortAnimation();
                }
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                int scrollY = getScrollY();
                int destY = (int) (mLastMotionY - y + scrollY);
                mLastMotionY = y;
                if ( destY >= mMaxHeight ) {
                    if ( scrollY == mMaxHeight ) {
                        break;
                    }
                    destY = mMaxHeight;
                } else if ( destY <= 0 ) {
                    if ( scrollY == 0 ) {
                        break;
                    }
                    destY = 0;
                }
                scrollTo(0, destY);
                break;
            case MotionEvent.ACTION_UP:
                VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityY = (int) velocityTracker.getYVelocity();
                if ( velocityY > SNAP_VELOCITY && getCurScreen() > 0 ) {
                    snapToScreen(getCurScreen() - 1);
                } else if ( velocityY < -SNAP_VELOCITY
                        && getCurScreen() < getChildCount() - 1 ) {
                    snapToScreen(getCurScreen() + 1);
                } else {
                    snapToDestination();
                }
                if ( mVelocityTracker != null ) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mTouchState = TOUCH_STATE_REST;
                break;
            case MotionEvent.ACTION_CANCEL:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        if ( mCurrentScreen == 0) {
            getChildAt(0).onTouchEvent(event);
        }
        return super.onTouchEvent(event);
        //       getChildAt(0).onTouchEvent(event);
        //       return true;
        //       }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if ( !mIsEnable ) {
            return super.onInterceptTouchEvent(ev);
        }
        int action = ev.getAction();
        if ( (action == MotionEvent.ACTION_MOVE)
                && (mTouchState != TOUCH_STATE_REST) ) {
            return true;
        }
        float y = ev.getY();
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                int yDiff = (int) Math.abs(mLastMotionY - y);
                if ( yDiff > mTouchSlop ) {
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;
            case MotionEvent.ACTION_DOWN:
                mLastMotionY = y;
                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST
                        : TOUCH_STATE_SCROLLING;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mTouchState = TOUCH_STATE_REST;
                break;
        }
        return super.onInterceptTouchEvent(ev);
      //  return mTouchState != TOUCH_STATE_REST;
    }

    public interface OnScreenChangeListener {
        public void onScreenChange(int whichScreen);
    }

}
