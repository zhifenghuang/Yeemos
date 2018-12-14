package com.yeemos.app.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.adapter.HashTagAdapter;
import com.yeemos.app.interfaces.OnItemClickListener;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;

public class HashTagsFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //	private String Title= "";
    protected ArrayList<PostBean> arrayList;

    private String keyword;

    protected int mStartNum;
    protected boolean mHasData = false;
    protected boolean mIsGetingData;


    @Override
    protected int getLayoutId() {
        return R.layout.layout_fragment_hashtag;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_THREE;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);

        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
//		((ImageButton)view.findViewById(R.id.btnBack)).setImageDrawable(getResources().getDrawable(R.drawable.whiteback_noshadow));
//		ImageButton btnCross = (ImageButton) view.findViewById(R.id.btnCross);
//		btnCross.setVisibility(View.VISIBLE);
//		btnCross.setOnClickListener(this);

//		Title = getArguments().getString(Constants.KEY_SEND_STRING_TO_HASHTAGSFRAGMENT);
        getNoDataView().setText(ServerDataManager.getTextFromKey("frndprfl_txt_nopost"));
        keyword = DataManager.getInstance().getCurKeyWord();
        initTopView();
        getListView().setAdapter(getHashTagAdapter());
        mStartNum = 0;
    }

    public void initTopView() {
        ((TextView) getView().findViewById(R.id.tvTitle)).setText("#" + keyword);
    }


    public HashTagAdapter getHashTagAdapter() {
        if (getListView().getAdapter() == null) {
            HashTagAdapter hashTagAdapter = new HashTagAdapter(getActivity());
            hashTagAdapter.setIsRecentPostList(false);
            hashTagAdapter.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(int position) {
                    DataManager.getInstance().setShowPostList(arrayList);
                    Bundle b = new Bundle();
                    b.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_FROM_SHOWLIST);
                    b.putInt(ShowPostViewPagerFragment.SHOW_INDEX, position);
                    gotoPager(ShowPostViewPagerFragment.class, b);
                }

                @Override
                public void onItemTouch(MotionEvent event) {

                }
            });
            getListView().setAdapter(hashTagAdapter);
        }
        return (HashTagAdapter) getListView().getAdapter();
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onStart() {
        super.onStart();
        if (arrayList == null || arrayList.isEmpty()) {
            getSwipeRefreshLayout().setProgressViewOffset(false, 0, Utils.dip2px(getActivity(), 24));
            getSwipeRefreshLayout().setRefreshing(true);
            getData(false);
        } else {
            getHashTagAdapter().setPostBeanListDirect(arrayList);
        }
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
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }


    public void getData(final boolean isGetNewData) {
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
                final ArrayList<PostBean> arrList = DataManager.getInstance().getPostByHashTag(keyword, 0, true);
                if (arrList != null && !arrList.isEmpty()) {
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
                    mHasData = false;
                    mIsGetingData = false;
                    if (getView() != null) {
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                DataManager.getInstance().setHashTagPostList(arrayList);
                                getHashTagAdapter().setPostBeanListDirect(arrayList);
                                if (getSwipeRefreshLayout().isRefreshing()) {
                                    getSwipeRefreshLayout().setRefreshing(false);
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    public void loadMoreData() {
        if (!mHasData || mIsGetingData) {
            return;
        }
        mIsGetingData = true;
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                {
                    ArrayList<PostBean> arrList = DataManager.getInstance()
                            .getPostByHashTag(keyword, mStartNum, true);
                    if (arrList != null && !arrList.isEmpty()) {
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
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    public TextView getNoDataView() {
        return (TextView) getView().findViewById(R.id.noDataView);
    }

    @Override
    public void onRefresh() {
        mStartNum = 0;
        getData(true);
    }

    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
    }

    protected ListView getListView() {
        return (ListView) getView().findViewById(R.id.listView);
    }

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        if (dataType == 1) {
            if (oprateType == 0) {
                for (PostBean postBean : arrayList) {
                    if (postBean.getId().equals(((PostBean) data).getId())) {
                        arrayList.remove(postBean);
                        getHashTagAdapter().removePost(postBean);
                        break;
                    }
                }
            } else {
                BasicUser dataOwer = ((PostBean) data).getOwner();
                for (int i = 0; i < arrayList.size(); i++) {
                    PostBean postBean = arrayList.get(i);
                    if (postBean.getOwner().getUserId().equals(dataOwer.getUserId())) {
                        postBean.getOwner().resetBasicUser(dataOwer);
                    }
                }
            }
        }
    }

}
