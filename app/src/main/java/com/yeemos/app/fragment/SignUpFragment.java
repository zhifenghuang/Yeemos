package com.yeemos.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.gigabud.core.util.Country;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-8.
 */
public class SignUpFragment extends BaseFragment implements View.OnClickListener {
    public String setcountryCode;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_signup;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    public void initView(View view) {
        ((EditText) view.findViewById(R.id.et_email)).setHint(ServerDataManager.getTextFromKey("sgn_up_txt_email"));
        ((EditText) view.findViewById(R.id.et_usrname)).setHint(ServerDataManager.getTextFromKey("sgn_up_txt_createusername"));
        ((EditText) view.findViewById(R.id.et_psw)).setHint(ServerDataManager.getTextFromKey("sgn_up_txt_password"));
        getPhoneEditText().setHint(ServerDataManager.getTextFromKey("vrfyphn_txt_number"));
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.countryCode).setOnClickListener(this);
        view.findViewById(R.id.countryName).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        Country country = getCountry(Constants.DEFAULT_PHONE_ZIP);
        if (country != null) {
            refreshFromNextFragment(country);
        } else {
            setOnlineText(R.id.countryName, "vrfyphn_txt_country");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    /**
     * 根据国家码获得电话区号
     *
     * @return
     */
    private Country getCountry(String countryZip) {
        ArrayList<Country> allCountries = LanguagePreferences.getInstanse(
                getActivity()).getAllCountries(getActivity());
        for (Country country : allCountries) {
            if (country.phoneZip.equalsIgnoreCase(countryZip)) {
                setcountryCode = country.phoneZip;
                return country;
            }
        }
        return null;
    }

    public EditText getPhoneEditText() {
        return (EditText) getView().findViewById(R.id.phoneNum);
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.top_title, "sgn_up_ttl_signup");
        setOnlineText(R.id.btn_ok, "pblc_btn_next");
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
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    public String getName() {
        return ((EditText) getView().findViewById(R.id.et_usrname)).getText().toString();
    }

    public String getPassword() {
        return ((EditText) getView().findViewById(R.id.et_psw)).getText().toString();
    }

    public String getEmail() {
        return ((EditText) getView().findViewById(R.id.et_email)).getText().toString();
    }

    public String getPhone() {
        return (TextUtils.isEmpty(getPhoneEditText().getText().toString())
                || getPhoneEditText().getText().toString().equals("")) ? null : setcountryCode + "-" + getPhoneEditText().getText().toString();
    }

    public boolean isValidUsername() {
        if (getName().equalsIgnoreCase("anonymous") || getName().equals("匿名")) {
            errorCodeDo(PlatformErrorKeys.CODE_USER_ALREADY_ACTIVATED);
            return true;
        }
        if (!MemberShipManager.isValidUsername(getName())) {
            MemberShipManager.showInVaildUsernameTipDialog((BaseActivity) getActivity());
            return true;
        }
        return false;
    }

    public boolean isVaildEmail() {
        if (!MemberShipManager.isVaildEmail(getEmail())) {
            MemberShipManager.showInVaildEmailTipDialog((BaseActivity) getActivity());
            return true;
        }
        return false;
    }

    public boolean verify() {
        if (isValidUsername()) {
            return false;
        }
        if (isVaildEmail()) {
            return false;
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
                loginButton();
                break;
            case R.id.countryCode:
            case R.id.countryName:
                Bundle bundle = new Bundle();
                bundle.putString(SelectCountryFragment.LAST_FRAGMENT_NAME, getClass().getName());
                gotoPager(SelectCountryFragment.class, bundle);
                break;
        }
    }

    public void refreshFromNextFragment(Object obj) {
        Country country = (Country) obj;
        setcountryCode = country.countryZip;
        ((TextView) getView().findViewById(R.id.countryCode)).setText("+" + country.phoneZip);
        ((TextView) getView().findViewById(R.id.countryName)).setText(country.countryName);
    }

    public void loginButton() {
        if (!verify()) {
            return;
        }
        showLoadingDialog(null, null, true);
        MemberShipManager.getInstance().register(getName(),
                getPassword(),
                getEmail(),
                getPhone(),
                getName(),
                GBMemberShip_V2.MemberShipInfoSendType.MemberShip_Send_None,
                GBMemberShip_V2.MemberShipUserType.MemberShip_UserType_UserName,
                GBMemberShip_V2.MemberShipActiveType.MemberShip_Active_Normal,
                new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {

                    @Override
                    public void timeOut() {
                        hideLoadingDialog();
                        errorCodeDo(PlatformErrorKeys.CONNECTTION_TIMEOUT);
                    }

                    @Override
                    public void success(GBUserInfo obj) {
                        Preferences.getInstacne().setValues(Constants.TUTORIAL_SEARCH_FRIEND, false);
                        Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                        startActivity(intent);
                        getActivity().finish();
                    }

                    @Override
                    public void fail(String errorStr) {
                        hideLoadingDialog();
                        errorCodeDo(errorStr);
                    }

                    @Override
                    public void cancel() {
                        hideLoadingDialog();
                        errorCodeDo(PlatformErrorKeys.CONNECTTION_EXCEPTION);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
    }
}
