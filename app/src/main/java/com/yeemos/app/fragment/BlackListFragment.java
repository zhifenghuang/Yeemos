package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerResultBean;
import com.yeemos.app.adapter.BlackListAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-15.
 */
public class BlackListFragment extends BaseFragment implements View.OnClickListener ,SwipeRefreshLayout.OnRefreshListener{
    private ArrayList<BasicUser> arrayList;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
    }
    @Override
    public void onStart() {
        super.onStart();
        if(arrayList == null || arrayList.isEmpty()){
            getArrayList();
        }else {
            getBlackListAdapter().setArrayList(arrayList);
        }
    }

    public BlackListAdapter getBlackListAdapter() {
        if (getBlackListView().getAdapter() == null) {
            BlackListAdapter adapter = new BlackListAdapter();
            getBlackListView().setAdapter(adapter);
        }
        return (BlackListAdapter) getBlackListView().getAdapter();
    }

    private void getArrayList() {
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    if(getView()!=null) {
                        getSwipeRefreshLayout().setRefreshing(true);
                    }
                }
            });
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> resultBean = DataManager.getInstance()
                        .getCurUserDetailInfo(GBSConstants.UserDataType.User_Data_BlockUser);
                if (resultBean.isSuccess() && resultBean.getData() != null) {

                    arrayList = resultBean.getData().getBlockUsers();

                    if (getActivity() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null ) {
                                return;
                            }
                            if(getSwipeRefreshLayout().isRefreshing()){
                                getSwipeRefreshLayout().setRefreshing(false);
                            }
                            getBlackListAdapter().setArrayList(arrayList);
                        }
                    });
                }
            }
        }).start();
    }

    private ListView getBlackListView() {
        return (ListView) getView().findViewById(R.id.blackList);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_black_list;
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
        setOnlineText(R.id.pageTitle, "blcklst_ttl_blacklist");
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
    public void onRefresh() {
        getArrayList();
    }
    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
    }
}
