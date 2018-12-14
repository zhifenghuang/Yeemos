package com.yeemos.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.platforms.GBUserInfo;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.view.PhonePickerView;
import com.yeemos.app.view.PickerViewWindow;
import com.yeemos.app.R;
import com.yeemos.app.utils.Constants.PHONE_FRAGMENT_UI_POSITION;

/**
 * Created by gigabud on 16-7-11.
 */
public class PhoneFragment extends BaseFragment implements View.OnClickListener{
    private String setcountryCode;
    @Override
    protected int getLayoutId() {
        return R.layout.layout_phone_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getEditText().setHint(ServerDataManager.getTextFromKey("vrfyphn_txt_number"));
        ((Button)view.findViewById(R.id.btn_ok)).setText(ServerDataManager.getTextFromKey("pblc_btn_next"));
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        view.findViewById(R.id.countryCode).setOnClickListener(this);
        view.findViewById(R.id.countryName).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
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
        setOnlineText(R.id.top_title,"vrfyphn_ttl_verifyphone");
        setOnlineText(R.id.countryName,"vrfyphn_txt_country");
    }
    private EditText getEditText(){
        return (EditText)getView().findViewById(R.id.phoneNum);
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
            case R.id.countryCode:
            case R.id.countryName:
                PickerViewWindow pickerViewWindow = new PickerViewWindow(getActivity());
                pickerViewWindow.setmOnSelectListener(new PhonePickerView.onSelectListener() {
                    @Override
                    public void onSelect(String countryName, String countryCode) {
                        setcountryCode = countryCode;
                        ((TextView)getView().findViewById(R.id.countryCode)).setText("+"+countryCode);
                        ((TextView)getView().findViewById(R.id.countryName)).setText(countryName);
                    }
                });
                pickerViewWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                break;
            case R.id.btn_ok:
                if(!TextUtils.isEmpty(getEditText().getText()) && getEditText().getText().length() > 0){
                    MemberShipManager.getInstance().updateUserInfoWithFile(
                            DataManager.getInstance().getBasicCurUser().getUserName(),
                            "", "",
                            DataManager.getInstance().getBasicCurUser().getEmail(),
                            setcountryCode+"-"+getEditText().getText().toString(), "", null,
                            null, new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {

                                @Override
                                public void timeOut() {
                                    hideLoadingDialog();
                                }

                                @Override
                                public void success(GBUserInfo obj) {
                                    hideLoadingDialog();
                                }

                                @Override
                                public void fail(String errorStr) {
                                    hideLoadingDialog();
                                }

                                @Override
                                public void cancel() {
                                    hideLoadingDialog();
                                }
                            });
                }
                Intent intent = new Intent(getActivity(),HomeActivity.class);
                startActivity(intent);
                getActivity().finish();
                break;
        }
    }

    @Override
    public void goBack() {
        getActivity().finish();
    }
}
