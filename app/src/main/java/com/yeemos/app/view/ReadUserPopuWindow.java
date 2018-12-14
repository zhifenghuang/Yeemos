package com.yeemos.app.view;

import android.app.Activity;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.adapter.ReadUserListAdapter;
import com.yeemos.app.manager.DataManager;

import java.util.ArrayList;

import static android.view.View.VISIBLE;

/**
 * 浏览过此post的用户列表
 * Created by gigabud on 17-1-23.
 */

public class ReadUserPopuWindow extends PostPopupWindow {

    private ArrayList<BasicUser> arrayList;
    private Activity context;
    private PostBean postBean;

    public ReadUserPopuWindow(Activity context) {
        super(context);
        this.context = context;
        getTvPostView().setText(ServerDataManager.getTextFromKey("vws_ttl_views"));
        //vws_txt_noview
        getNoFeelText().setText(ServerDataManager.getTextFromKey("vws_txt_noview"));
    }

    public void setPost(PostBean postBean) {
        this.postBean = postBean;
    }


    @Override
    protected void getData() {
        super.getData();
        //获取数据，并显示
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //获取数据
                ServerResultBean<PostBean> obj = DataManager.getInstance().getReadPostUserList(0, postBean);
                if (obj != null && obj.getData() != null) {
                    arrayList = obj.getData().getReadPostUsers();
                    //显示
                    if (BaseApplication.getCurFragment().getActivity() == null) {
                        setmHasData(false);
                        setmIsGetingData(false);
                        return;
                    }
                    BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (getSwipeRefreshLayout().isRefreshing()) {
                                getSwipeRefreshLayout().setRefreshing(false);
                            }
                            if (arrayList == null || arrayList.isEmpty()) {
                                getNoFillLayout().setVisibility(VISIBLE);
                                setmHasData(false);
                                setmIsGetingData(false);
                                return;
                            }
                            getReadUserListAdapter().setArrayList(arrayList);
                        }
                    });
                    setmIsGetingData(false);
                } else {
                    setmHasData(false);
                    setmIsGetingData(false);
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

    private ReadUserListAdapter getReadUserListAdapter() {
        ReadUserListAdapter readUserListAdapter = (ReadUserListAdapter) getListView().getAdapter();
        if (readUserListAdapter == null) {
            readUserListAdapter = new ReadUserListAdapter(context, postBean, this);
            getListView().setAdapter(readUserListAdapter);
        }
        return readUserListAdapter;
    }


    @Override
    public void loadMoreData() {
        super.loadMoreData();
        if (!ismHasData() || ismIsGetingData()) {
            return;
        }
        setmIsGetingData(true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                //获取数据
                ServerResultBean<PostBean> obj = DataManager.getInstance().getReadPostUserList(arrayList.size(), postBean);
                if (obj != null && obj.getData() != null) {
                    final ArrayList<BasicUser> aList = obj.getData().getReadPostUsers();
                    if (aList == null || aList.isEmpty() || aList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                        setmHasData(false);
                    }
                    //显示
                    if (BaseApplication.getCurFragment().getActivity() == null) {
                        setmIsGetingData(false);
                        return;
                    }

                    BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            arrayList.addAll(aList);
                            getReadUserListAdapter().setArrayList(arrayList);
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
}
