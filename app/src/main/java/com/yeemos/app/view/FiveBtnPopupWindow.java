package com.yeemos.app.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.R;

import static com.yeemos.app.R.id.btnCancel;
import static com.yeemos.app.R.id.btnFifth;
import static com.yeemos.app.R.id.btnFirst;
import static com.yeemos.app.R.id.btnFourth;
import static com.yeemos.app.R.id.btnSecond;
import static com.yeemos.app.R.id.btnThird;
import static com.yeemos.app.R.id.titleText;

public class FiveBtnPopupWindow extends PopupWindow implements OnClickListener{
    protected FiveBtnPopupWindowClickListener listener;
    protected Activity context;
    public interface FiveBtnPopupWindowClickListener {
        public void onFirstBtnClicked();
        public void onSecondBtnClicked();
        public void onThirdBtnClicked();
        public void onFourthBtnClicked();
        public void onFifthBtnClicked();
        public void onCancelBtnClicked();
    }
    public FiveBtnPopupWindow(Activity context,
                              FiveBtnPopupWindowClickListener listener) {
        // TODO Auto-generated constructor stub
        super(context);
        View morePopup = LayoutInflater.from(context).inflate(R.layout.popupview_more, null);
        setContentView(morePopup);
        this.listener = listener;
        this.context = context;
        // setWindowLayoutMode(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT);
        setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // //设置SelectPicPopupWindow弹出窗体可点击
        // this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        // 实例化一个ColorDrawable颜色为半透明
        // ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        setAnimationStyle(R.style.PopupWindowAnimation);
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        this.setBackgroundDrawable(null);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        morePopup.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = getPopLayout().getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        morePopup.setFocusableInTouchMode(true); // 设置view能够接听事件 标注2
        morePopup.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK) {
                    if (this != null) {
                        dismiss();
                    }
                }
                return false;
            }
        });

        getBtnFirst().setOnClickListener(this);
        getBtnSecond().setOnClickListener(this);
        getBtnThird().setOnClickListener(this);
        getBtnFourth().setOnClickListener(this);
        getBtnFifth().setOnClickListener(this);
        getBtnCancel().setOnClickListener(this);

    }

    @Override
    public void dismiss() {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(getViewG(), "alpha",0f);
        fadeAnim.setDuration(250);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
            public void onAnimationEnd(Animator animation) {
                FiveBtnPopupWindow.super.dismiss();
            }
        });
        fadeAnim.start();
    }
    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        viewShowLocation();
    }
    protected void viewShowLocation() {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(getViewG(), "alpha",0.75f).setDuration(250);
        fadeAnim.setStartDelay(250);
        fadeAnim.start();
    }
    @Override
    public void update() {
        super.update();
    }

    public void initView() {
        // TODO Auto-generated method stub

        getTitleText().setText(ServerDataManager.getTextFromKey("pblc_txt_whyreport"));
        getTitleText().setVisibility(View.VISIBLE);
        getBtnThird().setVisibility(View.VISIBLE);
        getBtnFourth().setVisibility(View.VISIBLE);
        getBtnFifth().setVisibility(View.VISIBLE);

        getTopLine().setVisibility(View.VISIBLE);
        getSecondLine().setVisibility(View.VISIBLE);
        getThirdLine().setVisibility(View.VISIBLE);
        getFourthLine().setVisibility(View.VISIBLE);

        getBtnFirst().setTextColor(context.getResources().getColor(R.color.popupView_btn_highLightRed));
        getBtnSecond().setTextColor(context.getResources().getColor(R.color.popupView_btn_highLightRed));
        getBtnThird().setTextColor(context.getResources().getColor(R.color.popupView_btn_highLightRed));
        getBtnFourth().setTextColor(context.getResources().getColor(R.color.popupView_btn_highLightRed));
        getBtnFifth().setTextColor(context.getResources().getColor(R.color.popupView_btn_highLightRed));

        getBtnFirst().setText(ServerDataManager.getTextFromKey("pblc_btn_porn"));
        getBtnSecond().setText(ServerDataManager.getTextFromKey("pblc_btn_scam"));
        getBtnThird().setText(ServerDataManager.getTextFromKey("pblc_btn_abuse"));
        getBtnFourth().setText(ServerDataManager.getTextFromKey("pblc_btn_commercialspam"));
        getBtnFifth().setText(ServerDataManager.getTextFromKey("pblc_btn_offensive"));
        getBtnCancel().setText(ServerDataManager.getTextFromKey("pblc_btn_cancel"));



    }

    public void setTitleText(String key){
        getTitleText().setText("Are you sure to report?");//ServerDataManager.getTextFromKey(key));
    }
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        // TODO Auto-generated method stub
        dismiss();
        switch (v.getId()) {
            case btnFirst:
                if (listener != null) {
                    listener.onFirstBtnClicked();
                }
                break;
            case btnSecond:
                if (listener != null) {
                    listener.onSecondBtnClicked();
                }
                break;
            case btnThird:
                if (listener != null) {
                    listener.onThirdBtnClicked();
                }
                break;
            case btnFourth:
                if (listener != null) {
                    listener.onFourthBtnClicked();
                }
                break;
            case btnFifth:
                if (listener != null) {
                    listener.onFifthBtnClicked();
                }
                break;
            case btnCancel:
                if (listener != null) {
                    listener.onCancelBtnClicked();
                }
                break;

            default:
                break;
        }


    }

//    private LinearLayout getMoreMenuView() {
//        if (popLayout == null && getContentView() != null) {
//            popLayout = (LinearLayout)getContentView().findViewById(R.id.pop_layout);
//        }
//        return popLayout;
//    }
    protected View getTopLine() {
        return getContentView().findViewById(R.id.popupTopLine);
    }
    protected View getFirstLine() {
        return getContentView().findViewById(R.id.popupFirstLine);
    }

    protected View getSecondLine() {
        return getContentView().findViewById(R.id.popupSecondLine);
    }
    protected View getThirdLine() {
        return getContentView().findViewById(R.id.popupThirdLine);
    }
    protected View getFourthLine() {
        return getContentView().findViewById(R.id.popupFourthLine);
    }
    protected View getViewG() {
        return getContentView().findViewById(R.id.view_bg);
    }

    protected LinearLayout getPopLayout() {
        return (LinearLayout) getContentView().findViewById(R.id.pop_layout);
    }

    protected Button getBtnFirst() {
        return (Button) getContentView().findViewById(btnFirst);
    }
    protected Button getBtnSecond() {
        return (Button) getContentView().findViewById(btnSecond);
    }
    protected Button getBtnThird() {
        return (Button) getContentView().findViewById(btnThird);
    }
    protected Button getBtnFourth() {
        return (Button) getContentView().findViewById(btnFourth);
    }
    protected Button getBtnFifth() {
        return (Button) getContentView().findViewById(btnFifth);
    }
    protected Button getBtnCancel() {
        return (Button) getContentView().findViewById(btnCancel);
    }
    protected TextView getTitleText() {
        return (TextView) getContentView().findViewById(titleText);
    }

}

