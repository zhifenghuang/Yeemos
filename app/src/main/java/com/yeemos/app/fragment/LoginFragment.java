package com.yeemos.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;

/**
 * Created by gigabud on 15-12-8.
 */
public class LoginFragment extends BaseFragment implements View.OnClickListener {

    private GBMemberShip_V2.MemberShipUserType userType;
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        view.findViewById(R.id.tvForgetPsw).setOnClickListener(this);
        ((EditText)view.findViewById(R.id.et_email)).setHint(ServerDataManager.getTextFromKey("lgn_txt_email"));
        ((EditText)view.findViewById(R.id.et_psw)).setHint(ServerDataManager.getTextFromKey("lgn_txt_password"));
        ((TextView)view.findViewById(R.id.tvForgetPsw)).setText(ServerDataManager.getTextFromKey("lgn_btn_forgotpassword"));
        ((Button)view.findViewById(R.id.btn_ok)).setText(ServerDataManager.getTextFromKey("lgn_btn_login"));
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
        setOnlineText(R.id.top_title,"lgn_ttl_login");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    private String getPassword() {
        return ((EditText) getView().findViewById(R.id.et_psw)).getText().toString();
    }

    private String getEmail() {
        return ((EditText) getView().findViewById(R.id.et_email)).getText().toString();
    }

    private boolean verify() {
        if (getEmail().isEmpty()) {
            showPublicDialog(null, ServerDataManager.getTextFromKey("lgn_txt_pleaseenteremailorusername"),ServerDataManager.getTextFromKey("pub_btn_ok"),null,null);
            return false;
        }
        if (getEmail().contains("@")) {
            userType = GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_Email;
            if (!MemberShipManager.isVaildEmail(getEmail())) {
                MemberShipManager.showInVaildEmailTipDialog((BaseActivity) getActivity());
                return false;
            }
        }else {
            userType = GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_UserName;
            if (!MemberShipManager.isValidUsername(getEmail())) {
                MemberShipManager.showInVaildUsernameTipDialog((BaseActivity) getActivity());
                return  false;
            }
        }

        if (!MemberShipManager.isValidPsw(getPassword())) {
            MemberShipManager.showInVaildPswTipDialog((BaseActivity) getActivity());
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                goBack();
                break;
            case R.id.btn_ok:
                if (!verify()) {
                    return;
                }
                showLoadingDialog(null, null, true);
                MemberShipManager.getInstance().login(userType,
                        getEmail(),
                        getPassword(),
                        new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {

                            @Override
                            public void timeOut() {
                                hideLoadingDialog();
                            }

                            @Override
                            public void success(GBUserInfo obj) {
                                Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                                Intent intent=new Intent(getActivity(),HomeActivity.class);
                                startActivity(intent);
                                getActivity().finish();
                            }

                            @Override
                            public void fail(String errorStr) {
                                hideLoadingDialog();
//                                if(errorStr.equals(PlatformErrorKeys.CODE_USER__NOT_EXIST)
//                                        || errorStr.equals(PlatformErrorKeys.CODE_USER_PASSWORD_NOT_MATCH)){
//                                    errorStr = PlatformErrorKeys.USER_PASSWORD_IS_INCORRECT;
//                                }
                                errorCodeDo(errorStr);
                            }

                            @Override
                            public void cancel() {
                                hideLoadingDialog();
                            }
                        });
                break;
            case R.id.tvForgetPsw:
                gotoPager(ChooseMethodFragment.class, null);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
    }
}
