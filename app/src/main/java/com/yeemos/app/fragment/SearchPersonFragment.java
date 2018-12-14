package com.yeemos.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.adapter.FindUserAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;

import static com.yeemos.app.R.id.listView;

/**
 * Created by gigabud on 16-9-8.
 */
public class SearchPersonFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
//    private boolean hasSearchText = false;
    private View headView;

    private ArrayList<BasicUser> searchUserArrayList;
    private ArrayList<BasicUser> recommendUsersarrayList;

    private boolean mHasRecommendUsers = false;
    private boolean mIsGetingRecommendUsers;

    private boolean mHasSearchUsers = false;
    private boolean mIsGetingSrearchUsers;
    private String searchStr;
    private int oldPosition;



    @Override
    protected int getLayoutId() {
        return R.layout.layout_search_person_fragment;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
//        getlistNoDataLayout().setVisibility(View.VISIBLE);
//        getUserNameIs().setText(ServerDataManager.getTextFromKey("addbyusrnm_txt_yourusernameis"));
//        getUsername().setText(DataManager.getInstance().getBasicCurUser().getUserName());
//        getPeopleSee().setText(ServerDataManager.getTextFromKey("addbyusrnm_txt_peopleseeyouas"));
//        getDisplayName().setText(DataManager.getInstance().getBasicCurUser().getRemarkName());
        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                hideKeyBoard();
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    oldPosition = getListView().getFirstVisiblePosition();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        getData(false);
    }

    private View getNoSearchHeadView() {
        if(headView == null) {
            headView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_no_search_result_header_view, getListView(), false);
            initHeaderView(headView);
        }
        return headView;
    }

    private void initHeaderView(View headerView) {
        ((TextView) headerView.findViewById(R.id.displayName)).setText(DataManager.getInstance().getBasicCurUser().getRemarkName());
        ((TextView) headerView.findViewById(R.id.userNameIs)).setText(ServerDataManager.getTextFromKey("addbyusrnm_txt_yourusernameis"));
        ((TextView) headerView.findViewById(R.id.username)).setText(DataManager.getInstance().getBasicCurUser().getUserName());
        ((TextView) headerView.findViewById(R.id.peopleSee)).setText(ServerDataManager.getTextFromKey("addbyusrnm_txt_peopleseeyouas"));
        ((TextView) headerView.findViewById(R.id.recommendUser)).setText(ServerDataManager.getTextFromKey("fndusr_txt_recommendeduser"));
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

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_MORE_DATA;
    }

    @Override
    public void onRefresh() {
        getData(true);
    }

    public void setSearchStrAndGetData() {
        getData(false);
    }


//    private FindUserAdapter getFindUserAdapter() {
//        if (getListView().getAdapter() == null) {
//            FindUserAdapter findUserAdapter = new FindUserAdapter();
//            getListView().setAdapter(findUserAdapter);
//        }
//        return (FindUserAdapter) getListView().getAdapter();
//    }

    private void getData(final boolean refresh) {
        if (!TextUtils.isEmpty(searchStr) && searchStr.equals(DataManager.getInstance().getCurKeyWord()) && !refresh) {
            return;
        }
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
        }
        setmHasRecommendUsers(true);
        setmIsGetingRecommendUsers(true);
        setmHasSearchUsers(true);
        setmIsGetingSrearchUsers(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getDataFromServer(refresh);
            }
        });
    }


    private synchronized void getDataFromServer(boolean refresh) {
        if (TextUtils.isEmpty(DataManager.getInstance().getCurKeyWord())
                || DataManager.getInstance().getCurKeyWord().equals("")) {
            if (!refresh) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getListView().removeHeaderView(getNoSearchHeadView());
                        getListView().setAdapter(null);
                        getListView().addHeaderView(getNoSearchHeadView(), null, false);
                    }
                });
            }
            searchStr = DataManager.getInstance().getCurKeyWord();
            getRecommendUsersFromServer(refresh);
        }else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getListView().removeHeaderView(getNoSearchHeadView());
                    getListView().setAdapter(null);
                }
            });
            searchStr = DataManager.getInstance().getCurKeyWord();
            searchUserArrayList = DataManager.getInstance().findFriendSearchUser(searchStr,
                    0, GBSConstants.SearchDataType.Search_Data_User);
            if (searchUserArrayList != null && !searchUserArrayList.isEmpty()) {
                if (searchUserArrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                    setmHasSearchUsers(false);
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }
                        if (getSwipeRefreshLayout().isRefreshing()) {
                            getSwipeRefreshLayout().setRefreshing(false);
                        }
                        FindUserAdapter findUserAdapter = new FindUserAdapter();
                        findUserAdapter.setBaseFragment(SearchPersonFragment.this);
                        findUserAdapter.setArrayList(searchUserArrayList);
                        getListView().setAdapter(findUserAdapter);
                    }
                });
                setmIsGetingSrearchUsers(false);
            } else {
                setmHasSearchUsers(false);
                setmIsGetingSrearchUsers(false);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }
                        if (getSwipeRefreshLayout().isRefreshing()) {
                            getSwipeRefreshLayout().setRefreshing(false);
                        }
                        FindUserAdapter findUserAdapter = new FindUserAdapter();
                        findUserAdapter.setBaseFragment(SearchPersonFragment.this);
                        findUserAdapter.setArrayList(new ArrayList<BasicUser>());
                        getListView().setAdapter(findUserAdapter);
                    }
                });
            }
        }
    }

    public void loadMoreData() {
        if (TextUtils.isEmpty(DataManager.getInstance().getCurKeyWord())
                || DataManager.getInstance().getCurKeyWord().equals("")) {
            loadMoreRecommendUsers();
        } else {
            loadMoreSearchUser();
        }
    }

    public void loadMoreSearchUser() {
        if (!ismHasSearchUsers() || ismIsGetingSrearchUsers()) {
            return;
        }
        setmIsGetingSrearchUsers(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<BasicUser> arrayList = DataManager.getInstance().findFriendSearchUser(searchStr,
                        searchUserArrayList.size(), GBSConstants.SearchDataType.Search_Data_User);
                if (arrayList != null && !arrayList.isEmpty()) {
                    if (arrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                        setmHasSearchUsers(false);
                    }
                    searchUserArrayList.addAll(arrayList);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            FindUserAdapter findUserAdapter = new FindUserAdapter();
                            findUserAdapter.setBaseFragment(SearchPersonFragment.this);
                            findUserAdapter.setArrayList(searchUserArrayList);
                            getListView().setAdapter(findUserAdapter);
                            getListView().setSelection(oldPosition);
                        }
                    });
                } else {
                    setmHasSearchUsers(false);
                    setmIsGetingSrearchUsers(false);
                }
            }
        });
    }

    public void loadMoreRecommendUsers() {
        if (!ismHasRecommendUsers() || ismIsGetingRecommendUsers()) {
            return;
        }
        setmIsGetingRecommendUsers(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<BasicUser> arrayList = DataManager.getInstance().getRecommendUsers(recommendUsersarrayList.size(), true);
                if (getActivity() == null) {
                    setmHasRecommendUsers(false);
                    setmIsGetingRecommendUsers(false);
                    return;
                }
                if (arrayList != null && !arrayList.isEmpty()) {
                    if (arrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                        setmHasRecommendUsers(false);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            FindUserAdapter findUserAdapter = new FindUserAdapter();
                            findUserAdapter.setBaseFragment(SearchPersonFragment.this);
                            findUserAdapter.setArrayList(recommendUsersarrayList);
                            getListView().setAdapter(findUserAdapter);
                            getListView().setSelection(oldPosition);
                            int size = recommendUsersarrayList.size();
                            for (int i = 0; i < size; ++i) {
                                if (!recommendUsersarrayList.get(i).getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                                    if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT) && !isHidden()) {
                                        showTourialView(i);
                                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT, true);
                                    }
                                    break;
                                }
                            }
                        }
                    });
                    setmIsGetingRecommendUsers(false);
                } else {
                    setmHasRecommendUsers(false);
                    setmIsGetingRecommendUsers(false);
                }
            }
        });
    }

    private void getRecommendUsersFromServer(boolean refresh) {
        recommendUsersarrayList = DataManager.getInstance().getRecommendUsers(0, refresh);
        if(getActivity() == null) {
            setmHasRecommendUsers(false);
            setmIsGetingRecommendUsers(false);
            return;
        }
        if (recommendUsersarrayList != null && !recommendUsersarrayList.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getView() == null) {
                        return;
                    }
                    if (getSwipeRefreshLayout().isRefreshing()) {
                        getSwipeRefreshLayout().setRefreshing(false);
                    }
                    FindUserAdapter findUserAdapter = new FindUserAdapter();
                    findUserAdapter.setBaseFragment(SearchPersonFragment.this);
                    findUserAdapter.setArrayList(recommendUsersarrayList);
                    getListView().setAdapter(findUserAdapter);

                    int size = recommendUsersarrayList.size();
                    if (size < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                        setmHasRecommendUsers(false);
                    }
                    for (int i = 0; i < size; ++i) {
                        if (!recommendUsersarrayList.get(i).getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                            if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT) && !isHidden()) {
                                showTourialView(i);
                                Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT, true);
                            }
                            break;
                        }
                    }
                }
            });
            setmIsGetingRecommendUsers(false);
        }else{
            setmHasRecommendUsers(false);
            setmIsGetingRecommendUsers(false);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getSwipeRefreshLayout().isRefreshing()) {
                        getSwipeRefreshLayout().setRefreshing(false);
                    }
                }
            });
        }
    }

    public void showTourialView(final int index) {

        final View rlTourialView = getActivity().findViewById(R.id.rlTourial);
        rlTourialView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getView() == null) {
                    return;
                }
                rlTourialView.setVisibility(View.VISIBLE);
                View tourialView5 = rlTourialView.findViewById(R.id.tourialView5);
                tourialView5.setVisibility(View.VISIBLE);
                Utils.setSubText((TextView) tourialView5.findViewById(R.id.tv5),
                        ServerDataManager.getTextFromKey("fndusr_txt_taptofollow"), ServerDataManager.getTextFromKey("fndusr_txt_tap"),
                        Color.WHITE, getResources().getColor(R.color.color_255_143_51));
                View itemView = getListView().getChildAt(index + 1);
                if (itemView == null) {
                    return;
                }
                int[] location1 = getInScreen(itemView);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView5.getLayoutParams();

                lp.topMargin = location1[1] + (itemView.getHeight() - Utils.dip2px(getActivity(), 37)) / 2 - Utils.getStatusBarHeight(getActivity());
                lp.leftMargin = ((BaseActivity) getActivity()).getDisplaymetrics().widthPixels - Utils.dip2px(getActivity(), 200);
                rlTourialView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.findViewById(R.id.tourialView5).setVisibility(View.GONE);
                        v.setVisibility(View.GONE);
                    }
                });
            }
        }, 500);

    }
    private ListView getListView() {
        return (ListView) getView().findViewById(listView);
    }


    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
    }

    public boolean ismHasRecommendUsers() {
        return mHasRecommendUsers;
    }

    public void setmHasRecommendUsers(boolean mHasRecommendUsers) {
        this.mHasRecommendUsers = mHasRecommendUsers;
    }

    public boolean ismIsGetingRecommendUsers() {
        return mIsGetingRecommendUsers;
    }

    public void setmIsGetingRecommendUsers(boolean mIsGetingRecommendUsers) {
        this.mIsGetingRecommendUsers = mIsGetingRecommendUsers;
    }

    public boolean ismHasSearchUsers() {
        return mHasSearchUsers;
    }

    public void setmHasSearchUsers(boolean mHasSearchUsers) {
        this.mHasSearchUsers = mHasSearchUsers;
    }

    public boolean ismIsGetingSrearchUsers() {
        return mIsGetingSrearchUsers;
    }

    public void setmIsGetingSrearchUsers(boolean mIsGetingSrearchUsers) {
        this.mIsGetingSrearchUsers = mIsGetingSrearchUsers;
    }
}
