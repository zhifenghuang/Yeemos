package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * ViewPager指示器基类
 * @author Damon
 */
public abstract class BaseViewPagerIndicator extends LinearLayout
{
    /**
     * 手指滑动时的偏移量
     */
    private float mTranslationX;

    /**
     * tab数量
     */
    private int mTabVisibleCount = onVisibleTabViewCount();

    /**
     * 与之绑定的ViewPager
     */
    public ViewPager mViewPager;

    /**
     * 上一个选中的View
     */
    private View lastSelectedView;

    public BaseViewPagerIndicator(Context context)
    {
        this(context, null);
    }

    public BaseViewPagerIndicator(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }
    abstract protected void onTabViewOnDraw(Canvas canvas, float curOffsetX);
    abstract protected void onTabViewSizeChange(int w, int h, int oldw, int oldh, float curOffsetX);
    abstract protected int onVisibleTabViewCount();
    abstract protected int onTabViewCount();
    abstract protected View onTabViewAtIndex(int index);
    abstract protected void onTabViewSelected(View lastSelectedView, View curSelectedView);

    protected int onSuitableCount() {
        return Math.min(onVisibleTabViewCount(), onTabViewCount());
    }

    /**
     * 指示器的绘制,重写此方法
     */
    @Override
    protected void dispatchDraw(Canvas canvas)
    {
        onTabViewOnDraw(canvas, mTranslationX);
        super.dispatchDraw(canvas);
    }

    /**
     * 翻转屏幕等操作使控件形变,需要重新计算,重写此方法
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        onTabViewSizeChange(w, h, oldw, oldh, mTranslationX);
    }

    /**
     * 设置tab的标题内容 可选，可以自己在布局文件中写死
     *
     */
    public void reload()
    {
        // 如果传入的list有值，则移除布局文件中设置的view
        if (onTabViewCount() > 0)
        {
            this.removeAllViews();
            for (int i = 0; i < onTabViewCount(); i++) {
                addView(onTabViewAtIndex(i));
            }
            // 设置item的click事件
            setItemClickEvent();
            if (mViewPager != null) {
                // 设置当前页
                onTabViewSelected(lastSelectedView, getChildAt(mViewPager.getCurrentItem()));
                lastSelectedView = getChildAt(mViewPager.getCurrentItem());
            }
        }
    }

    /**
     * 对外的ViewPager的回调接口
     *
     */
    public interface PageChangeListener
    {
        public void onPageScrolled(int position, float positionOffset,
                                   int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    // 对外的ViewPager的回调接口
    private PageChangeListener onPageChangeListener;

    // 对外的ViewPager的回调接口的设置
    public void setOnPageChangeListener(PageChangeListener pageChangeListener)
    {
        this.onPageChangeListener = pageChangeListener;
    }

    // 设置关联的ViewPager
    public void setViewPager(ViewPager mViewPager, int pos) {
        this.mViewPager = mViewPager;

        mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                onTabViewSelected(lastSelectedView, getChildAt(position));
                lastSelectedView = getChildAt(position);

                // 回调
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageSelected(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // 滚动
                scroll(position, positionOffset);

                // 回调
                if (onPageChangeListener != null) {
                    onPageChangeListener.onPageScrolled(position,
                            positionOffset, positionOffsetPixels);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // 回调
                if (onPageChangeListener != null){
                    onPageChangeListener.onPageScrollStateChanged(state);
                }

            }
        });
        // 设置当前页
        mViewPager.setCurrentItem(pos);
        onTabViewSelected(lastSelectedView, getChildAt(pos));
        lastSelectedView = getChildAt(pos);
    }

    /**
     * 设置点击事件
     */
    private void setItemClickEvent()
    {
        int cCount = getChildCount();
        for (int i = 0; i < cCount; i++)
        {
            final int j = i;
            View view = getChildAt(i);
            view.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mViewPager.setCurrentItem(j);
                }
            });
        }
    }

    /**
     * 指示器跟随手指滚动，以及容器滚动
     *
     * @param position
     * @param offset
     */
    private void scroll(int position, float offset)
    {
        /**
         * <pre>
         *  0-1:position=0 ;1-0:postion=0;
         * </pre>
         */
        // 不断改变偏移量，invalidate
        mTranslationX = getWidth() / onSuitableCount() * (position + offset);

        int tabWidth = getScreenWidth() / onSuitableCount();

        // 容器滚动，当移动到倒数最后一个的时候，开始滚动
        if (offset > 0 && position >= (onSuitableCount() - 2)
                && getChildCount() > onSuitableCount() && position != getChildCount() - 2 )
        {
            if (onSuitableCount() != 1)
            {
                this.scrollTo((position - (onSuitableCount() - 2)) * tabWidth
                        + (int) (tabWidth * offset), 0);
            } else
            // 为count为1时 的特殊处理
            {
                this.scrollTo(
                        position * tabWidth + (int) (tabWidth * offset), 0);
            }
        }

        invalidate();
    }

    /**
     * 设置布局中view的一些必要属性；如果设置了setTabTitles，布局中view则无效
     */
    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        int cCount = getChildCount();

        if (cCount == 0)
            return;

        for (int i = 0; i < cCount; i++)
        {
            View view = getChildAt(i);
            LayoutParams lp = (LayoutParams) view
                    .getLayoutParams();
            lp.weight = 0;
            lp.width = getScreenWidth() / mTabVisibleCount;
            view.setLayoutParams(lp);
        }
        // 设置点击事件
        setItemClickEvent();

    }

    /**
     * 获得屏幕的宽度
     *
     * @return
     */
    public int getScreenWidth()
    {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

}

