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

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.CommentBean;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

public class MorePopupWindow extends PopupWindow implements OnClickListener {
    private MorePopupWindowClickListener listener;
//    protected View viewG;
//    protected LinearLayout popLayout;
    protected Activity context;
//    protected Button btnSecond, btnThird;
    protected BasicUser basicUser;
    protected CommentBean cBean;
    protected int type;
    public interface MorePopupWindowClickListener {
        public void onFirstBtnClicked();
        public void onSecondBtnClicked();
        public void onThirdBtnClicked();
        public void onFourthBtnClicked();
        public void onCancelBtnClicked();
    }

    public MorePopupWindow(Activity context,MorePopupWindowClickListener listener, int type) {
        super(context);
        setContentView(LayoutInflater.from(context).inflate(R.layout.popupview_more, null));
        this.listener = listener;
        this.context = context;
        this.type = type;
//        initView(morePopup, type);
        // setWindowLayoutMode(LayoutParams.MATCH_PARENT,
        // LayoutParams.MATCH_PARENT);
        setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.MATCH_PARENT);

        //设置弹出窗体需要软键盘，
        this.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
        //再设置模式，和Activity的一样，覆盖，调整大小。
        this.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        // //设置SelectPicPopupWindow弹出窗体可点击
        // this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        // 实例化一个ColorDrawable颜色为半透明
        // ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        setAnimationStyle(R.style.PopupWindowAnimation);
        this.setBackgroundDrawable(null);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        getContentView().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = getMoreMenuView().getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        getContentView().setFocusableInTouchMode(true); // 设置view能够接听事件 标注2
        getContentView().setOnKeyListener(new OnKeyListener() {
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
    }

    public void setBasicUser(BasicUser basicUser) {
        this.basicUser = basicUser;
    }

    public void setcBean(CommentBean cBean) {
        this.cBean = cBean;
    }

    public void  initView(PostBean postBean) {
//        Button btnFirst = (Button) morePopup.findViewById(R.id.btnFirst);
//        btnSecond = (Button) morePopup.findViewById(R.id.btnSecond);
//        btnThird = (Button) morePopup.findViewById(R.id.btnThird);
//        Button btnFourth = (Button)morePopup.findViewById(R.id.btnFourth);
//        Button btnCancel = (Button) morePopup.findViewById(R.id.btnCancel);
//        View popupFirstLine = morePopup.findViewById(R.id.popupFirstLine);
//        View popupSecondLine = morePopup.findViewById(R.id.popupSecondLine);
//        getMoreMenuView();

//        viewG = getContentView().findViewById(R.id.view_bg);

        getCancelBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_cancel"));

        getFirstBtn().setOnClickListener(this);
        getSecondBtn().setOnClickListener(this);
        getThirdBtn().setOnClickListener(this);
        getFourthBtn().setOnClickListener(this);
        getCancelBtn().setOnClickListener(this);
        if(type == Constants.MORE_POPUPWINDOW_MYPOST){
            getFirstBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));
            getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_edit"));
            if(postBean.getAttachDataType() != Constants.POST_ATTACH_DATA_TYPE.ONLY_TEXT.GetValue()
                    && postBean.getIsPrivate() != 2 && postBean.getIsAnonymity() != 1) {
                getThirdBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_sharetofb"));
                getFourthBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_sharetoig"));
                getThirdBtn().setVisibility(View.VISIBLE);
                getFourthBtn().setVisibility(View.VISIBLE);
                getPopupThirdLine().setVisibility(View.VISIBLE);
            }
            getSecondBtn().setVisibility(View.VISIBLE);
            getPopupSecondLine().setVisibility(View.VISIBLE);
        }else if (type == Constants.MORE_POPUPWINDOW_OTHERPOST){
            if(postBean.getIsAnonymity() == 0) {
                getFirstBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_blockpost"));
                getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_block"));
                if (postBean.getOwner().getIsRecvHisNotification()) {
                    getThirdBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_notificationoff"));
                } else {
                    getThirdBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_notificationon"));
                }
                getSecondBtn().setVisibility(View.VISIBLE);
                getThirdBtn().setVisibility(View.VISIBLE);
                getPopupSecondLine().setVisibility(View.VISIBLE);
                getPopupThirdLine().setVisibility(View.VISIBLE);
            }else {
                getFirstBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_blockforanonymouspost"));
                getSecondBtn().setVisibility(View.GONE);
            }
            getFourthBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_report"));
            getFourthBtn().setVisibility(View.VISIBLE);

        } else if (type == Constants.MORE_POPUPWINDOW_UNFOLLOWREQUEST) {
            getFirstBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_unfollow"));

            getSecondBtn().setVisibility(View.GONE);
            getThirdBtn().setVisibility(View.GONE);

            getFifthBtn().setVisibility(View.GONE);
            getSecondBtn().setVisibility(View.GONE);
        } else if(type == Constants.MORE_POPUPWINDOW_OTHERUSER_MORE){

            getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_block"));
            getThirdBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_report"));
            if(basicUser.getIsRecvHisNotification()){
                getFourthBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_notificationoff"));
            }else {
                getFourthBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_notificationon"));
            }
            getFirstBtn().setVisibility(View.VISIBLE);
            if(basicUser.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                getFirstBtn().setText(ServerDataManager.getTextFromKey("usrprfl_btn_editprofile"));
                getSecondBtn().setVisibility(View.GONE);
                getThirdBtn().setVisibility(View.GONE);
                getFourthBtn().setVisibility(View.GONE);
                getPopupFirstLine().setVisibility(View.GONE);
                getPopupSecondLine().setVisibility(View.GONE);
                getPopupThirdLine().setVisibility(View.GONE);
            }else{
                getFirstBtn().setText(ServerDataManager.getTextFromKey("usrprfl_btn_editname"));
                getSecondBtn().setVisibility(View.VISIBLE);
                getThirdBtn().setVisibility(View.VISIBLE);
                if(basicUser.getFollowStatus()==1) {
                    getFourthBtn().setVisibility(View.VISIBLE);
                }else{
                    getFourthBtn().setVisibility(View.GONE);
                }
                getPopupSecondLine().setVisibility(View.VISIBLE);
                getPopupThirdLine().setVisibility(View.VISIBLE);
            }
        } else if (type == Constants.MORE_POPUPWINDOW_COMMENT) {
            getFirstBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));
            getSecondBtn().setVisibility(View.GONE);
            getThirdBtn().setVisibility(View.VISIBLE);
            getThirdBtn().setText(ServerDataManager.getTextFromKey("cmmnt_btn_tryagain"));
            getPopupSecondLine().setVisibility(View.GONE);
        } else if (type == Constants.MORE_POPUPWINDOW_MENU){
            getFirstBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_edit"));
            getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));
            getSecondBtn().setVisibility(View.VISIBLE);
            if (DataManager.getInstance().getCurTopic().isMyDefaultmenu()) {
                getSecondBtn().setVisibility(View.GONE);
                getPopupFirstLine().setVisibility(View.GONE);
            }
            getThirdBtn().setVisibility(View.GONE);
            getPopupSecondLine().setVisibility(View.VISIBLE);
        }else if(type == Constants.MORE_POPUPWINDOW_UNLINK){
            getFirstBtn().setText(ServerDataManager.getTextFromKey("likd_sttngs_btn_unlink"));
            getSecondBtn().setVisibility(View.GONE);
            getThirdBtn().setVisibility(View.GONE);
            getPopupFirstLine().setVisibility(View.GONE);
            getPopupSecondLine().setVisibility(View.GONE);
        }else if(type == Constants.MORE_POPUPWINDOW_COMMENT_OPERATE){
            getFirstBtn().setText(ServerDataManager.getTextFromKey("cmmnt_btn_reply"));
            getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_report"));
            getThirdBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));
            if (DataManager.getInstance().getCurPostBean().getOwner().getUserId()
                    .equals(DataManager.getInstance().getBasicCurUser().getUserId())) {
                getFirstBtn().setVisibility(View.VISIBLE);
                if (!cBean.getOwner().getUserId().equals(DataManager.getInstance().getBasicCurUser().getUserId())) {
                    getSecondBtn().setVisibility(View.VISIBLE);
                    getPopupSecondLine().setVisibility(View.VISIBLE);
                } else {
                    getSecondBtn().setVisibility(View.GONE);
                    getPopupSecondLine().setVisibility(View.GONE);
                }
                getThirdBtn().setVisibility(View.VISIBLE);
            } else {
                getFirstBtn().setVisibility(View.VISIBLE);
                if (cBean.getOwner().getUserId().equals(DataManager.getInstance().getBasicCurUser().getUserId())) {
                    getSecondBtn().setVisibility(View.GONE);
                    getPopupSecondLine().setVisibility(View.GONE);
                    getThirdBtn().setVisibility(View.VISIBLE);
                } else {
                    getSecondBtn().setVisibility(View.VISIBLE);
                    getPopupSecondLine().setVisibility(View.GONE);
                    getThirdBtn().setVisibility(View.GONE);
                }

            }
        } else if (type == Constants.MORE_POPUPWINDOW_FRIEND_GROUP_OPERATE) {
            getFirstBtn().setText(ServerDataManager.getTextFromKey("chsfrnd_btn_deletegroup"));
            getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_edit"));

            getFirstBtn().setVisibility(View.VISIBLE);
            getSecondBtn().setVisibility(View.VISIBLE);
            getThirdBtn().setVisibility(View.GONE);
            getFourthBtn().setVisibility(View.GONE);
            getFifthBtn().setVisibility(View.GONE);

        } else if(type==Constants.MORE_POPUPWINDOW_HANDLE_FAILED_POST){
            getFirstBtn().setText(ServerDataManager.getTextFromKey("hm_btn_uploadagain"));
            getSecondBtn().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));

            getFirstBtn().setVisibility(View.VISIBLE);
            getSecondBtn().setVisibility(View.VISIBLE);
            getThirdBtn().setVisibility(View.GONE);
            getFourthBtn().setVisibility(View.GONE);
            getFifthBtn().setVisibility(View.GONE);
        }

    }
    public Button getFirstBtn(){
        return (Button)getContentView().findViewById(R.id.btnFirst);
    }

    public Button getSecondBtn(){
        return (Button)getContentView().findViewById(R.id.btnSecond);
    }

    public Button getThirdBtn(){
        return (Button)getContentView().findViewById(R.id.btnThird);
    }

    public Button getFourthBtn(){
        return (Button)getContentView().findViewById(R.id.btnFourth);
    }

    public Button getCancelBtn(){
        return (Button) getContentView().findViewById(R.id.btnCancel);
    }

    public Button getFifthBtn(){
        return (Button)getContentView().findViewById(R.id.btnFifth);
    }

    public View getViewG(){
        return getContentView().findViewById(R.id.view_bg);
    }

    public View getPopupFirstLine(){
        return getContentView().findViewById(R.id.popupFirstLine);
    }

    public View getPopupSecondLine(){
        return getContentView().findViewById(R.id.popupSecondLine);
    }

    public View getPopupThirdLine(){
        return getContentView().findViewById(R.id.popupThirdLine);
    }

    public View getPopupFourthLine(){
        return getContentView().findViewById(R.id.popupFourthLine);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        viewShowLocation();
    }

    protected void viewShowLocation() {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(getViewG(), "alpha",0.5f).setDuration(250);
        fadeAnim.setStartDelay(250);
        fadeAnim.start();
    }
    @Override
    public void update() {
        super.update();
    }

    private LinearLayout getMoreMenuView() {
        return (LinearLayout)getContentView().findViewById(R.id.pop_layout);
    }


    @Override
    public void dismiss() {
        ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(getViewG(), "alpha",0f).setDuration(250);
        fadeAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
            public void onAnimationEnd(Animator animation) {
                MorePopupWindow.super.dismiss();
            }
        });
        fadeAnim.start();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        dismiss();
        switch (v.getId()) {
            case R.id.btnFirst:
                if (listener != null) {
                    listener.onFirstBtnClicked();
                }
                break;
            case R.id.btnSecond:
                if (listener != null) {
                    listener.onSecondBtnClicked();
                }
                break;
            case R.id.btnThird:
                if (listener != null) {
                    listener.onThirdBtnClicked();
                }
                break;
            case R.id.btnFourth:
                if(listener != null){
                    listener.onFourthBtnClicked();
                }
                break;
            case R.id.btnCancel:
                if (listener != null) {
                    listener.onCancelBtnClicked();
                }
                break;

            default:
                break;
        }

    }

}

