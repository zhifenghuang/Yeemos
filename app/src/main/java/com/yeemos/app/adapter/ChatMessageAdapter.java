package com.yeemos.app.adapter;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.BasicStatusMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomUrlImageView;
import com.yeemos.app.view.ViewDragLayout;
import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by gigabud on 15-12-29.
 */
public class ChatMessageAdapter extends BaseAdapter {
    private Context mContext;

    private ArrayList<BasicUser> mListChatUser;

    private ArrayList<BasicUser> mRecentListChatUser;

    private boolean dragEnable = true;

    private OnChatUserOperationListener onChatUserOperationListener;

    private long mMyUserID;

    private static final long SHOW_LAST_POST_MAX_TIME = 6 * 3600 * 1000;//6小时以内的Post才需要显示

    //    private int mCurrentShowType = ChatListFragment.SHOW_TYPE_RECENT_CHAT;
    public interface OnChatUserOperationListener {
        void deleteBtnClickable();

        void deleteBtnUnClickable();

        void onChatItemViewClick(BasicUser chatUser);

        void checkIsSelectAll();
    }

    public ChatMessageAdapter(Context context) {
        mContext = context;
        mMyUserID = Long.parseLong(DataManager.getInstance().getBasicCurUser().getUserId());
    }

//    private Character mLastChar;

    public void setCurrentShowType(int currentShowType) {
//        mCurrentShowType = currentShowType;
//        mLastChar = null;
    }

    public void removeUser(BasicUser basicUser) {
        if (mListChatUser != null) {
            ArrayList<BasicUser> deleteList = new ArrayList<>();
            for (int i = 0; i < mListChatUser.size(); i++) {
                BasicUser user = mListChatUser.get(i);
                if (basicUser.getUserId().equals(user.getUserId())) {
                    deleteList.add(user);
                }
            }
            mListChatUser.removeAll(deleteList);
        }
        for (int i = 0; mRecentListChatUser != null && i < mRecentListChatUser.size(); i++) {
            BasicUser user = mRecentListChatUser.get(i);
            if (basicUser.getUserId().equals(user.getUserId())) {
                mRecentListChatUser.remove(i);
            }
        }
        notifyDataSetChanged();
    }

    public void addRecentChatUsers(ArrayList<BasicUser> chatUsers) {
        if (mListChatUser == null) {
            mListChatUser = new ArrayList<>();
        }
        if (mRecentListChatUser == null) {
            mRecentListChatUser = new ArrayList<>();
        }
        mListChatUser.removeAll(mRecentListChatUser);
        for (int i = 0; i < chatUsers.size(); ) {
            if (chatUsers.get(i).getLastMessage() == null) {
                chatUsers.remove(i);
            } else {
                ++i;
            }
        }
        if (!chatUsers.isEmpty()) {
            Collections.sort(chatUsers, new Comparator<BasicUser>() {
                @Override
                public int compare(BasicUser user1, BasicUser user2) {
                    return String.valueOf(((BasicMessage) user2.getLastMessage()).getCliTime()).compareTo(String.valueOf(((BasicMessage) user1.getLastMessage()).getCliTime()));
                }
            });
        }
        mListChatUser.addAll(0, chatUsers);
        mRecentListChatUser.clear();
        mRecentListChatUser.addAll(chatUsers);
    }


    public void setUserLastMsg(IMsg msg) {
        if (mListChatUser != null) {
            // detect peer userID
            if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE || msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
                long peerUserId = Long.parseLong(DataManager.getInstance().getBasicCurUser().getUserId()) == msg.getrUID() ? msg.getsUID() : msg.getrUID();
                for (BasicUser user : mListChatUser) {
                    if (Long.parseLong(user.getUserId()) == peerUserId) {
                        user.setLastMessage(msg);
                    }
                }

                for (BasicUser user : mListChatUser) {
                    if (Long.parseLong(user.getUserId()) == peerUserId) {
                        mListChatUser.remove(user);
                        mListChatUser.add(0, user);
                        break;
                    }
                }
            } else {
                BasicStatusMessage baseStatsMsg = (BasicStatusMessage) msg;
                IMsg.IMSG_SEND_STATUS send_status = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS;
                boolean bIsSatateMsg = false;
                boolean bIsNeedChange = false;
                switch (baseStatsMsg.getMessageType()) {
                    case MSG_SEND_FAILED: //发送失败
                    {
                        send_status = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE;
                        bIsSatateMsg = true;
                        bIsNeedChange = true;
                    }
                    break;
                    case SEVR_CONFIRM_MSG_TYPE: //发送成功
                    {
                        send_status = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS;
                        bIsSatateMsg = true;
                        bIsNeedChange = true;
                    }
                    break;
                    case PEER_RECV_MSG_TYPE: //对方已收
                    {
                        send_status = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_RECEIVED;
                        bIsSatateMsg = true;
                    }
                    break;
                    case PEER_READ_MSG_TYPE: //对方已读
                    case PEER_RCVD_READ_MSG_TYPE: //对方确认收到 自己发的已读状态 消息
                    {
                        send_status = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ;
                        bIsSatateMsg = true;
                    }
                    break;

                    default:
                        break;
                }

                if (bIsSatateMsg) {
                    int count = 0;
                    IMsg lastMsg;
                    BasicUser needChangUser = null;
                    for (BasicUser user : mListChatUser) {
                        if (user.getLastMessage() != null) {
                            lastMsg = (IMsg) user.getLastMessage();
                            if (lastMsg.getMsgID().equals(baseStatsMsg.getConfirmMsgID())) {
                                lastMsg.setSendStatus(send_status);
                                if (bIsNeedChange && needChangUser == null) {
                                    needChangUser = user;
                                }
                                if (++count >= 2) {
                                    break;
                                }
                            }
                        }
                    }
                    if (needChangUser != null) {
                        if (mRecentListChatUser != null) {
                            for (BasicUser user : mRecentListChatUser) {
                                if (needChangUser.getUserId().equals(user.getUserId())) {
                                    mRecentListChatUser.remove(user);
                                    mRecentListChatUser.add(0, user);
                                    break;
                                }
                            }

                        }

                        for (BasicUser user : mListChatUser) {
                            if (needChangUser.getUserId().equals(user.getUserId())) {
                                mListChatUser.remove(user);
                                mListChatUser.add(0, user);
                                break;
                            }
                        }
                    }
                }
            }
        }

        notifyDataSetChanged();
    }

    public void setChatChatUsers(ArrayList<BasicUser> chatUsers) {
        if (chatUsers == null) {
            return;
        }
        if (mListChatUser == null) {
            mListChatUser = new ArrayList<>();
        }
        mListChatUser.clear();
        mListChatUser.addAll(chatUsers);
        if (!mListChatUser.isEmpty()) {
            Collections.sort(mListChatUser, new Comparator<BasicUser>() {
                @Override
                public int compare(BasicUser user1, BasicUser user2) {
                    return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
                }
            });
        }
    }

    public void reOrderListChat(ArrayList<BasicUser> chatUsers) {
        if (chatUsers == null) {
            return;
        }
        if (mListChatUser == null) {
            mListChatUser = new ArrayList<>();
        }
        mListChatUser.removeAll(chatUsers);
        Collections.sort(chatUsers, new Comparator<BasicUser>() {
            @Override
            public int compare(BasicUser user1, BasicUser user2) {
                return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
            }
        });
        mListChatUser.addAll(chatUsers);
        notifyDataSetChanged();
    }

    public void changeRecentUserRemarkName(BasicUser user) {
        if (user == null || mRecentListChatUser == null || mRecentListChatUser.isEmpty()) {
            return;
        }
        for (BasicUser friend : mRecentListChatUser) {
            if (friend.getUserId().equals(user.getUserId())) {
                friend.setRemarkName(user.getRemarkName());
                break;
            }
        }
    }

    @Override
    public int getCount() {
        if (dragEnable) {
            return mListChatUser == null ? 0 : mListChatUser.size();
        } else {
            return mRecentListChatUser == null ? 0 : mRecentListChatUser.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return mListChatUser == null ? null : mListChatUser.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_message_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            if (viewHolder == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_message_item, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dragEnable) {
                    BasicUser basicUser = ((ViewHolder) v.getTag()).getBasicUser();
                    if (basicUser != null && onChatUserOperationListener != null) {
                        onChatUserOperationListener.onChatItemViewClick(basicUser);
                        if (basicUser.getLastMessage() != null) {
                            IMsg msg = (IMsg) basicUser.getLastMessage();
                            if (msg.getsUID() != mMyUserID) {
                                int statusValue = msg.getRecvStatus().GetValues();
                                if (statusValue < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues()) {
                                    msg.setRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM);
                                    notifyDataSetChanged();
                                }
                            }
                        }
                    }
                }
            }
        });
        viewHolder.setPosition(position);
        return convertView;
    }

    /**
     * 设置Item是否可以拖动
     *
     * @param dragEnable
     */
    public void setDragLayoutDragEnable(boolean dragEnable) {
        this.dragEnable = dragEnable;
    }

    /**
     * 全选
     *
     * @param chect
     */
    public void setListChatUserChect(boolean chect) {
        for (int i = 0; i < getCount(); i++) {
            mListChatUser.get(i).setChect(chect);
        }
        notifyDataSetChanged();
    }

    public boolean isAllChect() {
        if (mListChatUser == null || mListChatUser.isEmpty()) {
            return false;
        }
        for (int i = 0; i < getCount(); i++) {
            if (!mListChatUser.get(i).isChect()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        changeDeleteBtn();
    }

    /**
     * 移除选中的选项
     */
    public void removeChectUser(BasicUser mySelf) {
        ArrayList<BasicUser> removeList = new ArrayList<>();
        ArrayList<Long> userIds = new ArrayList<>();
        List<UserMsgSummery> listUserMsgSummery = IChat.getInstance().getUserMsgSummery();
        for (int i = 0; i < getCount(); i++) {
            BasicUser basicUser = mListChatUser.get(i);
            if (basicUser.isChect()) {
                removeList.add(basicUser);
                if (listUserMsgSummery != null && !listUserMsgSummery.isEmpty()) {
                    for (UserMsgSummery userMsgSummery : listUserMsgSummery) {
                        if (String.valueOf(userMsgSummery.getUser().getUserId()).equals(basicUser.getUserId())) {
                            listUserMsgSummery.remove(userMsgSummery);
                            break;
                        }
                    }
                }
                long userID = Long.parseLong(basicUser.getUserId());
                if (!userIds.contains(userID)) {
                    userIds.add(userID);
                }
            }
        }
        mRecentListChatUser.removeAll(removeList);
        mListChatUser.removeAll(removeList);
        notifyDataSetChanged();
        IChat.getInstance().deleteMsgsRecordByUserIds(userIds, Long.parseLong(mySelf.getUserId()), mySelf.getToken());
        int unreadMsgNum = IMsg.getUnReadMsgFriendNum(Long.parseLong(MemberShipManager.getInstance().getUserID()));
        BadgeUtil.setBadgeCount(mContext, unreadMsgNum);
    }

    /**
     *
     */
    public void removeUserById(String userId) {
        ArrayList<BasicUser> removeList = new ArrayList<>();
        List<UserMsgSummery> listUserMsgSummery = IChat.getInstance().getUserMsgSummery();
        for (int i = 0; i < getCount(); i++) {
            BasicUser basicUser = mListChatUser.get(i);
            if (basicUser.getUserId().equals(userId)) {
                removeList.add(basicUser);
                if (listUserMsgSummery != null && !listUserMsgSummery.isEmpty()) {
                    for (UserMsgSummery userMsgSummery : listUserMsgSummery) {
                        if (String.valueOf(userMsgSummery.getUser().getUserId()).equals(basicUser.getUserId())) {
                            listUserMsgSummery.remove(userMsgSummery);
                            break;
                        }
                    }
                }
            }
        }
        mRecentListChatUser.removeAll(removeList);
        mListChatUser.removeAll(removeList);
        notifyDataSetChanged();
    }


    /**
     *
     */
    public void changeFollowState(String userId, int followState) {
        for (int i = 0; i < getCount(); i++) {
            BasicUser basicUser = mListChatUser.get(i);
            if (basicUser.getUserId().equals(userId)) {
                basicUser.setFollowedStatus(followState);
            }
        }
    }

    /**
     * 根据ListView的当前位置获取分类的首字母的char ascii值
     */
    public int getSectionForPosition(int position) {
        return mListChatUser.get(position).getPinyinName().toUpperCase().charAt(0);
    }

    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
     */
    public int getPositionForSection(int section) {
        for (int i = mRecentListChatUser.size(); i < getCount(); i++) {
            String sortStr = mListChatUser.get(i).getPinyinName();
            char firstChar = sortStr.toUpperCase().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }

        return -1;
    }

    public void setOnChatUserOperationListener(OnChatUserOperationListener onChatUserOperationListener) {
        this.onChatUserOperationListener = onChatUserOperationListener;
    }

    public void changeDeleteBtn() {
        if (onChatUserOperationListener != null) {
            for (int i = 0; i < getCount(); i++) {
                if (mListChatUser.get(i).isChect()) {
                    onChatUserOperationListener.deleteBtnClickable();
                    break;
                }
                if (i == getCount() - 1) {
                    onChatUserOperationListener.deleteBtnUnClickable();
                }
            }
        }
    }

    private class ViewHolder implements View.OnClickListener {
        public ViewDragLayout viewDragLayout;
        public TextView tvTitle;
        public TextView tvChar;
        public ImageView ivEmo;
        public TextView tvUserName;
        public TextView tvText;
        public ImageView editCheckbox;
        public CustomUrlImageView ivLastPost;
        public BasicUser basicUser;

        public ViewHolder(View convertView) {
            viewDragLayout = (ViewDragLayout) convertView.findViewById(R.id.viewdraglayout);
            tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            tvChar = (TextView) convertView.findViewById(R.id.tvChar);
            ivEmo = (ImageView) convertView.findViewById(R.id.ivEmo);
            tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            tvText = (TextView) convertView.findViewById(R.id.tvText);
            editCheckbox = (ImageView) convertView.findViewById(R.id.editCheckbox);
            ivLastPost = (CustomUrlImageView) convertView.findViewById(R.id.ivLastPost);
            editCheckbox.setOnClickListener(this);
        }

        public void setPosition(int position) {
            basicUser = mListChatUser.get(position);
            viewDragLayout.setTag(basicUser);
            viewDragLayout.setDragEnable(dragEnable);
            editCheckbox.setSelected(basicUser.isChect());

            if (dragEnable) {
                editCheckbox.setVisibility(View.GONE);
            } else {
                editCheckbox.setVisibility(View.VISIBLE);
                ivLastPost.setVisibility(View.GONE);
            }
            String displayName = basicUser.getRemarkName();
            tvUserName.setText(displayName);
//        if (mCurrentShowType == ChatListFragment.SHOW_TYPE_RECENT_CHAT) {
            if (position < mRecentListChatUser.size()) {
                tvChar.setVisibility(View.GONE);
//                if (position == 0) {
//                    tvChar.setVisibility(View.VISIBLE);
//                    tvChar.setTextSize(12);
//                    tvChar.setText(ServerDataManager.getTextFromKey("chtlstandcntcts_ttl_recentchat"));
//                    tvChar.setAlpha(0.5f);
//                } else {
//                    tvChar.setVisibility(View.GONE);
//                }
                if (position == 0) {
                    tvTitle.setVisibility(View.VISIBLE);
                    tvTitle.setText(ServerDataManager.getTextFromKey("chtlstandcntcts_ttl_recentchat"));
                } else {
                    tvTitle.setVisibility(View.GONE);
                }
            } else {
                if (position == mRecentListChatUser.size()) {
                    tvTitle.setVisibility(View.VISIBLE);
                    tvTitle.setText(ServerDataManager.getTextFromKey("chtlstandcntcts_ttl_allfriends"));
                } else {
                    tvTitle.setVisibility(View.GONE);
                }
                int section = getSectionForPosition(position);
                if (position == getPositionForSection(section)) {
                    tvChar.setVisibility(View.VISIBLE);
                    tvChar.setText(basicUser.getPinyinName().toUpperCase().charAt(0) + "");
                    tvChar.setTextSize(17);
                    tvChar.setAlpha(1.0f);
                } else {
                    tvChar.setVisibility(View.GONE);
                }
            }
            //    if (basicUser.getLastMessage() == null) {
            ArrayList<IMsg> iMsgsList = IChat.getInstance().getPeerLastChatMsgList(Integer.parseInt(basicUser.getUserId()), 1);
            if (iMsgsList != null && iMsgsList.size() > 0) {
                basicUser.setLastMessage(iMsgsList.get(0));
            } else {
                basicUser.setLastMessage(null);
            }
            //    }
            if (basicUser.getLastMessage() != null) {
                IMsg msg = (IMsg) basicUser.getLastMessage();
                long lastMsgTime = ((BasicMessage) msg).getCliTime();
                String timeStr = Utils.getLastMessageTime(lastMsgTime);
                if (msg.getsUID() == mMyUserID) {
                    int statusValue = msg.getSendStatus().GetValues();
                    if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues()) {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_failed") + timeStr;
                    } else if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS.GetValues()) {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_delivered") + timeStr;
                    } else if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_RECEIVED.GetValues()) {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_received") + timeStr;
                    } else if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues()) {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_opened") + timeStr;
                    } else {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_sending") + timeStr;
                    }
                } else {
                    int statusValue = msg.getRecvStatus().GetValues();
                    if (statusValue < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues() && ((BasicMessage) msg).getReadNum() == 0) {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_taptoopen") + timeStr;
                    } else {
                        timeStr = ServerDataManager.getTextFromKey("chtlstandcntcts_txt_meopened") + timeStr;
                    }
                }
                tvText.setText(timeStr);
            } else {
                tvText.setText("@" + basicUser.getUserName());
            }

            if (dragEnable) {
                PostBean postBean = basicUser.getLastPost();
                if (postBean != null && postBean.getIsPrivate() != 2
                        && Utils.getCurrentServerTime() - postBean.getCreateTime() < SHOW_LAST_POST_MAX_TIME) {
                    ivLastPost.setVisibility(View.VISIBLE);
                    ivLastPost.setViewWH(Utils.dip2px(mContext, 52), Utils.dip2px(mContext, 52));
                    ivLastPost.setNeedRouctRect(true, Utils.dip2px(mContext, 52) / 2);
                    ivLastPost.setPostBean(postBean);
                    ivLastPost.setTag(R.id.post_bean, postBean);
                    ivLastPost.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PostBean pb = (PostBean) v.getTag(R.id.post_bean);
                            if (pb != null) {
                                ArrayList<PostBean> list = new ArrayList<>();
                                list.add(pb);
                                DataManager.getInstance().setShowPostList(list);
                                Bundle b = new Bundle();
                                b.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_FROM_SHOWLIST);
                                b.putInt(ShowPostViewPagerFragment.SHOW_INDEX, 0);
                                ((BaseActivity) mContext).gotoPager(ShowPostViewPagerFragment.class, b);
                            }
                        }
                    });
                    if (basicUser.getLastMessage() != null) {
                        IMsg msg = (IMsg) basicUser.getLastMessage();
                        if (msg.getsUID() != mMyUserID) {
                            if (msg.getRecvStatus().GetValues() < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues()) {
                                ivEmo.setImageResource(Constants.EMO_ID_COLOR[postBean.getTags().get(0).getId()][2]);
                            } else {
                                ivEmo.setImageResource(Constants.EMO_ID_COLOR[postBean.getTags().get(0).getId()][3]);
                            }
                        } else {
                            if (msg.getSendStatus().GetValues() < IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues()) {
                                ivEmo.setImageResource(Constants.EMO_ID_COLOR[postBean.getTags().get(0).getId()][4]);
                            } else {
                                ivEmo.setImageResource(Constants.EMO_ID_COLOR[postBean.getTags().get(0).getId()][5]);
                            }
                        }
                    } else {
                        ivEmo.setImageResource(R.drawable.round_emo_noraml_off);
                    }
                } else {
                    if (basicUser.getLastMessage() != null) {
                        IMsg msg = (IMsg) basicUser.getLastMessage();
                        if (msg.getsUID() != mMyUserID) {
                            int statusValue = msg.getRecvStatus().GetValues();
                            if (statusValue < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues() && ((BasicMessage) msg).getReadNum() == 0) {
                                ivEmo.setImageResource(R.drawable.round_emo_noraml_on);
                            } else {
                                ivEmo.setImageResource(R.drawable.round_emo_noraml_off);
                            }
                        } else {
                            if (msg.getSendStatus().GetValues() < IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues()) {
                                ivEmo.setImageResource(R.drawable.sent_noraml_on);
                            } else {
                                ivEmo.setImageResource(R.drawable.sent_noraml_off);
                            }
                        }
                    } else {
                        ivEmo.setImageResource(R.drawable.round_emo_noraml_off);
                    }
                    ivLastPost.setVisibility(View.GONE);
                }
            } else {
                ivLastPost.setVisibility(View.GONE);
            }
        }

        public BasicUser getBasicUser() {
            return basicUser;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.editCheckbox:
                    basicUser.setChect(basicUser.isChect() ? false : true);
                    editCheckbox.setSelected(basicUser.isChect());
                    changeDeleteBtn();
                    if (onChatUserOperationListener != null) {
                        onChatUserOperationListener.checkIsSelectAll();
                    }
                    break;
            }
        }
    }
}
