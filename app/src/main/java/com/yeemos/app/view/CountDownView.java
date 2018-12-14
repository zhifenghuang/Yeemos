package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.gbsocial.BeansBase.PostBean;
import com.yeemos.app.utils.Utils;

/**
 * Created by gigabud on 16-5-23.
 */
public class CountDownView extends View {

    private long mTotal;
    private long mProgress;
    private Paint mPaint;
    private RectF mArcRectF;
    private boolean mIsAddProgress;
    private boolean mFlag;
    private PostBean mPostBean;

    public CountDownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIsAddProgress = false;
    }

    public CountDownView(Context context) {
        super(context);
    }

    public void setPaintColor(int color) {
        getPaint().setColor(color);
    }

    public void setIsAddProgress(boolean isAddProgress, PostBean postBean) {
        if (postBean == null) {
            mFlag = false;
            return;
        }
        mIsAddProgress = isAddProgress;
        mPostBean = postBean;
        mProgress = postBean.getUploadProgress();
        startProgressThread();
    }

    /**
     *
     */
    private Paint getPaint() {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStrokeWidth(Utils.dip2px(getContext(), 3));
            mPaint.setColor(Color.WHITE);
            mPaint.setStyle(Paint.Style.STROKE);
        }
        return mPaint;
    }

    public void setProgress(long total, long progress) {
        mTotal = total;
        mProgress = progress;
        if (mProgress >= 90) {
            mFlag = false;
        }
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mTotal <= 0) {
            return;
        }
        if (mArcRectF == null) {
            float radius = getWidth() * 0.45f;
            float centerX = getWidth() * 0.5f;
            float centerY = getHeight() * 0.5f;
            mArcRectF = new RectF(centerX - radius, centerY - radius, centerX
                    + radius, centerY + radius);
        }
        if (mIsAddProgress) {
            canvas.drawArc(mArcRectF, -90, mProgress * 360f / mTotal, false, getPaint());
        } else {
            canvas.drawArc(mArcRectF, -90, -(mTotal - mProgress) * 360f / mTotal, false, getPaint());
        }
    }

    private synchronized void startProgressThread() {
        if (mFlag && mPostBean.getUploadProgress() >= 90) {
            mFlag = false;
            return;
        }
        mFlag = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mFlag && mPostBean != null && mPostBean.getUploadState() == 1) {
                    try {
                        int progress = mPostBean.getUploadProgress();
                        if (progress < 90) {
                            mPostBean.setUploadProgress(progress + 1);
                            mProgress = mPostBean.getUploadProgress();
                            postInvalidate();
                            Thread.sleep(30);
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
                mFlag = false;
            }
        }).start();
    }
}
