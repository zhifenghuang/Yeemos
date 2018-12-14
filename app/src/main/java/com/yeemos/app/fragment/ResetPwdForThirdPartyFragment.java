package com.yeemos.app.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBPlatform;
import com.gigabud.common.platforms.GBUserInfo;
import com.google.gson.Gson;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-7-13.
 */
public class ResetPwdForThirdPartyFragment extends BaseFragment implements View.OnClickListener{

    private Dialog dialog;
    private GBMemberShip_V2.MemberShipThirdPartyType type = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_reset_password_third_party;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String userinfoStr = bundle.getString("thirdPartyUser");
            GBUserInfo userinfo = new Gson().fromJson(userinfoStr, GBUserInfo.class);
//            getImageView().setDefaultImageResId(R.drawable.default_avater);
//            getImageView().setImageUrl(Preferences.getAvatarUrl(userinfo.getAvatarURL()));

            Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(userinfo.getAvatarURL()),getImageView());
            getWelcomeBackText().setText(String.format(ServerDataManager.getTextFromKey("rst_psswrd_usngfb_txt_content"),userinfo.getUserName()));
            if (userinfo.getEnPlatFormType() == GBPlatform.PLATFORM_TYPE.EN_INSTAGRAM_PLATFORM) {
                type = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram;
            }
        }

        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btnSearch).setOnClickListener(this);
        ((Button)view.findViewById(R.id.btnSearch)).setText(ServerDataManager.getTextFromKey("rst_psswrd_usngfb_btn_reset"));
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
    private EditText getFirstEditText(){
        return (EditText) getView().findViewById(R.id.firstSet);
    }
    private EditText getSecondEditText(){
        return (EditText) getView().findViewById(R.id.secondSet);
    }
    @Override
    public void updateUIText() {
        setOnlineText(R.id.top_title,"rst_psswrd_usngfb_ttl_resetpassword");
        setOnlineText(R.id.inputPwdText,"rst_psswrd_usngfb_txt_inputtwice");
        setOnlineText(R.id.firstSet,"rst_psswrd_usngfb_txt_newpassword");
        setOnlineText(R.id.secondSet,"rst_psswrd_usngfb_txt_newpasswordagain");
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
                if(!getFirstEditText().getText().toString().equals(getSecondEditText().getText().toString())){
                    String content = ServerDataManager.getTextFromKey("rst_psswrd_usngFB_txt_passwordsnotmatch");
                    String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                    showPublicDialog(null, content, null, Okey, errorHandler);
                }else {
                    if (!verify()) {
                        return;
                    }
                    showLoadingDialog(null, null, false);
                    MemberShipManager.getInstance().changePasswordByThirdParty(type,
                            getFirstEditText().getText().toString(),
                            new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {
                                public void timeOut() {
                                    hideLoadingDialog();
                                }

                                public void success(GBUserInfo obj) {
                                    showPublicDialog(null,
                                            ServerDataManager.getTextFromKey("chng_psswrd_txt_passwordchangedsuccessful"),
                                            ServerDataManager.getTextFromKey("pub_btn_ok"),
                                            null, new Handler(new Handler.Callback() {
                                                public boolean handleMessage(Message msg) {
                                                    hideLoadingDialog();
                                                    //上个页面第三方登陆,需要登出清数据
                                                    goBack();
                                                    return false;
                                        }
                                    }));

                                }

                                public void fail(String errorStr) {
                                    hideLoadingDialog();
                                    errorCodeDo(errorStr);
                                }

                                public void cancel() {
                                    hideLoadingDialog();
                                }
                            });
                }
                break;
        }
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
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private boolean verify() {
        if (!MemberShipManager.isValidPsw(getFirstEditText().getText().toString())) {
            MemberShipManager.showInVaildPswTipDialog((BaseActivity) getActivity());
            return false;
        }
        return true;
    }

    public RoundedImageView getImageView() {
        return (RoundedImageView)getView().findViewById(R.id.ivAvater);
    }

    private TextView getWelcomeBackText(){
        return (TextView)getView().findViewById(R.id.welcomeBack);
    }
}
