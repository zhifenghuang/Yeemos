package com.yeemos.app.view;

/**
 * Created by gigabud on 16-4-8.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ShowChatTimeView extends View {

    private ListView mListView;
    private Paint mPaint;
    private SimpleDateFormat mSdf;

    public ShowChatTimeView(Context context, AttributeSet attr) {
        super(context, attr);
        mPaint = new Paint();
        mPaint.setTextSize(Utils.sp2px(context, 10));
        mPaint.setColor(Color.BLACK);
    }

    public void setListView(ListView listView) {
        mListView = listView;
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mListView == null) {
            return;
        }
        int listviewChildCount = mListView.getChildCount();
        int[] location = new int[2];
        int top;
        View itemView, childView;
        ViewGroup ll;
        int statusBarHeight = 0;
      //  if (!isFullScreen()) {
            statusBarHeight = getStatusBarHeight();
     //   }
        for (int i = 0; i < listviewChildCount; ++i) {
            itemView = mListView.getChildAt(i);
            if (itemView != null && itemView.getVisibility() == View.VISIBLE) {
                ll = (ViewGroup) itemView.findViewById(R.id.ll);
                if (ll == null) {
                    continue;
                }
                int childCount = ll.getChildCount();
                for (int j = 0; j < childCount; ++j) {
                    childView = ll.getChildAt(j);
                    if (childView.getVisibility() == View.GONE) {
                        continue;
                    }
                    childView.getLocationOnScreen(location);
                    top = (int) (location[1] - getTop() - statusBarHeight + (childView.getHeight() + mPaint.getTextSize()) * 0.5f);
                    BasicMessage msg = (BasicMessage) ll.getChildAt(j).getTag();
                    if (msg != null) {
                        canvas.drawText(getTimeStr(msg.getCliTime()), 0, top, mPaint);
                    }
                }
            }
        }
    }

    private String getTimeStr(long time) {
        if (mSdf == null) {
            mSdf = new SimpleDateFormat("MM-dd HH:mm");
        }
        Date dt = new Date(time);
        return mSdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
    }


    /**
     * 如果不是全屏需要获取View位置时，y值减去状态栏高度
     *
     * @return
     */
    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height",
                "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 判断是否是全屏
     *
     * @return
     */
    private boolean isFullScreen() {
        return ((Activity) getContext()).getWindow().getAttributes().flags == 66816;
    }
}

