package com.yeemos.app.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.ConnectedUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.adapter.ChatMessageAdapter;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.BroadcastMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.chat.rpcBean.User;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomListView;
import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigabud on 15-12-28.
 */
public class ChatListFragment extends BaseFragment implements IChatListener, View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<BasicUser> allFriends = null;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chatlist;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        BitmapCacheManager.getInstance().evictAll();
        IChat.getInstance().addChatListener(this);
        getDeleteBtn().setOnClickListener(this);
        getSelectAllBtn().setOnClickListener(this);
        getEditBtn().setOnClickListener(this);
        view.findViewById(R.id.btnCamera).setOnClickListener(this);
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
        view.findViewById(R.id.tvAddFriend).setOnClickListener(this);
    }

    public CustomListView getListView() {
        return (CustomListView) getView().findViewById(R.id.listView);
    }

    private TextView getSelectAllBtn() {
        return (TextView) getView().findViewById(R.id.btnSelectAll);
    }

    private TextView getDeleteBtn() {
        return (TextView) getView().findViewById(R.id.btnDelete);
    }

    private TextView getEditBtn() {
        return (TextView) getView().findViewById(R.id.editChatList);
    }


    public void onResume() {
        super.onResume();
        ((ChatMessageAdapter) getListView().getAdapter()).notifyDataSetChanged();
    }

    public void showOrHideRedPoint() {
        if (getView() == null) {
            return;
        }
        if (Preferences.getInstacne().getValues(HomeActivity.HAD_NEW_NOTIFICATION_MESSAGE, false)
                || Preferences.getInstacne().getValues(HomeActivity.HAD_REQUEST_FOLLOW_YOU_MESSAGE, false)) {
            getView().findViewById(R.id.redPoint).setVisibility(View.VISIBLE);
        } else {
            getView().findViewById(R.id.redPoint).setVisibility(View.GONE);
        }
    }

    @Override
    public void refreshFromNextFragment(Object obj) {
        if (obj != null) {
            BasicUser user = (BasicUser) obj;
            if (allFriends != null) {
                for (BasicUser friend : allFriends) {
                    if (friend.getUserId().equals(user.getUserId())) {
                        friend.setRemarkName(user.getRemarkName());
                        break;
                    }
                }
            }
            ((ChatMessageAdapter) getListView().getAdapter()).changeRecentUserRemarkName(user);
        }
        ((ChatMessageAdapter) getListView().getAdapter()).reOrderListChat(allFriends);
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
        setOnlineText(R.id.tvChatList, "chtlstandcntcts_ttl_chatlist");
        setOnlineText(R.id.editChatList, "chtlstandcntcts_btn_edit");
        setOnlineText(R.id.tvNoFriends, "cht_txt_disappear");
        setOnlineText(R.id.tvAddFriend, "cht_btn_addfriends");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        IChat.getInstance().removeChatListener(this);
    }

    @Override
    public void beginConnect() {

    }

    @Override
    public void connectSuccess() {

    }

    @Override
    public void connectFailure() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void receiveMsg(final IMsg msg) {
        if (getView() == null || getListView() == null) {
            return;
        }
        if (msg.getMessageType() == IMsg.MES_TYPE.BROADCAST_MSG_TYPE) {
            BroadcastMessage broadcastMessage = (BroadcastMessage) msg;
            int btype = broadcastMessage.getBtype();
            if (btype == 2 || btype == 4) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ChatMessageAdapter) getListView().getAdapter()).changeFollowState(String.valueOf(msg.getsUID()), 0);
                        if (allFriends == null || allFriends.isEmpty()) {
                            showOrHideNoFriendView(true);
                        }
                        allFriends = DataManager.getInstance().getAllFriends(true);
                        if (allFriends != null && !allFriends.isEmpty()) {
                            for (BasicUser user : allFriends) {
                                if (user.getUserId().equals(String.valueOf(msg.getsUID()))) {
                                    user.setFollowedStatus(0);
                                    break;
                                }
                            }
                        }
                    }
                });
                return;
            } else if (btype == 3) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((ChatMessageAdapter) getListView().getAdapter()).changeFollowState(String.valueOf(msg.getsUID()), 1);
                        if (allFriends == null || allFriends.isEmpty()) {
                            showOrHideNoFriendView(true);
                        }
                        allFriends = DataManager.getInstance().getAllFriends(true);
                        if (allFriends != null && !allFriends.isEmpty()) {
                            for (BasicUser user : allFriends) {
                                if (user.getUserId().equals(String.valueOf(msg.getsUID()))) {
                                    user.setFollowedStatus(1);
                                    break;
                                }
                            }
                        }
                    }
                });
                getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() != null) {
                            getAllFriends();
                        }
                    }
                }, 1000);
            }
            Utils.saveArrayCache(HomeActivity.FRIEND_LIST, allFriends);
            return;
        }


        if (getView() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getView() != null) {
                        ((ChatMessageAdapter) getListView().getAdapter()).setUserLastMsg(msg);
                    }
                }
            });
        }

    }

    @Override
    public void sendingMsg(IMsg msg) {

    }

    public void offlineMsgRcvd(ArrayList<String> userArr) {

    }

    @Override
    public void msgUploading(IMsg msg, int progress) {

    }

    @Override
    public void msgDownloading(IMsg msg, int progress) {

    }

    @Override
    public void msgError(IMsg msg) {

    }

    @Override
    public void onStart() {
        super.onStart();
        final ChatMessageAdapter adapter = new ChatMessageAdapter(getActivity());
        getListView().setAdapter(adapter);
        adapter.setOnChatUserOperationListener(new ChatMessageAdapter.OnChatUserOperationListener() {
            @Override
            public void deleteBtnClickable() {
                getDeleteBtn().setAlpha(1.0f);
                getDeleteBtn().setClickable(true);
            }

            @Override
            public void deleteBtnUnClickable() {
                getDeleteBtn().setAlpha(0.5f);
                getDeleteBtn().setClickable(false);
            }

            @Override
            public void onChatItemViewClick(BasicUser chatUser) {
                ((HomeActivity) getActivity()).toChatFragmentWithChatUser(chatUser);
            }

            @Override
            public void checkIsSelectAll() {
                ChatMessageAdapter adapter = (ChatMessageAdapter) getListView().getAdapter();
                if (adapter.isAllChect()) {
                    setOnlineText(R.id.btnSelectAll, "chtlstandcntcts_btn_deselectall");
                } else {
                    setOnlineText(R.id.btnSelectAll, "chtlstandcntcts_btn_selectall");
                }
            }
        });
        if (allFriends == null || allFriends.isEmpty()) {
            getAllFriends();
        }
        ArrayList<BasicUser> friends = DataManager.getInstance().getAllFriends(true);
        if (friends != null) {
            allFriends = friends;
        }
        adapter.setChatChatUsers(allFriends);
        updateChatList(null);
        showOrHideNoFriendView(allFriends == null || allFriends.isEmpty());
        showOrHideRedPoint();
    }

    public void resetChatList() {
        if (allFriends == null || allFriends.isEmpty()) {
            getAllFriends();
            showOrHideNoFriendView(true);
        }
    }

    private void showOrHideNoFriendView(boolean isShow) {
        if (getView() == null) {
            return;
        }
        if (isShow) {
            getView().findViewById(R.id.rlNoFriendLayout).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.swipeRefreshLayout).setVisibility(View.GONE);
            getView().findViewById(R.id.editChatList).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.rlNoFriendLayout).setVisibility(View.GONE);
            getView().findViewById(R.id.swipeRefreshLayout).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.editChatList).setVisibility(View.VISIBLE);
        }
    }

    protected void updateChatList(List<UserMsgSummery> msgSummery) {
        if (getView() == null) {
            return;
        }
        final ArrayList<BasicUser> chatUsers = convertUserMsgSummeryToBasicUser(msgSummery);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getView() == null || getListView() == null) {
                    return;
                }
                ChatMessageAdapter adapter = (ChatMessageAdapter) getListView().getAdapter();
                adapter.addRecentChatUsers(chatUsers);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void removeUser() {
        BasicUser basicUser = DataManager.getInstance().getRemoveUser();
        if (basicUser != null) {
            ((ChatMessageAdapter) getListView().getAdapter()).removeUser(basicUser);
            DataManager.getInstance().setRemoveUser(null);
            if (allFriends == null || allFriends.isEmpty()) {
                showOrHideNoFriendView(true);
                return;
            }
            for (BasicUser user : allFriends) {
                if (basicUser.getUserId().equals(user.getUserId())) {
                    allFriends.remove(user);
                    break;
                }
            }
        }
        if (allFriends == null || allFriends.isEmpty()) {
            showOrHideNoFriendView(true);
        }
        Utils.saveArrayCache(HomeActivity.FRIEND_LIST, allFriends);
    }

    /**
     * 消息概述发生变化
     */
    @Override
    public void msgSummeryChange(List<UserMsgSummery> msgSummery) {
        updateChatList(msgSummery);
    }

    protected ArrayList<BasicUser> convertUserMsgSummeryToBasicUser(List<UserMsgSummery> msgSummery) {
        ArrayList<BasicUser> chatUsers = new ArrayList<BasicUser>();
        if (allFriends != null && !allFriends.isEmpty()) {
            BasicUser baseUser = null;
            for (BasicUser user : allFriends) {
                ArrayList<IMsg> iMsgsList = IChat.getInstance().getPeerLastChatMsgList(Integer.parseInt(user.getUserId()), 1);
                if (iMsgsList != null && iMsgsList.size() > 0) {
                    user.setLastMessage(iMsgsList.get(0));
                    baseUser = new BasicUser();
                    baseUser.setUserId(user.getUserId());
                    baseUser.setUserName(user.getUserName());
                    baseUser.setNick(user.getNick());
                    baseUser.setRemarkName(user.getRemarkName());
                    baseUser.setLastPost(user.getLastPost());
                    baseUser.setSetting(user.getSetting());
                    baseUser.setAvatar(user.getAvatar());
                    baseUser.setFollowStatus(user.getFollowStatus());
                    baseUser.setFollowedStatus(user.getFollowedStatus());
                    baseUser.setIsAuthenticate(user.isAuthenticate() ? 1 : 0);
                    baseUser.setLastMessage(iMsgsList.get(0));
                    chatUsers.add(baseUser);
                } else if (msgSummery != null && !msgSummery.isEmpty()) {
                    long userId = Long.parseLong(user.getUserId());
                    for (UserMsgSummery userMsgSummery : msgSummery) {
                        User userSummery = userMsgSummery.getUser();
                        if (userSummery.getUserId() == userId) {
                            user.setLastMessage(userMsgSummery.getLastMsg());
                            baseUser = new BasicUser();
                            baseUser.setUserId(user.getUserId());
                            baseUser.setUserName(user.getUserName());
                            baseUser.setNick(user.getNick());
                            baseUser.setRemarkName(user.getRemarkName());
                            baseUser.setLastPost(user.getLastPost());
                            baseUser.setSetting(user.getSetting());
                            baseUser.setAvatar(user.getAvatar());
                            baseUser.setFollowStatus(user.getFollowStatus());
                            baseUser.setFollowedStatus(user.getFollowedStatus());
                            baseUser.setIsAuthenticate(user.isAuthenticate() ? 1 : 0);
                            baseUser.setLastMessage(userMsgSummery.getLastMsg());
                            chatUsers.add(baseUser);
                            break;
                        }
                    }
                }
            }
        }
        return chatUsers;
    }

    private RelativeLayout getBootLayout() {
        return (RelativeLayout) getView().findViewById(R.id.bootLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvAddFriend:
                gotoPager(FindUserFragment.class, null);
                break;
            case R.id.btnCamera:
                ((HomeActivity) getActivity()).toCameraFragment();
                break;
            case R.id.editChatList:
                editBtnChange();
                break;
            case R.id.btnSelectAll:
                ChatMessageAdapter adapter = (ChatMessageAdapter) getListView().getAdapter();
                if (adapter.isAllChect()) {
                    ((ChatMessageAdapter) getListView().getAdapter()).setListChatUserChect(false);
                    setOnlineText(R.id.btnSelectAll, "chtlstandcntcts_btn_selectall");
                } else {
                    ((ChatMessageAdapter) getListView().getAdapter()).setListChatUserChect(true);
                    setOnlineText(R.id.btnSelectAll, "chtlstandcntcts_btn_deselectall");
                }
                break;
            case R.id.btnDelete:
                ((ChatMessageAdapter) getListView().getAdapter()).removeChectUser(DataManager.getInstance().getBasicCurUser());
                getBootLayout().setVisibility(View.GONE);
                setOnlineText(R.id.editChatList, "chtlstandcntcts_btn_edit");
                ((HomeActivity) getActivity()).getCustomViewPager().setEditState(false);
                ((HomeActivity) getActivity()).setViewPagerCanScroll(true);
                getListView().setEditState(false);
                ((ChatMessageAdapter) getListView().getAdapter()).setDragLayoutDragEnable(true);
                ((ChatMessageAdapter) getListView().getAdapter()).setListChatUserChect(false);
                getDeleteBtn().setAlpha(0.5f);
                getDeleteBtn().setClickable(false);
                break;
        }
    }

    private void editBtnChange() {
        ChatMessageAdapter adapter = (ChatMessageAdapter) getListView().getAdapter();
        if (((HomeActivity) getActivity()).getCustomViewPager().getEditState()) {
            setOnlineText(R.id.editChatList, "chtlstandcntcts_btn_edit");
            ((HomeActivity) getActivity()).getCustomViewPager().setEditState(false);
            getListView().setEditState(false);
            ((HomeActivity) getActivity()).setViewPagerCanScroll(true);
            adapter.setDragLayoutDragEnable(true);
            getBootLayout().setVisibility(View.GONE);
        } else {
            setOnlineText(R.id.editChatList, "pblc_btn_cancel");
            ((HomeActivity) getActivity()).getCustomViewPager().setEditState(true);
            getListView().setEditState(true);
            ((HomeActivity) getActivity()).setViewPagerCanScroll(false);
            adapter.setDragLayoutDragEnable(false);
            getBootLayout().setVisibility(View.VISIBLE);
            setOnlineText(R.id.btnDelete, "chtlstandcntcts_btn_delete");
            setOnlineText(R.id.btnSelectAll, "chtlstandcntcts_btn_selectall");
        }
        ((ChatMessageAdapter) getListView().getAdapter()).setListChatUserChect(false);
//        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRefresh() {
        getAllFriends();
    }

    private void getAllFriends() {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                allFriends = DataManager.getInstance().getAllFriends(false);
                if (ConnectedUtil.isConnected(BaseApplication.getAppContext())) {
                    Utils.saveArrayCache(HomeActivity.FRIEND_LIST, allFriends);
                }
                if (allFriends == null) {
                    allFriends = new ArrayList<>();
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null || getListView() == null) {
                            return;
                        }
                        ((HomeActivity) getActivity()).resetCameraFragment();
                        showOrHideNoFriendView(allFriends.isEmpty());
                        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
                        if (swipeLayout.isRefreshing()) {
                            swipeLayout.setRefreshing(false);
                        }
                        ChatMessageAdapter adapter = (ChatMessageAdapter) getListView().getAdapter();
                        adapter.setChatChatUsers(allFriends);
                        //     List<UserMsgSummery> listUserMsgSummery = IChat.getInstance().getUserMsgSummery();
                        updateChatList(null);
                        if (((HomeActivity) getActivity()).getCustomViewPager().getCurrentItem() == 2) {
                            showTourialView();
                        }
                    }
                });
            }
        });
    }

    public void showTourialView() {
        if (getListView().getAdapter() == null || getListView().getAdapter().getCount() == 0) {
            return;
        }

        if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_CHAT_LIST_FRAGMENT)) {
            showTourialView1();
            Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_CHAT_LIST_FRAGMENT, true);
        }
    }

    private void showTourialView1() {
        final View rlTourialView = getActivity().findViewById(R.id.rlTourial);
        rlTourialView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rlTourialView.setVisibility(View.VISIBLE);
                final View tourialView8 = rlTourialView.findViewById(R.id.tourialView8);
                tourialView8.setVisibility(View.VISIBLE);
                View itemView = getListView().getChildAt(0).findViewById(R.id.ivEmo);
                final int[] location1 = getInScreen(itemView);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView8.getLayoutParams();
                lp.topMargin = location1[1] + Utils.dip2px(getActivity(), 15) / 2 - Utils.getStatusBarHeight(getActivity());
                lp.leftMargin = (((BaseActivity) getActivity()).getDisplaymetrics().widthPixels - Utils.dip2px(getActivity(), 218)) / 2;

                Utils.setSubText((TextView) tourialView8.findViewById(R.id.tv8),
                        ServerDataManager.getTextFromKey("chtlstandcntcts_txt_swipeleftortaptochat"), ServerDataManager.getTextFromKey("chtlstandcntcts_txt_swipeleft"),
                        ServerDataManager.getTextFromKey("chtlstandcntcts_txt_tap"),
                        Color.WHITE, getResources().getColor(R.color.color_34_166_166), getResources().getColor(R.color.color_255_143_51));
                rlTourialView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View tourialView9 = rlTourialView.findViewById(R.id.tourialView9);
                        if (tourialView8.getVisibility() == View.VISIBLE) {
                            v.findViewById(R.id.tourialView8).setVisibility(View.GONE);

                            tourialView9.setVisibility(View.VISIBLE);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView9.getLayoutParams();
                            lp.topMargin = location1[1] + Utils.dip2px(getActivity(), 15) / 2 - Utils.getStatusBarHeight(getActivity());
                        } else {
                            tourialView9.setVisibility(View.GONE);
                            rlTourialView.setVisibility(View.GONE);
                        }

                    }
                });
            }
        }, 300);
    }
}
