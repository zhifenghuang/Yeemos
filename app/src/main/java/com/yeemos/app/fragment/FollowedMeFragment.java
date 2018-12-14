package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.adapter.FollowedListAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;
import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-28.
 */
public class FollowedMeFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    @Override
    protected int getLayoutId() {
        return R.layout.layout_followed_fragment;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.requestLayout).setOnClickListener(this);
        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
        initData();
    }

    private void initData() {
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ServerResultBean<BasicUser> serverBean = DataManager.getInstance().getUserDetailInfo(
                        DataManager.getInstance().getBasicCurUser(), 0,
                        GBSConstants.UserDataType.User_Data_Followed,
                        GBSConstants.SortType.SortType_Time,
                        GBSConstants.SortWay.SortWay_Descending);
                if (serverBean.isSuccess()) {
                    ArrayList<BasicUser> arrayList = serverBean.getData().getFollowerUsers();
                    final ArrayList<BasicUser> followOnlyList = new ArrayList<BasicUser>();
                    if (arrayList != null && !arrayList.isEmpty()) {
                        for (BasicUser basicUser : arrayList) {
                            if (basicUser.getFollowStatus() != 1 && basicUser.getFollowedStatus() == 1) {
                                followOnlyList.add(basicUser);
                            }
                        }
                    }
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (getView() == null) {
                                    return;
                                }
                                if (getSwipeRefreshLayout().isRefreshing()) {
                                    getSwipeRefreshLayout().setRefreshing(false);
                                }
                                FollowedListAdapter followedListAdapter = new FollowedListAdapter(followOnlyList);
                                getListView().setAdapter(followedListAdapter);
                                getCountText().setText(serverBean.getData().getRequestNums() + "");
                            }
                        });
                    }
                }
            }
        });
    }

    private ListView getListView() {
        return (ListView) getView().findViewById(R.id.followList);
    }

    private TextView getCountText() {
        return (TextView) getView().findViewById(R.id.countText);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout)  getView().findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    protected void initFilterForBroadcast() {

    }


    @Override
    public void refreshFromNextFragment(Object obj) {
        initData();
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.pageTitle, "fllwd_ttl_followed");
        setOnlineText(R.id.requestText, "fllwd_btn_request");
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
            case R.id.requestLayout:
                Bundle bundle = new Bundle();
                bundle.putString(SelectCountryFragment.LAST_FRAGMENT_NAME, getClass().getName());
                gotoPager(RequestFragment.class, bundle);
                break;
        }
    }

    @Override
    public void onRefresh() {
        initData();
    }
}
