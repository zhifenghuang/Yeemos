package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.core.util.Country;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by gigabud on 15-12-29.
 */
public class EditProfileFragment extends BaseFragment implements View.OnClickListener {

    private String mCurrentCountryZip; // 当前电话国家码
    private ArrayList<String> mSexArr;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_edit_profile;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.tvDone).setOnClickListener(this);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.tvPhoneZip).setOnClickListener(this);
        BasicUser userinfo = MemberShipManager.getInstance().getUserInfo()
                .to(BasicUser.class);
        ((EditText) view.findViewById(R.id.etDisplayName)).setText(userinfo.getNick());
        ((EditText) view.findViewById(R.id.etDisplayName)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        ((TextView) view.findViewById(R.id.tvUserName)).setText(userinfo.getUserName());
        ((EditText) view.findViewById(R.id.etBio)).setText(userinfo.getBio() == null ? "" : userinfo.getBio());
        ((EditText) view.findViewById(R.id.etBio)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        ((EditText) view.findViewById(R.id.etEmail)).setText(userinfo.getEmail() == null ? "" : userinfo.getEmail());
        setPhone(view, userinfo.getMobile());
//        ArrayList<String> sexArr = getSexList();
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
//                android.R.layout.simple_spinner_item, sexArr);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        getSexSpinner().setAdapter(adapter);
        //       setConfig();
    }

    @Override
    public void refreshFromNextFragment(Object object) {
        Country country = (Country) object;
        mCurrentCountryZip = country.countryZip;
        ((TextView) getView().findViewById(R.id.tvPhoneZip)).setText("+"
                + country.phoneZip);
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
        setOnlineText(R.id.tvTitle, "edtprfl_ttl_editprofile");
        setOnlineText(R.id.tvDone, "pblc_btn_done");
        setOnlineText(R.id.etDisplayName, "edtprfl_txt_displayname");
        setOnlineText(R.id.etBio, "edtprfl_txt_bio");
        setOnlineText(R.id.etEmail, "edtprfl_txt_email");
    }

    private void setInputFieldHint(int ID, String key) {
//        ((EditProfileInputField) getView().findViewById(ID))
//                .setHint(ServerDataManager.getTextFromKey(key));
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    //    private void setConfig() {
//        BasicUser userinfo = MemberShipManager.getInstance().getUserInfo()
//                .to(BasicUser.class);
//        setName(userinfo.getNick());
//        setEmail(userinfo.getEmail());
//        setPhone(userinfo.getMobile());
//        setWeb(userinfo.getWebsite());
//        setBio(userinfo.getBio());
//        setSex(userinfo.getGender());
//        setUserName(userinfo.getUserName());
    //   }
//
//    private ArrayList<String> getSexList() {
//        if (mSexArr == null) {
//            mSexArr = new ArrayList<String>();
//            mSexArr.add("male");
//            mSexArr.add("female");
//            mSexArr.add("notspecified");
//        }
//        return mSexArr;
//    }
//    private Spinner getSexSpinner() {
//        return ((Spinner) getView().findViewById(R.id.spSex));
//    }
//
    private String getName() {
        return ((EditText) getView().findViewById(
                R.id.etDisplayName)).getText().toString();
    }

    //
//    private String getWeb() {
//        return ((EditProfileInputField) getView().findViewById(
//                R.id.InputField_web)).getText();
//    }
//
    private String getBio() {
        return ((EditText) getView().findViewById(
                R.id.etBio)).getText().toString();
    }

    private String getEmail() {
        return ((EditText) getView().findViewById(
                R.id.etEmail)).getText().toString();
    }

    //
//    private String getEmail() {
//        return ((EditProfileInputField) getView().findViewById(
//                R.id.InputField_email)).getText();
//    }
//
    private String getPhone() {
        String phoneStr = ((EditText) getView().findViewById(R.id.etPhone)).getText().toString();
        if (phoneStr.isEmpty()) {
            return phoneStr;
        } else
            return mCurrentCountryZip + "-" + phoneStr;
    }

    //
//
//
//    private void setName(String text) {
//        ((EditProfileInputField) getView().findViewById(R.id.InputField_name))
//                .setText(text);
//    }
//
//    private void setWeb(String text) {
//        ((EditProfileInputField) getView().findViewById(R.id.InputField_web))
//                .setText(text);
//    }
//
//    private void setBio(String text) {
//        ((EditProfileInputField) getView().findViewById(R.id.InputField_bio))
//                .setText(text);
//    }
//
//    private void setEmail(String text) {
//        ((EditProfileInputField) getView().findViewById(R.id.InputField_email))
//                .setText(text);
//    }
//
    private void setPhone(View view, String text) {
        String phoneZipCode = null;
        if (text != null) {
            String[] phoneInfos = text.split("-");
            if (phoneInfos.length == 2) {
                ((EditText) view.findViewById(R.id.etPhone))
                        .setText(phoneInfos[1]);
                phoneZipCode = getCountryPhoneZip(phoneInfos[0]);
            } else {
                ((EditText) view.findViewById(R.id.etPhone))
                        .setText(text);
                phoneZipCode = getCountryPhoneZip("");
            }

        } else {
            mCurrentCountryZip = Constants.DEFAULT_COUNTRY_ZIP;
            phoneZipCode = "+" + Constants.DEFAULT_PHONE_ZIP;
            ((EditText) view.findViewById(R.id.etPhone))
                    .setText("");
        }

        ((TextView) getView().findViewById(R.id.tvPhoneZip))
                .setText(phoneZipCode);
    }
//
////    private void setUserName(String text) {
////        ((EditProfileInputField) getView()
////                .findViewById(R.id.InputField_usrname)).setEditable(false);
////        ((EditProfileInputField) getView()
////                .findViewById(R.id.InputField_usrname)).setText(text);
////    }
////
////    private void setSex(String text) {
////        getSexSpinner().setSelection(GBSConstants.UserSexType.getObject(text).getValues());
////    }
////
////    private String getSex() {
////        return GBSConstants.UserSexType.getObject(getSexSpinner().getSelectedItemPosition()).getServerValue();
////    }
//
//

    /**
     * 根据国家码获得电话区号
     *
     * @return
     */
    private String getCountryPhoneZip(String countryZip) {
        if (countryZip == null || countryZip.trim().length() == 0) {
            countryZip = Locale.getDefault().getCountry();
        }
        mCurrentCountryZip = countryZip;
        ArrayList<Country> allCountries = LanguagePreferences.getInstanse(
                getActivity()).getAllCountries(getActivity());
        for (Country country : allCountries) {
            if (country.countryZip.equalsIgnoreCase(countryZip)) {
                return "+" + country.phoneZip;
            }
        }
        return "";
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDone:
                String email = getEmail();
                if (!MemberShipManager.isVaildEmail(getEmail())) {
                    MemberShipManager.showInVaildEmailTipDialog((BaseActivity) getActivity());
                    return;
                }
                showLoadingDialog("", null, true);
                MemberShipManager.getInstance().updateUserInfoWithFile(getName(),
                        "", getBio(), email, getPhone(), "", null,
                        null, new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {

                            @Override
                            public void timeOut() {
                                hideLoadingDialog();
                            }

                            @Override
                            public void success(GBUserInfo obj) {
                                hideLoadingDialog();
                                goBack();
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
                break;
            case R.id.btnBack:
                goBack();
                break;
            case R.id.tvPhoneZip:
                Bundle bundle = new Bundle();
                bundle.putString(SelectCountryFragment.LAST_FRAGMENT_NAME, getClass().getName());
                gotoPager(SelectCountryFragment.class, bundle);
                break;
            default:
                break;
        }

    }
}
