package com.yeemos.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.constants.GBSConstants;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.yeemos.app.R;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;

/**
 * Created by gigabud on 16-7-13.
 */
public class ThirdPartySignUpFragment extends SignUpFragment {
    private static GBMemberShip_V2.MemberShipThirdPartyType partyType = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_third_party_signup;
    }


    @Override
    public void initView(View view) {
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
//        view.findViewById(R.id.tvTermsOfService).setOnClickListener(this);
//        view.findViewById(R.id.tvPrivacyPolicy).setOnClickListener(this);
        ((EditText) view.findViewById(R.id.et_email)).setHint(ServerDataManager.getTextFromKey("sgn_up_txt_email"));
        ((EditText) view.findViewById(R.id.et_usrname)).setHint(ServerDataManager.getTextFromKey("sgn_up_txt_createusername"));
        if (GBSDataManager.getInstance().getThirdPartUser() != null) {
            ((EditText) view.findViewById(R.id.et_email)).setText(DataManager.getInstance().getThirdPartUser().getEmail());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                goBack();
                break;
            case R.id.btn_ok:
                loginButton();
                break;
//            case R.id.tvTermsOfService:
//                Bundle b = new Bundle();
//                b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_TERMS_OF_SERVICE);
//                gotoPager(TermsOrPrivacyFragment.class, b);
//                break;
//            case R.id.tvPrivacyPolicy:
//                b = new Bundle();
//                b.putInt(TermsOrPrivacyFragment.HTML_TYPE, TermsOrPrivacyFragment.HTML_PRIVACY_POLICY);
//                gotoPager(TermsOrPrivacyFragment.class, b);
//                break;
        }
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.top_title, "sgn_up_ttl_signup");
        setOnlineText(R.id.btn_ok, "pblc_btn_next");
//        setOnlineText(R.id.tvYouAgree, "sgn_up_txt_youagreetothe");
//        setOnlineText(R.id.tvTermsOfService, "sgn_up_txt_termsofservice");
//        setOnlineText(R.id.tvAnd, "sgn_up_txt_and");
//        setOnlineText(R.id.tvPrivacyPolicy, "sgn_up_txt_privacypolicy");
        setOnlineText(R.id.requiredItems, "sgn_up_txt_requireditems");

        TextView tvYouAgree = (TextView) getView().findViewById(R.id.tvYouAgree);
        String text = ServerDataManager.getTextFromKey("sgn_up_txt_youagreetothe");
        String subText1 = ServerDataManager.getTextFromKey("sgn_up_txt_termsofservice");
        String subText2 = ServerDataManager.getTextFromKey("sgn_up_txt_privacypolicy");
        String subText3 = ServerDataManager.getTextFromKey("sgn_up_txt_communityguidelines");
        int textColor = getResources().getColor(R.color.color_142_153_168);
        int subTextColor = getResources().getColor(R.color.color_45_223_227);
        Utils.setTermsPrivacyText(this, tvYouAgree, text, subText1, subText2, subText3, textColor, subTextColor, subTextColor, subTextColor);
    }

    @Override
    public void loginButton() {
        if (isValidUsername() || isVaildEmail()) {
            return;
        }
        showLoadingDialog(null, null, true);
        partyType = GBMemberShip_V2.MemberShipThirdPartyType.GetObject(Preferences.getInstacne().getValues(GBSConstants.MS_THIRDPARTY_TYPE_KEY, 1));
        MemberShipManager.getInstance().registerByThirdParty(DataManager.getInstance().getThirdPartUser().getUserId(),
                getName(),
                null,
                getEmail(),
                null,
                GBSDataManager.getInstance().getThirdPartUser().getUserName(),
                GBSDataManager.getInstance().getThirdPartUser().getAvatarURL(),
                GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_UserName,
                GBMemberShip_V2.MemberShipInfoSendType.MemberShip_Send_None,
                0,
                partyType,
                GBMemberShip_V2.MemberShipRelationType.MemberShip_RelationType_NoExistToCrtAcc_ExistNotToMatch,
                new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {
                    public void timeOut() {
                        hideLoadingDialog();
                    }

                    public void success(GBUserInfo obj) {
//                        hideLoadingDialog();
                        Preferences.getInstacne().setValues(Constants.TUTORIAL_SEARCH_FRIEND, false);
//                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_CAMERA_FRAGMENT,false);
//                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_HOME_FRAGMENT,false);
//                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_EDIT_POST_FRAGMENT,false);
//                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_SHOW_POST_FRAGMENT,false);
//                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_CHAT_LIST_FRAGMENT,false);
//                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT,false);

//                        Preferences.getInstacne().setValues(HomeActivity.HAD_READ_POST_IDS, HomeActivity.FIRST_LOGIN_POST_ID);
                        Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().finish();
//                        gotoPager(PhoneFragment.class, null);
//                        if (BasicUser.from(obj).getIsNeedAddFirds()) DataManager.getInstance().fitToFacebookTip();
//                        hideLoadingDialog();
//                        if (BasicUser.from(obj).getIsNeedAddFirds()) DataManager.getInstance().fitToFacebookTip();
//                        gotoPager(RecommendFrdsFragment.class, null);
//                        getActivity().finish();
                    }

                    public void fail(String strError) {
                        hideLoadingDialog();
                        errorCodeDo(strError);
                    }

                    public void cancel() {
                        hideLoadingDialog();
                    }
                });
    }
}
