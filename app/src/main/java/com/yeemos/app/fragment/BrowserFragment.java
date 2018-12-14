package com.yeemos.app.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yeemos.app.R;
import com.yeemos.app.utils.Constants;

import static com.yeemos.app.R.id.webView;

/**
 * Created by gigabud on 16-8-19.
 */
public class BrowserFragment extends BaseFragment implements View.OnClickListener{

    public final static String BrowserFragment_Url = "browser_fragment_url";
    @Override
    protected int getLayoutId() {
        return R.layout.layout_browser_fragment;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.cancelImg).setOnClickListener(this);
        getLoadingFailed().setOnClickListener(this);


        WebSettings settings = getWebView().getSettings();
        settings.setSupportZoom(false);          //支持缩放
        settings.setBuiltInZoomControls(false);  //启用内置缩放装置
        settings.setJavaScriptEnabled(true);    //启用JS脚本

        Bundle bundle = getArguments();
        if (bundle == null) {
            goBack();
        }
        String url = bundle.getString(BrowserFragment.BrowserFragment_Url);
        if (!url.contains("http://")) {
            url = "http://" + url;
        }

        getWebView().loadUrl(url);

        getWebView().setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override


            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (getView() != null) {
                    getProgessBar().setVisibility(View.GONE);
                    getLoadingFailed().setVisibility(View.GONE);
                }

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (getView() != null) {
                    getProgessBar().setMax(100);
                    getProgessBar().setProgress(0);
                    getProgessBar().setVisibility(View.VISIBLE);
                    getWebView().setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (getView() != null) {
                    getWebView().setVisibility(View.GONE);
                    getProgessBar().setVisibility(View.GONE);
                    getLoadingFailed().setVisibility(View.VISIBLE);
                }

            }
        });
        getWebView().setWebChromeClient(new WebChromeClient(){
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (getView() != null) {
                    getProgessBar().setProgress(newProgress);
                }
            }
        });
        getWebView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK && getWebView().canGoBack()) {
                        getWebView().goBack();   //后退
                        return true;    //已处理
                    }
                }
                return false;
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

    private TextView getLoadingFailed(){
        return (TextView) getView().findViewById(R.id.loadingFailed);
    }

    private WebView getWebView() {
        return (WebView) getView().findViewById(webView);
    }

    private ProgressBar getProgessBar() {
        return (ProgressBar) getView().findViewById(R.id.loading);
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cancelImg:
                goBack();
                break;
            case R.id.btnBack:
                if(getWebView().canGoBack()) {
                    getWebView().goBack();   //后退
                }else {
                    goBack();
                }
                break;
            case R.id.loadingFailed:
                getWebView().reload();   //重新加载
                break;
        }
    }

    @Override
    public void onDestroy() {
        if (getView() != null && getWebView() != null) {
            getWebView().destroy();
        }
        super.onDestroy();
    }
}
