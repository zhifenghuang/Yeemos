package com.yeemos.app.view;

import android.content.Context;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.adapter.GroupUserAdapter;
import com.yeemos.app.manager.DataManager;

import java.util.ArrayList;

/**
 * Created by gigabud on 17-3-2.
 */

public class EditGroupView extends NewGroupView {

    private FriendGroup friendGroup;

    public EditGroupView(Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setIdArrayList(FriendGroup friendGroup) {
        this.friendGroup = friendGroup;
        getEtGroupName().setText(friendGroup.getGroupName());
        getEtGroupName().setSelection(getEtGroupName().length());
    }

    @Override
    protected void initView() {
        super.initView();
        getTitleText().setText(ServerDataManager.getTextFromKey("nwedtgrp_ttl_editgroup"));

    }

    @Override
    public void getData(final boolean isFromCache) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<BasicUser> allFriends = (ArrayList<BasicUser>) DataManager.getInstance().getAllFriends(isFromCache).clone();

                BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSwipeRefreshLayout().setRefreshing(false);
                        GroupUserAdapter mGroupUserAdapter = new GroupUserAdapter();
                        mGroupUserAdapter.setArrayList(allFriends);
                        getListView().setAdapter(mGroupUserAdapter);
                        if (friendGroup != null) {
                            ((GroupUserAdapter) getListView().getAdapter()).setGroupUserAndRefresh(friendGroup.getGroupUsers());
                        }
                        if (!mGroupUserAdapter.hasSelect()) {
                            getDone().setAlpha(0.5f);
                            getDone().setEnabled(false);
                        } else {
                            getDone().setAlpha(1.0f);
                            getDone().setEnabled(true);
                        }

                    }
                });

            }
        });
    }

    @Override
    protected boolean isTheSameGroup(FriendGroup friendGroup1) {
        return friendGroup1.getId().equals(friendGroup.getId());
    }

    @Override
    public FriendGroup getFriendGroup() {
        return (FriendGroup) super.getFriendGroup().setId(friendGroup.getId());
    }
}
