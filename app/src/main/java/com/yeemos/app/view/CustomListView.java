package com.yeemos.app.view;

/**
 * Created by gigabud on 16-4-12.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.R;
import com.yeemos.app.utils.Utils;

public class CustomListView extends ListView {
    private boolean mIsCanScroll;
    private int mCustomListViewType;
    private float mTapX, mTapY;
    private int mTapPosition;
    private boolean isEditState = false;


    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsCanScroll = true;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CustomListView);
            mCustomListViewType = array.getInteger(R.styleable.CustomListView_custom_listview_type, 0);
            array.recycle();
        }
    }

    public void setCanScroll(boolean isCanScroll) {
        mIsCanScroll = isCanScroll;
    }

    public void setEditState(boolean isEditState) {
        this.isEditState = isEditState;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mIsCanScroll && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mCustomListViewType == 0 && !isEditState) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTapX = event.getX();
                    mTapY = event.getY();
                    mTapPosition = pointToPosition((int) event.getX(),
                            (int) event.getY());
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTapPosition == INVALID_POSITION) {
                        break;
                    }
                    if (Math.abs(event.getX() - mTapX) < 5 && Math.abs(event.getY() - mTapY) < 5) {
                        int firstVisiblePosition = getFirstVisiblePosition();
                        View view = getChildAt(mTapPosition - firstVisiblePosition);
                        if (view != null) {
                            View ivLastPost = view.findViewById(R.id.ivLastPost);
                            if (ivLastPost.getVisibility() == View.VISIBLE) {
                                int[] location = new int[2];
                                ivLastPost.getLocationOnScreen(location);
                                if (event.getRawX() > location[0]) {
                                    ivLastPost.performClick();
                                    break;
                                }
                            }
                            view.performClick();
                        }
                    }
                    break;
            }
        }

        return mIsCanScroll && super.onInterceptTouchEvent(event);
    }
}

