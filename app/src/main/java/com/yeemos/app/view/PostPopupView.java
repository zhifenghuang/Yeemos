package com.yeemos.app.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;

/**
 * Created by gigabud on 15-12-25.
 */
public class PostPopupView extends ViewGroup {

    private Context mContext;
    private int resId;

    public interface OnPopupItemClick {
        public void popupItemClick(int popupType, int clickNo);
    }

    public static final int POST_PUPUP_EMOS = 0;   //显示表情的PopupView
    public static final int POST_PUPUP_PRIVACY = 1;   //显示隐私的PopupView

    private int mPopViewType = POST_PUPUP_EMOS;

    public static final int[] POST_PRIVACY_IDS = {   //关于隐私的资源ID
            R.drawable.public_selected,
            R.drawable.friend_unselected,
            R.drawable.custom_unselected,
            R.drawable.stranger_unselected
    };
    public static final String[] POST_PRIVACY_STRING_IDS = {
            ServerDataManager.getTextFromKey("edtpst_btn_public"),
            ServerDataManager.getTextFromKey("edtpst_btn_friendonly"),
            ServerDataManager.getTextFromKey("edtpst_btn_custom"),
            ServerDataManager.getTextFromKey("edtpst_btn_strangeronly")
    };
    private OnPopupItemClick mOnPopupItemClick;


    public PostPopupView(Context context, int popViewType, int selectId, int resId) {
        super(context);
        mContext = context;
        mPopViewType = popViewType;
        this.resId = resId;
        addChilds(selectId);
    }

    public PostPopupView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    private void setPostPrivacyIds(int i) {
        switch (i) {
            case 0:
                POST_PRIVACY_IDS[0] = R.drawable.public_selected;
                POST_PRIVACY_IDS[1] = R.drawable.friend_unselected;
                POST_PRIVACY_IDS[2] = R.drawable.custom_unselected;
                POST_PRIVACY_IDS[3] = R.drawable.stranger_unselected;
                break;
            case 1:
                POST_PRIVACY_IDS[0] = R.drawable.public_unselected;
                POST_PRIVACY_IDS[1] = R.drawable.friend_selected;
                POST_PRIVACY_IDS[2] = R.drawable.custom_unselected;
                POST_PRIVACY_IDS[3] = R.drawable.stranger_unselected;
                break;
            case 2:
                POST_PRIVACY_IDS[0] = R.drawable.public_unselected;
                POST_PRIVACY_IDS[1] = R.drawable.friend_unselected;
                POST_PRIVACY_IDS[2] = R.drawable.custom_selected;
                POST_PRIVACY_IDS[3] = R.drawable.stranger_unselected;
                break;
            case 3:
                POST_PRIVACY_IDS[0] = R.drawable.public_unselected;
                POST_PRIVACY_IDS[1] = R.drawable.friend_unselected;
                POST_PRIVACY_IDS[2] = R.drawable.custom_unselected;
                POST_PRIVACY_IDS[3] = R.drawable.stranger_selected;
                break;
            default:
                POST_PRIVACY_IDS[0] = R.drawable.public_selected;
                POST_PRIVACY_IDS[1] = R.drawable.friend_unselected;
                POST_PRIVACY_IDS[2] = R.drawable.custom_unselected;
                POST_PRIVACY_IDS[3] = R.drawable.stranger_unselected;
                break;
        }
    }


    public PostPopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        if (attrs != null) {
            TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.PostPopupView);
            mPopViewType = array.getInteger(R.styleable.PostPopupView_popup_view_type, 1);
            array.recycle();
        }
        addChilds(0);
    }

    public void setOnPopupItemClick(OnPopupItemClick onPopupItemClick) {
        mOnPopupItemClick = onPopupItemClick;
    }


    /**
     * 获取高和宽
     *
     * @return
     */
    public int[] getViewSize() {

        int width = ((BaseActivity) mContext).getDisplaymetrics().widthPixels;
        int height;
        Bitmap bmp;
        if (mPopViewType == POST_PUPUP_EMOS) {
            bmp = BitmapFactory.decodeResource(getResources(), resId);
            height = bmp.getHeight();
        } else {
            resId = R.drawable.post_bg_privacy;
            bmp = BitmapFactory.decodeResource(getResources(), resId);
            height = bmp.getHeight();
        }
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), resId);
//        int width = bmp.getWidth();
//        int height = bmp.getHeight();
        return new int[]{width, height};
    }

    private void addChilds(int selectId) {
        ImageView iv = new ImageView(getContext());
        if (mPopViewType == POST_PUPUP_EMOS) {
            iv.setImageResource(resId);
            addView(iv);
            int length = Constants.EMO_ID_COLOR.length;
            for (int i = 0; i < length; ++i) {
                iv = new ImageView(getContext());
                iv.setImageResource(Constants.EMO_ID_COLOR[i][0]);
                addView(iv);
            }
        } else {
            iv.setImageResource(R.drawable.post_bg_privacy);
            addView(iv);
            int length = POST_PRIVACY_IDS.length;
            setPostPrivacyIds(selectId);
            for (int i = 0; i < length; ++i) {
//                iv = new ImageView(getContext());
//                iv.setImageResource(POST_PRIVACY_IDS[i]);
                Drawable drawable = getResources().getDrawable(POST_PRIVACY_IDS[i]);
                TextView tv = new TextView(getContext());
                tv.setText(POST_PRIVACY_STRING_IDS[i]);
                tv.setTextSize(11);
                if (i == selectId) {
                    tv.setTextColor(getResources().getColor(R.color.color_45_223_227));
                } else {
                    tv.setTextColor(getResources().getColor(R.color.color_255_255_255));
                }
                tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                tv.setGravity(Gravity.CENTER_HORIZONTAL);
                addView(tv);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int viewWidth = r - l - Utils.dip2px(getContext(), 15);
        View v = getChildAt(0);
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        int childCount = getChildCount();
        float perWidth = viewWidth * 1.0f / (childCount - 1);
        int startX = 0;
        int startY;
        for (int i = 1; i < childCount; ++i) {
            v = getChildAt(i);
            startX = (int) (perWidth * i - (v.getMeasuredWidth() + perWidth) * 0.5f) + 15;
            v.layout(startX, v.getMeasuredHeight() / 3, startX + v.getMeasuredWidth(), 4 * v.getMeasuredHeight() / 3);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ImageView iv = (ImageView) getChildAt(0);
        int[] wh = getViewSize();
        int width = wh[0];
        int height = wh[1];
        setMeasuredDimension(width, height);
        measureChild(iv, MeasureSpec.makeMeasureSpec(width,
                MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height,
                MeasureSpec.EXACTLY));
        int childCount = getChildCount();
        Drawable drawable;
        if (mPopViewType == POST_PUPUP_EMOS) {
            drawable = ((ImageView) getChildAt(1)).getDrawable();
        } else {
            drawable = ((TextView) getChildAt(1)).getCompoundDrawables()[1];
        }
        width = drawable.getBounds().width();
        height = drawable.getBounds().height();
        if (mPopViewType == POST_PUPUP_EMOS) {
            width = (int) (Utils.dip2px(getContext(), 54) * 0.8f + 0.5f);
            height = (int) (Utils.dip2px(getContext(), 72) * 0.8f + 0.5f);
        } else {
            width += Utils.dip2px(getContext(), 33);
            height += Utils.dip2px(getContext(), 20);
        }
        View v;
        for (int i = 1; i < childCount; ++i) {
            v = getChildAt(i);
            int parentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            int parentHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

            measureChild(v, parentWidthMeasureSpec, parentHeightMeasureSpec);
            v.setTag(i - 1);
            v.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mOnPopupItemClick != null) {
                        mOnPopupItemClick.popupItemClick(mPopViewType, (int) v.getTag());
                        if (mPopViewType != POST_PUPUP_EMOS) {
                            setPostPrivacyIds((int) v.getTag());
                        }
                    }
                }
            });
        }
    }
}
