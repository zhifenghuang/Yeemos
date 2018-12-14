package com.yeemos.app.fragment;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.gbsocial.BeanResponse.BasicResponseBean;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.YeemosTask;
import com.gigabud.core.JobDaddy.DaddyLook;
import com.gigabud.core.JobDaddy.JobDaddy;
import com.gigabud.core.JobDaddy.KittyJob;
import com.gigabud.core.http.HttpUtil;
import com.gigabud.core.http.RequestBean;
import com.gigabud.core.util.GBExecutionPool;
import com.google.gson.Gson;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.adapter.ChatAdapter;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.BasicStatusMessage;
import com.yeemos.app.chat.bean.BroadcastMessage;
import com.yeemos.app.chat.bean.FileURLMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.TextMessage;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.chat.bean.UserStartTyping;
import com.yeemos.app.chat.bean.readIdsBean;
import com.yeemos.app.chat.manager.CRabbitMQChat;
import com.yeemos.app.chat.manager.FileHtttpManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomViewPager;
import com.yeemos.app.view.RenameView;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.view.ShowChatTimeView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigabud on 15-12-30.
 */
@SuppressWarnings("WrongConstant")
public class ChatFragment extends BaseFragment implements View.OnClickListener, IChatListener, DaddyLook {

    public static final String LAST_SEND_TYPING_GCM_TIME = "lastSendTypingGCMTime";

    private static final long SEND_TYPING_GCM_TIME = 30 * 60 * 1000;


    private ChatAdapter mChatAdapter;
    private ArrayList<IMsg> mMsgList;
    private BasicUser mMySelf, mChatUser;

    private TextView mTitleView = null;
    private boolean mIsResetFragment;
    private boolean mIsHadChatListener;
    private InputMethodManager mInputMethodManager;
    private String mSendText;
    private static final int MAX_INPUT_LENGTH = 500;
    private boolean mIsSoftKeyBoardShow;
    private int mMaxListViewBottom;
    private int mCurrentTypeTextSize;   //当键盘弹起时，当前字符个数
    private boolean mIsScreenVisible;
    private boolean mIsNeedResetonCreate = false;
    private Dialog dialog;
    private ListView mListView;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    public void setIsResetFragment(boolean isResetFragment) {
        mIsResetFragment = isResetFragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        if (getActivity() instanceof HomeActivity) {
            ((ImageButton) view.findViewById(R.id.btnChatList)).setImageResource(R.drawable.chat_tabwhiteline);
        } else {
            ((ImageButton) view.findViewById(R.id.btnChatList)).setImageResource(R.drawable.white_back_no_shadow);
            mIsNeedResetonCreate = true;
            mIsResetFragment = false;
        }
        EditText editText = (EditText) view.findViewById(R.id.etChat);
        editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        view.findViewById(R.id.ivShowPic).setVisibility(View.GONE);
        view.findViewById(R.id.picDownloadBar).setVisibility(View.GONE);
        view.findViewById(R.id.tvUserOperation).setVisibility(View.GONE);
        view.findViewById(R.id.ivShowPic).setOnClickListener(this);
        mMySelf = DataManager.getInstance().getBasicCurUser();
        mCurrentTypeTextSize = -1;
        mIsScreenVisible = true;

        CustomViewPager customViewPager = (CustomViewPager) view.findViewById(R.id.customViewPager);
        customViewPager.setScrollLimit(true, Utils.dip2px(getActivity(), 60));
        final ArrayList<View> viewList = new ArrayList<>();
        LayoutInflater lf = LayoutInflater.from(getActivity());
        View view1 = lf.inflate(R.layout.chat_fragment_listview, null);
        viewList.add(view1);
        View view2 = new View(getActivity());
        view2.setBackgroundColor(Color.TRANSPARENT);
        viewList.add(view2);
        customViewPager.setAdapter(new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                // TODO Auto-generated method stub
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return viewList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                // TODO Auto-generated method stub
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                // TODO Auto-generated method stub
                container.addView(viewList.get(position));
                return viewList.get(position);
            }
        });
        mListView = (ListView) view1.findViewById(R.id.listView);
        View listTopView = LayoutInflater.from(getActivity()).inflate(R.layout.chat_fragment_list_top, null);
        mListView.addHeaderView(listTopView);
        ((TextView) listTopView.findViewById(R.id.tvSaveTime)).setText(ServerDataManager.getTextFromKey("cht_txt_disappear"));
        if (mIsNeedResetonCreate) {
            resetChatFragment();
            mIsNeedResetonCreate = false;
        }
        final DrawerLayout drawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED,
                Gravity.END);
    }

    public boolean isCreateView() {
        return getView() != null;
    }

    public void setNeedResetonCreate(boolean isNeedResetonCreate) {
        mIsNeedResetonCreate = isNeedResetonCreate;
    }


    public void resetChatFragment() {
        if (getView() == null) {
            mIsNeedResetonCreate = true;
            return;
        }
        mMySelf = DataManager.getInstance().getBasicCurUser();
        TextView tvChatFriendStateChage = (TextView) getView().findViewById(R.id.tvChatFriendStateChage);
        tvChatFriendStateChage.setVisibility(View.GONE);
        ((DrawerLayout) getView().findViewById(R.id.drawer_layout)).closeDrawers();
        EditText editText = (EditText) getView().findViewById(R.id.etChat);
        editText.setText("");
        if (!mIsResetFragment) {
            mIsScreenVisible = true;
            IChat.getInstance().addChatListener(this);
            JobDaddy.getInstance().addObserver(this);

            mIsHadChatListener = true;
            mIsResetFragment = true;
            mChatUser = DataManager.getInstance().getCurOtherUser();
            mTitleView = (TextView) getView().findViewById(R.id.tvUserName);
            mTitleView.setText(mChatUser.getRemarkName());

            Preferences.getInstacne().setValues(mChatUser.getUserId(), 0);

            // 标记所有的消息为已读
            IChat.getInstance().readUserMsg(Integer.parseInt(mChatUser.getUserId()));

            // 检查是否有缺失的消息并取回来
            IChat.getInstance().detectMissingAndGetMsgs(Long.parseLong(mChatUser.getUserId()));


            mMsgList = IChat.getInstance().getPeerRecentChatRecordList(Integer.parseInt(mChatUser.getUserId()));

            mChatAdapter = new ChatAdapter(getActivity());
            mChatAdapter.setChatUsers(mMySelf, mChatUser);
            mChatAdapter.setChatMsgs(mMsgList);
            mChatAdapter.setOnItemOperation(new ChatAdapter.OnItemOperation() {
                @Override
                public void onItemLongClick(IMsg msg) {    //saveMessage

                    // save or unsave
                    IMsg.IMSG_DELETE_STATUS Del_statuw = msg.getDeleteStatus();
                    if (Del_statuw != IMsg.IMSG_DELETE_STATUS.IMSG_DELETE_NEEDSAVE) {
                        msg.updateDeleteStatus(IMsg.IMSG_DELETE_STATUS.IMSG_DELETE_NEEDSAVE);
                    } else {
                        msg.updateDeleteStatus(IMsg.IMSG_DELETE_STATUS.IMSG_DELETE_DEFAULT);
                    }
                }

                @Override
                public void onPicClick(IMsg msg) {
                    String photoPath = Preferences.getInstacne().getDownloadFilePathByName(msg.getfName());
                    if (msg.getsUID() == Long.parseLong(mMySelf.getUserId()) && msg.getSendStatus().GetValues() == IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues()) {
                        msg.setSendStatus(IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SENDING);
                        IChat.getInstance().reSendMsg(msg);
                        return;
                    }

                    if (getActivity() instanceof HomeActivity) {
                        ((HomeActivity) getActivity()).setViewPagerCanScroll(false);
                    }
                    ((BaseActivity) getActivity()).setScreenFull(true);
                    File imgFile = new File(photoPath);
                    final ImageView ivPic = (ImageView) getView().findViewById(R.id.ivShowPic);
                    ivPic.setVisibility(View.VISIBLE);
                    showOrHideSoftKey(false);
                    if (!imgFile.exists()) {
                        getView().findViewById(R.id.picDownloadBar).setVisibility(View.VISIBLE);
                        String imageURL = Preferences.getInstacne().getChatFileDownloadURLByName(msg.getfName());
                        //         Utils.loadImage(BaseApplication.getAppContext(), 0, imageURL, ivPic);
                        SimpleTarget<Bitmap> target = new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                                if (getView() != null) {
                                    ivPic.setImageBitmap(bitmap);
                                    getView().findViewById(R.id.picDownloadBar).setVisibility(View.GONE);
                                }
                            }
                        };
                        Glide.with(BaseApplication.getAppContext()) // could be an issue!
                                .load(imageURL)
                                .asBitmap()   //强制转换Bitmap
                                .into(target);
                    } else {
                        Utils.loadImage(BaseApplication.getAppContext(), 0, Uri.fromFile(imgFile), ivPic);
                    }
                }

                @Override
                public void onResendMsg(IMsg msg) {   //resend message
                    // resend msgs
                    IChat.getInstance().reSendMsg(msg);
                }
            });
            mListView.setAdapter(mChatAdapter);
            getView().findViewById(R.id.btnCamera).setOnClickListener(this);
            getView().findViewById(R.id.btnChatList).setOnClickListener(this);
            getView().findViewById(R.id.btnUserMoreInfo).setOnClickListener(this);
            mListView.setSelection(mChatAdapter.getCount());
            ((ShowChatTimeView) getView().findViewById(R.id.showChatTimeView)).setListView(mListView);

            editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendMessage();
                        return true;
                    }
                    return false;
                }
            });
            onEditKeyListener(editText);

            if (!TextUtils.isEmpty(mSendText)) {
                editText.setText(mSendText);
            }
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_INPUT_LENGTH)});
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    changeCameraBtnByText(s.toString());
                    if (mChatUser.getFollowedStatus() == 0) {
                        return;
                    }
                    if (mCurrentTypeTextSize != -1 && s.length() != mCurrentTypeTextSize) {
                        mCurrentTypeTextSize = -1;

                        UserStartTyping msg = new UserStartTyping();
                        msg.setrUID(Long.parseLong(mChatUser.getUserId()));
                        IChat.getInstance().sendMsg(msg);

                        String lastSendTypingGCMTime = Preferences.getInstacne().getValues(LAST_SEND_TYPING_GCM_TIME, "");
                        boolean isCanSend = false;
                        if (TextUtils.isEmpty(lastSendTypingGCMTime)) {
                            isCanSend = true;
                            JSONObject object = new JSONObject();
                            try {
                                object.put(mChatUser.getUserId(), System.currentTimeMillis());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            Preferences.getInstacne().setValues(LAST_SEND_TYPING_GCM_TIME, object.toString());
                        } else {
                            JSONObject object;
                            try {
                                object = new JSONObject(lastSendTypingGCMTime);
                                long time = object.getLong(mChatUser.getUserId());
                                if (System.currentTimeMillis() - time > SEND_TYPING_GCM_TIME) {
                                    object.put(mChatUser.getUserId(), System.currentTimeMillis());
                                    isCanSend = true;
                                    Preferences.getInstacne().setValues(LAST_SEND_TYPING_GCM_TIME, object.toString());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                isCanSend = true;
                                object = new JSONObject();
                                try {
                                    object.put(mChatUser.getUserId(), System.currentTimeMillis());
                                } catch (JSONException e1) {
                                    e.printStackTrace();
                                }
                                Preferences.getInstacne().setValues(LAST_SEND_TYPING_GCM_TIME, object.toString());
                            }
                        }

                        if (!isCanSend) {
                            return;
                        }

                        JSONObject jsonObject = new JSONObject();
                        JSONObject jo = new JSONObject();
                        try {
                            jo.put("sUID", mMySelf.getUserId());
                            jo.put("rUID", mChatUser.getUserId());
                            jsonObject.put("pushMessage", jo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String requesturl = Constants.DEBUG_MODE ? Constants.DEBUG_PUSH_MSG_URL : Constants.PRODUCT_PUSH_MSG_URL;
                        final RequestBean request = new RequestBean(requesturl, jsonObject.toString(), 10, 10,
                                BasicResponseBean.class, RequestBean.HttpMethod.POST);
                        GBExecutionPool.getExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                new HttpUtil().httpPost(request.getUrl(), request.getData(),
                                        request.getAppType(), request.getTimeout(), null);
                            }
                        });
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    ((ShowChatTimeView) getView().findViewById(R.id.showChatTimeView)).setListView((ListView) view);
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (view.getBottom() > mMaxListViewBottom) {
                        mMaxListViewBottom = view.getBottom();
                    }
                    if (mMaxListViewBottom - view.getBottom() > 100) {  //此时软弹出键盘
                        if (!mIsSoftKeyBoardShow) {
                            mIsSoftKeyBoardShow = true;
                            mCurrentTypeTextSize = ((EditText) getView().findViewById(R.id.etChat)).getText().toString().length();
                        }
                    } else {
                        if (mIsSoftKeyBoardShow) {
                            mIsSoftKeyBoardShow = false;
                            mCurrentTypeTextSize = -1;
                        }
                    }
                }
            });
            editText.setOnClickListener(this);
            ((TextView) getView().findViewById(R.id.tvDisplayName)).setText(mChatUser.getRemarkName());
            RoundedImageView ivAvater = (RoundedImageView) getView().findViewById(R.id.ivAvater);
            ivAvater.setNeedDrawVipBmp(mChatUser.isAuthenticate());
//            ivAvater.setDefaultImageResId(R.drawable.default_avater);
//            ivAvater.setImageUrl(Preferences.getAvatarUrl(mChatUser.getUserAvatar()));
            Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(mChatUser.getUserAvatar()), ivAvater);
            getView().findViewById(R.id.tvEditName).setOnClickListener(this);
            getView().findViewById(R.id.tvBlock).setOnClickListener(this);
            getView().findViewById(R.id.tvRemoveFriend).setOnClickListener(this);
            ((TextView) getView().findViewById(R.id.tvProfileUserName)).setText("@" + mChatUser.getUserName());
            //          showOrHideSoftKey(true);
            if (!(getActivity() instanceof HomeActivity)) {
                editText.setFocusable(true);
                editText.setFocusableInTouchMode(true);
                editText.requestFocus();
            }

            if (mChatUser.getFollowedStatus() == 0) {
                tvChatFriendStateChage = (TextView) getView().findViewById(R.id.tvChatFriendStateChage);
                tvChatFriendStateChage.setVisibility(View.VISIBLE);
                tvChatFriendStateChage.setText(String.format(ServerDataManager.getTextFromKey("cht_txt_friendunfollow"),
                        mChatUser == null ? "he/she" : mChatUser.getRemarkName()));
            }
        }
    }

    private void sendMessage() {
        if (mChatUser == null) {
            return;
        }
        EditText etChat = (EditText) getView().findViewById(R.id.etChat);
        String sendText = etChat.getText().toString();
        if (sendText == null || sendText.trim().length() == 0) {
            return;
        }
        BasicMessage textMsg = new TextMessage();
        textMsg.setsUID(Long.parseLong(mMySelf.getUserId()));
        textMsg.setrUID(Long.parseLong(mChatUser.getUserId()));
        textMsg.setText(sendText);
        mMsgList.add(textMsg);
        mChatAdapter.addMessage(textMsg);
        IChat.getInstance().sendMsg(textMsg);
        mListView.setSelection(mChatAdapter.getCount());
        etChat.setText("");
        changeCameraBtnByText("");
        mCurrentTypeTextSize = 0;
    }

    private void changeCameraBtnByText(String text) {
        ImageButton btn = (ImageButton) getView().findViewById(R.id.btnCamera);
        if (!Utils.isTextHadChar(text) && text.length() > 0) {
            btn.setImageResource(R.drawable.send_messgae);
            btn.setTag(false);
        } else {
            btn.setImageResource(R.drawable.camera_icon_black);
            btn.setTag(true);
        }
    }

    @Override
    public void refreshFromNextFragment(Object obj) {
        mTitleView.setText(mChatUser.getRemarkName());
        ((TextView) getView().findViewById(R.id.tvDisplayName)).setText(mChatUser.getRemarkName());
        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).getChatListFragment().refreshFromNextFragment(mChatUser);
        }
        showOrHideSoftKey(false);
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            }
        }, 200);
    }

    private void showRenameView() {
        RenameView renameView = new RenameView(getActivity());
        renameView.setEditUser(mChatUser);
        renameView.setFragment(this);
        ViewGroup view = (ViewGroup) getActivity().getWindow().getDecorView();
        FrameLayout content = (FrameLayout) view.findViewById(android.R.id.content);
        ViewGroup viewGroup = (ViewGroup) content.getChildAt(0);
        viewGroup.addView(renameView);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    public void onResume() {
        super.onResume();
        String filePath = DataManager.getInstance().getFilePath();
        if (!TextUtils.isEmpty(filePath)) {
            DataManager.getInstance().setFilePath(null);
            sendPicture(filePath);
        }
        mIsScreenVisible = true;
    }

    public void onPause() {
        super.onPause();
        mIsScreenVisible = false;
        onHiddenChanged(true);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((BaseActivity) getActivity()).setScreenFull(false);
            if (!mIsScreenVisible) {
                if (mChatUser != null) {
                    IChat.getInstance().readUserMsg(Integer.parseInt(mChatUser.getUserId()));
                }
                ImageView ivPic = (ImageView) getView().findViewById(R.id.ivShowPic);
                if (ivPic.getVisibility() == View.VISIBLE) {
                    IMsg msg = (IMsg) ivPic.getTag();
                    String photoPath = Preferences.getInstacne().getDownloadFilePathByName(msg.getfName());
                    File imgFile = new File(photoPath);
                    if (imgFile.exists()) {
                        // 通知服务器已经读取该消息
                        if (msg.getRecvStatus().GetValues() < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM.GetValues()) {
                            IChat.getInstance().readMsg(msg);
                        }
                    }
                }
                mIsScreenVisible = true;
            }
            if (mChatAdapter != null) {
                mChatAdapter.notifyDataSetChanged();
            }
        } else {
            mIsScreenVisible = false;
            // 标记用户又读了次消息
            if (mChatUser != null) {
                IChat.getInstance().markUserReadMsgsOnceMore(Integer.parseInt(mChatUser.getUserId()));
            }
            if (mMsgList != null) {
                for (IMsg msg : mMsgList) {
                    BasicMessage bMsg = (BasicMessage) msg;
                    bMsg.setReadNum(bMsg.getReadNum() + 1);
                }
            }
        }
    }


    private void sendPicture(final String picPath) {
        final EditText editText = (EditText) getView().findViewById(R.id.etChat);
        editText.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsResetFragment = false;
                resetChatFragment();
                FileURLMessage fileURLMsg = new FileURLMessage();
                fileURLMsg.setsUID(Long.parseLong(mMySelf.getUserId()));
                fileURLMsg.setrUID(Long.parseLong(mChatUser.getUserId()));

                File imgFile = new File(picPath);
                if (imgFile.exists()) {
                    String[] strs = picPath.split("/");
                    String fileName = strs[strs.length - 1];
                    fileURLMsg.setfName(fileName);//和自己的文件缓存机制放到一起，以保证统一
                    String thumb = fileName.replace(Constants.IMAGE_EXTENSION, "_s" + Constants.IMAGE_EXTENSION);
                    fileURLMsg.setThumb(thumb); //
                    fileURLMsg.setfSize(imgFile.length());
                    IChat.getInstance().sendMsg(fileURLMsg);
                } else {
                    Toast.makeText(getActivity(), "文件不存在:" + picPath, Toast.LENGTH_SHORT).show();
                }
                mMsgList.add(fileURLMsg);
                mChatAdapter.addMessage(fileURLMsg);
                editText.requestFocus();
                //             showOrHideSoftKey(true);
                mListView.setSelection(mChatAdapter.getCount());
            }
        }, 500);

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
        setOnlineText(R.id.etChat, "cht_txt_texthere");
        setOnlineText(R.id.tvEditName, "usrprfl_btn_editname");
        setOnlineText(R.id.tvBlock, "pblc_btn_block");
        setOnlineText(R.id.tvRemoveFriend, "pblc_btn_removefriend");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    public void removeChatListener() {
        IChat.getInstance().removeChatListener(this);
        JobDaddy.getInstance().removeObserver(this);
        mIsHadChatListener = false;
    }

    public void markUserReadMsgs() {
        if (mChatUser != null) {
            IChat.getInstance().markUserReadMsgsOnceMore(Integer.parseInt(mChatUser.getUserId()));
        }
    }

    @Override
    public void onClick(View v) {
        if (getView() == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.btnCamera:
                boolean isTextHadChar = v.getTag() == null ? true : (Boolean) v.getTag();
                if (isTextHadChar) {
                    showOrHideSoftKey(false);
                    mSendText = ((EditText) getView().findViewById(R.id.etChat)).getText().toString();
                    //    ((HomeActivity) getActivity()).toCameraFragmentFromChat();
                    Bundle bundle = new Bundle();
                    bundle.putInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE, CameraForChatOrAvaterFragment.TYPE_CAMERA_FOR_CHAT);
                    gotoPager(CameraForChatOrAvaterFragment.class, bundle);
                } else {
                    sendMessage();

                    v.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            mListView.setSelection(mChatAdapter.getCount());
                        }
                    }, 200);

                }
                break;
            case R.id.btnChatList:
                showOrHideSoftKey(false);
                if (mIsSoftKeyBoardShow) {
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getActivity() instanceof HomeActivity) {
                                ((HomeActivity) getActivity()).toChatListFragment();
                            } else {
                                goBack();
                            }
                        }
                    }, 500);
                } else {
                    if (getActivity() instanceof HomeActivity) {
                        ((HomeActivity) getActivity()).toChatListFragment();
                    } else {
                        goBack();
                    }
                }

                break;
            case R.id.btnUserMoreInfo:
                if (mIsSoftKeyBoardShow) {
                    showOrHideSoftKey(false);
                    getView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (getView() == null) {
                                return;
                            }
                            ((DrawerLayout) getView().findViewById(R.id.drawer_layout)).openDrawer(Gravity.END);
                            ((DrawerLayout) getView().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                                    Gravity.END);    //解除锁定
                        }
                    }, 500);
                } else {
                    ((DrawerLayout) getView().findViewById(R.id.drawer_layout)).openDrawer(Gravity.END);
                    ((DrawerLayout) getView().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED,
                            Gravity.END);    //解除锁定
                }
                break;
            case R.id.etChat:
                setListViewBottomDelay();
                break;
            case R.id.ivShowPic:
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).setViewPagerCanScroll(true);
                }
                ((BaseActivity) getActivity()).setScreenFull(false);
                v.setVisibility(View.GONE);
                getView().findViewById(R.id.picDownloadBar).setVisibility(View.GONE);
                ((ImageView) v).setImageBitmap(null);
                break;
            case R.id.tvEditName:
                showRenameView();
                break;
            case R.id.tvBlock:
                showPublicDialog(null,
                        ServerDataManager.getTextFromKey("mssg_block_confirmblock"),
                        ServerDataManager.getTextFromKey("pblc_btn_no"),
                        ServerDataManager.getTextFromKey("pblc_btn_yes"), blockDialog);
                break;
            case R.id.tvRemoveFriend:
                showPublicDialog(null,
                        ServerDataManager.getTextFromKey("mssg_removefriend_confirm"),
                        ServerDataManager.getTextFromKey("pblc_btn_no"),
                        ServerDataManager.getTextFromKey("pblc_btn_yes"), removeDialog);
                break;

        }
    }

    Handler removeDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    DataManager.getInstance().setRemoveUser(mChatUser);
                    DataManager.getInstance().follow(mChatUser);
                    onBackKeyClick();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    Handler blockDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    ArrayList<Long> arrayList = new ArrayList<Long>();
                    arrayList.add(Long.parseLong(mChatUser.getUserId()));
                    IChat.getInstance().deleteMsgsRecordByUserIds(arrayList,
                            Long.parseLong(MemberShipManager.getInstance().getUserID()),
                            MemberShipManager.getInstance().getToken());
                    DataManager.getInstance().setRemoveUser(mChatUser);
                    DataManager.getInstance().blockUser(mChatUser);
                    BroadcastMessage m = new BroadcastMessage();
                    m.setBtype(4);
                    m.setsUID(Long.parseLong(MemberShipManager.getInstance().getUserID()));
                    m.setrUID(Long.parseLong(mChatUser.getUserId()));
                    IChat.getInstance().sendMsg(m);
//                    NotifyCenter.sendBoardcastByDataUpdate(Constants.ROME_USER);
                    onBackKeyClick();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        IChat.getInstance().removeChatListener(this);
        JobDaddy.getInstance().removeObserver(this);
        //      DownloadFileManager.getInstance().addDownloadListener(this);
        mIsHadChatListener = false;
    }

    public void showOrHideSoftKey(boolean isShow) {
        if (getView() == null) {
            return;
        }
        if (isShow) {
            EditText et = (EditText) getView().findViewById(R.id.etChat);
            getInputMethodManager().toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            et.postDelayed(new Runnable() {
                @Override
                public void run() {

                    setListViewBottomDelay();
                }
            }, 50);
            et.requestFocus();
        } else {
            if (getInputMethodManager().isActive()) {
                EditText et = (EditText) getView().findViewById(R.id.etChat);
                getInputMethodManager().hideSoftInputFromWindow(et.getWindowToken(), 0);
            }
        }
    }

    private InputMethodManager getInputMethodManager() {
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return mInputMethodManager;
    }

    private void setListViewBottomDelay() {
        if (getView() == null) {
            return;
        }
        mListView.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mChatAdapter == null) {
                    return;
                }
                mListView.setSelection(mChatAdapter.getCount());
                mListView.setSelection(ListView.FOCUS_DOWN);
            }
        }, 200);
    }

    @Override
    public void onBackKeyClick() {
        View view = getView().findViewById(R.id.ivShowPic);
        if (view.getVisibility() == View.VISIBLE) {
            view.performClick();
            setListViewBottomDelay();
            ((BaseActivity) getActivity()).setScreenFull(false);
        } else {
            showOrHideSoftKey(false);
            if (getActivity() instanceof HomeActivity) {
                ((HomeActivity) getActivity()).toChatListFragment();
            } else {
                goBack();
            }
        }
    }

    @Override
    public void beginConnect() {

    }


    @Override
    public void connectSuccess() {

        // _______________


    }

    @Override
    public void connectFailure() {

    }

    @Override
    public void disconnect() {

    }

    /*
     *   检查消息是否已经在聊天列表里
     */
    private boolean detectMsgIfAlreadyIn(final IMsg msg) {
        boolean bisIn = false;
        for (int i = mMsgList.size() - 1; i >= 0; i--) {

            IMsg checkmsg = mMsgList.get(i);
            if (checkmsg.getMsgID().equals(msg.getMsgID())) {
                bisIn = true;
                break;
            }
        }

        return bisIn;
    }

    /**
     * 接收离线消息结束
     *
     * @param userArr
     */
    public void offlineMsgRcvd(ArrayList<String> userArr) {
        final ArrayList<String> finaleuserArr = userArr;

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // 证明离线消息里有属于正在聊天的用户
                if (finaleuserArr.contains(String.valueOf(mChatUser.getUserId()))) {

                    Log.i("ChatFragment", "offlineMsgRcvd msg111:" + finaleuserArr);

                    mIsResetFragment = false;
                    resetChatFragment();
                }

            }
        });


    }

    @Override
    public void sendingMsg(IMsg msg) {

    }


    @Override
    public void receiveMsg(final IMsg msg) {
        if (mChatUser == null || !mIsHadChatListener) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mIsHadChatListener) {
                    return;
                }
                // 需要检查 消息来源用户是否是当前聊天用户再决定刷新UI
                boolean bIsCuurentChatUsersMsg = (Integer.parseInt(mChatUser.getUserId()) == msg.getsUID());

                if (msg.getMessageType().GetValues() == IMsg.MES_TYPE.TYPING_SEND_MSG_TYPE.GetValues()) {
                    if (bIsCuurentChatUsersMsg) {
                        TextView tvUserOperation = (TextView) getView().findViewById(R.id.tvUserOperation);
                        tvUserOperation.setVisibility(View.VISIBLE);
                        tvUserOperation.setText(String.format(ServerDataManager.getTextFromKey("cht_txt_typing"), mChatUser.getRemarkName()));
                        tvUserOperation.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (getView() != null) {
                                    getView().findViewById(R.id.tvUserOperation).setVisibility(View.GONE);
                                }
                            }
                        }, 3000);
                    }
                } else if (msg.getMessageType().GetValues() == IMsg.MES_TYPE.STOP_TYPING_SEND_MSG_TYPE.GetValues()) {
                    if (bIsCuurentChatUsersMsg) {
                        //mTitleView.setText(mChatUser.getUserName() + "取消输入");
                        mTitleView.setText(mChatUser.getRemarkName());
                    }
                } else if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE ||
                        msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
                    //Log.i("ChatFragment", "incoming msg:" + msg.toChatJson());

                    // 得要判断用户还在当前聊天页 并且不是在息屏的时候 jxq
                    if (!bIsCuurentChatUsersMsg) {
                        // 通知服务器已经收取该消息
                        IChat.getInstance().receivedMsg(msg);
                    } else {
                        if (!detectMsgIfAlreadyIn(msg)) {
                            if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE) {
                                if (mIsScreenVisible) {
                                    // 通知服务器已经读取该消息
                                    IChat.getInstance().readMsg(msg);
                                } else {
                                    IChat.getInstance().receivedMsg(msg);
                                }
                            } else {
                                IChat.getInstance().receivingMsg(msg);
                            }

                            // 来源消息位置不确定
                            sortMsgsIfNeeded(msg);

                            // 检查消息readids字段
                            updateReadIds(msg);

                            mListView.setSelection(mChatAdapter.getCount());
                        }
                    }

                } else if (msg.getMessageType() == IMsg.MES_TYPE.BROADCAST_MSG_TYPE) {
                    //  广播消息
                    BroadcastMessage broadcastMessage = (BroadcastMessage) msg;
                    int btype = broadcastMessage.getBtype();
                    if (getView() != null) {
                        if (btype == 2 || btype == 4) {  //2取消Following,4block拉黑 互相unfollow
                            TextView tvChatFriendStateChage = (TextView) getView().findViewById(R.id.tvChatFriendStateChage);
                            tvChatFriendStateChage.setVisibility(View.VISIBLE);
                            tvChatFriendStateChage.setText(String.format(ServerDataManager.getTextFromKey("cht_txt_friendunfollow"),
                                    mChatUser == null ? "he/she" : mChatUser.getRemarkName()));
                            mChatUser.setFollowedStatus(0);
                            ArrayList<BasicUser> allFriend = DataManager.getInstance().getAllFriends(true);
                            if (allFriend != null && !allFriend.isEmpty()) {
                                for (BasicUser user : allFriend) {
                                    if (user.getUserId().equals(mChatUser.getUserId())) {
                                        user.setFollowedStatus(0);
                                        break;
                                    }
                                }
                            }
                        } else if (btype == 3) {
                            TextView tvChatFriendStateChage = (TextView) getView().findViewById(R.id.tvChatFriendStateChage);
                            tvChatFriendStateChage.setVisibility(View.GONE);
                            mChatUser.setFollowedStatus(1);
                            ArrayList<BasicUser> allFriend = DataManager.getInstance().getAllFriends(true);
                            if (allFriend != null && !allFriend.isEmpty()) {
                                for (BasicUser user : allFriend) {
                                    if (user.getUserId().equals(mChatUser.getUserId())) {
                                        user.setFollowedStatus(1);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Log.i("ChatFragment", "Receive message type:" + msg.getMessageType() + " json:" + msg.toChatJson());

                    BasicStatusMessage baseStatsMsg = (BasicStatusMessage) msg;

                    //发送失败
                    if (baseStatsMsg.getMessageType().GetValues() == IMsg.MES_TYPE.MSG_SEND_FAILED.GetValues()) {
                        updateMessageStatus(baseStatsMsg, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);
                    }
                    //发送成功
                    else if (baseStatsMsg.getMessageType().GetValues() == IMsg.MES_TYPE.SEVR_CONFIRM_MSG_TYPE.GetValues()) {
                        updateMessageStatus(baseStatsMsg, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS);
                    }
                    //对方已收
                    else if (baseStatsMsg.getMessageType().GetValues() == IMsg.MES_TYPE.PEER_RECV_MSG_TYPE.GetValues()) {
                        updateMessageStatus(baseStatsMsg, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_RECEIVED);
                    }
                    //对方已读
                    else if (baseStatsMsg.getMessageType().GetValues() == IMsg.MES_TYPE.PEER_READ_MSG_TYPE.GetValues()) {
                        updateMessageStatus(baseStatsMsg, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ);
                    }
                    //对方确认收到 自己发的已读状态 消息
                    else if (baseStatsMsg.getMessageType().GetValues() == IMsg.MES_TYPE.PEER_RCVD_READ_MSG_TYPE.GetValues()) {
                        IMsg msgInmemery = updateMessageStatus(baseStatsMsg, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ);
                        if (msgInmemery != null) {
                            msgInmemery.updateRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM);
                        }
                    }

                    // 检查消息readids字段
                    updateReadIds(msg);

                    // Log.i("update", "a:" +baseStatsMsg.getMessageType().GetValues() + " msg:" + baseStatsMsg.getConfirmMsgID() );

                    // 刷新UI
                    mChatAdapter.notifyDataSetChanged();
                }
                ((ShowChatTimeView) getView().findViewById(R.id.showChatTimeView)).setListView(mListView);
            }
        });

    }

    public void getChatUserOfflineMsg() {
        if (mChatUser != null) {
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ((CRabbitMQChat) CRabbitMQChat.getInstance()).getRabbitMQManager().getPeerOfflineMessage(Long.parseLong(mChatUser.getUserId()), null);
                }
            });
        }
    }

    /**
     * 根据状态，收到的消息 有很小的机会不是顺序的，所以需要检查排序
     *
     * @param msg
     * @return
     */
    private void sortMsgsIfNeeded(IMsg msg) {
        if (!mMsgList.isEmpty()) {
            IMsg lastMsg = mMsgList.get(mMsgList.size() - 1);
            String startSeqNum = msg.getSendSeqNum().split("-")[0];

            if (!lastMsg.getSendSeqNum().startsWith(startSeqNum)) {
                mMsgList.add(msg);
                mChatAdapter.addMessage(msg);
            } else {
                IMsg m;
                int size = mMsgList.size();
                for (int i = size - 1; i >= 0; --i) {
                    m = mMsgList.get(i);
                    if (!m.getSendSeqNum().startsWith(startSeqNum)) {
                        mMsgList.add(i + 1, msg);
                        mChatAdapter.setChatMsgs(mMsgList);
                        break;
                    } else {
                        if (m.getSendSeqNum().compareTo(msg.getSendSeqNum()) <= 0) {
                            mMsgList.add(i + 1, msg);
                            if (i == size - 1) {
                                mChatAdapter.addMessage(msg);
                            } else {
                                mChatAdapter.setChatMsgs(mMsgList);
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            mMsgList.add(msg);
            mChatAdapter.addMessage(msg);
        }

    }

    /**
     * 根据状态，readIds 字段保存了 对方已读的信息
     *
     * @param baseStatsMsg
     * @return
     */
    private void updateReadIds(IMsg baseStatsMsg) {
        try {
            if (baseStatsMsg.getReadIds() != null && baseStatsMsg.getReadIds().length() > 0) {
                // loop to update msglist
                readIdsBean readidObject = new Gson().fromJson(baseStatsMsg.getReadIds(), readIdsBean.class);
                if (readidObject == null) return;

                // Log.i("ChatFragment", "getReadIds:" + baseStatsMsg.getReadIds() );
                String targetUser = readidObject.getrUID();
                if (targetUser.equals(mChatUser.getUserId())) {
                    String[] readIdslist = readidObject.getReadIds().split(",");
                    if (readIdslist.length > 0) {
                        IMsg m;
                        int size = mMsgList.size();
                        for (int i = size - 1; i >= 0; --i) {
                            m = mMsgList.get(i);
                            for (int j = readIdslist.length - 1; j >= 0; --j) {
                                String seqNum = readIdslist[j].replaceAll("'", "");

                                //Log.i("ChatFragment", "getReadIds2:" + seqNum );
                                if (m.getSendSeqNum().equals(seqNum)) {
                                    m.setSendStatus(IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private IMsg updateMessageStatus(BasicStatusMessage baseStatsMsg, IMsg.IMSG_SEND_STATUS sendStatus) {

        IMsg tempMsg = null;

        if (baseStatsMsg != null) {
            for (int i = 0; i < mMsgList.size(); ++i) {
                tempMsg = mMsgList.get(i);
                if (baseStatsMsg.getConfirmMsgID().equals(tempMsg.getMsgID())) {
                    if (tempMsg.getSendStatus().GetValues() < sendStatus.GetValues()) {
                        tempMsg.updateSendStatus(sendStatus);
                    }
                    break;
                }
            }
        }

        return tempMsg;
    }

    @Override
    public void msgUploading(final IMsg msg, final int progress) {
        if (getView() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (updateMsgStaus(msg, progress)) {
                    if (mChatAdapter != null) {
                        mChatAdapter.notifyDataSetChanged();
                    }
                }
            }

        });
    }

    @Override
    public void msgDownloading(final IMsg msg, final int progress) {

        updateMsgStaus(msg, progress);
    }

    @Override
    public void msgError(IMsg msg) {

    }


    private boolean updateMsgStaus(IMsg msg, int progress) {
        IMsg tempMsg = null;
        boolean bFlag = false;
        if (msg != null) {
            for (int i = 0; i < mMsgList.size(); ++i) {
                tempMsg = mMsgList.get(i);

                // Log.i("ChatFragment", "index is:" + i + " confim msg id:" + baseStatsMsg.getConfirmMsgID() + " temp MsgId:" + tempMsg.getMsgID() );
                if (msg.getMsgID().equals(tempMsg.getMsgID())) {
                    tempMsg.setProgress(progress);
                    bFlag = true;
                    break;
                }
            }
        }
        return bFlag;

    }

    /**
     * 消息概述发生变化
     */
    @Override
    public void msgSummeryChange(List<UserMsgSummery> msgSummery) {

    }

    @Override
    public void onJobDone(KittyJob job) {

    }

    @Override
    public void onNewJobIn(KittyJob job) {
        if (job instanceof YeemosTask) {
            YeemosTask task = (YeemosTask) job;
            if (task.getYeemosTask() == YeemosTask.YeemosTaskType.UPLOAD_CHAT_FILE) {
                task.addListener(FileHtttpManager.getInstance());
            }
        }
    }

    @Override
    public void onOldJobRestore(List<KittyJob> jobs) {
        for (KittyJob job : jobs) {
            if (job instanceof YeemosTask) {
                YeemosTask task = (YeemosTask) job;
                if (task.getYeemosTask() == YeemosTask.YeemosTaskType.UPLOAD_CHAT_FILE) {
                    task.addListener(FileHtttpManager.getInstance());
                }
            }
        }
    }

    @Override
    public void onJobCancel(KittyJob job) {

    }
}
