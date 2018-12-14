package com.yeemos.app.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.AuthUserDtoBean;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-14.
 */
public class LinkedAccountFragment extends BaseFragment implements View.OnClickListener {

    private Dialog dialog;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.rlLinkedFb).setOnClickListener(this);
        view.findViewById(R.id.rlLinkedInsta).setOnClickListener(this);
        uiConfig();
    }

    private void uiConfig() {
        ArrayList<AuthUserDtoBean> authBean = MemberShipManager.getInstance()
                .getUserInfo().getAuthUsers();
        String fbName = null;
        String insName = null;
        for (AuthUserDtoBean authUserDtoBean : authBean) {
            if (authUserDtoBean.getPartyType() == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook
                    .GetValues()) {
                fbName = authUserDtoBean.getAuthUserName();
            } else if (authUserDtoBean.getPartyType() == GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram
                    .GetValues()) {
                insName = authUserDtoBean.getAuthUserName();
            }
        }
        setFbAccName(fbName);
        setInsAccName(insName);
    }

    private void setFbAccName(String name) {
        if (name == null) {
            getFbButton().setSelected(false);
            ((TextView) getView().findViewById(R.id.tvFBUserName)).setText("");
        } else {
            getFbButton().setSelected(true);
            ((TextView) getView().findViewById(R.id.tvFBUserName)).setText(name);
        }
    }

    private void setInsAccName(String name) {
        if (name == null) {
            getInsButton().setSelected(false);
            ((TextView) getView().findViewById(R.id.tvInstaUserName)).setText("");
        } else {
            getInsButton().setSelected(true);
            ((TextView) getView().findViewById(R.id.tvInstaUserName)).setText(name);
        }
    }

    private ImageButton getFbButton() {
        return (ImageButton) getView().findViewById(R.id.btnLinkedFb);
    }

    private ImageButton getInsButton() {
        return (ImageButton) getView().findViewById(R.id.btnLinkedInsta);
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {

        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.tvTitle, "likd_sttngs_ttl_linkedsettings");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_linked_account;
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
            case R.id.rlLinkedFb:
                if (!getFbButton().isSelected()) {
                    bind(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook);
                } else {
                    unbind(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook);
                }
                break;
            case R.id.rlLinkedInsta:
                if (!getInsButton().isSelected()) {
                    bind(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram);
                } else
                    unbind(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram);
                break;
        }
    }

    private void bind(final GBMemberShip_V2.MemberShipThirdPartyType type) {
        MemberShipManager.getInstance().checkThirdPartyOperationVaild(
                getActivity(), type, new GBSMemberShipManager.memberShipCallBack<Object>() {
                    public void timeOut() {
                    }

                    public void success(Object obj) {
                        MemberShipManager.getInstance().bindByThirdParty(0,
                                type, new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {
                                    public void timeOut() {

                                    }

                                    public void success(GBUserInfo obj) {
                                        show(type);
                                    }

                                    public void fail(String errorStr) {
                                        errorCodeDo(errorStr);
                                        if (errorStr.equals(PlatformErrorKeys.CODE_THIRDPARTY_ALREADY_RELATION)) {
                                            //如果绑定过,就登出第三方
                                            logoutThirdParty(type);
                                        }
                                    }

                                    public void cancel() {

                                    }
                                });
                    }

                    public void fail(String errorStr) {
                        errorCodeDo(errorStr);
                    }

                    public void cancel() {
                    }
                });

    }

    private void show(final GBMemberShip_V2.MemberShipThirdPartyType type) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                uiConfig();
            }
        });
    }

    private void logoutThirdParty(GBMemberShip_V2.MemberShipThirdPartyType type) {
        switch (type) {
            case MemberShip_ThirdParty_Facebook:
                MemberShipManager.getInstance().getFacebook(getActivity()).logout(null);
                break;
        }
    }

    private void unbind(final GBMemberShip_V2.MemberShipThirdPartyType type) {
        MemberShipManager.getInstance().checkThirdPartyOperationVaild(
                getActivity(), type, new GBSMemberShipManager.memberShipCallBack<Object>() {
                    public void timeOut() {
                    }

                    public void success(Object obj) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                MorePopupWindow popUpWindow = new MorePopupWindow(
                                        getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
                                    public void onThirdBtnClicked() {
                                    }

                                    @Override
                                    public void onCancelBtnClicked() {

                                    }

                                    public void onSecondBtnClicked() {
                                    }

                                    @Override
                                    public void onFourthBtnClicked() {

                                    }

                                    public void onFirstBtnClicked() {
                                        MemberShipManager.getInstance().unbindByThirdParty(
                                                type, new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {
                                                    public void timeOut() {

                                                    }

                                                    public void success(GBUserInfo obj) {
                                                        logoutThirdParty(type);
                                                        show(type);
                                                    }

                                                    public void fail(String errorStr) {
                                                        if(errorStr.equals(PlatformErrorKeys.CODE_HAVE_NO_PASSWORD)){
                                                            String content = ServerDataManager.getTextFromKey(errorStr);
                                                            String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                                                            String canel = ServerDataManager.getTextFromKey("pblc_btn_cancel");
                                                            showPublicDialog(null, content, canel, Okey, errorHandler);
                                                        }else {
                                                            errorCodeDo(errorStr);
                                                        }

                                                    }

                                                    public void cancel() {

                                                    }
                                                });
                                    }
                                }, Constants.MORE_POPUPWINDOW_UNLINK);

                                popUpWindow.initView(null);
                                popUpWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                            }
                        });

                    }

                    @Override
                    public void fail(String errorStr) {
                    }

                    @Override
                    public void cancel() {
                    }
                });

    }
    Handler errorHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if(dialog != null){
                        dialog.dismiss();
                    }
                    gotoPager(ChangePasswordFragment.class, null);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
}
