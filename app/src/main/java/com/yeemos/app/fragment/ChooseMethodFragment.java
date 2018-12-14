package com.yeemos.app.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.platforms.GBPlatform;
import com.gigabud.common.platforms.GBUserInfo;
import com.google.gson.Gson;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.R;
import com.yeemos.app.utils.Constants.PHONE_FRAGMENT_UI_POSITION;

/**
 * Created by gigabud on 16-7-12.
 */
public class ChooseMethodFragment extends BaseFragment implements View.OnClickListener{

    private boolean isThridPartyRequestCancel = false;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_forgot_password_fragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.usernameReset).setOnClickListener(this);
        view.findViewById(R.id.fbReset).setOnClickListener(this);
        view.findViewById(R.id.igReset).setOnClickListener(this);
    }

    @Override
    public PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
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
        setOnlineText(R.id.top_title,"sgninhlp_ttl_signinhelp");
        setOnlineText(R.id.forgotText,"sgninhlp_txt_howreset");
        setOnlineText(R.id.usernameReset,"sgninhlp_btn_usernameoremail");
        setOnlineText(R.id.fbReset,"sgninhlp_btn_resetusingfb");
        setOnlineText(R.id.igReset,"sgninhlp_btn_resetusingig");
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
            case R.id.usernameReset:
                gotoPager(UsernameOrEmailMethodFragment.class, null);
                break;
            case R.id.fbReset:
                showLoadingDialog(null, null,true);
                MemberShipManager.getInstance().loginByFaceBook(getActivity(), new GBSMemberShipManager.memberShipThirdPartyCallBack<GBUserInfo>() {
                    public void timeOut() {
                        hideLoadingDialog();
                    }
                    public void success(GBUserInfo obj) {
                        hideLoadingDialog();
                        String objStr = null;
                        Bundle data = new Bundle();
                        if (obj != null) {
                            obj.setEnPlatFormType(GBPlatform.PLATFORM_TYPE.EN_FACEBOOK_PLATFORM);
                            objStr = new Gson().toJson(obj);
                            data.putString("thirdPartyUser",objStr);
                        }
                        gotoPager(ResetPwdForThirdPartyFragment.class, data);
                    }
                    public void fail(String errorStr) {
                        hideLoadingDialog();
                    }
                    public void cancel() {
                        hideLoadingDialog();
                    }
                    public void needToMatchDisplayName() {
                        hideLoadingDialog();
                        showPublicDialog(null, ServerDataManager.getTextFromKey("sgninhlp_txt_notlinkedemail"), ServerDataManager.getTextFromKey("pub_btn_ok"), null, null);
                    }
                });
                break;
            case R.id.igReset:
                showLoadingDialog(null, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        isThridPartyRequestCancel = true;
                    }
                }, true);
                MemberShipManager.getInstance().loginByInstagram(getActivity(), new GBSMemberShipManager.memberShipThirdPartyCallBack<GBUserInfo>() {
                    public void timeOut() {
                        hideLoadingDialog();
                        if (isThridPartyRequestCancel) {
                            isThridPartyRequestCancel = false;
                            return;
                        }
                    }
                    public void success(GBUserInfo obj) {
                        hideLoadingDialog();
                        if (isThridPartyRequestCancel) {
                            isThridPartyRequestCancel = false;
                            return;
                        }
                        String objStr = null;
                        Bundle data = new Bundle();
                        if (obj != null) {
                            obj.setEnPlatFormType(GBPlatform.PLATFORM_TYPE.EN_INSTAGRAM_PLATFORM);
                            objStr = new Gson().toJson(obj);
                            data.putString("thirdPartyUser",objStr);
                        }
                        gotoPager(ResetPwdForThirdPartyFragment.class, data);
                    }
                    public void fail(String errorStr) {
                        hideLoadingDialog();
                        if (isThridPartyRequestCancel) {
                            isThridPartyRequestCancel = false;
                            return;
                        }
                    }
                    public void cancel() {
                        hideLoadingDialog();
                        if (isThridPartyRequestCancel) {
                            isThridPartyRequestCancel = false;
                            return;
                        }
                    }
                    public void needToMatchDisplayName() {
                        hideLoadingDialog();
                        if (isThridPartyRequestCancel) {
                            isThridPartyRequestCancel = false;
                            return;
                        }
                        showPublicDialog(null, ServerDataManager.getTextFromKey("sgninhlp_txt_ignotlinkedemail"), ServerDataManager.getTextFromKey("pub_btn_ok"), null, null);
                    }
                });
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        hideLoadingDialog();
    }
}
