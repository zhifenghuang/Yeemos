package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Created by gigabud on 16-4-22.
 */
public class MessageItemView extends RelativeLayout {

    private Paint mPaint;
    private int mColor;
    private int mLineWidth;

    public MessageItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }

    public void setLineColor(int color){
        mColor=color;

    }

    public void setLineWidth(int lineWidth){
        mLineWidth=lineWidth;
        invalidate();
    }

    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if(mPaint==null){
            mPaint=new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
        }
        mPaint.setStrokeWidth(mLineWidth);
        mPaint.setColor(mColor);
        canvas.drawLine(0,0,0,getHeight(),mPaint);
    }
}
