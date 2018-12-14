package com.yeemos.app.view;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.adapter.CustomViewAdapter;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.view.View.VISIBLE;

/**
 * Created by gigabud on 17-3-28.
 */

public class CustomView extends PostPopupWindow {

    private PostBean postBean;

    public CustomView(Activity context) {
        super(context);

        getTvPostView().setText(ServerDataManager.getTextFromKey("cstmvw_ttl_customviewtitle"));
        //vws_txt_noview
        getNoFeelText().setText(ServerDataManager.getTextFromKey(""));


        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DataManager.getInstance().setCurOtherUser((BasicUser) getCustomViewAdapter().getItem(position));
                BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
            }
        });
    }

    public void setPost(PostBean postBean) {
        this.postBean = postBean;
    }

    @Override
    protected void getData() {
        super.getData();
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<BasicUser> friendsList = DataManager.getInstance().getAllFriends(true);
                ServerResultBean<PostBean> resultBean = DataManager.getInstance().getOnePostInfo(postBean);
                if (resultBean != null && resultBean.isSuccess()
                        && resultBean.getData() != null && resultBean.getData().getShareUsers() != null) {

                    ArrayList<Integer> cutomViewUserIdList = resultBean.getData().getShareUsers();
                    final ArrayList<BasicUser> cutomViewUserList = new ArrayList<BasicUser>();

                    for (Integer userId : cutomViewUserIdList) {
                        for (BasicUser basicUser : friendsList) {
                            if (basicUser.getUserId().equals(String.valueOf(userId))) {
                                cutomViewUserList.add(basicUser);
                                break;
                            }
                        }
                    }

                    Collections.sort(cutomViewUserList, new Comparator<BasicUser>() {
                        @Override
                        public int compare(BasicUser lhs, BasicUser rhs) {
                            return lhs.getRemarkName().toUpperCase().compareTo(rhs.getRemarkName().toUpperCase());
                        }
                    });

                    BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            getSwipeRefreshLayout().setRefreshing(false);
                            getNoFillLayout().setVisibility(cutomViewUserList.isEmpty() ? View.VISIBLE : View.GONE);

                            getCustomViewAdapter().setArrayList(cutomViewUserList);
                        }
                    });

                } else {
                    BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSwipeRefreshLayout().setRefreshing(false);
                            getNoFillLayout().setVisibility(VISIBLE);
                        }
                    });
                }
            }
        });
    }

    private CustomViewAdapter getCustomViewAdapter() {

        CustomViewAdapter customViewAdapter = (CustomViewAdapter) getListView().getAdapter();
        if (customViewAdapter == null) {
            customViewAdapter = new CustomViewAdapter();
            getListView().setAdapter(customViewAdapter);
        }
        return customViewAdapter;
    }
}
