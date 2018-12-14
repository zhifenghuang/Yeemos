package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import static android.graphics.Paint.Style.FILL_AND_STROKE;

/**
 * Created by gigabud on 16-7-15.
 */
public class CutAvaterView extends View {

    private Paint mPaint;

    public CutAvaterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawARGB(0, 0, 0, 0);

        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        int layerId = canvas.saveLayer(0, 0, canvasWidth, canvasHeight, null, Canvas.ALL_SAVE_FLAG);
        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setStyle(FILL_AND_STROKE);
            mPaint.setAntiAlias(true);
        }
        mPaint.setColor(Color.argb(50,0,0,0));
        canvas.drawRect(0,0,canvasWidth,canvasHeight,mPaint);
        mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.CLEAR));
        mPaint.setColor(Color.TRANSPARENT);
        canvas.drawCircle(canvasWidth / 2,canvasHeight * 5 / 12,canvasWidth / 2,mPaint);
        mPaint.setXfermode(null);
        canvas.restoreToCount(layerId);
    }

    public int getCutLeft() {
        return 0;
    }

    public int getCutRight() {
        return getWidth();
    }

    public int getCutTop() {
        return getHeight() * 5 / 12 - getWidth() / 2;
    }

    public int getCutBottom() {
        return getHeight() * 5 / 12 + getWidth() / 2;
    }


}
