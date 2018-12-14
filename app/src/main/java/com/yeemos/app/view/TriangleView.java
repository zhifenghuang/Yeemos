package com.yeemos.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-9-13.
 */
public class TriangleView extends View {
    //三角形头部方向
    public static final int TRIANGLE_HEAD_TYPE_DIR_TOP = 0;
    public static final int TRIANGLE_HEAD_TYPE_DIR_LEFT = 1;
    public static final int TRIANGLE_HEAD_TYPE_DIR_RIGHT = 2;
    public static final int TRIANGLE_HEAD_TYPE_DIR_BOTTOM = 3;

    private Paint mPaint;
    private int mTriangleHeadType;
    private int mColor;
    private Path mPath;

    public TriangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        mColor = Color.TRANSPARENT;

        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.TourialView);
            mTriangleHeadType = array.getInteger(R.styleable.TourialView_triangleHeadDir, 0);
            mColor=getResources().getColor(array.getResourceId(R.styleable.TourialView_triangleColor, android.R.color.transparent));
            array.recycle();
        }
        resetPaintColor(mColor);
    }

    public void setTriangleHeadType(int triangleHeadType) {
        mTriangleHeadType = triangleHeadType;
        invalidate();
    }

    public void setColor(int color) {
        mColor = color;
        resetPaintColor(color);
    }


    /**
     * 设置画笔颜色
     *
     * @param color
     */
    private void resetPaintColor(int color) {
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
        }
        mPaint.setColor(color);
        invalidate();
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPath == null) {
            mPath = new Path();
            if (mTriangleHeadType == TRIANGLE_HEAD_TYPE_DIR_TOP) {
                mPath.moveTo(getWidth()/2, 0);
                mPath.lineTo(0, getHeight());
                mPath.lineTo(getWidth(), getHeight());
            } else if (mTriangleHeadType == TRIANGLE_HEAD_TYPE_DIR_LEFT) {
                mPath.moveTo(0, getHeight() / 2);
                mPath.lineTo(getWidth(), 0);
                mPath.lineTo(getWidth(), getHeight());
            } else if (mTriangleHeadType == TRIANGLE_HEAD_TYPE_DIR_RIGHT) {
                mPath.moveTo(getWidth(), getHeight() / 2);
                mPath.lineTo(0, 0);
                mPath.lineTo(0, getHeight());
            } else {
                mPath.moveTo(getWidth()/2, getHeight());
                mPath.lineTo(0, 0);
                mPath.lineTo(getWidth(), 0);
            }
            mPath.close();
        }
        if (mPath == null) {
            resetPaintColor(mColor);
        }
        canvas.drawPath(mPath, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = Utils.dip2px(getContext(), 15);
        setMeasuredDimension(viewWidth, viewWidth);
    }
}
