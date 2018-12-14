package com.yeemos.app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.yeemos.app.activity.EmptyTwoActivity;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-9-13.
 */
public class TutorialSearchFriendFragment extends BaseFragment implements View.OnClickListener {
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_turorial_search_friend;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        Preferences.getInstacne().setValues(Constants.TUTORIAL_SEARCH_FRIEND, true);
        view.findViewById(R.id.tvSearchFriend).setOnClickListener(this);
        view.findViewById(R.id.tvSkip).setOnClickListener(this);
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
        setOnlineText(R.id.tv, "cm_txt_searchfriend");
        setOnlineText(R.id.tvSearchFriend, "cm_btn_searchfriend");
        setOnlineText(R.id.tvSkip, "cm_btn_skip");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvSearchFriend:
//                getActivity().getSupportFragmentManager().popBackStack();
                gotoPager(FindUserExFragment.class, null);
                Intent intent = new Intent(getActivity(), EmptyTwoActivity.class);
                intent.putExtra("FRAGMENT_NAME", FindUserExFragment.class.getName());
                startActivity(intent);
                break;
            case R.id.tvSkip:
                goBack();
                break;
        }
    }
}
