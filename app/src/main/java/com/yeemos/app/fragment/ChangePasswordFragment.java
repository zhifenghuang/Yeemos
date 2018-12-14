package com.yeemos.app.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-6-14.
 */
public class ChangePasswordFragment extends BaseFragment implements View.OnClickListener {

    private Dialog dialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.tvDone).setOnClickListener(this);
        if(MemberShipManager.getInstance().hasBindThirdParty() && MemberShipManager.getInstance().getUserInfo().getHasPW() == 0){
            getOldTextEditText().setVisibility(View.GONE);
            getNewAgainTextEditText().setVisibility(View.GONE);
        }
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {

        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvTitle,"chng_psswrd_ttl_changepassword");
        setOnlineText(R.id.tvDone,"pblc_btn_done");
        setOnlineText(R.id.etCurrentPassword,"chng_psswrd_txt_currentpassword");
        setOnlineText(R.id.etNewPassword,"chng_psswrd_txt_newpassword");
        setOnlineText(R.id.etRepeatNewPassword,"chng_psswrd_txt_newpasswordagain");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_change_password;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.tvDone:
                String oldPsw = null;
                String newPsw1 = getNewTextEditText().getText().toString();
                showLoadingDialog("", null, true);
                if(MemberShipManager.getInstance().hasBindThirdParty() && MemberShipManager.getInstance().getUserInfo().getHasPW() == 0){
                    if (!MemberShipManager.isValidPsw(newPsw1)) {
                        showPwdEmptyDialog();
                        return;
                    }
                    MemberShipManager.getInstance().thirdPartyChangePassWord(newPsw1, new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {
                        @Override
                        public void success(GBUserInfo obj) {
//                            showToast(ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordchangedsuccessfully"));
//                            goBack();
                            hideLoadingDialog();
                            String content = ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordchangedsuccessfully");
                            String ok = ServerDataManager.getTextFromKey("pub_btn_ok");
                            showPublicDialog(null, content, ok, null, successDialoghandler);

                        }

                        @Override
                        public void timeOut() {
                            hideLoadingDialog();
                        }

                        @Override
                        public void fail(String errorStr) {
                            hideLoadingDialog();
                            errorCodeDo(errorStr);
                        }

                        @Override
                        public void cancel() {
                            hideLoadingDialog();
                        }
                    });
                }else {
                    oldPsw = getOldTextEditText().getText().toString();
                    String newPsw2 = getNewAgainTextEditText().getText().toString();
                    if(!MemberShipManager.isValidPsw(newPsw1) && !MemberShipManager.isValidPsw(newPsw2)){
                        showPwdEmptyDialog();
                        return;
                    }

                    if (!newPsw1.equals(newPsw2)) {
                        hideLoadingDialog();
                        String content = ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordnotmatch");
                        String ok = ServerDataManager.getTextFromKey("pub_btn_ok");
                        showPublicDialog(null, content, ok, null, errorDialoghandler);
                        return;
                    }
                    MemberShipManager.getInstance().updateCrediential(GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_Email,
                            MemberShipManager.getInstance().getEmail(),
                            oldPsw,
                            newPsw1,
                            null,
                            null,
                            2,
                            0,
                            new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {
                                @Override
                                public void success(
                                        GBUserInfo obj) {
                                    // TODO Auto-generated damethod stub
//                                    showToast(ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordchangedsuccessfully"));
//                                    goBack();
                                    hideLoadingDialog();
                                    String content = ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordchangedsuccessfully");
                                    String ok = ServerDataManager.getTextFromKey("pub_btn_ok");
                                    ((BaseActivity)getActivity()).showPublicDialog(null, content, ok, null, false, true, successDialoghandler);
                                }

                                @Override
                                public void timeOut() {
                                    // TODO Auto-generated method stub
                                    hideLoadingDialog();
                                }

                                @Override
                                public void fail(String errorStr) {
                                    // TODO Auto-generated method stub
                                    hideLoadingDialog();
                                    if(errorStr.equals(PlatformErrorKeys.CODE_USER_PASSWORD_FORMAT_ERROR)){
                                        errorStr = PlatformErrorKeys.CODE_PASSWORD_NOT_MATCH;
                                    }
                                    errorCodeDo(errorStr);
                                }

                                @Override
                                public void cancel() {
                                    // TODO Auto-generated method stub
                                    hideLoadingDialog();
                                }
                            });
                }
                break;
            default:
                break;
        }
    }
    private void showPwdEmptyDialog() {
        hideLoadingDialog();
        String content = ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordempty");
        String ok = ServerDataManager.getTextFromKey("pub_btn_ok");
        showPublicDialog(null, content, ok, null, errorDialoghandler);
    }

    private boolean verify() {
        if (!MemberShipManager.isValidPsw(getNewTextEditText().getText().toString())) {
            MemberShipManager.showInVaildPswTipDialog((BaseActivity) getActivity());
            return false;
        }
        return true;
    }

    private EditText getOldTextEditText() {
        return (EditText) getView().findViewById(R.id.etCurrentPassword);
    }

    private EditText getNewTextEditText() {
        return (EditText) getView().findViewById(R.id.etNewPassword);
    }

    private EditText getNewAgainTextEditText() {
        return (EditText) getView().findViewById(R.id.etRepeatNewPassword);
    }

    private void showToast(String toastStr) {
        hideLoadingDialog();
        Toast.makeText(getActivity(), toastStr, Toast.LENGTH_SHORT).show();
    }

    private Handler errorDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private Handler successDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
            goBack();
        }
    };
}
