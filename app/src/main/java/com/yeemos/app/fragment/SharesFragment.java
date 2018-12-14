package com.yeemos.app.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import java.util.ArrayList;

/**
 * Created by gigabud on 17-2-22.
 */
public class SharesFragment extends HashTagsFragment {

    private BasicUser mBasicUser;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBasicUser = DataManager.getInstance().getCurOtherUser();
        if (mBasicUser == null) {
            goBack();
            return;
        }
        arrayList = mBasicUser.getObjectsPosts();
    }

    @Override
    public void initTopView() {
        setOnlineText(R.id.tvTitle, "usrprfl_btn_posts");
    }

    public void getData(final boolean isGetNewData) {
        if (mBasicUser == null) {
            goBack();
            return;
        }
        mHasData = true;
        mIsGetingData = true;
        if (!getSwipeRefreshLayout().isRefreshing() && isGetNewData) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null) {
                        getSwipeRefreshLayout().setRefreshing(true);
                    }
                }
            });
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> serverBean = DataManager.getInstance().getUserDetailInfoByPage(mBasicUser, 0,
                        GBSConstants.UserDataType.User_Data_AllPost, GBSConstants.SortType.SortType_Time, GBSConstants.SortWay.SortWay_Descending);
                if (getView() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
                            swipeLayout.setRefreshing(false);
                        }
                    });
                }
                if (serverBean != null) {
                    mBasicUser = serverBean.getData();
                    if (mBasicUser == null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                if (getSwipeRefreshLayout().isRefreshing()) {
                                    getSwipeRefreshLayout().setRefreshing(false);
                                }
                                getNoDataView().setVisibility(View.VISIBLE);
                            }
                        });
                        mHasData = false;
                        mIsGetingData = false;
                        return;
                    }
                    ArrayList<PostBean> arrList = mBasicUser.getObjectsPosts();
                    arrayList = new ArrayList<>();
                    if (arrList != null && !arrList.isEmpty()) {
                        if (arrList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                            mHasData = false;
                        } else {
                            mStartNum += GBSConstants.PAGE_NUMBER_PAGINATION_20;
                        }

                        if (MemberShipManager.getInstance().getUserID().equals(mBasicUser.getUserId())) {
                            arrayList = arrList;
                        } else {
                            for (PostBean postBean : arrList) {
                                if (postBean.getOwner().getFollowedStatus() == 1 || !postBean.isPrivate()) {
                                    arrayList.add(postBean);
                                }
                            }
                        }
                        if (getView() != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    getNoDataView().setVisibility(View.GONE);
                                    if (getSwipeRefreshLayout().isRefreshing()) {
                                        getSwipeRefreshLayout().setRefreshing(false);
                                    }
                                    DataManager.getInstance().setHashTagPostList(arrayList);
                                    getHashTagAdapter().setPostBeanListDirect(arrayList);

                                }
                            });
                        }
                        mIsGetingData = false;
                    } else {
                        if (getView() != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    if (getSwipeRefreshLayout().isRefreshing()) {
                                        getSwipeRefreshLayout().setRefreshing(false);
                                    }
                                    DataManager.getInstance().setHashTagPostList(arrayList);
                                    getHashTagAdapter().setPostBeanListDirect(arrayList);
                                    getNoDataView().setVisibility(View.VISIBLE);
                                }
                            });
                        }
                        mHasData = false;
                        mIsGetingData = false;
                    }
                } else {
                    if (getView() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                if (getSwipeRefreshLayout().isRefreshing()) {
                                    getSwipeRefreshLayout().setRefreshing(false);
                                }
                                DataManager.getInstance().setHashTagPostList(arrayList);
                                getHashTagAdapter().setPostBeanListDirect(arrayList);
                                getNoDataView().setVisibility(View.VISIBLE);
                            }
                        });
                    }
                    mHasData = false;
                    mIsGetingData = false;
                }
            }
        });
    }

    @Override
    public void loadMoreData() {
        if (!mHasData || mIsGetingData) {
            return;
        }
        mIsGetingData = true;
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> serverBean = DataManager.getInstance().getUserDetailInfoByPage(mBasicUser,
                        mStartNum,
                        GBSConstants.UserDataType.User_Data_AllPost,
                        GBSConstants.SortType.SortType_Time,
                        GBSConstants.SortWay.SortWay_Descending);
                if (serverBean != null) {
                    mBasicUser = serverBean.getData();
                    if (mBasicUser == null) {
                        mHasData = false;
                        mIsGetingData = false;
                        return;
                    }
                    ArrayList<PostBean> arrList = mBasicUser.getObjectsPosts();
                    if (arrList != null && !arrList.isEmpty()) {
                        getNoDataView().setVisibility(View.GONE);
                        if (arrList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                            mHasData = false;
                        } else {
                            mStartNum += GBSConstants.PAGE_NUMBER_PAGINATION_20;
                        }
                        arrayList = new ArrayList<PostBean>();
                        for (PostBean postBean : arrList) {
                            if (postBean.getOwner().getFollowedStatus() == 1 || !postBean.isPrivate()) {
                                arrayList.add(postBean);
                            }
                        }
                        if (getView() != null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    DataManager.getInstance().setHashTagPostList(arrayList);
                                    getHashTagAdapter().setPostBeanList(arrayList);

                                }
                            });
                        }
                        mIsGetingData = false;
                    } else {
                        mHasData = false;
                        mIsGetingData = false;
                    }
                } else {
                    mHasData = false;
                    mIsGetingData = false;
                }
            }
        });
    }
}
