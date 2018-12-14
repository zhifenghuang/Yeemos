package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.List;

public class MenuViewPagerTextIndicator extends BaseViewPagerIndicator {

    /**
     * 绘制的画笔
     */
    private Paint mPaint;
    /**
     * path构成一个长方体
     */
    private Path mPath;
    /**
     * 指示器的宽度
     */
    private int mIndicatorWidth;
    /**
     * 指示器的高度
     */
    private int mIndicatorHeight = 6;

    /**
     * 初始时，指示器的偏移量
     */
    private int mInitTranslationX;

    private ArrayList<String> dataSource;

    public MenuViewPagerTextIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(getResources().getColor(R.color.color_45_223_227));
        mPaint.setStyle(Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(2));
        dataSource = new ArrayList<String> ();
    }

    @Override
    protected void onTabViewOnDraw(Canvas canvas, float curOffsetX) {
        // TODO Auto-generated method stub
        canvas.save();
        // 画笔平移到正确的位置
        canvas.translate(mInitTranslationX + curOffsetX, getHeight());
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
    }

    @Override
    protected void onTabViewSizeChange(int w, int h, int oldw, int oldh,
                                       float curOffsetX) {
        mIndicatorWidth = (int) (w / onSuitableCount());

        initIndicator();

        // 初始时的偏移量
        mInitTranslationX = getWidth() / onSuitableCount() / 2 - mIndicatorWidth / 2;
    }

    /**
     * 初始化三角形指示器
     */
    private void initIndicator() {
        mPath = new Path();
        mPath.moveTo(0, 0);
        mPath.lineTo(mIndicatorWidth, 0);
        mPath.lineTo(mIndicatorWidth, -mIndicatorHeight);
        mPath.lineTo(0, -mIndicatorHeight);
        mPath.close();
    }

    @Override
    protected int onTabViewCount() {
        return dataSource.size();
    }

    @Override
    protected int onVisibleTabViewCount() {
        return 3;
    }
    @Override
    protected View onTabViewAtIndex(int index) {
        return generateTextView(dataSource.get(index));
    }

    public void setDataSource(List<String> titles) {
        dataSource.addAll(titles);
        reload();
    }

    @Override
    protected void onTabViewSelected(View lastSelectedView, View curSelectedView) {
        highLightTextView((TextView) curSelectedView);
        resetTextViewColor((TextView) lastSelectedView);
    }

    /**
     * 根据标题生成我们的TextView
     *
     * @param text
     * @return
     */
    private TextView generateTextView(String text) {
        TextView tv = new TextView(getContext());
        LayoutParams lp = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp.width = getScreenWidth() / onSuitableCount();
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(getResources().getColor(R.color.color_88_89_91));
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        tv.setLayoutParams(lp);
        tv.setPadding(0, 16, 0, 26);
        return tv;
    }

    /**
     * 高亮文本
     */
    protected void highLightTextView(TextView view) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(getResources().getColor(R.color.color_45_223_227));
        }
    }

    /**
     * 重置文本颜色
     */
    private void resetTextViewColor(TextView view) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(getResources().getColor(R.color.color_88_89_91));
        }
    }
}

