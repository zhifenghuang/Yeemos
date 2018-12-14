package com.yeemos.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.adapter.AddByUserAdapter;
import com.yeemos.app.adapter.FindUserAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.VerticalSwipeRefreshLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 16-7-28.
 */
public class AddByUserFragment extends BaseFragment implements View.OnClickListener,SwipeRefreshLayout.OnRefreshListener{

    private ArrayList<BasicUser> searchUserArrayList;
    private ArrayList<BasicUser> recommendUsersarrayList;
    private View headView;
    private String searchStr;
    private Runnable runnable;

    private boolean mHasRecommendUsers = false;
    private boolean mIsGetingRecommendUsers;

    private boolean mHasSearchUsers = false;
    private boolean mIsGetingSrearchUsers;
    private int oldPosition;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_addbyuser_fagment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSearchEditText().setHint(ServerDataManager.getTextFromKey("addbyusrnm_txt_searchbyusername"));
//        getAddByUserText().setText(ServerDataManager.getTextFromKey("addbyusrnm_ttl_Addbyusername"));

        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
        getBtnBack().setOnClickListener(this);
        getBtnCancel().setOnClickListener(this);

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

        getSearchEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                new Handler().postDelayed(getRunnable(), 1 * 1000);

            }
        });

        getArrayList(false);
    }

    private Runnable getRunnable() {

        if (runnable == null) {

            runnable = new Runnable() {
                @Override
                public void run() {
                    getArrayList(false);
                }
            };
        }
        return runnable;
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        if(arrayList == null || arrayList.isEmpty()){
//            getArrayList();
//        }else {
//            getAddByUserAdapter().setArrayList(arrayList);
//        }
//    }

//    public AddByUserAdapter getAddByUserAdapter() {
//        if (getListView().getAdapter() == null) {
//            AddByUserAdapter adapter = new AddByUserAdapter();
//            getListView().setAdapter(adapter);
//        }
//        return (AddByUserAdapter) getListView().getAdapter();
//    }

    private void getArrayList(final boolean refresh) {
        if (!TextUtils.isEmpty(searchStr) && searchStr.equals(getSearchEditText().getText().toString()) && !refresh) {
            return;
        }
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
        setmHasRecommendUsers(true);
        setmHasSearchUsers(true);
        setmIsGetingRecommendUsers(true);
        setmIsGetingSrearchUsers(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getDataAndShow(refresh);
            }

        });



    }

    private void getRecommendUsersFromServer(boolean refresh) {
        recommendUsersarrayList = DataManager.getInstance().getRecommendUsers(0, refresh);
        if (recommendUsersarrayList != null && !recommendUsersarrayList.isEmpty()) {
            if(getActivity() == null) {
                setmIsGetingRecommendUsers(false);
                setmHasRecommendUsers(false);
                return;
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
                    findUserAdapter.setBaseFragment(AddByUserFragment.this);
                    findUserAdapter.setArrayList(recommendUsersarrayList);
                    getListView().setAdapter(findUserAdapter);
                    if (recommendUsersarrayList != null) {
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
                    } else {
                        setmHasRecommendUsers(false);
                    }
                }
            });
            setmIsGetingRecommendUsers(false);
        }else{
            setmIsGetingRecommendUsers(false);
            setmHasRecommendUsers(false);
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

    private void loadMoreRecommendUsers() {
        if (!ismHasRecommendUsers() || ismIsGetingRecommendUsers()) {
            return;
        }
        setmIsGetingRecommendUsers(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<BasicUser> result = DataManager.getInstance().getRecommendUsers(recommendUsersarrayList.size(), true);
                if (result != null && !result.isEmpty()) {
                    if (getActivity() == null) {
                        setmIsGetingRecommendUsers(false);
                        setmHasRecommendUsers(false);
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            FindUserAdapter findUserAdapter = new FindUserAdapter();
                            findUserAdapter.setBaseFragment(AddByUserFragment.this);
                            findUserAdapter.setArrayList(recommendUsersarrayList);
                            getListView().setAdapter(findUserAdapter);
                            getListView().setSelection(oldPosition);
                            if (result == null) {
                                setmHasRecommendUsers(false);
                                return;
                            }
                            int size = result.size();
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
                } else {
                    setmIsGetingRecommendUsers(false);
                    setmHasRecommendUsers(false);
                }
            }
        });

    }

    @Override
    public void loadMoreData() {
        super.loadMoreData();
        if (TextUtils.isEmpty(getSearchEditText().getText().toString())
                || getSearchEditText().getText().toString().equals("")) {
            loadMoreRecommendUsers();
        } else {
            loadMoreSearchUser();
        }
    }

    private synchronized void getDataAndShow(boolean refresh) {
        if (getActivity() == null) {
            return;
        }
        if(TextUtils.isEmpty(getSearchEditText().getText().toString())
                || getSearchEditText().getText().toString().equals("")) {
            if (!refresh) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getListView().removeHeaderView(getNoSearchHeadView());
                        getAddByUserText().setVisibility(View.GONE);
                        getListView().setAdapter(null);
                        getListView().addHeaderView(getNoSearchHeadView(), null, false);
                    }
                });
            }
            searchStr = getSearchEditText().getText().toString();
            getRecommendUsersFromServer(refresh);
        }else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getListView().removeHeaderView(getNoSearchHeadView());
                    getListView().setAdapter(null);
                }
            });
            searchStr = getSearchEditText().getText().toString();
            searchUserArrayList = DataManager.getInstance().findFriendSearchUser(searchStr,
                    0, GBSConstants.SearchDataType.Search_Data_User);

            if (searchUserArrayList == null) {
                setmHasSearchUsers(false);
                searchUserArrayList = new ArrayList<BasicUser>();
            }
            if (searchUserArrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                setmHasSearchUsers(false);
            }
            sortAscendList(searchUserArrayList);

            if (getActivity() == null) {
                setmHasSearchUsers(false);
                setmIsGetingSrearchUsers(false);
                return;
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
                    AddByUserAdapter adapter = new AddByUserAdapter();
                    adapter.setArrayList(searchUserArrayList);
                    getListView().setAdapter(adapter);
                    getAddByUserText().setVisibility(View.VISIBLE);
//                getlistNoDataLayout().setVisibility(View.GONE);
                }
            });
            setmIsGetingSrearchUsers(false);
        }
    }

    private void loadMoreSearchUser() {
        if (!ismHasSearchUsers() || ismIsGetingSrearchUsers()) {
            return;
        }
        setmIsGetingSrearchUsers(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<BasicUser> arrayList = DataManager.getInstance().findFriendSearchUser(getSearchEditText().getText().toString(),
                        searchUserArrayList.size(), GBSConstants.SearchDataType.Search_Data_User);

                if (arrayList == null) {
                    setmHasSearchUsers(false);
                    arrayList = new ArrayList<BasicUser>();
                }
                if (arrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                    setmHasSearchUsers(false);
                }
                searchUserArrayList.addAll(arrayList);
                sortAscendList(searchUserArrayList);

                if (getActivity() == null) {
                    setmHasSearchUsers(false);
                    setmIsGetingSrearchUsers(false);
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }

                        AddByUserAdapter adapter = new AddByUserAdapter();
                        adapter.setArrayList(searchUserArrayList);
                        getListView().setAdapter(adapter);
                        getListView().setSelection(oldPosition);
                    }
                });
                setmIsGetingSrearchUsers(false);
            }
        });
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

    private void sortAscendList(ArrayList<BasicUser> arrayList) {
        Collections.sort(arrayList, new Comparator<BasicUser>() {
            @Override
            public int compare(BasicUser user1, BasicUser user2) {
                return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
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
        setOnlineText(R.id.addByUserText, "addbyusrnm_ttl_Addbyusername");
    }

    private ImageView getBtnBack() {
        return (ImageView)getView().findViewById(R.id.btnBack);
    }

    private ImageView getBtnCancel() {
        return (ImageView)getView().findViewById(R.id.btnCancel);
    }

    private EditText getSearchEditText() {
        return (EditText)getView().findViewById(R.id.searchEditText);
    }



    private TextView getAddByUserText(){
        return (TextView)getView().findViewById(R.id.addByUserText);
    }



    private ListView getListView() {
        return (ListView)getView().findViewById(R.id.listView);
    }

    private VerticalSwipeRefreshLayout getSwipeRefreshLayout() {
        return (VerticalSwipeRefreshLayout)getView().findViewById(R.id.swipeRefreshLayout);
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
            case R.id.btnCancel:
                getSearchEditText().setText("");
                break;
        }
    }

    @Override
    public void onRefresh() {
        getArrayList(true);
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
