package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.gbsocial.constants.GBSConstants;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-7-28.
 */
public class FollowedFragment extends FollowingFragment {

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUserDataType(GBSConstants.UserDataType.User_Data_Followed);
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.pageTitle,"usrprfl_btn_follower");
    }
}
