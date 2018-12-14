package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.TimeCount;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-7-12.
 */
public class UsernameOrEmailMethodFragment extends BaseFragment implements View.OnClickListener{

    private TimeCount time;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_reset_password_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getEtEmail().setHint(ServerDataManager.getTextFromKey("rst_psswrd_eandu_txt_usernameoremail"));
        getButton().setText(ServerDataManager.getTextFromKey("rst_psswrd_eandu_btn_search"));
        buttonDisable();
        time = DataManager.getInstance().getTimeCount();
        if(time != null){
            time.setOnTimeCountListener(onTimeCountListener);
            time.start();
            getsentEmail().setVisibility(View.VISIBLE);
        }
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        getsentEmail().setVisibility(View.INVISIBLE);

        getEtEmail().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(DataManager.getInstance().getTimeCount() == null
                        && s != null && s.length()>0 ){
                    buttonEnable();
                }else{
                    buttonDisable();
                }
            }
        });
    }

    private void buttonDisable(){
        getButton().setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_roundrect_bg_color_187_187_187_whitetext));
        getButton().setClickable(false);
    }

    private void buttonEnable(){
        getButton().setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_roundrect_redbg_whitetext));
        getButton().setClickable(true);
        getButton().setOnClickListener(this);
    }

    private Button getButton(){
        return (Button)getView().findViewById(R.id.btnSearch);
    }

    private TextView getsentEmail(){
        return (TextView) getView().findViewById(R.id.sentEmail);
    }

    private EditText getEtEmail(){
        return (EditText)getView().findViewById(R.id.etEmail);
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.top_title,"rst_psswrd_eandu_ttl_resetpassword");
        setOnlineText(R.id.sentEmail,"rst_psswrd_eandu_txt_emailsent");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_back:
                goBack();
                break;
            case R.id.btnSearch:
                if (!verify()) {
                    return;
                }
                MemberShipManager.getInstance().findPwd(getEtEmail().getText().toString(),
                        GBMemberShip_V2.MemberShipInfoSendType.MemberShip_Send_Email,
                        getInputType(),
                        1,
                        new GBSMemberShipManager.memberShipCallBack<Object>() {
                            public void timeOut() {

                            }

                            public void success(Object obj) {
                                Preferences.getInstacne().setLeaveSendEmailPageTime();
                                time = new TimeCount(60000, 1000);//构造CountDownTimer对象
                                time.setOnTimeCountListener(onTimeCountListener);
                                time.start();
                                DataManager.getInstance().setTimeCount(time);
                                getsentEmail().setVisibility(View.VISIBLE);
                            }

                            public void fail(String errorStr) {
                                errorCodeDo(errorStr);
                            }

                            public void cancel() {

                            }
                        });
                break;
        }
    }

    private GBMemberShip_V2.MemberShipUserType getInputType() {
        if (getEtEmail().getText().toString().contains("@")) {
            return GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_Email;
        }
        return GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_UserName;
    }

    private boolean verify() {
        if (getInputType() == GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_Email
                && !MemberShipManager.isVaildEmail(getEtEmail().getText().toString())) {
            MemberShipManager.showInVaildEmailTipDialog((BaseActivity) getActivity());
            return false;
        }
        GBMemberShip_V2.MemberShipUserType tyep =  getInputType();
        boolean ref = MemberShipManager.isValidUsername(getEtEmail().getText().toString());
        if (getInputType() == GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_UserName
                && !MemberShipManager.isValidUsername(getEtEmail().getText().toString())) {
            MemberShipManager.showInVaildUsernameTipDialog((BaseActivity) getActivity());
            return false;
        }
        return true;
    }

    TimeCount.OnTimeCountListener onTimeCountListener = new TimeCount.OnTimeCountListener() {
        @Override
        public void timeCountOnTick(long millisUntilFinished) {
            if(getView() != null) {
                buttonDisable();
                getButton().setText(ServerDataManager.getTextFromKey("rst_psswrd_eandu_btn_search") + "(" + millisUntilFinished / 1000 + ")");
            }
            time = new TimeCount(millisUntilFinished, 1000);
            DataManager.getInstance().setTimeCount(time);
        }

        @Override
        public void timeCountOnFinish() {
            if(getView() != null) {
                if(getEtEmail().getText().length() > 0 ) {
                    buttonEnable();
                }
                getButton().setText(ServerDataManager.getTextFromKey("rst_psswrd_eandu_btn_search"));
            }
            DataManager.getInstance().setTimeCount(null);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
