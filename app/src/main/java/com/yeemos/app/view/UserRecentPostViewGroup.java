package com.yeemos.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.yeemos.app.interfaces.OnItemClickListener;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigabud on 16-3-17.
 */
public class UserRecentPostViewGroup extends ViewGroup {

    private PostBean[] mPostBeanArray;
    public static final int MAX_POST_NUM = 6;
    private OnItemClickListener mOnItemClickListener;
    private int mCurrentIndex;
    private int mPadding;

    public UserRecentPostViewGroup(Context context) {
        this(context, null);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setCurrentIndex(int index) {
        mCurrentIndex = index;
    }

    public void setPostBeans(List<PostBean> postBeanList) {
        if (mPostBeanArray == null) {
            mPostBeanArray = new PostBean[MAX_POST_NUM];
        }
        for (int i = 0; i < MAX_POST_NUM; ++i) {
            mPostBeanArray[i] = null;
        }
        for (int i = 0; i < postBeanList.size(); ++i) {
            mPostBeanArray[i] = postBeanList.get(i);
        }
    }

    public UserRecentPostViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserRecentPostViewGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPadding = Utils.dip2px(context, 1.5f);
        View view;
        LayoutInflater lf = LayoutInflater.from(context);
        for (int i = 0; i < MAX_POST_NUM; ++i) {
            view = lf.inflate(R.layout.past_post_item, null);
            addView(view);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if(!changed){
            return;
        }
        int childCount = getChildCount();
        int startX = 0, startY = 0;
        int width = ((r - l)  - 4 * mPadding) / 3;
        int height = (b - t) / 2 - mPadding;
        View view;
        for (int i = 0; i < childCount; ++i) {
            view = getChildAt(i);
            startX = (i / 2) * width + ((i + 2) / 2) * mPadding;
            if (i % 2 == 0) {
                startY = 0;
            } else {
                startY += (height + mPadding);
            }
            view.layout(startX, startY, startX + width, startY + height);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(mCurrentIndex * MAX_POST_NUM + ((int) v.getTag()));
                    }
                }
            });

            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemTouch(event);
                    }
                    return v.onTouchEvent(event);
                }
            });
        }
        resetView();
    }

    public void resetView() {
        int childCount = getChildCount();
        View view;
        CustomUrlImageView iv;
        ImageView ivEmo;
        for (int i = 0; i < childCount; ++i) {
            view = getChildAt(i);
            if (mPostBeanArray[i] == null) {
                view.setVisibility(View.GONE);
            } else {
                view.setVisibility(View.VISIBLE);
                iv = ((CustomUrlImageView) view.findViewById(R.id.customImageView));
                ivEmo = (ImageView) view.findViewById(R.id.ivEmo);
                iv.setPostBean(mPostBeanArray[i]);
                iv.setTag(R.id.post_bean,mPostBeanArray[i]);
                iv.setNeedRouctRect(true);
                view.setTag(i);
                ArrayList<TagBean> tagList = mPostBeanArray[i].getTags();
                if (tagList != null && !tagList.isEmpty()) {
                    ivEmo.setVisibility(View.VISIBLE);
                    int resId = Constants.EMO_ID_COLOR[tagList.get(0).getId()][0];
                    ivEmo.setImageResource(resId);
                } else {
                    ivEmo.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measureByType(widthMeasureSpec);
        int measureHeight = measureByType(heightMeasureSpec);
        int childWidth = (measureWidth - mPadding * 4) / 3;
        int childHeight = (measureHeight - mPadding) / 2;
        int childCount = getChildCount();
        for (int i = 0; i < childCount; ++i) {
            ((CustomUrlImageView) getChildAt(i).findViewById(R.id.customImageView)).setViewWH(childWidth, childHeight);
            measureChild(getChildAt(i), MeasureSpec.makeMeasureSpec(childWidth,
                    MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(childHeight,
                    MeasureSpec.EXACTLY));

        }
        setMeasuredDimension(measureWidth, measureHeight);
    }

    private int measureByType(int pMeasureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(pMeasureSpec);
        int specSize = MeasureSpec.getSize(pMeasureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
            case MeasureSpec.EXACTLY:
                result = specSize;
                break;
        }
        return result;
    }
}
