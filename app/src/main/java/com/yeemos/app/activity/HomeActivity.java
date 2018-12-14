package com.yeemos.app.activity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.PushMessageBean;
import com.gbsocial.BeansBase.StickerBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.BeansBase.TopicBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerResultBean;
import com.gbsocial.utils.GPSUtils;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.core.http.DownloadFileManager;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.GBExecutionPool;
import com.google.firebase.iid.FirebaseInstanceId;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.R;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.chat.manager.CRabbitMQChat;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.fragment.BrowserFragment;
import com.yeemos.app.fragment.ChatFragment;
import com.yeemos.app.fragment.ChatListFragment;
import com.yeemos.app.fragment.DirectionViewPagerFragment;
import com.yeemos.app.fragment.HashTagsFragment;
import com.yeemos.app.fragment.HomeFragment;
import com.yeemos.app.fragment.RequestFragment;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.fragment.TutorialSearchFriendFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.hardwrare.SensorControler;
import com.yeemos.app.manager.DataChangeManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomListView;
import com.yeemos.app.view.CustomViewPager;
import com.yeemos.app.view.ViewDragLayout;
import com.yeemos.yeemos.jni.ShaderJNILib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gigabud on 15-12-23.
 */
public class HomeActivity extends BaseActivity implements IChatListener {

    public static final String FIRST_LOGIN_POST_ID = "firstLoginPostId";
    //   public static final String HAD_READ_POST_IDS = "hadReadPostIds";
    public static final String HAD_OPEN_POST_IDS = "hadOpenPostIds";
    public static final String HAD_SET_PUSH_TOKEN = "hadSetPushToken";

    public static final String HAD_SET_FIREBASE_PUSH_TOKEN = "hadSetFireBasePushToken";
    public static final String NOTIFICATION_MESSAGE_LISTENER = "gcmMessageListener";
    public static final String HAD_NEW_NOTIFICATION_MESSAGE = "hadNewNotificationMessage";
    public static final String HAD_REQUEST_FOLLOW_YOU_MESSAGE = "hadRequestFollowYouMessage";
    public static final String NEED_REMOVE_POST_IDS = "needRemovePostIds";
    public static final String BLOCK_USER_IDS = "blockUserIds";
    public static final String FILTER_STICKERS = "filterStickers";

    public static final String TAG_LIST = "tag_list";
    public static final String TOPIC_LIST = "topic_list";
    public static final String RECENT_POST_LIST = "recent_post_list";
    public static final String MY_POST_LIST = "my_post_list";
    public static final String FRIEND_LIST = "friend_list";

    private ArrayList<BaseFragment> mFragmentList;
    private float mLastX = -1, mLastY = -1;
    private float mViewPageStartX;
    private int mCurrentViewPageState;  //0表示没动，1表示向左滑动，2表示向右滑动,3表示listview不动，4表示只动ListView
    private int mCurrentItem;
    private ArrayList<TopicBean> mTopicList;
    private ArrayList<TagBean> mTagBeanList;
    private ArrayList<PostBean> mRecentPostList;
    private BasicUser mMyUserInfo;
    private BroadcastReceiver mGCMMessageBrocastReceiver;
    private boolean mIsFromGCM;
    private boolean mIsFirstLogin;
    private boolean mIsNewIntent;
    private boolean mIsHadGetData;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        initViewPager();
        IChat.getInstance().connectServer(MemberShipManager.getInstance().getToken(),
                Long.parseLong(DataManager.getInstance().getBasicCurUser().getUserId()));
        IChat chat = IChat.getInstance();
        chat.addChatListener(this);
        Preferences.getInstacne().setIsAppLogged(true);

        // add test fun copy database
        //       copyDB();
        mIsFromGCM = false;
//        try {
//            registerGCM(Preferences.getInstacne().getAppConfigURL(),
//                    Constants.SENDER_ID);
//        } catch (Exception e) {
//
//        }
        registerGCMBroadcastReceiver();

        mIsFirstLogin = !Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_SEARCH_FRIEND);
        if (!mIsFirstLogin) {
            mIsHadGetData = true;
            getHomeFragmentData(null);
            getMyDetailInfo(null);
        }
        onNewIntent(getIntent());
        showViewFromCache();
    }

    private void registerGCMBroadcastReceiver() {
        mGCMMessageBrocastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mFragmentList == null || mFragmentList.isEmpty()) {
                    return;
                }
                ((HomeFragment) mFragmentList.get(0)).showOrHideRedPoint();
                ((DirectionViewPagerFragment) mFragmentList.get(1)).resetCameraFragment();
                ((ChatListFragment) mFragmentList.get(2)).showOrHideRedPoint();
                PushMessageBean pushMessageBean = (PushMessageBean) intent
                        .getSerializableExtra(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN);
                if (pushMessageBean != null) {
                    refreshOnePost(pushMessageBean);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_MESSAGE_LISTENER);
        registerReceiver(mGCMMessageBrocastReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_SEARCH_FRIEND)) {
            Intent intent = new Intent(this, EmptyTwoActivity.class);
            intent.putExtra("FRAGMENT_NAME", TutorialSearchFriendFragment.class.getName());
            startActivity(intent);
        } else {
            if (mIsFirstLogin) {
                mIsFirstLogin = !Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_SEARCH_FRIEND);
                getHomeFragmentData(null);
                getMyDetailInfo(null);
            }
        }
        mIsNewIntent = false;
        mIsFromGCM = false;
        if (BaseUtils.isGrantPermission(this, Manifest.permission.READ_PHONE_STATE)) {
            if (!Preferences.getInstacne().getBoolByKey(HAD_SET_FIREBASE_PUSH_TOKEN)) {
                String token = "";
                try {
                    token = FirebaseInstanceId.getInstance().getToken();
                } catch (Exception e) {
                }
                if (!TextUtils.isEmpty(token)) {
                    MemberShipManager.getInstance().pushOn(
                            token,
                            GBMemberShip_V2.MemberShipPushServerType
                                    .GetObject(1),
                            new GBSMemberShipManager.memberShipCallBack<Object>() {

                                @Override
                                public void timeOut() {
                                }

                                @Override
                                public void success(Object obj) {
                                    Preferences.getInstacne().setValues(
                                            HAD_SET_FIREBASE_PUSH_TOKEN, true);
                                }

                                @Override
                                public void fail(String errorStr) {
                                    errorCodeDo(errorStr);
                                }

                                @Override
                                public void cancel() {
                                }
                            });
                }
            }
        }
    }


    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mIsNewIntent = true;
        PushMessageBean pushMessageBean = (PushMessageBean) intent
                .getSerializableExtra(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN);
        if (pushMessageBean == null) {
            return;
        }
        mIsFromGCM = true;
        handlePushMessageBean(pushMessageBean);
    }


    @Override
    protected void onFromBackground() {
        super.onFromBackground();
        if (!mIsFromGCM) {
            int currentItem = getCustomViewPager().getCurrentItem();
            if (currentItem == 1) {
                ((DirectionViewPagerFragment) mFragmentList.get(1)).toCameraFragment();
            } else if (currentItem == 0 || currentItem == 2) {
                toCameraFragment();
            }
        }
        ((CRabbitMQChat) CRabbitMQChat.getInstance()).getRabbitMQManager().httpGetUnReceiverMessage(this,
                Long.parseLong(DataManager.getInstance().getBasicCurUser().getUserId()));
        mIsFromGCM = false;
        if (mIsNewIntent) {
            mIsNewIntent = false;
            return;
        }
        if (mIsHadGetData) {
            getHomeFragmentData(null);
            getMyDetailInfo(null);
        }
        mIsHadGetData = false;
    }

    private void handlePushMessageBean(final PushMessageBean pushMessageBean) {
        MyFirebaseMessagingService.PushType pushType = MyFirebaseMessagingService.PushType.valueOf(Integer.parseInt(pushMessageBean
                .getType()));
        if (pushType == null || pushType == MyFirebaseMessagingService.PushType.TYPE_SYSTEM_MESSAGE
                || pushType == MyFirebaseMessagingService.PushType.TYPE_OPEN_APP_STORE
                || pushType == MyFirebaseMessagingService.PushType.TYPE_OPEN_CAMERA) {
            return;
        }
        Class<?> pagerClass = null;
        Bundle bundle = new Bundle();
        boolean isForChat = false;
        switch (pushType) {
            case TYPE_OTHER_REPLY_TAG_FOR_YOUR_POST:
            case TYPE_OTHER_COMMENT_YOUR_POST:
            case TYPE_OTHER_AT_YOU:
            case TYPE_OPEN_POST_NOTIFICATION_FOR_OTHER:
            case TYPE_YOUR_FOLLOWER_SEND_POST:
            case TYPE_OPEN_A_POST:
                pagerClass = ShowPostViewPagerFragment.class;
                bundle.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_GCM);
                break;
            case TYPE_OTHER_DRAWING_COMMENT:
            case TYPE_OTHER_REPLY_YOUR_DRAWING_COMMENT:
                pagerClass = ShowPostViewPagerFragment.class;
                bundle.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_DRAWING_COMMENT_GCM);
                break;
            case TYPE_OTHER_REQUEST_FOLLOW_YOU:
                pagerClass = RequestFragment.class;
                break;
            case TYPE_OTHER_FOLLOW_YOU:
            case TYPE_OTHER_AGREE_YOUR_REQUEST:
            case TYPE_FACEBOOK_FRIENDS_JOIN:
            case TYPE_INSTAGRAM_FRIENDS_JOIN:
            case TYPE_SEE_A_USER:
                pagerClass = UserInfoFragment.class;
                break;
            case TYPE_FRIEND_IS_TYPING:
            case TYPE_RECEIVE_FRIEND_MESSAGE:
                isForChat = true;
                break;
            case TYPE_OPEN_A_EMO:
                TagBean tagBean = new TagBean();
                tagBean.setId(Constants.getEmoIdByTagStr(pushMessageBean.getId()));
                DataManager.getInstance().setCurTag(tagBean);
                pagerClass = ShowPostViewPagerFragment.class;
                bundle.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_TAG);
                break;
            case TYPE_OPEN_A_HASHTAG:
                DataManager.getInstance().setCurKeyWord(pushMessageBean.getId());
                pagerClass = HashTagsFragment.class;
                break;
            case TYPE_OPEN_A_TOPIC:
                pagerClass = ShowPostViewPagerFragment.class;
                bundle.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_TOPIC);
                TopicBean topicBean = new TopicBean();
                topicBean.setId(pushMessageBean.getId());
                DataManager.getInstance().setCurTopic(topicBean);
                break;
            case TYPE_OPEN_A_WEBSITE:
                pagerClass = BrowserFragment.class;
                break;
            default:
                break;
        }
        if (isForChat) {
            List<UserMsgSummery> listUserMsgSummery = IChat.getInstance().getUserMsgSummery();
            if (listUserMsgSummery != null) {
                IMsg lastMsg;
                for (UserMsgSummery summery : listUserMsgSummery) {
                    if (String.valueOf(summery.getUser().getUserId()).equals(pushMessageBean.getCuid())) {
                        lastMsg = summery.getLastMsg();
                        if (lastMsg != null) {
                            lastMsg.setRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ);
                        }
                        break;
                    }
                }
            }
            ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
            if (allFriends != null) {
                for (BasicUser basicUser : allFriends) {
                    if (basicUser.getUserId().equals(pushMessageBean.getCuid())) {
                        toChatFragmentWithChatUser(basicUser);
                        return;
                    }
                }
            }

            showLoadingDialog("", null, true);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    final ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(false);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                            if (allFriends != null) {
                                for (BasicUser basicUser : allFriends) {
                                    if (basicUser.getUserId().equals(pushMessageBean.getCuid())) {
                                        toChatFragmentWithChatUser(basicUser);
                                        return;
                                    }
                                }
                            }
                        }
                    });
                }
            });

        } else {
            if (pagerClass != null) {
                if (pagerClass == UserInfoFragment.class) {
                    bundle.putSerializable(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN, pushMessageBean);
                } else if (pagerClass == BrowserFragment.class) {
                    bundle.putString(BrowserFragment.BrowserFragment_Url, pushMessageBean.getId());
                } else {
                    bundle.putString(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN, pushMessageBean.getId());
                }
                gotoPager(pagerClass, bundle);
            }
        }
    }

    private void refreshOnePost(PushMessageBean pushMessageBean) {
        MyFirebaseMessagingService.PushType pushType = MyFirebaseMessagingService.PushType.valueOf(Integer.parseInt(pushMessageBean
                .getType()));
        if (pushType == null) {
            return;
        }
        switch (pushType) {
            case TYPE_OTHER_REPLY_TAG_FOR_YOUR_POST:
            case TYPE_OTHER_COMMENT_YOUR_POST:
            case TYPE_OTHER_DRAWING_COMMENT:
            case TYPE_OTHER_REPLY_YOUR_DRAWING_COMMENT:
                final PostBean postBean = new PostBean();
                postBean.setId(pushMessageBean.getId());
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        ServerResultBean<PostBean> serverResultBean = DataManager.getInstance().getOnePostInfo(postBean);
                        if (serverResultBean != null && serverResultBean.getData() != null) {
                            PostBean newPostBean = serverResultBean.getData();
                            if (newPostBean == null) {
                                return;
                            }
                            if (mRecentPostList != null) {
                                int size = mRecentPostList.size();
                                PostBean pb;
                                for (int i = 0; i < size; ++i) {
                                    pb = mRecentPostList.get(i);
                                    if (pb.getId().equals(newPostBean.getId())) {
                                        mRecentPostList.set(i, newPostBean);
                                        break;
                                    }
                                }
                            }
                            if (mRecentPostList != null) {
                                int size = mRecentPostList.size();
                                PostBean pb;
                                for (int i = 0; i < size; ++i) {
                                    pb = mRecentPostList.get(i);
                                    if (pb.getId().equals(newPostBean.getId())) {
                                        mRecentPostList.set(i, newPostBean);
                                        break;
                                    }
                                }
                            }
                            if (mMyUserInfo != null && mMyUserInfo.getObjectsPosts() != null) {
                                ArrayList<PostBean> list = mMyUserInfo.getObjectsPosts();
                                int size = list.size();
                                PostBean pb;
                                for (int i = 0; i < size; ++i) {
                                    pb = list.get(i);
                                    if (pb.getId().equals(newPostBean.getId())) {
                                        list.set(i, newPostBean);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    public void onStart() {
        super.onStart();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        setViewPagerCanScroll(true);
        setFullOrNotScreen();
        Preferences.getInstacne().setIsRunning(true);
        DataManager.getInstance().setShowPostList(null);
        Object object = DataManager.getInstance().getSelectObject();
        if (object != null) {

            if (object instanceof PushMessageBean) {
                handlePushMessageBean((PushMessageBean) object);
            } else if (object instanceof Boolean) {  //表示超过1小时从后台进来，需要进入拍照页面
                toCameraFragment();
            } else if (object instanceof BasicUser) {  //表示要和这个好友聊天
                toChatFragmentWithChatUser((BasicUser) object);
            } else if (object instanceof PostBean) {
                ViewPager viewPager = getCustomViewPager();
                if (viewPager.getCurrentItem() != 0) {
                    viewPager.setCurrentItem(0);
                }
            } else if (object instanceof Integer) {
                ViewPager viewPager = getCustomViewPager();
                int item = (int) object;
                if (viewPager.getCurrentItem() != item) {
                    viewPager.setCurrentItem(item);
                }
            }
        }
        DataManager.getInstance().setSelectObject(null);
        removePost();
    }


    public void setFullOrNotScreen() {
        int currentItem = getCustomViewPager().getCurrentItem();
        setScreenFull(currentItem == 1);
        DataManager.getInstance().setCurPostBean(null);
    }


    public void removePost() {
        if (mRecentPostList == null) {
            return;
        }
        String savedIds = Preferences.getInstacne().getValues(HomeActivity.NEED_REMOVE_POST_IDS, "");
        PostBean postBean;
        for (int i = 0; i < mRecentPostList.size(); ) {
            postBean = mRecentPostList.get(i);
            if (savedIds.contains(postBean.getId() + ",")
                    || savedIds.contains(postBean.getOwner().getUserId() + ",")) {
                mRecentPostList.remove(i);
                continue;
            }
            ++i;
        }
        showRecentPostView(mRecentPostList);
        Utils.saveArrayCache(RECENT_POST_LIST, mRecentPostList);
        Preferences.getInstacne().setValues(HomeActivity.NEED_REMOVE_POST_IDS, "");
    }

    public void addRecentPost(PostBean postBean) {
        if (mRecentPostList == null) {
            mRecentPostList = new ArrayList<>();
        }
        for (PostBean pb : mRecentPostList) {
            if (pb.getId().equals(postBean.getId())) {
                return;
            }
        }
        mRecentPostList.add(0, postBean);
        Utils.saveArrayCache(RECENT_POST_LIST, mRecentPostList);
        showRecentPostView(mRecentPostList);
    }

    public void addUserPost(PostBean postBean) {
        if (mMyUserInfo != null) {
            ArrayList<PostBean> postBeens = mMyUserInfo.getObjectsPosts();
            if (postBeens == null) {
                postBeens = new ArrayList<>();
                mMyUserInfo.setObjectsPosts(postBeens);
            }
            postBeens.add(0, postBean);
            Utils.saveArrayCache(MY_POST_LIST, postBeens);
            ((DirectionViewPagerFragment) mFragmentList.get(1)).resetMyUserInfo(mMyUserInfo, mMyUserInfo.getObjectsPosts());
        }
    }

    public int getUnReadPostNum() {
        if (mRecentPostList == null) {
            return 0;
        }
        String postIds = Preferences.getInstacne().getValues(HAD_OPEN_POST_IDS, FIRST_LOGIN_POST_ID);
        if (postIds.equals(FIRST_LOGIN_POST_ID)) {
            Preferences.getInstacne().setValues(HAD_OPEN_POST_IDS, "");
            return 0;
        }
        String readIds = "";
        int unReadNum = 0;
        String postId;
        int size = mRecentPostList.size();
        for (int i = 0; i < size; ++i) {
            postId = mRecentPostList.get(i).getId();
            if (!postIds.contains(postId)) {
                if (mRecentPostList.get(i).getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    continue;
                }
                ++unReadNum;
            } else {
                if (size % GBSConstants.PAGE_NUMBER_COMMENTS_POP == 0) {
                    readIds = postIds;
                } else {
                    if (i == 0) {
                        readIds += postId;
                    } else {
                        readIds += ("," + postId);
                    }
                }
            }
        }
        Preferences.getInstacne().setValues(HAD_OPEN_POST_IDS, readIds);
        return unReadNum;
    }

    /**
     * 获取HomeFragment数据
     */
    public void getHomeFragmentData(final Handler hanler) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<ArrayList<TagBean>> resultList = DataManager.getInstance().getTagsPostNum();
                if (resultList != null) {
                    if (resultList.getData() != null) {
                        mTagBeanList = resultList.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showTags();
                            }
                        });
                    }
                    if (resultList.isSuccess()) {
                        Utils.saveArrayCache(TAG_LIST, mTagBeanList);
                    }
                }

                ServerResultBean<ArrayList<TopicBean>> serverBean2 = DataManager.getInstance().getTopics();
                if (serverBean2 != null) {
                    if (serverBean2.getData() != null) {
                        mTopicList = serverBean2.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showTopics();
                            }
                        });
                    }
                    if (serverBean2.isSuccess()) {
                        Utils.saveArrayCache(TOPIC_LIST, mTopicList);
                    }
                }
                ServerResultBean<ArrayList<PostBean>> serverBean1 = DataManager.getInstance().getHomePostList(0, -1, false);
                if (serverBean1 != null) {
                    if (serverBean1.getData() != null) {
                        mRecentPostList = serverBean1.getData();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showRecentPostView(mRecentPostList);
                            }
                        });
                    }
                    if (serverBean1.isSuccess()) {
                        Utils.saveArrayCache(RECENT_POST_LIST, mRecentPostList);
                    }
                }

                if (hanler != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hanler.sendEmptyMessage(0);
                        }
                    });
                }
                getStricker();
            }
        });
    }


    private void showViewFromCache() {
        if (mTagBeanList == null || mTagBeanList.isEmpty()) {
            mTagBeanList = Utils.getCache(TagBean.class, TAG_LIST);
            showTags();
        }
        if (mTopicList == null || mTopicList.isEmpty()) {
            mTopicList = Utils.getCache(TopicBean.class, TOPIC_LIST);
            showTopics();
        }
        if (mRecentPostList == null || mRecentPostList.isEmpty()) {
            mRecentPostList = Utils.getCache(PostBean.class, RECENT_POST_LIST);
            showRecentPostView(mRecentPostList);
        }
    }

    private void getStricker() {
        if (!BaseUtils.isGrantPermission(HomeActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            return;
        }
        if (GPSUtils.getLocation(HomeActivity.this) != null) {
            ServerResultBean<ArrayList<StickerBean>> serverBean3 = DataManager.getInstance().getSubjectSticker();
            if (serverBean3 != null && serverBean3.isSuccess()) {
                ArrayList<StickerBean> stickers = serverBean3.getData();
                String filterPath = "";
                if (stickers != null && !stickers.isEmpty()) {
                    ArrayList<String> pictures;
                    String photoPath;
                    for (StickerBean bean : stickers) {
                        pictures = bean.getPhotos();
                        if (pictures == null) {
                            continue;
                        }
                        try {
                            if (TextUtils.isEmpty(DataManager.getInstance().getBasicCurUser().getToken())) {
                                return;
                            }
                            for (String photo : pictures) {
                                if (TextUtils.isEmpty(photo)) {
                                    continue;
                                }
                                photoPath = Preferences.getInstacne().getDownloadFilePathByName(photo);
                                if (!new File(photoPath).exists()) {
                                    String imageURL = Preferences.getInstacne().getPostFileDownloadURLByName(URLEncoder.encode(photo, "utf-8"), URLEncoder.encode(DataManager.getInstance().getBasicCurUser().getToken(), "utf-8"));
                                    DownloadFileManager.getInstance().addDownloadFile(BaseApplication.getAppContext(), photo, imageURL, photo, 1);
                                }
                                filterPath += photo + ",";
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Preferences.getInstacne().setValues(FILTER_STICKERS, filterPath);
            }
        }
    }


    public void getMyDetailInfo(final Handler handler) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> serverBean = DataManager.getInstance().getUserDetailInfo(DataManager.getInstance().getBasicCurUser(), 0,
                        GBSConstants.UserDataType.User_Data_AllPost, GBSConstants.SortType.SortType_Time, GBSConstants.SortWay.SortWay_Descending);
                if (handler != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handler.sendEmptyMessage(0);
                        }
                    });
                }
                if (serverBean != null) {
                    mMyUserInfo = serverBean.getData();
                    if (mMyUserInfo == null) {
                        return;
                    }
                    DataManager.getInstance().cacheCurrentUser(serverBean);
                    Utils.saveArrayCache(MY_POST_LIST, mMyUserInfo.getObjectsPosts());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((DirectionViewPagerFragment) mFragmentList.get(1)).resetMyUserInfo(mMyUserInfo, mMyUserInfo.getObjectsPosts());
                        }
                    });
                }
            }
        });
    }

    public ArrayList<PostBean> getRecentPostList() {
        if (mRecentPostList == null) {
            mRecentPostList = new ArrayList<>();
        }
        return mRecentPostList;
    }

    public ArrayList<TopicBean> getTopicList() {
        return mTopicList;
    }

    public ArrayList<TagBean> getTagBeanList() {
        return mTagBeanList;
    }

    public BasicUser getMyUserInfo() {
        return mMyUserInfo == null ? DataManager.getInstance().getBasicCurUser() : mMyUserInfo;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:  //禁止多指触碰
            case MotionEvent.ACTION_POINTER_UP:
                return true;
        }
        final CustomViewPager viewPager = getCustomViewPager();
        int currentItem = viewPager.getCurrentItem();
        if (currentItem == 2 && !viewPager.getEditState()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                viewPager.setCanScroll(true);
                mLastX = event.getX();
                mLastY = event.getY();
                mViewPageStartX = viewPager.getScrollX();
                ViewDragLayout.mIsMinLeft = false;
                ((ChatFragment) mFragmentList.get(3)).setIsResetFragment(false);
                mCurrentViewPageState = 0;
            } else {
                if (mCurrentViewPageState == 0) {
                    if (event.getX() - mLastX < 0) {
                        mCurrentViewPageState = 2;
                    } else if (event.getX() - mLastX > 0) {
                        mCurrentViewPageState = 1;
                    }
                }
                if (mCurrentViewPageState == 2) {
                    float detalX = event.getX() - mLastX;
                    float detalY = event.getY() - mLastY;
                    if (Math.abs(detalX) > 5 || Math.abs(detalY) > 5) {
                        CustomListView listView = ((ChatListFragment) mFragmentList.get(2)).getListView();
                        if (mLastX > event.getX()
                                && Math.abs(detalX) > Math.abs(detalY)) {
                            mCurrentViewPageState = 3;
                            listView.setCanScroll(false);
                        } else {
                            mCurrentViewPageState = 4;
                            listView.setCanScroll(true);
                        }
                    }
                }
                if (mCurrentViewPageState == 3) {
                    if (ViewDragLayout.mIsMinLeft) {
                        ((ChatFragment) mFragmentList.get(3)).resetChatFragment();
                        if (viewPager.getScrollX() < mViewPageStartX) {
                            ViewDragLayout.mIsMinLeft = false;
                            viewPager.scrollTo((int) mViewPageStartX, 0);
                        } else {
                            viewPager.scrollBy(-(int) (event.getX() - mLastX), 0);
                        }
                    } else {
                        viewPager.setCanScroll(false);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        viewPager.setCanScroll(true);
                        float offset = (viewPager.getScrollX() - mViewPageStartX) / viewPager.getWidth();
                        if (offset < 0.2f) {
                            viewPager.scrollTo((int) mViewPageStartX, 0);
                            viewPager.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getCustomViewPager().setCurrentItem(2);
                                }
                            }, 50);
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    int detalX = (int) (mViewPageStartX + viewPager.getWidth() - viewPager.getScrollX());
                                    int perX = detalX / 15;
                                    for (int i = 0; i < 15; ++i) {
                                        viewPager.scrollBy(perX, 0);
                                        try {
                                            Thread.sleep(15 - i);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    viewPager.scrollTo((int) (mViewPageStartX + viewPager.getWidth()), 0);
                                }
                            }).start();
                            viewPager.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    getCustomViewPager().setCurrentItem(3);
                                }
                            }, 200);
                        }
                    }
                } else if (mCurrentViewPageState == 1) {
                    if (event.getX() < mLastX) {
                        if (viewPager.getScrollX() >= mViewPageStartX) {
                            viewPager.setCanScroll(false);
                            viewPager.scrollTo((int) mViewPageStartX, 0);
                        } else {
                            viewPager.setCanScroll(true);
                        }
                    } else {
                        viewPager.setCanScroll(true);
                    }
                }
                if (mCurrentViewPageState != 2) {
                    mLastX = event.getX();
                }
            }

        } else if (currentItem == 1 && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (((DirectionViewPagerFragment) mFragmentList.get(1)).isCurrentCameraFragment()) {
                viewPager.setCanScroll(true);
            } else {
                viewPager.setCanScroll(false);
            }
            return super.dispatchTouchEvent(event);
        }
        return super.dispatchTouchEvent(event);
    }

    private boolean mIsGoingChat = false;

    public void toChatFragmentWithChatUser(BasicUser basicUser) {
        if (mIsGoingChat) {
            return;
        }
        mIsGoingChat = true;
        DataManager.getInstance().setCurOtherUser(basicUser);
        ChatFragment chatFragment = (ChatFragment) mFragmentList.get(3);
        chatFragment.setIsResetFragment(false);
        if (chatFragment.isCreateView()) {
            chatFragment.resetChatFragment();
        } else {
            chatFragment.setNeedResetonCreate(true);
        }
        ViewPager viewPager = getCustomViewPager();
        if (viewPager.getCurrentItem() != 3) {
            viewPager.setCurrentItem(3);
        }
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsGoingChat = false;
            }
        }, 2000);
    }

    public CustomViewPager getCustomViewPager() {
        return (CustomViewPager) findViewById(R.id.viewPager);
    }

    private void initViewPager() {
        final ViewPager viewPager = getCustomViewPager();
        if (mFragmentList == null) {
            mFragmentList = new ArrayList<>();
        }
        if (mFragmentList.isEmpty()) {
            mFragmentList.add(new HomeFragment());
            mFragmentList.add(new DirectionViewPagerFragment());
            mFragmentList.add(new ChatListFragment());
            mFragmentList.add(new ChatFragment());
        }
        viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {
                return mFragmentList.size();
            }

            @Override
            public Fragment getItem(int position) {
                return mFragmentList.get(position);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                BaseApplication.setCurFragment(mFragmentList.get(arg0));
                if (arg0 == 2) {
                    ((ChatFragment) mFragmentList.get(3)).removeChatListener();
                    DataManager.getInstance().setCurOtherUser(null);
                    if (mCurrentItem != 3) {
                        ((ChatListFragment) mFragmentList.get(2)).resetChatList();
                    }
                }

                if (mCurrentItem == 3 && arg0 == 2) {
                    ((ChatFragment) mFragmentList.get(3)).markUserReadMsgs();
                }

                if (mCurrentItem == 1 && arg0 == 0) {
                    int unReadPostNum = getUnReadPostNum();
                    ((DirectionViewPagerFragment) mFragmentList.get(1)).resetUnReadPostNum(unReadPostNum);
                }
                if (mCurrentItem == 3 && arg0 == 2) {
                    ((ChatFragment) mFragmentList.get(3)).showOrHideSoftKey(false);
                }
                mCurrentItem = arg0;
                if (mCurrentItem == 1) {
                    if (mMyUserInfo != null) {
                        ((DirectionViewPagerFragment) mFragmentList.get(1)).resetMyUserInfo(mMyUserInfo, mMyUserInfo.getObjectsPosts());
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (!BaseUtils.isGrantPermission(HomeActivity.this, Manifest.permission.CAMERA)) {
                            requestPermission(PERMISSION_CAMERA_CODE, Manifest.permission.CAMERA);
                        }
                    }
                    SensorControler.getInstance().onStart();
                } else {
                    if (mCurrentItem == 3) {
                        ((ChatFragment) mFragmentList.get(3)).showOrHideSoftKey(true);
                        ((ChatFragment) mFragmentList.get(3)).getChatUserOfflineMsg();
                    }
                    SensorControler.getInstance().onStop();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                if (arg0 == 0 || arg0 == 1) {
                    setScreenFull(true);
                    mFragmentList.get(0).setViewPadding(true);
                    mFragmentList.get(2).setViewPadding(true);
                }
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                if (arg0 == 0) {
                    setFullOrNotScreen();

                    if (mCurrentItem == 0 || mCurrentItem == 2) {
                        mFragmentList.get(mCurrentItem).setViewPadding(false);
                        if (mCurrentItem == 2) {
                            ((ChatListFragment) mFragmentList.get(2)).showTourialView();
                        }
                        if (mCurrentItem == 0 && !Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_HOME_FRAGMENT)) {
                            ((HomeFragment) mFragmentList.get(0)).showTourialView();
                            Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_HOME_FRAGMENT, true);
                        }
                    }
                    if (mCurrentItem == 1) {
                        mFragmentList.get(0).setViewPadding(true);
                        mFragmentList.get(2).setViewPadding(true);
                    }
                }
            }
        });
        mCurrentItem = 1;
        viewPager.setCurrentItem(mCurrentItem);
    }

    public void showRecentPostView(ArrayList<PostBean> recentPostList) {
        if (recentPostList != null && recentPostList.isEmpty()) {
            Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, "");
        }
        ((HomeFragment) mFragmentList.get(0)).resetRecentPostView(recentPostList);
        ((DirectionViewPagerFragment) mFragmentList.get(1)).resetCameraFragment();
    }

    public void resetCameraFragment() {
        ((DirectionViewPagerFragment) mFragmentList.get(1)).resetCameraFragment();
    }

    public void showTopics() {
        ((HomeFragment) mFragmentList.get(0)).resetTopics();
    }

    public void showTags() {
        ((HomeFragment) mFragmentList.get(0)).resetTags();
    }

    public void toCameraFragment() {
        getCustomViewPager().setCurrentItem(1);
    }

    public void toHomeFragment() {
        getCustomViewPager().setCurrentItem(0);
    }

    public void toChatListFragment() {
        getCustomViewPager().setCurrentItem(2);
        ((ChatListFragment) mFragmentList.get(2)).removeUser();
    }

    public ChatListFragment getChatListFragment() {
        return (ChatListFragment) mFragmentList.get(2);
    }

    public void setViewPagerCanScroll(boolean isCanScroll) {
        CustomViewPager viewPager = getCustomViewPager();
        viewPager.setCanScroll(isCanScroll);
    }

    public void onDestroy() {
        super.onDestroy();
        ShaderJNILib.destroySource();
        if (mGCMMessageBrocastReceiver != null) {
            unregisterReceiver(mGCMMessageBrocastReceiver);
            mGCMMessageBrocastReceiver = null;
        }
        Preferences.getInstacne().setIsRunning(false);
        IChat.getInstance().removeChatListener(this);
        DownloadFileManager.getInstance().clearDownloadListener();
        DataChangeManager.getInstance().clearDataChangeListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            CustomViewPager viewPager = getCustomViewPager();
            int currentItem = viewPager.getCurrentItem();
            if (currentItem == 1 || currentItem == 3) {
                mFragmentList.get(currentItem).onBackKeyClick();
                return false;
            }
            return super.onKeyDown(keyCode, event);
        } else {
            return super.onKeyDown(keyCode, event);
        }

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
    public void receiveMsg(IMsg msg) {

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

    /**
     * 消息概述发生变化
     */
    @Override
    public void msgSummeryChange(List<UserMsgSummery> msgSummery) {

    }


    // test
    public void copyDB() {
        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            File oldfile = new File("/data/data/" + getPackageName()
                    + "/databases/chat.db");
            if (oldfile.exists()) {
                inStream = new FileInputStream(oldfile);
                fs = new FileOutputStream(
                        Environment.getExternalStorageDirectory() + "/chat.db");
                byte[] buffer = new byte[2048];
                int length = -1;
                while ((length = inStream.read(buffer)) != -1) {
                    fs.write(buffer, 0, length);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            try {
                if (inStream != null) {
                    inStream.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
    }

}
