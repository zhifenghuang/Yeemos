package com.yeemos.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * 圆形头像Imageview
 *
 * @author gigabud
 */
public class RoundedImageView extends ImageView {

    /**
     * 3x3 矩阵，主要用于缩小放大
     */
    private Matrix mMatrix;
    /**
     * 渲染图像，使用图像为绘制图形着色
     */
    private BitmapShader mBitmapShader;

    /**
     * 绘图的Paint
     */
    private Paint mBitmapPaint;

    /**
     * 圆角的半径
     */
    private int mRadius;


    private Bitmap mVipBmp;

    private Rect mSrcRect;
    private RectF mDstRect;

    private boolean mIsNeedDrawVipBmp;

    public RoundedImageView(Context context) {
        super(context, null);
        init();
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setNeedDrawVipBmp(boolean isNeedDrawVipBmp) {
        mIsNeedDrawVipBmp = isNeedDrawVipBmp;
        invalidate();
    }

    @SuppressLint("NewApi")
    public void init() {
        mIsNeedDrawVipBmp = false;
        mMatrix = new Matrix();
        mBitmapPaint = new Paint();
        mBitmapPaint.setAntiAlias(true);
        setBackgroundColor(Color.TRANSPARENT);
    }

    private Bitmap getVipBmp() {
        if (mVipBmp == null) {
            mVipBmp = ((BitmapDrawable) getResources().getDrawable(R.drawable.vip_tick)).getBitmap();
        }
        return mVipBmp;
    }

    @SuppressLint("DrawAllocation")
    public void onDraw(Canvas canvas) {
        if (getDrawable() == null || getWidth() == 0 || getHeight() == 0) {
            super.onDraw(canvas);
            return;
        }
        Drawable drawable = getDrawable();
        Bitmap bmp = drawableToBitamp(drawable);
        if (bmp == null) {
            return;
        }
        // 将bmp作为着色器，就是在指定区域内绘制bmp
        mBitmapShader = new BitmapShader(bmp, TileMode.CLAMP, TileMode.CLAMP);
        mMatrix.setScale(getWidth() * 1.0f / bmp.getWidth(), getHeight() * 1.0f
                / bmp.getHeight());
        // 设置变换矩阵
        mBitmapShader.setLocalMatrix(mMatrix);
        // 设置shader
        mBitmapPaint.setShader(mBitmapShader);
        mRadius = getWidth() / 2;
        canvas.drawCircle(mRadius, mRadius, mRadius, mBitmapPaint);

        if (mIsNeedDrawVipBmp) {
            Bitmap vipBmp = getVipBmp();
            canvas.drawBitmap(vipBmp, getSrcRect(), getDstRect(), null);
        }
    }

    private Rect getSrcRect() {
        if (mSrcRect == null) {
            mSrcRect = new Rect(0, 0, getVipBmp().getWidth(), getVipBmp().getHeight());
        }
        return mSrcRect;
    }

    private RectF getDstRect() {
        if (mDstRect == null) {
            int dstWidth = Utils.dip2px(getContext(), 130);
            float scale = getWidth() * 1.0f / dstWidth;
            float drawWidth = getVipBmp().getWidth() * scale;
            float drawHeight = getVipBmp().getHeight() * scale;
            float start = getWidth() - 3 * drawWidth / 2;
            mDstRect = new RectF(start, getHeight() - drawHeight, start + drawWidth, getHeight());
        }
        return mDstRect;
    }

    /**
     * drawable转bitmap
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }
}
