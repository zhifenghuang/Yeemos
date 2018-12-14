package com.yeemos.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yeemos.app.R;

import java.util.ArrayList;


/**
 * NotificationSetting页面用的RadioGroup
 *
 * @author Damon
 * @ClassName: MenuRadioGroup
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @date Oct 21, 2015 3:54:33 PM
 */
public class MenuRadioGroup extends LinearLayout {
    int lastCheckedID = 0;
    private MenuRadioGroupListener mListener = null;

    public interface MenuRadioGroupListener {
        void onClicked(MenuRadioGroup radioGroup, int index);
    }

    public MenuRadioGroup(Context context) {
        super(context);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
    }

    public MenuRadioGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
    }

    public MenuRadioGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);
    }

    /**
     * @param titles
     * @param defaultCheckedIndex
     * @param paddingLeft         参数
     * @return void    返回类型
     * @Description: 主要设置方法
     * @date Oct 21, 2015 6:32:27 PM
     */
    public void setRadioButtonTitles(ArrayList<String> titles, int defaultCheckedIndex, int paddingLeft, int paddingRight) {
        if (titles != null) {
            for (int i = 0; i < titles.size(); i++) {
//				if (i == 0) {
//					addView(createLine());
//				}
                final MenuRadioButton radioButton = createRadioButton(titles.get(i), getID(i), paddingLeft, paddingRight);
                if (defaultCheckedIndex == i) {
                    radioButton.setChecked(true);
                    lastCheckedID = radioButton.getId();
                }
                addView(radioButton);

                if (i == titles.size() - 1) {
                    //addView(createLine());
                } else {
                    final int index = i;
                    post(new Runnable() {
                        public void run() {
                            View line = createLine();
                            LayoutParams lp = new LayoutParams(line.getLayoutParams());
                            lp.setMargins(radioButton.getTitleTextView().getLeft(), 0, 0, 0);
                            addView(line, index * 2 + 1, lp);
                        }
                    });
                }
            }
        }
    }

    private MenuRadioButton createRadioButton(String title, int ID, int paddingLeft, int paddingRight) {
        MenuRadioButton radioButton = new MenuRadioButton(getContext(), R.drawable.setting_tick_next, title, paddingLeft, paddingRight);
        radioButton.setId(ID);
        radioButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (v.getId() == lastCheckedID) {
                    return;
                }
                ((MenuRadioButton) findViewById(lastCheckedID))
                        .setChecked(false);
                ((MenuRadioButton) v).setChecked(true);
                lastCheckedID = v.getId();
                if (getListener() != null) {
                    getListener().onClicked(MenuRadioGroup.this, getCheckedIndex(lastCheckedID));
                }
            }
        });
        return radioButton;
    }

    private View createLine() {
        View line = new View(getContext());
        line.setBackgroundColor(getResources().getColor(R.color.color_170_170_170));
        line.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 1));
        line.setAlpha(0.3f);
        return line;
    }

    public void setMenuRadioButtonChecked(int index) {
        ((MenuRadioButton) findViewById(lastCheckedID))
                .setChecked(false);
        lastCheckedID = getID(index);
        ((MenuRadioButton) findViewById(lastCheckedID))
                .setChecked(true);
    }

    private int getID(int index) {
        return index + 0x1111;
    }

    private int getCheckedIndex(int ID) {
        return ID - 0x1111;
    }

    private MenuRadioGroupListener getListener() {
        return mListener;
    }

    public void setMenuRadioGroupListener(MenuRadioGroupListener mListener) {
        this.mListener = mListener;
    }

    /**
     * @author Damon
     * @Des:MenuRadioGroup的button
     * @date Oct 21, 2015 6:35:18 PM
     */
    class MenuRadioButton extends LinearLayout {
        //        private final int imgCheckedID = 0x00101245;
//        private final int tvTitleID = 0x00101246;
        private boolean isChecked = false;
        private int paddingLeft = 0;
        private int paddingRight = 0;

        public MenuRadioButton(Context context, int checkedResource,
                               String title, int paddingLeft, int paddingRight) {
            super(context);
            this.paddingLeft = paddingLeft;
            this.paddingRight = paddingRight;
            init();
            getTitleTextView().setText(title);
            getCheckedImgView().setImageResource(checkedResource);
            setChecked(false);
        }

        private void init() {
            setClickable(true);
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));
            setPadding(paddingLeft, 16, paddingRight, 16);

            TextView tvTitle = new TextView(getContext());
            tvTitle.setTextSize(14);
            tvTitle.setId(R.id.tv_title_id);
            tvTitle.setTextColor(getResources().getColor(R.color.color_187_187_187));
            LayoutParams tvTitleParams = new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            tvTitleParams.setMargins(12, 0, 12, 0);
            tvTitleParams.weight = 1;
            addView(tvTitle, tvTitleParams);

            ImageView checkedImg = new ImageView(getContext());
            checkedImg.setId(R.id.img_checked_id);
            LayoutParams checkedImgParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            addView(checkedImg, checkedImgParams);
        }

        public ImageView getCheckedImgView() {
            return (ImageView) findViewById(R.id.img_checked_id);
        }

        public TextView getTitleTextView() {
            return (TextView) findViewById(R.id.tv_title_id);
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean isChecked) {
            this.isChecked = isChecked;
            getCheckedImgView().setVisibility(isChecked ? VISIBLE : INVISIBLE);
            getTitleTextView().setTextColor(isChecked ? getResources().getColor(R.color.color_119_119_119) : getResources().getColor(R.color.color_187_187_187));
        }
    }
}
