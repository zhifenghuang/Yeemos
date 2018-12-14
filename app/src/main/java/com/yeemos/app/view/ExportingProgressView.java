package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-6-22.
 */
public class ExportingProgressView extends View {
    private Paint mPaint;
    private int mProgress;
    private int mRadius;
    private RectF mRectF;

    public ExportingProgressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRadius = Utils.dip2px(context, 5);
    }

    private Paint getPaint() {
        if ( mPaint == null ) {
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.FILL);
        }
        return mPaint;
    }

    private RectF getRect() {
        if ( mRectF == null ) {
            mRectF = new RectF(0, 0, getWidth(), getHeight());
        }
        return mRectF;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        invalidate();
    }


    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        getPaint().setColor(getResources().getColor(R.color.color_187_187_187));
        getRect().right = getWidth();
        canvas.drawRoundRect(getRect(), mRadius, mRadius, getPaint());

        getPaint().setColor(getResources().getColor(R.color.color_45_223_227));
        getRect().right = getWidth() * mProgress / 100;
        canvas.drawRoundRect(getRect(), mRadius, mRadius, getPaint());
    }
}
