package com.yeemos.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.BeansBase.TopicBean;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-4.
 */
public class SelectItemHorizontalScrollView extends HorizontalScrollView {

    private static final int SELECT_EMO_VIEW_TYPE_CAMERA = 0;
    private static final int SELECT_EMO_VIEW_TYPE_SEARCH = 1;
    private static final int SELECT_TOPIC_VIEW_TYPE_SEARCH = 2;

    private Context mContext;
    private int mScreenWidth, mCenterScreenX;
    private int mLastSelectItem;
    private static final float NO_SELECT_ITEM_SCALE = 1.0f;
    private static final float SELECT_ITEM_SCALE = 1.2f;

    private ImageView mIvFrontEmo;
    private boolean mIsItemMoved = false;

    private int mSelectEmoViewType = SELECT_EMO_VIEW_TYPE_CAMERA;


    public SelectItemHorizontalScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SelectItemHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SelectEmoViewType);
            mSelectEmoViewType = array.getInteger(R.styleable.SelectEmoViewType_select_emo_view_type, 0);
            array.recycle();
        }
    }

    private void init(Context context) {
        mContext = context;
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        mCenterScreenX = mScreenWidth / 2;
    }


    public void addTopics(ArrayList<TopicBean> topicList) {
        final LinearLayout ll = (LinearLayout) getChildAt(0);
        ll.removeAllViews();
        TopicItemView topicItemView;
        int length = topicList.size();
        int padding = Utils.dip2px(mContext, 3);
        int itemWidth = Utils.dip2px(mContext, 83);
        int itemHeight = Utils.dip2px(mContext, 132);
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(itemWidth, itemHeight);
        for (int i = 0; i < length; ++i) {
            topicItemView = new TopicItemView(mContext);
            layoutParam.leftMargin = padding;
            ll.addView(topicItemView, layoutParam);
            topicItemView.setTopic(topicList.get(i));
        }
    }


    public void addEmosInHomeFragment(ArrayList<TagBean> tagBeanArrayList) {
        final LinearLayout ll = (LinearLayout) getChildAt(0);
        ll.removeAllViews();
        int length = Constants.EMO_ID_COLOR.length;
        int padding = Utils.dip2px(mContext, 9);
        int itemWidth = (mScreenWidth - padding * (length + 1)) / length;
        int restWidth = mScreenWidth - itemWidth * length;
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), Constants.EMO_ID_COLOR[0][0]);
        int itemHeight = (int) (itemWidth * bmp.getHeight() * 1.0f / bmp.getWidth());
        bmp.recycle();
        bmp = null;
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(itemWidth, itemHeight);
        EmoItemView emoView;
        TagBean tagBean;
        for (int i = 0; i < length; ++i) {
            emoView = new EmoItemView(mContext);
            ((ImageView) emoView.findViewById(R.id.emoIcon)).setImageResource(Constants.EMO_ID_COLOR[i][0]);
            if (i == 0) {
                layoutParam.leftMargin = padding + restWidth / 2;
            } else {
                layoutParam.leftMargin = padding;
            }
            ll.addView(emoView, layoutParam);
            tagBean = tagBeanArrayList.get(i);
            emoView.setTagBean(tagBean);
        }
    }

    public void addEmosInCameraFragment(ImageView ivFrontEmo) {
        mIvFrontEmo = ivFrontEmo;
        final LinearLayout ll = (LinearLayout) getChildAt(0);
        ll.removeAllViews();
        ImageView emoImageView;
        int length = 350;
        int drawableCount = Constants.EMO_ID_COLOR.length;
        int padding = Utils.dip2px(mContext, 17);
        int ivWidth = (mScreenWidth - padding * 7) / 7;
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), Constants.EMO_ID_COLOR[0][0]);
        int ivHeight = ivWidth * bmp.getHeight() / bmp.getWidth();
        bmp.recycle();
        bmp = null;
        LinearLayout.LayoutParams layoutParam = new LinearLayout.LayoutParams(ivWidth, ivHeight);
        RelativeLayout.LayoutParams reLp = new RelativeLayout.LayoutParams(ivWidth, ivHeight);
        reLp.addRule(RelativeLayout.CENTER_VERTICAL);
        reLp.leftMargin = mCenterScreenX - ivWidth / 2;
        ivFrontEmo.setLayoutParams(reLp);
        layoutParam.leftMargin = padding;
        int oneWidth = padding + ivWidth;
        int count = mCenterScreenX / oneWidth;  //计算半个屏幕能容纳个数
        mLastSelectItem = length / 2;
        for (int i = 0; i < length; ++i) {
            emoImageView = new ImageView(mContext);
            emoImageView.setImageResource(Constants.EMO_ID_COLOR[i % drawableCount][0]);
            emoImageView.setAlpha(0.3f);
            if (i == mLastSelectItem) {
                mIvFrontEmo.setVisibility(View.VISIBLE);
                mIvFrontEmo.setImageResource(Constants.EMO_ID_COLOR[i % drawableCount][0]);
                mIvFrontEmo.setScaleX(SELECT_ITEM_SCALE);
                mIvFrontEmo.setScaleY(SELECT_ITEM_SCALE);
            } else {
                emoImageView.setScaleX(NO_SELECT_ITEM_SCALE);
                emoImageView.setScaleY(NO_SELECT_ITEM_SCALE);
            }
            if (i == 0) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ivWidth, ivHeight);
                lp.leftMargin = mCenterScreenX - oneWidth * count;
                lp.leftMargin -= ivWidth / 2;
                ll.addView(emoImageView, lp);
                continue;
            }
            ll.addView(emoImageView, layoutParam);
            emoImageView.setTag(i);
        }
        final int initScrollX = oneWidth * (mLastSelectItem - count);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getScrollX() <= 0) {
                    scrollBy(initScrollX, 0);
                }
            }
        }, 10);

    }

    private int lastX = 0;
    private int touchEventId = -9983761;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == touchEventId) {
                if (lastX == getScrollX()) {
                    selectEmoToCenter();
                } else {
                    handler.sendMessageDelayed(handler.obtainMessage(touchEventId), 5);
                    lastX = getScrollX();
                }
            }
        }
    };

    /**
     * 使选中表情移到中间
     */
    private void selectEmoToCenter() {
        LinearLayout ll = (LinearLayout) getChildAt(0);
        View v = ll.getChildAt(mLastSelectItem);
        final int dX = v.getLeft() + v.getWidth() / 2 - getScrollX() - mCenterScreenX;
        smoothScrollBy(dX - 1, 0);   //用兩次smoothScrollBy的目的是为了解决有时不滚动的bug
        postDelayed(new Runnable() {
            @Override
            public void run() {
                smoothScrollBy(1, 0);
                if(mIvFrontEmo!=null){
                    mIvFrontEmo.setVisibility(View.VISIBLE);
                }
            }
        }, 100);
    }

    public void setItemMoved(boolean isItemMoved) {
        mIsItemMoved = isItemMoved;
    }

    public boolean getItemMoved() {
        return mIsItemMoved;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //  super.on
        if (mSelectEmoViewType == SELECT_EMO_VIEW_TYPE_SEARCH || mSelectEmoViewType == SELECT_TOPIC_VIEW_TYPE_SEARCH) {
            return super.onTouchEvent(event);
        }
        if(event.getAction()==MotionEvent.ACTION_DOWN){
            if (mIvFrontEmo != null) {
                mIvFrontEmo.setVisibility(View.INVISIBLE);
            }
        }else if (event.getAction() == MotionEvent.ACTION_UP && mIsItemMoved) {
            handler.sendMessageDelayed(handler.obtainMessage(touchEventId), 5);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        mIsItemMoved = true;
        if (mSelectEmoViewType == SELECT_EMO_VIEW_TYPE_SEARCH || mSelectEmoViewType == SELECT_TOPIC_VIEW_TYPE_SEARCH) {
            return;
        }
        LinearLayout ll = (LinearLayout) getChildAt(0);
        int count = ll.getChildCount();
        View v = ll.getChildAt(mLastSelectItem);
        int minX = Math.abs(v.getLeft() + v.getWidth() / 2 - x - mCenterScreenX);
        int itemX;
        int selectItem = mLastSelectItem;
        boolean isHadRest = false;
        if (x > oldx) {
            for (int i = mLastSelectItem + 1; i < count; ++i) {
                v = ll.getChildAt(i);
                itemX = Math.abs(v.getLeft() + v.getWidth() / 2 - x - mCenterScreenX);
                if (itemX < minX) {
                    minX = itemX;
                    selectItem = i;
                    isHadRest = true;
                } else {
                    if (isHadRest) {
                        break;
                    }
                }
            }
        } else if (x < oldx) {
            for (int i = mLastSelectItem - 1; i >= 0; --i) {
                v = ll.getChildAt(i);
                itemX = Math.abs(v.getLeft() + v.getWidth() / 2 - x - mCenterScreenX);
                if (itemX < minX) {
                    minX = itemX;
                    selectItem = i;
                    isHadRest = true;
                } else {
                    if (isHadRest) {
                        break;
                    }
                }
            }
        }

        if (selectItem != mLastSelectItem) {
            v = ll.getChildAt(mLastSelectItem);
            v.setScaleX(NO_SELECT_ITEM_SCALE);
            v.setScaleY(NO_SELECT_ITEM_SCALE);
            v.setVisibility(View.VISIBLE);
            mLastSelectItem = selectItem;
            if (mIvFrontEmo != null) {
                mIvFrontEmo.setImageResource(Constants.EMO_ID_COLOR[mLastSelectItem % Constants.EMO_ID_COLOR.length][0]);
            }
        }
        if (mIvFrontEmo != null) {
            v = ll.getChildAt(mLastSelectItem);
            RelativeLayout.LayoutParams reLp = (RelativeLayout.LayoutParams) mIvFrontEmo.getLayoutParams();
            reLp.leftMargin = v.getLeft() - x;
            mIvFrontEmo.setLayoutParams(reLp);
        }
    }

    /**
     * 获取被选中表情的序号Id
     *
     * @return
     */
    public int getLastSelectItem() {
        return mLastSelectItem;
    }
}
