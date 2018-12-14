package com.yeemos.app.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.R;
import com.yeemos.app.interfaces.IUpdateUI;
import com.yeemos.app.utils.Constants;

/**
 * Created by gigabud on 16-11-14.
 */

public class HelpFragment extends BaseFragment {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_help;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });
        view.findViewById(R.id.tvHelpWay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("mailto:" + ServerDataManager.getTextFromKey("hlp_txt_email"));
                Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(it);
            }
        });
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
        setOnlineText(R.id.tvTitle, "hlp_ttl_help");
        String content = ServerDataManager.getTextFromKey("hlp_txt_contact");
        String email = ServerDataManager.getTextFromKey("hlp_txt_email");
        if (content != null && email != null) {
            setSubText((TextView) getView().findViewById(R.id.tvHelpWay), content, email);
        }
    }

    public void setSubText(TextView tv, String text, final String subText1) {
        SpannableString spStr = new SpannableString(text);
        int index1 = text.indexOf(subText1);
        spStr.setSpan(new UnderlineSpan(), index1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.comment_text_content)), index1, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tv.setText(spStr);

    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }
}
