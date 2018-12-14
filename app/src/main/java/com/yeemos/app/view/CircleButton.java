package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;

import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * Created by gigabud on 15-12-4.
 */
public class CircleButton extends ImageButton {

    private Paint mPaint;
    private int mShutterBtnWidth = -1;
    private RectF mArcRect = null;
    private float mArcAngle = 0;
    private int mPaintWidth;
    private Bitmap mEmoBmp;

    private static final float SMALL_SCALE = 0.8f;
    private static final float NORMAL_SCALE = 1.0f;

    public CircleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleX(SMALL_SCALE);
        setScaleY(SMALL_SCALE);
        setAlpha(0.7f);
        mEmoBmp = null;
    }

    /**
     * 获取拍照按钮的大小
     *
     * @return
     */
    public int getShutterButtonWidth() {
        if ( mShutterBtnWidth <= 0 ) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.current_background);
            mShutterBtnWidth = bmp.getWidth();
            bmp.recycle();
            bmp = null;
        }
        return mShutterBtnWidth;
    }

    /**
     * 根据表情序号，设置表情图和绘制颜色
     *
     * @param emoId
     */
    public void setEmo(int emoId) {
        mEmoBmp = null;
        if ( emoId != -1 ) {
  //          mEmoBmp = BitmapFactory.decodeResource(getResources(), Constants.EMO_ID_COLOR[emoId][0]);
            resetPaintColor(Constants.EMO_ID_COLOR[emoId][1]);
        }
        startScaleAnim();
    }

    /**
     * 还原到初始状态
     */
    public void resetCircleButton() {
        setVisibility(View.VISIBLE);
        setScaleX(SMALL_SCALE);
        setScaleY(SMALL_SCALE);
        clearAnimation();
        mEmoBmp = null;
        mPaint = null;
        mArcAngle = 0;
        postInvalidate();

    }

    private void startScaleAnim() {
        clearAnimation();
        Animation scaleAnimation = new ScaleAnimation(1.0f, 1.15f, 1.0f, 1.15f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnimation.setDuration(500);
        scaleAnimation.setInterpolator(new AccelerateInterpolator());
        scaleAnimation.setFillAfter(true);
        startAnimation(scaleAnimation);
    }

    /**
     * 计算弧形角度
     *
     * @param current
     * @param total
     */
    public void resetArcAngle(long current, long total) {
        mArcAngle = current * 360f / total;
        if ( mArcAngle >= 360 ) {
            mArcAngle = 360;
        }
        postInvalidate();
    }

    /**
     * 设置画笔颜色
     *
     * @param color
     */
    private void resetPaintColor(int color) {
        if ( mPaint == null ) {
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaintWidth = Utils.dip2px(getContext(), 5);
            mPaint.setStrokeWidth(mPaintWidth);
            mPaint.setStyle(Paint.Style.STROKE);
        }
        mPaint.setColor(color);
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if ( mPaint == null ) {
            return;
        }

        if ( mEmoBmp != null ) {
            canvas.drawBitmap(mEmoBmp, (getWidth() - mEmoBmp.getWidth()) * 1.0f / 2, (getHeight() - mEmoBmp.getHeight()) * 1.0f / 2, null);
        }

        if ( mArcRect == null ) {
            float centerX = getWidth() * 1.0f / 2;
            float centerY = getHeight() * 1.0f / 2;
            float radius = (getShutterButtonWidth() - mPaintWidth) * 0.5f;
            mArcRect = new RectF(centerX - radius, centerY - radius, centerX
                    + radius, centerY + radius);
        }
        canvas.drawArc(mArcRect, -90, mArcAngle, false, mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int viewWidth = getShutterButtonWidth() + Utils.dip2px(getContext(),15);
        setMeasuredDimension(viewWidth, viewWidth);
    }

    public void destroyView() {
        clearAnimation();
        if ( mEmoBmp != null ) {
            mEmoBmp.recycle();
            mEmoBmp = null;
        }
    }
}
