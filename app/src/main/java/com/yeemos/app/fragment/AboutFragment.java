package com.yeemos.app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-8-11.
 */
public class AboutFragment extends BaseFragment implements View.OnClickListener {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_about;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.officialsite).setOnClickListener(this);
        view.findViewById(R.id.supportemail).setOnClickListener(this);
        ((TextView) view.findViewById(R.id.version)).setText(String.format(ServerDataManager.getTextFromKey("abtus_txt_version"), Utils.getVersion()));
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
        setOnlineText(R.id.tvSetting, "abtus_txt_aboutus");
        setOnlineText(R.id.officialsite, "abtus_txt_officialsite");
        setOnlineText(R.id.supportemail, "abtus_txt_supportemail");
        setOnlineText(R.id.copyright, "abtus_txt_copyright");
    }


    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    private TextView getOfficialsite() {
        return (TextView) getView().findViewById(R.id.officialsite);
    }

    private TextView getSupportemail() {
        return (TextView) getView().findViewById(R.id.supportemail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.officialsite:
                if (TextUtils.isEmpty(getOfficialsite().getText()) || getOfficialsite().getText().equals("")) {
                    return;
                }
                Bundle bundle = new Bundle();
                bundle.putString(BrowserFragment.BrowserFragment_Url, ServerDataManager.getTextFromKey("abtus_txt_officialsite"));
                gotoPager(BrowserFragment.class, bundle);
                break;
            case R.id.supportemail:
                Uri uri = Uri.parse("mailto:" + getSupportemail().getText().toString());
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(it);
                break;
        }

    }
}
