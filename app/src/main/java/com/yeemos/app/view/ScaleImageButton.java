package com.yeemos.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import com.yeemos.app.R;
import com.yeemos.app.utils.Utils;

/**
 * Created by gigabud on 15-12-24.
 */
public class ScaleImageButton extends ImageButton {

    private float mScale;
    private int mWidthPadding;

    public ScaleImageButton(Context context, float scale) {
        super(context);
        mScale = scale;
        mWidthPadding = 0;
    }

    public ScaleImageButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ScaleImageButton);
            mScale = array.getFloat(R.styleable.ScaleImageButton_btnScale, 0f);
            mWidthPadding = array.getInteger(R.styleable.ScaleImageButton_widthPadding, 0);
            array.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable drawable = getDrawable();
        if (drawable != null) {
            Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
            if (bmp != null) {
                int width = bmp.getWidth();
                int height = bmp.getHeight();
                setMeasuredDimension((int) (width * mScale + Utils.dip2px(getContext(), mWidthPadding) + 0.5f), (int) (height * mScale + 0.5f));
            }
        }
    }
}
