package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.adapter.RequestAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;
import com.yeemos.app.utils.Preferences;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-7-19.
 */
public class RequestFragment extends BaseFragment implements View.OnClickListener{

    private ArrayList<BasicUser> requestList;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_request_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Preferences.getInstacne().setValues(HomeActivity.HAD_REQUEST_FOLLOW_YOU_MESSAGE, false);
        view.findViewById(R.id.btnBack).setOnClickListener(this);

    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    private ListView getListView(){
        return (ListView)getView().findViewById(R.id.requestList);
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
        setOnlineText(R.id.pageTitle, "rqst_ttl_request");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    @Override
    public void goBack() {
        String className = getArguments().getString(SelectCountryFragment.LAST_FRAGMENT_NAME);
        BaseFragment fragment = (BaseFragment) getActivity().getSupportFragmentManager().findFragmentByTag(className);
        if ( fragment != null ) {
            fragment.refreshFromNextFragment(null);
        }
        super.goBack();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(requestList == null || requestList.isEmpty()){
            getRequestList();
        }else{
            getRequestUserAdapter().setArrayList(requestList);
        }
    }

    private RequestAdapter getRequestUserAdapter() {
        if (getListView().getAdapter() == null) {
            RequestAdapter adapter = new RequestAdapter(getActivity());
            getListView().setAdapter(adapter);
        }
        return (RequestAdapter) getListView().getAdapter();
    }
    private void getRequestList(){
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ServerResultBean<BasicUser> serverBean = DataManager.getInstance().getUserDetailInfo(
                        DataManager.getInstance().getBasicCurUser(), 0,
                        GBSConstants.UserDataType.User_Data_RequestUsers,
                        GBSConstants.SortType.SortType_Time,
                        GBSConstants.SortWay.SortWay_Descending);
                if (getActivity() == null) {
                    return;
                }
                if(serverBean.isSuccess()) {
                    requestList = serverBean.getData().getRequestUsers();
                    if (requestList == null){
                        requestList = new ArrayList<BasicUser>();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            getRequestUserAdapter().setArrayList(requestList);
                        }
                    });
                }
            }
        });

    }
}
