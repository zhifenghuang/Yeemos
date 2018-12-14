package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.message;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.R;
import com.yeemos.app.adapter.ActivitiesAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-2.
 */
public class ActivitiesFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<message> mMessageList;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_activities_fragment;
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
        setOnlineText(R.id.tvTitle, "actvty_ttl_activities");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
//        ((ListView) view.findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mMessageList == null || mMessageList.isEmpty()) {
            getSwipeRefreshLayout().setProgressViewOffset(false, 0, Utils.dip2px(getActivity(), 24));
            getSwipeRefreshLayout().setRefreshing(true);
            getMessageList(DataManager.getInstance().isMessageListEmty() ? true : false);
        } else {
            getActivitiesAdapter().setArrayList(mMessageList);
        }
    }

    public ActivitiesAdapter getActivitiesAdapter() {
        ListView listView = (ListView) getView().findViewById(R.id.listView);
        if (listView.getAdapter() == null) {
            ActivitiesAdapter adapter = new ActivitiesAdapter(getActivity());
            listView.setAdapter(adapter);
        }
        return (ActivitiesAdapter) listView.getAdapter();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
        }
    }

    @Override
    public void onRefresh() {
        getMessageList(true);
    }

    private void getMessageList(final boolean isGetNewData) {
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
                ArrayList<message> arrayList = (ArrayList<message>) DataManager.getInstance().getPagerDataResource(
                        0, Constants.MPagerListMode.MPagerListMode_ACTIVITIES_You, isGetNewData);
                if (getActivity() == null) {
                    return;
                }
                if (mMessageList == null) {
                    mMessageList = new ArrayList<>();
                }
                mMessageList.clear();
                if (arrayList != null && !arrayList.isEmpty()) {
                    String timeStr;
                    MyFirebaseMessagingService.PushType pushType;
                    boolean isTargetPostBean;
                    for (message msg : arrayList) {
                        pushType = MyFirebaseMessagingService.PushType.valueOf(msg.getMeesageType());

                        isTargetPostBean = pushType == MyFirebaseMessagingService.PushType.TYPE_OTHER_REPLY_TAG_FOR_YOUR_POST
                                || pushType == MyFirebaseMessagingService.PushType.TYPE_OTHER_COMMENT_YOUR_POST
                                || pushType == MyFirebaseMessagingService.PushType.TYPE_OTHER_DRAWING_COMMENT;
                        if (pushType == MyFirebaseMessagingService.PushType.TYPE_OTHER_FOLLOW_YOU
                                || isTargetPostBean) {
                            timeStr = Utils.getTime(msg.getCreateTime());
                            msg.setTimeStr(timeStr);
                            boolean isAdd = false;
                            for (message message : mMessageList) {
                                if (pushType.value() == message.getMeesageType() && message.getTimeStr().equals(timeStr)) {
                                    if (isTargetPostBean) {
                                        if (msg.getTargetObject().getId().equals(message.getTargetObject().getId())) {
                                            isAdd = true;
                                        }
                                    } else {
                                        isAdd = true;
                                    }
                                    if (isAdd) {
                                        ArrayList<BasicUser> operatorUsers = message.getOperatorUsers();
                                        if (operatorUsers == null) {
                                            operatorUsers = new ArrayList<>();
                                            message.setOperatorUsers(operatorUsers);
                                        }
                                        boolean isInoperatorUsers = false;
                                        for (BasicUser user : operatorUsers) {
                                            if (user.getUserId().equals(msg.getCreateUser().getUserId())) {
                                                isInoperatorUsers = true;
                                                break;
                                            }
                                        }
                                        if (!isInoperatorUsers) {
                                            operatorUsers.add(msg.getCreateUser());
                                        }
                                        break;
                                    }
                                }
                            }
                            if (!isAdd) {
                                ArrayList<BasicUser> operatorUsers = new ArrayList<>();
                                msg.setOperatorUsers(operatorUsers);
                                operatorUsers.add(msg.getCreateUser());
                                mMessageList.add(msg);
                            }
                        } else {
                            mMessageList.add(msg);
                        }
                    }
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
                        getActivitiesAdapter().setArrayList(mMessageList);
                        if (!isGetNewData) {
                            getMessageList(true);
                        }
                    }
                });
            }
        });

    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
    }
}
