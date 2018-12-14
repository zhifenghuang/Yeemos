package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-7-28.
 */
public class TermsOrPrivacyFragment extends BaseFragment {
    public static final int HTML_TERMS_OF_SERVICE = 1;
    public static final int HTML_PRIVACY_POLICY = 2;
    public static final int HTML_COMMUNITY = 3;
    public static final String HTML_TYPE = "html_type";

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_terms_or_privacy;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        WebView webView = (WebView) view.findViewById(R.id.webView);
        webView.setBackgroundColor(0);
        webView.getSettings().setJavaScriptEnabled(true);
        int htmlType = getArguments().getInt(HTML_TYPE, HTML_PRIVACY_POLICY);
        if (htmlType == HTML_PRIVACY_POLICY) {
            webView.loadUrl("file:///android_asset/html/privacy.html");
            ((TextView) view.findViewById(R.id.tvTitle)).setText(ServerDataManager.getTextFromKey("sttngs_btn_privacypolicy"));
        } else if (htmlType == HTML_TERMS_OF_SERVICE) {
            webView.loadUrl("file:///android_asset/html/terms.html");
            ((TextView) view.findViewById(R.id.tvTitle)).setText(ServerDataManager.getTextFromKey("sttngs_btn_terms"));
        } else {
            webView.loadUrl("file:///android_asset/html/community_guidelines.html");
            ((TextView) view.findViewById(R.id.tvTitle)).setText(ServerDataManager.getTextFromKey("sgn_up_txt_communityguidelines"));
        }
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
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

    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }
}
