package com.yeemos.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.adapter.FindUserAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.VerticalSwipeRefreshLayout;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-7-20.
 */
public class FindUserFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<BasicUser> basicUserArrayList;
    private boolean mHasData = false;
    private boolean mIsGetingData;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_find_user_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
        View headView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_find_user_list_header, null);
        initHeaderView(headView);
        getListView().addHeaderView(headView, null, true);
        getListView().setAdapter(null);
        getData(false);
    }

    private void getData(final boolean isGetNewData) {
        setmHasData(true);
        setmIsGetingData(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSwipeRefreshLayout().setRefreshing(true);

                    }});
                basicUserArrayList = DataManager.getInstance().getRecommendUsers(0, isGetNewData);
                if (basicUserArrayList != null && !basicUserArrayList.isEmpty()) {
                    if (basicUserArrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
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
                            if (getSwipeRefreshLayout().isRefreshing()) {
                                getSwipeRefreshLayout().setRefreshing(false);
                            }
                            showData(basicUserArrayList);
                        }
                    });
                    setmIsGetingData(false);
                }else{
                    setmHasData(false);
                    setmIsGetingData(false);
                    if (getActivity() == null) {
                        return;
                    }
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
        });
    }

    public void loadMoreData() {
        if (!ismHasData() || ismIsGetingData()) {
            return;
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                ArrayList<BasicUser> basicUserList = DataManager.getInstance().getRecommendUsers(basicUserArrayList.size(), true);
                if (basicUserList != null && !basicUserList.isEmpty()) {
                    if (basicUserList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
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
                            showData(basicUserArrayList);
                        }
                    });
                    setmIsGetingData(false);
                } else {
                    setmHasData(false);
                    setmIsGetingData(false);
                    if (getActivity() == null) {
                        return;
                    }
                }
            }
        });
    }

    private void showData(ArrayList<BasicUser> basicUserArrayList) {
        FindUserAdapter findUserAdapter = new FindUserAdapter();
        findUserAdapter.setBaseFragment(this);
        findUserAdapter.setArrayList(basicUserArrayList);
        getListView().setAdapter(findUserAdapter);

        if (basicUserArrayList != null) {
            int size = basicUserArrayList.size();
            for (int i = 0; i < size; ++i) {
                if (!basicUserArrayList.get(i).getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT) && !isHidden()) {
                        showTourialView(i);
                        Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_FIND_USERS_FRAGMENT, true);
                    }
                    break;
                }
            }
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

    private void initHeaderView(View headerView) {
        headerView.findViewById(R.id.addByUser).setOnClickListener(this);
        headerView.findViewById(R.id.addfromAddress).setOnClickListener(this);
        headerView.findViewById(R.id.addfromFB).setOnClickListener(this);
        headerView.findViewById(R.id.addfromIG).setOnClickListener(this);
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    private ListView getListView() {
        return (ListView) getView().findViewById(R.id.listView);
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.pageTitle, "fndusr_ttl_finduser");
        setOnlineText(R.id.addByUser, "fndusr_btn_addbyusername");
        setOnlineText(R.id.addfromAddress, "fndusr_btn_addfromaddressbook");
        setOnlineText(R.id.addfromFB, "fndusr_btn_addbyfacebook");
        setOnlineText(R.id.addfromIG, "fndusr_btn_addbyinstagram");
        setOnlineText(R.id.recommendUser, "fndusr_txt_recommendeduser");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    private VerticalSwipeRefreshLayout getSwipeRefreshLayout() {
        return (VerticalSwipeRefreshLayout)getView().findViewById(R.id.swipeRefreshLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.addByUser:
                gotoPager(AddByUserFragment.class, null);
                break;
            case R.id.addfromAddress:
                gotoPager(AddByAddressFragment.class, null);
                break;
            case R.id.addfromFB:
                if (GBSMemberShipManager.getInstance().getFacebookAccessToken() != null) {
                    gotoPager(AddByFBFragment.class, null);
                } else {
                    showLoadingDialog(null, null, true);
                    MemberShipManager.getInstance().loginByFaceBook(getActivity(),
                            new GBSMemberShipManager.memberShipThirdPartyCallBack<GBUserInfo>() {
                                @Override
                                public void timeOut() {
                                    hideLoadingDialog();
                                    Logs("FB登录超时");
                                }

                                @Override
                                public void success(GBUserInfo obj) {
                                    hideLoadingDialog();
                                    Logs("FB登录成功 用户信息为:" + obj);
//                                    startActivity(new Intent(getActivity(), HomeActivity.class));
                                    gotoPager(AddByFBFragment.class, null);
                                    getActivity().finish();
                                }

                                @Override
                                public void fail(String errorStr) {
                                    hideLoadingDialog();
                                    MemberShipManager.getInstance().getFacebook(getActivity()).logout(null);
                                    Logs("FB登录失败 : " + errorStr);
                                }

                                @Override
                                public void cancel() {
                                    hideLoadingDialog();
                                    Logs("FB登录取消");
                                }

                                @Override
                                public void needToMatchDisplayName() {
                                    hideLoadingDialog();
                                    Logs("此次FB登录需要绑定用户名来注册,所以需要设置displayName");
                                    Preferences.getInstacne().setThirdPartyType(
                                            GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook.GetValues());
                                    gotoPager(AddByFBFragment.class, null);
//                                gotoMatchDisplayNamePageWithAccessTokenForInPage(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook);
                                }
                            });
                }
                break;
            case R.id.addfromIG:
//                gotoPager(AddByFBFragment.class, null);
                break;
        }
    }

    @Override
    public void onRefresh() {
        getData(true);
    }

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        if(dataType == 0) {
            if(oprateType == 0 || oprateType == 2){
                for(BasicUser basicUser : basicUserArrayList) {
                    if(((BasicUser)data).getUserId().equals(basicUser.getUserId())){
                        basicUserArrayList.remove(basicUser);
                        break;
                    }
                }
                getListView().setAdapter(null);
                showData(basicUserArrayList);
            }
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
