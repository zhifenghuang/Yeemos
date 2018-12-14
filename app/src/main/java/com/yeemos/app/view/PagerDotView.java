package com.yeemos.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-3-17.
 */
public class PagerDotView extends View {

    private int mSelectPageColor;
    private int mOtherPageColor;
    private int mTotalPage, mCurrentPageIndex;
    private int mScreenWidth;
    private int mDotWidth, mDotRadius, mPadding;
    private Paint mPaint;

    public PagerDotView(Context context) {
        this(context, null);
    }

    public PagerDotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PagerDotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        mDotWidth = Utils.dip2px(context, 8);
        mPadding = Utils.dip2px(context, 4);
        mDotRadius = (int) (mDotWidth * 1.0f / 2 + 0.5f);
        mCurrentPageIndex = 0;
        if ( attrs != null ) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PagerDotView);
            mSelectPageColor = array.getInteger(R.styleable.PagerDotView_selectedPageDotColor, 0);
            mOtherPageColor = array.getInteger(R.styleable.PagerDotView_otherPageDotColor, 0);
            mTotalPage = array.getInteger(R.styleable.PagerDotView_totalPage, 0);
            array.recycle();
        }
    }

    public void setTotalPage(int totalPage) {
        mTotalPage = totalPage;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        if ( mCurrentPageIndex == currentPageIndex ) {
            return;
        }
        mCurrentPageIndex = currentPageIndex;
        if ( mCurrentPageIndex > mTotalPage - 1 ) {
            mCurrentPageIndex = mTotalPage - 1;
        } else if ( mCurrentPageIndex < 0 ) {
            mCurrentPageIndex = 0;
        }
        invalidate();
    }

    public Paint getPaint() {
        if ( mPaint == null ) {
            mPaint = new Paint();
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setStrokeWidth(10);
        }
        return mPaint;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ( mTotalPage <= 0 ) {
            return;
        }
        int radiusY = mDotWidth;
        int radiuStartX;
        if ( mTotalPage % 2 == 0 ) {
            radiuStartX = mScreenWidth / 2 - (mTotalPage - 1) * mDotWidth;
        } else {
            radiuStartX = mScreenWidth / 2 - (mTotalPage / 2) * mDotWidth * 2;
        }
        Paint paint = getPaint();
        for (int i = 0; i < mTotalPage; ++i) {
            if ( i == mCurrentPageIndex ) {
                paint.setColor(mSelectPageColor);
            } else {
                paint.setColor(mOtherPageColor);
            }
            canvas.drawCircle(radiuStartX, radiusY, mDotRadius, paint);
            radiuStartX += 2 * mDotWidth;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mScreenWidth, mDotWidth * 2);
    }
}
