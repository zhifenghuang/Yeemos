package com.yeemos.app.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.adapter.GroupUserAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;

import java.util.ArrayList;

import static com.yeemos.app.R.id.groupName;

/**
 * Created by gigabud on 17-3-2.
 */

public class NewGroupView extends ChooseFriendView implements View.OnClickListener {

    private OnAddNewGroupListener onAddNewGroupListener;
    protected ArrayList<FriendGroup> friendGroupsList;

    private Dialog dialog;

    public interface OnAddNewGroupListener {
        void addNewGroup(FriendGroup friendGroup);
    }

    public void setFriendGroupsList(ArrayList<FriendGroup> friendGroupsList) {
        this.friendGroupsList = friendGroupsList;
    }

    public NewGroupView(Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.layout_new_or_edit_group_view;
    }

    public void setOnAddNewGroupListener(OnAddNewGroupListener onAddNewGroupListener) {
        this.onAddNewGroupListener = onAddNewGroupListener;
    }

    @Override
    protected void initView() {
        getTitleText().setText(ServerDataManager.getTextFromKey("nwedtgrp_ttl_newgroup"));
        getDone().setText(ServerDataManager.getTextFromKey("pblc_btn_done"));
        getGroupName().setText(ServerDataManager.getTextFromKey("nwedtgrp_txt_groupname"));
        getEtGroupName().setHint(ServerDataManager.getTextFromKey("nwedtgrp_txt_entergroupname"));
        getEtGroupName().setTextSize(12);
        getFriendList().setText(ServerDataManager.getTextFromKey("nwedtgrp_txt_myfriend"));
        getEtGroupName().setFilters(new InputFilter[]{new InputFilter.LengthFilter(50)});
        getImgCloseBtn().setOnClickListener(this);
        getDone().setOnClickListener(this);
        getDone().setAlpha(0.5f);
        getDone().setEnabled(false);
        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BasicUser basicUser = ((GroupUserAdapter) getListView().getAdapter()).getArrayList().get(i);
                basicUser.setSelect(!basicUser.isSelect());
                if (!((GroupUserAdapter) getListView().getAdapter()).hasSelect()) {
                    getDone().setAlpha(0.5f);
                    getDone().setEnabled(false);
                } else {
                    getDone().setAlpha(1.0f);
                    getDone().setEnabled(true);
                }
                ((GroupUserAdapter) getListView().getAdapter()).notifyDataSetChanged();
            }
        });
        getEtGroupName().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() <= 0) {
                    getEtGroupName().setTextSize(12);
                } else {
                    getEtGroupName().setTextSize(17);
                }
            }
        });
    }

    @Override
    public void getData(final boolean isFromCache) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(isFromCache);

                BaseApplication.getCurFragment().getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getSwipeRefreshLayout().setRefreshing(false);
                        GroupUserAdapter mGroupUserAdapter = new GroupUserAdapter();
                        mGroupUserAdapter.setArrayList(allFriends);
                        getListView().setAdapter(mGroupUserAdapter);
                    }
                });

            }
        });
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getContentView().findViewById(R.id.swipeRefreshLayout);
    }

    protected ListView getListView() {
        return (ListView) getContentView().findViewById(R.id.listView);
    }

    private ImageView getImgCloseBtn() {
        return (ImageView) getContentView().findViewById(R.id.imgCloseBtn);
    }

    protected TextView getTitleText() {
        return (TextView) getContentView().findViewById(R.id.titleText);
    }

    protected TextView getDone() {
        return (TextView) getContentView().findViewById(R.id.done);
    }

    private TextView getGroupName() {
        return (TextView) getContentView().findViewById(groupName);
    }

    protected EditText getEtGroupName() {
        return (EditText) getContentView().findViewById(R.id.etGroupName);
    }

    private TextView getFriendList() {
        return (TextView) getContentView().findViewById(R.id.friendList);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgCloseBtn:
                dismiss();
                break;
            case R.id.done:
                if (!((GroupUserAdapter) getListView().getAdapter()).hasSelect()) {
                    return;
                }
                FriendGroup mFriendGroup = hasCreateThisGroup();
                if (mFriendGroup != null) {
                    String content = String.format(ServerDataManager.getTextFromKey("nwedtgrp_txt_samegroupexist"), getGroupName(mFriendGroup));
                    String okay = ServerDataManager.getTextFromKey("pub_btn_ok");
                    BaseApplication.getCurFragment().showPublicDialog(null, content, okay, null, handler);
                    return;
                }
                onAddNewGroupListener.addNewGroup(getFriendGroup());
                createOrUpdateFriendGroup();
                dismiss();
                break;
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
            }
        }
    };

    /**
     * 发送数据
     * 创建或者修改组
     */
    private void createOrUpdateFriendGroup() {

        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                DataManager.getInstance().createOrUpdateFriendGroup(getFriendGroup());
            }
        });
    }

    /**
     * 判断所选的人是否与已经存在的某个组完全相同
     *
     * @return 已经存在的组
     */
    protected FriendGroup hasCreateThisGroup() {
        FriendGroup mFriendGroup = null;
        boolean flag = false;
        String selectUserIDStr = getSelectUserIDStr();
        ArrayList<Integer> selectArrayList = getSelectUserIDList();

        for (FriendGroup friendGroup : friendGroupsList) {

            if (!isTheSameGroup(friendGroup)) {

                ArrayList<Integer> groupUsers = friendGroup.getGroupUsers();

                if (selectArrayList.size() == groupUsers.size()) {
                    for (Integer userID : groupUsers) {

                        if (!selectUserIDStr.contains(userID + ",")) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        mFriendGroup = friendGroup;
                        break;
                    }
                }
            }
        }
        return mFriendGroup;
    }

    /**
     * 用于修改Group
     *
     * @param friendGroup
     * @return
     */
    protected boolean isTheSameGroup(FriendGroup friendGroup) {
        return false;
    }

    /**
     * 选择的好友的ID组成String
     *
     * @return
     */

    protected String getSelectUserIDStr() {
        String selectUserIDStr = "";

        ArrayList<Integer> basicUserIdArralList = getSelectUserIDList();

        for (Integer userID : basicUserIdArralList) {
            selectUserIDStr += userID + ",";
        }
        return selectUserIDStr;
    }

    /**
     * 选择的好友的ID数组
     *
     * @return
     */

    private ArrayList<Integer> getSelectUserIDList() {
        ArrayList<Integer> basicUserIdArralList = new ArrayList<>();

        ArrayList<BasicUser> basicUserArrayList = ((GroupUserAdapter) getListView().getAdapter()).getArrayList();
        for (BasicUser basicUser : basicUserArrayList) {
            if (basicUser.isSelect()) {
                basicUserIdArralList.add(Integer.valueOf(basicUser.getUserId()));
            }
        }
        return basicUserIdArralList;
    }

    /**
     * 创建或修改组
     *
     * @return
     */
    protected FriendGroup getFriendGroup() {
        FriendGroup friendGroup = new FriendGroup();

        if (!TextUtils.isEmpty(getEtGroupName().getText())) {
            friendGroup.setGroupName(getEtGroupName().getText().toString());
        }
        friendGroup.setGroupUsers(getSelectUserIDList());
        return friendGroup;
    }

    /**
     * 根据组获取组名(有的组没有名字,由好友名字组成)
     *
     * @param friendGroup 组
     * @return 组名
     */
    private String getGroupName(FriendGroup friendGroup) {

        String groupName = friendGroup.getGroupName();

        if (TextUtils.isEmpty(groupName)) {
            groupName = "";
            ArrayList<BasicUser> basicUserArrayList = ((GroupUserAdapter) getListView().getAdapter()).getArrayList();
            for (BasicUser basicUser : basicUserArrayList) {
                if (basicUser.isSelect()) {
                    groupName += basicUser.getRemarkName() + ",";
                }
            }

            if (groupName.length() > 50) {
                groupName = groupName.substring(0, 49);
            }
            char[] groupNamechar = groupName.toCharArray();
            if (groupNamechar[groupName.length() - 1] == ',') {
                groupName = groupName.substring(0, groupName.length() - 1);
            }
        }
        return groupName;
    }
}
