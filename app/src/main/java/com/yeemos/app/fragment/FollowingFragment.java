package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.constants.GBSConstants.UserDataType;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.adapter.FollowAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-7-28.
 */
public class FollowingFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener{

    private ArrayList<BasicUser> mBasicList;
    protected UserDataType userDataType = UserDataType.User_Data_Following;
    private boolean mHasData = false;
    private boolean mIsGetingData;

    private BasicUser basicUser;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_following_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        basicUser = DataManager.getInstance().getCurOtherUser();
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }
    protected void setUserDataType(UserDataType userDataType){
        this.userDataType = userDataType;
    }
    @Override
    public void onStart() {
        super.onStart();
        if(mBasicList == null || mBasicList.isEmpty()) {
            getFollowingList();
        }else {
            getFollowingAdapter().setArrayList(mBasicList);
        }
    }

    private ListView getListView() {
        return (ListView) getView().findViewById(R.id.listView);
    }

    public FollowAdapter getFollowingAdapter() {
        if (getListView().getAdapter() == null) {
            FollowAdapter adapter = new FollowAdapter(getActivity());
            getListView().setAdapter(adapter);
        }
        return (FollowAdapter) getListView().getAdapter();
    }

    private void getFollowingList() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        if (!swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(true);
        }
        setmHasData(true);
        setmIsGetingData(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> resultBean = DataManager.getInstance().getUserDetailInfoByPage(basicUser,
                        0, userDataType,
                        GBSConstants.SortType.SortType_PeopleName,
                        GBSConstants.SortWay.SortType_Ascending);
                if(resultBean.isSuccess()) {
                    mBasicList = userDataType == UserDataType.User_Data_Following ?
                            resultBean.getData().getFollowingUsers() : resultBean.getData().getFollowerUsers();
                    if (mBasicList == null) {
                        setmHasData(false);
                        mBasicList = new ArrayList<BasicUser>();
                    }
                    if (mBasicList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                        setmHasData(false);
                    }
                    if (getActivity() == null) {
                        setmHasData(false);
                        setmIsGetingData(false);
                        return;
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
                            if (swipeLayout.isRefreshing()) {
                                swipeLayout.setRefreshing(false);
                            }

                            getFollowingAdapter().setArrayList(mBasicList);
                        }
                    });
                    setmIsGetingData(false);
                } else {
                    setmHasData(false);
                    setmIsGetingData(false);
                }
            }
        });

    }

    public void loadMoreData() {
        if (!ismHasData() || ismIsGetingData()) {
            return;
        }
        setmIsGetingData(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> resultBean = DataManager.getInstance().getUserDetailInfoByPage(basicUser,
                        mBasicList.size(), userDataType,
                        GBSConstants.SortType.SortType_PeopleName,
                        GBSConstants.SortWay.SortType_Ascending);
                if (resultBean.isSuccess()) {
                    ArrayList<BasicUser> mList = userDataType == UserDataType.User_Data_Following ?
                            resultBean.getData().getFollowingUsers() : resultBean.getData().getFollowerUsers();
                    if (mList == null) {
                        setmHasData(false);
                        mList = new ArrayList<BasicUser>();
                    }
                    if (mList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                        setmHasData(false);
                    }
                    if (getActivity() == null) {
                        setmHasData(false);
                        setmIsGetingData(false);
                        return;
                    }
                    mBasicList.addAll(mList);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            getFollowingAdapter().setArrayList(mBasicList);
                        }
                    });
                    setmIsGetingData(false);
                } else {
                    setmHasData(false);
                    setmIsGetingData(false);
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        getFollowingList();
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.pageTitle,"usrprfl_btn_following");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        if(dataType == 0) {
            if(((BasicUser)data).getBlockStatus() == 1
                    && basicUser != null
                    && basicUser.getUserId()
                    .equals(DataManager.getInstance().getBasicCurUser().getUserId())){
                for(BasicUser basicUser : mBasicList) {
                    if(basicUser.getUserId().equals(((BasicUser)data).getUserId())) {
                        mBasicList.remove(basicUser);
                        break;
                    }
                }

            }
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden) {
            getFollowingAdapter().notifyDataSetChanged();
        }
    }

    public boolean ismHasData() {
        return mHasData;
    }

    public void setmHasData(boolean mHasData) {
        this.mHasData = mHasData;
    }

    public boolean ismIsGetingData() {
        return mIsGetingData;
    }

    public void setmIsGetingData(boolean mIsGetingData) {
        this.mIsGetingData = mIsGetingData;
    }
}
