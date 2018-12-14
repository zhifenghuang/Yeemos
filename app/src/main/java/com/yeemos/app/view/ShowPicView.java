package com.yeemos.app.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.yeemos.app.activity.BaseActivity;

/**
 * Created by gigabud on 17-9-27.
 */

@SuppressLint("AppCompatCustomView")
public class ShowPicView extends ImageView {

    private float[] mOriginPoints;
    private float[] mPoints;
    private RectF mOriginContentRect;
    private RectF mContentRect;

    private Bitmap mBitmap;
    private Matrix mMatrix;

    private float mScaleSize = 1.0f;

    public ShowPicView(Context context) {
        this(context, null);
    }

    public ShowPicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShowPicView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(null);
        if (drawable != null) {
            if (drawable instanceof GlideBitmapDrawable) {
                setImageBitmap(((GlideBitmapDrawable) drawable).getBitmap());
            } else {
                setImageBitmap(((BitmapDrawable) drawable).getBitmap());
            }
        } else {
            setImageBitmap(null);
        }
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (mBitmap == null) {
            invalidate();
            return;
        }
        try {
            float px = mBitmap.getWidth();
            float py = mBitmap.getHeight();
            mPoints = new float[10];
            if (getWidth() == 0 || getHeight() == 0) {
                DisplayMetrics dis = ((BaseActivity) getContext()).getDisplaymetrics();
                mScaleSize = dis.widthPixels / px;
                mPoints[8] = dis.widthPixels * 0.5f;
                mPoints[9] = dis.heightPixels * 0.5f;
            } else {
                mScaleSize = getWidth() / px;
                mPoints[8] = getWidth() * 0.5f;
                mPoints[9] = getHeight() * 0.5f;
            }
            mOriginPoints = new float[]{0, 0, px, 0, px, py, 0, py, px / 2, py / 2};
            mOriginContentRect = new RectF(0, 0, px, py);

            mContentRect = new RectF();
            mMatrix = new Matrix();
            float dy = mPoints[9] - py / 2;

            mMatrix.postTranslate(mPoints[8] - px / 2, dy);
            mMatrix.postScale(mScaleSize, mScaleSize, mPoints[8], mPoints[9]);
            RectF rectF = new RectF();
            mMatrix.mapRect(rectF, mOriginContentRect);
        } catch (Exception e) {
            e.printStackTrace();
        }
        invalidate();

    }


    @Override
    public void setFocusable(boolean focusable) {
        super.setFocusable(focusable);
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBitmap == null || mMatrix == null || mContentRect == null) {
            return;
        }
        mMatrix.mapPoints(mPoints, mOriginPoints);
        mMatrix.mapRect(mContentRect, mOriginContentRect);
        canvas.drawBitmap(mBitmap, mMatrix, null);
    }


}
