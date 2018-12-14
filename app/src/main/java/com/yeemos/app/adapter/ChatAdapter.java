package com.yeemos.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.R;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.ImageMessageView;
import com.yeemos.app.view.MessageItemView;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-30.
 */
public class ChatAdapter extends BaseAdapter {

    private ArrayList<ArrayList<IMsg>> mListChatMsgs;   //信息列表
    private Context mContext;
    private BasicUser mMySelf, mChatUser;   //聊天对象
    private long mMyUserID;
    private OnItemOperation mOnItemOperation;
    private LayoutInflater mLayoutInflater;


    public ChatAdapter(Context context) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    public void setChatUsers(BasicUser mySelf, BasicUser chatUser) {
        mMySelf = mySelf;
        mChatUser = chatUser;
        mMyUserID = Long.parseLong(mySelf.getUserId());
    }

    public void setChatMsgs(ArrayList<IMsg> chatMsgs) {
        if (mListChatMsgs == null) {
            mListChatMsgs = new ArrayList<>();
        }
        mListChatMsgs.clear();
        ArrayList<IMsg> msgList = null;
        long lastUserId = -1;
        for (IMsg msg : chatMsgs) {
            if (msg.getsUID() != lastUserId || msgList == null) {
                msgList = new ArrayList<>();
                msgList.add(msg);
                mListChatMsgs.add(msgList);
            } else {
                msgList.add(msg);
            }
            lastUserId = msg.getsUID();
        }
        mListChatMsgs.add(null);  //最后加一个null的Item为了方便到View可以显示到最后,listview.setSelection()就不会出现bug
    }

    public void addMessage(IMsg msg) {
        if (mListChatMsgs == null) {
            mListChatMsgs = new ArrayList<>();
        }
        int size = mListChatMsgs.size();
        if (size > 0 && mListChatMsgs.get(size - 1) == null) {
            mListChatMsgs.remove(size - 1);
        }
        long lastUserId = -1;
        if (!mListChatMsgs.isEmpty()) {
            lastUserId = mListChatMsgs.get(mListChatMsgs.size() - 1).get(0).getsUID();
        }
        if (lastUserId == msg.getsUID()) {
            mListChatMsgs.get(mListChatMsgs.size() - 1).add(msg);
        } else {
            ArrayList<IMsg> msgList = new ArrayList<>();
            msgList.add(msg);
            mListChatMsgs.add(msgList);
        }
        mListChatMsgs.add(null);  //最后加一个null的Item为了方便到View可以显示到最后,listview.setSelection()就不会出现bug
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mListChatMsgs == null ? 0 : mListChatMsgs.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ArrayList<IMsg> msgList = mListChatMsgs.get(position);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.chat_item, null);
            viewHolder = new ViewHolder();
            viewHolder.mTvUserName = (TextView) convertView
                    .findViewById(R.id.tvUserName);
            viewHolder.mLlMessage = (LinearLayout) convertView
                    .findViewById(R.id.ll);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (msgList == null) {
            viewHolder.mTvUserName.setVisibility(View.GONE);
            viewHolder.mLlMessage.setVisibility(View.GONE);
            return convertView;
        } else {
            viewHolder.mTvUserName.setVisibility(View.VISIBLE);
            viewHolder.mLlMessage.setVisibility(View.VISIBLE);
        }

        long userId = msgList.get(0).getsUID();
        if (mMyUserID == userId) {
            viewHolder.mTvUserName.setText(ServerDataManager.getTextFromKey("cht_txt_me"));
            viewHolder.mTvUserName.setTextColor(mContext.getResources().getColor(R.color.color_142_153_168));
        } else {
            viewHolder.mTvUserName.setText(mChatUser.getRemarkName());
            viewHolder.mTvUserName.setTextColor(mContext.getResources().getColor(R.color.color_45_223_227));
        }
        TextView tvMessage = null;
        ImageMessageView imageMessageView;
        ImageView ivMessageState;
        MessageItemView messageItemView;
        TextView tvSavedState;
        int msgSize = msgList.size();
        int childCount = viewHolder.mLlMessage.getChildCount();
        if (childCount > msgSize) {
            for (int i = msgSize; i < childCount; ++i) {
                viewHolder.mLlMessage.getChildAt(i).setVisibility(View.GONE);
            }
        }
        IMsg msg;
        final int lineWidth = Utils.dip2px(mContext, 1);
        for (int i = 0; i < msgSize; ++i) {
            msg = msgList.get(i);
            String strShowText = "";
            messageItemView = (MessageItemView) viewHolder.mLlMessage.getChildAt(i);
            if (messageItemView == null) {
                messageItemView = (MessageItemView) mLayoutInflater.inflate(R.layout.message_item, null);
                viewHolder.mLlMessage.addView(messageItemView);
            }
            messageItemView.setLineColor(mMyUserID == userId ? mContext.getResources().getColor(R.color.color_142_153_168) : mContext.getResources().getColor(R.color.color_45_223_227));
            messageItemView.setVisibility(View.VISIBLE);
            messageItemView.setTag(msg);

            imageMessageView = (ImageMessageView) messageItemView.findViewById(R.id.imageMessageView);
            imageMessageView.setTag(msg);
            tvSavedState = (TextView) messageItemView.findViewById(R.id.tvSavedState);
            IMsg.IMSG_DELETE_STATUS Del_statuw = msg.getDeleteStatus();
            if (Del_statuw == IMsg.IMSG_DELETE_STATUS.IMSG_DELETE_NEEDSAVE) {
                messageItemView.setLineWidth(3 * lineWidth);
                tvSavedState.setText(ServerDataManager.getTextFromKey("cht_txt_unsaved"));
            } else {
                messageItemView.setLineWidth(lineWidth);
                tvSavedState.setText(ServerDataManager.getTextFromKey("cht_txt_saved"));
            }
            ivMessageState = (ImageView) messageItemView.findViewById(R.id.ivMessageState);
            ivMessageState.setTag(msg);
            ivMessageState.setVisibility(View.INVISIBLE);
            if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE) {
                tvMessage = (TextView) messageItemView.findViewById(R.id.tvMessage);
                tvMessage.setVisibility(View.VISIBLE);
                imageMessageView.setVisibility(View.GONE);
                strShowText = msg.getText();
                if (!Utils.isTextHadChar(strShowText)) {
                    tvMessage.setTextSize(32);
                } else {
                    tvMessage.setTextSize(16);
                }
                tvMessage.setText(strShowText);
                if (msg.getsUID() == mMyUserID) {
                    int statusValue = msg.getSendStatus().GetValues();
                    if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues()) {
                        tvMessage.setTypeface(Typeface.DEFAULT);
                        tvMessage.setTextColor(mContext.getResources().getColor(R.color.color_142_153_168));
                    } else {
                        if (statusValue <= IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SENDING.GetValues()) {
                            tvMessage.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                            tvMessage.setTextColor(Color.BLACK);
                            ivMessageState.setVisibility(View.VISIBLE);
                            ivMessageState.setImageResource(R.drawable.sending_timer);
                        } else if (statusValue == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues()) {
                            ivMessageState.setVisibility(View.VISIBLE);
                            ivMessageState.setImageResource(R.drawable.resend_icon);
                            tvMessage.setTextColor(mContext.getResources().getColor(R.color.color_226_45_45));
                            ivMessageState.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (mOnItemOperation != null) {
                                        mOnItemOperation.onResendMsg((IMsg) v.getTag());
                                    }
                                }
                            });
                        } else {
                            tvMessage.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                            tvMessage.setTextColor(Color.BLACK);
                        }
                    }

                } else {
                    if (((BasicMessage) msg).getReadNum() == 0) {
                        tvMessage.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                        tvMessage.setTextColor(mContext.getResources().getColor(R.color.color_0_0_255));
                    } else {
                        tvMessage.setTypeface(Typeface.DEFAULT);
                        tvMessage.setTextColor(mContext.getResources().getColor(R.color.color_180_180_255));
                    }
                }
            } else if (msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
                messageItemView.findViewById(R.id.tvMessage).setVisibility(View.GONE);
                imageMessageView.setVisibility(View.VISIBLE);
                imageMessageView.setImageMessage(msg, mMyUserID);
                imageMessageView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (mOnItemOperation != null && v.getTag() != null && ((IMsg) v.getTag()).getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
                            mOnItemOperation.onPicClick((IMsg) v.getTag());
                        }
                    }
                });
                imageMessageView.setTag(R.id.tag, messageItemView);
                imageMessageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (v.getTag(R.id.tag) != null) {
                            ((View) v.getTag(R.id.tag)).performLongClick();
                        }
                        return true;
                    }
                });
            }

            messageItemView.setOnLongClickListener(new View.OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemOperation != null) {
                        IMsg msg = (IMsg) v.getTag();
                        mOnItemOperation.onItemLongClick(msg);
                        final IMsg.IMSG_DELETE_STATUS Del_statuw = msg.getDeleteStatus();
                        final TextView tvSavedState = (TextView) v.findViewById(R.id.tvSavedState);
                        if (Del_statuw == IMsg.IMSG_DELETE_STATUS.IMSG_DELETE_NEEDSAVE) {
                            ((MessageItemView) v).setLineWidth(3 * lineWidth);
                        } else {
                            ((MessageItemView) v).setLineWidth(lineWidth);
                        }
                        final View rl = v.findViewById(R.id.rl);
                        final int l = rl.getLeft();
                        final int r = rl.getRight();
                        rl.layout(l - tvSavedState.getWidth(), rl.getTop(), r - tvSavedState.getWidth(), rl.getBottom());
                        rl.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                rl.layout(l, rl.getTop(), r, rl.getBottom());
                                rl.requestLayout();
                                if (Del_statuw == IMsg.IMSG_DELETE_STATUS.IMSG_DELETE_NEEDSAVE) {
                                    tvSavedState.setText(ServerDataManager.getTextFromKey("cht_txt_unsaved"));
                                } else {
                                    tvSavedState.setText(ServerDataManager.getTextFromKey("cht_txt_saved"));
                                }
                            }
                        }, 1500);
                    }
                    return true;
                }
            });
        }
        return convertView;
    }


    private class ViewHolder {
        public TextView mTvUserName;
        public LinearLayout mLlMessage;
    }

    public void setOnItemOperation(OnItemOperation onItemOperation) {
        mOnItemOperation = onItemOperation;
    }

    public interface OnItemOperation {
        public void onItemLongClick(IMsg msg);

        public void onPicClick(IMsg msg);

        public void onResendMsg(IMsg msg);
    }
}
