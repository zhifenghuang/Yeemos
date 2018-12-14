package com.yeemos.app.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.PushMessageBean;
import com.gbsocial.BeansBase.ReplyTagBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.BeansBase.TopicBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.gigabud.core.util.GBExecutionPool;
import com.gigabud.core.util.NetUtil;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomViewPager;
import com.yeemos.app.view.FlyEmoView;
import com.yeemos.app.view.OverscrollViewPager;
import com.yeemos.app.view.ShowPostView;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-1.
 */
public class ShowPostViewPagerFragment extends BaseFragment {

    public static final String SHOW_POST_TYPE = "show_post_type";
    public static final String SHOW_INDEX = "show_index";

    public static final int SHOW_POST_FROM_SHOWLIST = 0;
    public static final int SHOW_POST_FROM_RECENT = 1;
    public static final int SHOW_POST_BY_TAG = 2;
    public static final int SHOW_POST_BY_TOPIC = 3;
    public static final int SHOW_POST_BY_USER = 4;
    public static final int SHOW_POST_BY_HASHTAG = 5;
    public static final int SHOW_POST_BY_GCM = 6;
    public static final int SHOW_POST_BY_DRAWING_COMMENT_GCM = 7;
    private boolean isAnimation = false;
    private Dialog dialog;

    private int mShowPostType;
    private int mFirstShowIndex;

    private long mLastTapTime;


    private ArrayList<ShowPostView> mShowPostList;
    private ArrayList<PostBean> mPostBeanList;
    private int mCurrentItem;
    private boolean mIsViewPagerMove;
    private ArrayList<String> mReadPostIds;
    private int mLastReplyTagId;

    private boolean mNeedDectHasNetFlag;


    private ReplyTagBean mReplyTag;
    private long mStartCountTime;
    private boolean mIsStartThread = false;
    private static final long SEND_REPLYTAG_TIME = 3 * 1000;

    private static final int VIEW_PAGER_COUNT = 4;

    private GestureDetector mGestureDetector;
    private TagBean tagBean;

    private boolean mIsTouchFinish;
    private boolean hasMoreData = true;
    private boolean isGettingData = true;
    private boolean isOldData = false;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_show_post_view_pager;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        BitmapCacheManager.getInstance().evictAll();
        Bundle bundle = getArguments();
        if (bundle == null) {
            super.goBack();
            return;
        }
        mFirstShowIndex = 0;
        mShowPostType = bundle.getInt(SHOW_POST_TYPE, SHOW_POST_FROM_SHOWLIST);
        mFirstShowIndex = bundle.getInt(SHOW_INDEX, 0);
        if (mShowPostType == SHOW_POST_FROM_SHOWLIST
                || mShowPostType == SHOW_POST_FROM_RECENT
                || mShowPostType == SHOW_POST_BY_USER
                || mShowPostType == SHOW_POST_BY_HASHTAG) {
            if (mShowPostType == SHOW_POST_BY_USER) {
                isOldData = true;
                mPostBeanList = (ArrayList<PostBean>) DataManager.getInstance().getShowUserPostList().clone();
            } else if (mShowPostType == SHOW_POST_BY_HASHTAG) {
                mPostBeanList = DataManager.getInstance().getHashTagPostList();
            } else {
                if (mShowPostType == SHOW_POST_FROM_RECENT) {
                    isOldData = true;
                }
                mPostBeanList = DataManager.getInstance().getShowPostList();
            }
            if (mPostBeanList == null || mPostBeanList.isEmpty()) {
                getActivity().finish();
                return;
            }
            setRemarkName(mPostBeanList);
            initViewPager();
        } else if (mShowPostType == SHOW_POST_BY_TOPIC) {
            final TopicBean topicBean = DataManager.getInstance().getCurTopic();
            if (topicBean == null) {
                getActivity().finish();
                return;
            }
            showLoadingDialog("", null, true);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ServerResultBean<TopicBean> serverResultBean = DataManager.getInstance()
                            .getPostFromTopic(0, topicBean, GBSConstants.SortType.SortType_Time);
                    if (getView() == null) {
                        hasMoreData = false;
                        isGettingData = false;
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                        }
                    });
                    if (serverResultBean != null && serverResultBean.getData() != null) {
                        TopicBean tb = serverResultBean.getData();
                        mPostBeanList = tb.getSubObjects();
                        if (mPostBeanList == null || mPostBeanList.isEmpty()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showNoPostDialog();
                                }
                            });
                            return;
                        }
                        if (mPostBeanList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                            hasMoreData = false;
                        }
                        setRemarkName(mPostBeanList);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initViewPager();
                            }
                        });
                    } else {
                        hasMoreData = false;
                    }
                    isGettingData = false;
                }
            });
        } else if (mShowPostType == SHOW_POST_BY_TAG) {
            tagBean = DataManager.getInstance().getCurTag();
            if (tagBean == null) {
                super.goBack();
                return;
            }
            showLoadingDialog("", null, true);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ServerResultBean<ArrayList<PostBean>> serverResultBean = DataManager.getInstance().searchPostByTag(0, tagBean, GBSConstants.SortType.SortType_Time);
                    if (getView() == null) {
                        hasMoreData = false;
                        isGettingData = false;
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                        }
                    });
                    if (serverResultBean != null) {
                        mPostBeanList = serverResultBean.getData();
                        if (getActivity() == null) {
                            hasMoreData = false;
                            isGettingData = false;
                            return;
                        }
                        if (mPostBeanList == null || mPostBeanList.isEmpty()) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showNoPostDialog();
                                }
                            });
                            hasMoreData = false;
                            isGettingData = false;
                            return;
                        }
                        if (mPostBeanList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                            hasMoreData = false;
                        }

                        setRemarkName(mPostBeanList);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initViewPager();
                            }
                        });
                        isGettingData = false;
                    } else {
                        hasMoreData = false;
                        isGettingData = false;
                    }
                }
            });
        } else if (mShowPostType == SHOW_POST_BY_GCM || mShowPostType == SHOW_POST_BY_DRAWING_COMMENT_GCM) {
            String str = bundle
                    .getString(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN);
            if (TextUtils.isEmpty(str)) {
                getActivity().finish();
                return;
            }
            showLoadingDialog("", null, true);
            final PostBean postBean = new PostBean();
            postBean.setId(str);
            GBExecutionPool.getExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    ServerResultBean<PostBean> serverResultBean = DataManager.getInstance().getOnePostInfo(postBean);
                    if (getView() == null) {
                        return;
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideLoadingDialog();
                        }
                    });
                    if (!NetUtil.isConnected(getActivity())) {
                        return;
                    }
                    if (serverResultBean != null && serverResultBean.getData() != null) {
                        PostBean newPostBean = serverResultBean.getData();
                        if (newPostBean == null
                                || (!newPostBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())
                                && newPostBean.getOwner().isPrivate()
                                && newPostBean.getOwner().getFollowStatus() != 1)) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    String content = ServerDataManager.getTextFromKey("GB3305107");
                                    String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                                    ((BaseActivity) getActivity()).showPublicDialog(null, content, Okey, null, false, true, oneBtnDialoghandler);
                                }
                            });
                            return;
                        }
                        mPostBeanList = new ArrayList<>();
                        mPostBeanList.add(newPostBean);
                        setRemarkName(mPostBeanList);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initViewPager();
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String content = ServerDataManager.getTextFromKey("pblc_txt_postnotavailable");
                                String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                                ((BaseActivity) getActivity()).showPublicDialog(null, content, Okey, null, false, true, oneBtnDialoghandler);
                            }
                        });
                    }
                }
            });
        }
        ((BaseActivity) getActivity()).setScreenFull(true);
        initGestureDetector();
    }


    private void getMorePostFromServer() {
        if (isGettingData || !hasMoreData) {
            return;
        }
        isGettingData = true;
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (mShowPostType == SHOW_POST_BY_TOPIC) {
                    getTopicPost();
                } else {
                    getTagPost();
                }
            }
        });
    }

    private synchronized void getTagPost() {
        ServerResultBean<ArrayList<PostBean>> serverResultBean = DataManager.getInstance().searchPostByTag(mPostBeanList.size(), tagBean, GBSConstants.SortType.SortType_Time);
        if (serverResultBean != null && serverResultBean.getData() != null) {
            final ArrayList<PostBean> arrayList = serverResultBean.getData();
            if (arrayList.size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                hasMoreData = false;
            }
            if (getView() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostBeanList.addAll(arrayList);
                        OverscrollViewPager viewPager = (OverscrollViewPager) getView().findViewById(R.id.viewPager);
                        viewPager.getOverscrollView().getAdapter().notifyDataSetChanged();
                    }
                });
            }
            setRemarkName(arrayList);

        } else {
            hasMoreData = false;
        }
        isGettingData = false;
    }

    private synchronized void getTopicPost() {
        ServerResultBean<TopicBean> serverResultBean = DataManager.getInstance()
                .getPostFromTopic(mPostBeanList.size(), DataManager.getInstance().getCurTopic(), GBSConstants.SortType.SortType_Time);
        if (serverResultBean != null && serverResultBean.getData() != null && serverResultBean.getData().getSubObjects() != null) {
            final TopicBean tb = serverResultBean.getData();
            if (tb.getSubObjects().size() < GBSConstants.PAGE_NUMBER_PAGINATION_20) {
                hasMoreData = false;
            }
            if (getView() != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPostBeanList.addAll(tb.getSubObjects());
                        OverscrollViewPager viewPager = (OverscrollViewPager) getView().findViewById(R.id.viewPager);
                        viewPager.getOverscrollView().getAdapter().notifyDataSetChanged();
                    }
                });
            }
            setRemarkName(tb.getSubObjects());

        } else {
            hasMoreData = false;
        }
        isGettingData = false;
    }


    private void showNoPostDialog() {
        if (mShowPostType == SHOW_POST_BY_TAG) {
            String noPostText = ServerDataManager.getTextFromKey("hm_txt_nopost");
            ((BaseActivity) getActivity()).showPublicDialog(null,
                    noPostText,
                    ServerDataManager.getTextFromKey("pblc_btn_cancel"),
                    ServerDataManager.getTextFromKey("pub_btn_ok"), false, true, noPostDialog);
        } else if (mShowPostType == SHOW_POST_BY_TOPIC) {
            String noPostText = ServerDataManager.getTextFromKey("hm_txt_nofeaturepost");
            ((BaseActivity) getActivity()).showPublicDialog(null, noPostText,
                    ServerDataManager.getTextFromKey("pub_btn_ok"), null, false, true, oneBtnDialoghandler);
        }

    }

    Handler noPostDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    goBack();
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    DataManager.getInstance().setSelectObject(1);
                    goBack();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void initGestureDetector() {
        mGestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (getActivity() == null || (e1 == null && e2 == null)) {
                    return true;
                }
                if (velocityY < 50) {
                    return true;
                }
                // 手势向下 down
                if (!mIsViewPagerMove && (e2.getRawY() - e1.getRawY()) > Utils.dip2px(getActivity(), 60)) {
                    mIsTouchFinish = true;
                    onHiddenChanged(true);
                    goBack();
                    getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_THREE;
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

    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }


    private void showTourialView() {

        final View rlTourialView = getActivity().findViewById(R.id.rlTourial);
        rlTourialView.postDelayed(new Runnable() {
            @Override
            public void run() {
                rlTourialView.setVisibility(View.VISIBLE);
                View tourialView3 = rlTourialView.findViewById(R.id.tourialView3);
                Utils.setSubText((TextView) tourialView3.findViewById(R.id.tv3),
                        ServerDataManager.getTextFromKey("pst_vw_txt_feelthepost"), ServerDataManager.getTextFromKey("pst_vw_txt_tapanywhere"),
                        ServerDataManager.getTextFromKey("chtlstandcntcts_txt_swipeleft"), Color.WHITE,
                        getResources().getColor(R.color.color_255_143_51), getResources().getColor(R.color.color_34_166_166));
                tourialView3.setVisibility(View.VISIBLE);
                rlTourialView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        View tourialView3 = v.findViewById(R.id.tourialView3);
                        View tourialView4 = v.findViewById(R.id.tourialView4);
                        View tourialView4_1 = v.findViewById(R.id.tourialView4_1);
                        if (tourialView3.getVisibility() == View.VISIBLE) {
                            tourialView3.setVisibility(View.GONE);
                            tourialView4.setVisibility(View.VISIBLE);
                            View view2 = getView().findViewById(R.id.btnSelectEmo);
                            int[] location2 = getInScreen(view2);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView4.getLayoutParams();
                            lp.topMargin = location2[1] - Utils.dip2px(getActivity(), 52);
                        } else if (tourialView4.getVisibility() == View.VISIBLE) {
                            tourialView4.setVisibility(View.GONE);
                            tourialView4_1.setVisibility(View.VISIBLE);
                            View view3 = getView().findViewById(R.id.tvDrawCommentNum);
                            int[] location3 = getInScreen(view3);
                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) tourialView4_1.getLayoutParams();
                            lp.leftMargin = location3[0] - Utils.dip2px(getActivity(), 195);
                            lp.topMargin = location3[1] - (Utils.dip2px(getActivity(), 37) - view3.getHeight()) / 2;
                        } else {
                            tourialView4_1.setVisibility(View.GONE);
                            v.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }, 500);

    }

    public int getShowPostType() {
        return mShowPostType;
    }

    public void setViewPagerCanScroll(boolean isCanScroll) {
        final OverscrollViewPager viewPager = (OverscrollViewPager) getView().findViewById(R.id.viewPager);
        ((CustomViewPager) viewPager.getOverscrollView()).setCanScroll(isCanScroll);
    }

    public void changeReplyTag(int id) {
        if (mReplyTag != null && mLastReplyTagId == id) {
            return;
        }
        if (mReplyTag == null) {
            mReplyTag = new ReplyTagBean();
        }
        mReplyTag.setReplyUser(DataManager.getInstance().getBasicCurUser());
        if (mReplyTag.getTag() == null) {
            TagBean tagBean = new TagBean();
            mReplyTag.setTag(tagBean);
        }
        mReplyTag.getTag().setId(id);
        mLastReplyTagId = id;
    }


    public void dispatchTouchEvent(MotionEvent event) {
        if (getView() == null) {
            return;
        }
        if (mShowPostList == null || mShowPostList.isEmpty()) {
            return;
        }
        if (mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).isShowDrawCommentsView()) {
            return;
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mIsTouchFinish = false;
        }
        mGestureDetector.onTouchEvent(event);
    }

    public void addEmo() {
        if (getView() == null) {
            return;
        }
        if (mShowPostList == null || mShowPostList.isEmpty()) {
            return;
        }
        PostBean postBean = mPostBeanList.get(mCurrentItem);
        if (postBean.getOwner().getUserId().equals(DataManager.getInstance().getBasicCurUser().getUserId())) {
            return;  //不能自己为自己点赞
        }
        if (mIsTouchFinish || mIsViewPagerMove) {
            return;
        }
        if (((FlyEmoView) getView().findViewById(R.id.flyEmoView)).addEmo(mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).getEmoBmp())) {
            if (mReplyTag == null) {
                return;
            }
            mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).addReplyTag();
            mStartCountTime = System.currentTimeMillis();
            long replyNum = mReplyTag.getReplyNums();
            mReplyTag.setReplyNums(++replyNum);
            if (!mIsStartThread) {
                startReplyTagThread();
            }
        }
    }

    private void setRemarkName(ArrayList<PostBean> PostArrayList) {
        ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
        if (PostArrayList != null && allFriends != null) {
            BasicUser ower;
            for (PostBean postBean : PostArrayList) {
                ower = postBean.getOwner();
                if (ower.getFollowedStatus() == 1 && ower.getFollowStatus() == 1) {
                    for (BasicUser friend : allFriends) {
                        if (friend.getUserId().equals(ower.getUserId())) {
                            postBean.getOwner().setRemarkName(friend.getRemarkName());
                            break;
                        }
                    }
                }
            }
        }
    }

    private void initViewPager() {
        if (getView() == null || mPostBeanList == null || mPostBeanList.isEmpty()) {
            return;
        }
        final OverscrollViewPager viewPager = getView().findViewById(R.id.viewPager);
        if (mShowPostList == null) {
            mShowPostList = new ArrayList<>();
        }
        ShowPostView showPostView;
        for (int i = 0; i < VIEW_PAGER_COUNT; ++i) {
            showPostView = new ShowPostView(getActivity());
            showPostView.setParentFragment(this);
            mShowPostList.add(showPostView);
        }
        viewPager.getOverscrollView().setAdapter(new PagerAdapter() {
            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return mPostBeanList == null ? 0 : mPostBeanList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mShowPostList.get(position % VIEW_PAGER_COUNT));
            }

            @Override
            public int getItemPosition(Object object) {
                ShowPostView showPostView = (ShowPostView) object;
                PostBean postBean = showPostView.getPostBean();
                int position = mShowPostList.indexOf(postBean);
                if (position >= 0) {
                    return position;
                } else {
                    return POSITION_NONE;
                }
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return "";
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ShowPostView view = mShowPostList.get(position % VIEW_PAGER_COUNT);
                container.removeView(view);
                container.addView(view);
                view.setPostBean(mPostBeanList.get(position), false);
                return view;
            }

        });
        viewPager.getOverscrollView().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {

                if (mShowPostType == SHOW_POST_FROM_RECENT && arg0 < mPostBeanList.size()) {
                    PostBean postBean = mPostBeanList.get(arg0);
                    String openIds = Preferences.getInstacne().getValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                    if (openIds.contains(postBean.getId())) {
                        return;
                    }
                    if (openIds.length() == 0) {
                        openIds += postBean.getId();
                    } else {
                        openIds += ("," + postBean.getId());
                    }
                    Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, openIds);
                }
                if ((mShowPostType == SHOW_POST_BY_TOPIC || mShowPostType == SHOW_POST_BY_TAG) && arg0 == mPostBeanList.size() / 2) {
                    getMorePostFromServer();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                if (arg1 > 0.1f) {
                    mIsViewPagerMove = true;
                }

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                //     viewPager.getOverscrollView().getAdapter().notifyDataSetChanged();
                if (arg0 == 0 && mCurrentItem != viewPager.getOverscrollView().getCurrentItem()) {
                    if (mReplyTag != null && mReplyTag.getReplyNums() > 0) {
                        sendReplyTagToServer();
                    }
                    mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).destroyView();
                    mCurrentItem = viewPager.getOverscrollView().getCurrentItem();
                    mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).showViewByType();
                    readPost(mPostBeanList.get(mCurrentItem));
                    if (mCurrentItem > 0) {
                        mShowPostList.get((mCurrentItem - 1) % VIEW_PAGER_COUNT).showPostPic(mPostBeanList.get(mCurrentItem - 1));
                    }
                    if (mCurrentItem + 1 < mPostBeanList.size()) {
                        mShowPostList.get((mCurrentItem + 1) % VIEW_PAGER_COUNT).showPostPic(mPostBeanList.get(mCurrentItem + 1));
                    }
                }


                if (arg0 == 0) {
                    mIsViewPagerMove = false;
                }


            }
        });
        if (mFirstShowIndex >= mPostBeanList.size()) {
            mFirstShowIndex = 0;
        }
        viewPager.getOverscrollView().setCurrentItem(mFirstShowIndex);
        int index = mFirstShowIndex % VIEW_PAGER_COUNT;
        readPost(mPostBeanList.get(mFirstShowIndex));
        mShowPostList.get(index).setPostBean(mPostBeanList.get(mFirstShowIndex), true);
//        mShowPostList.get(index).showViewByType();
        mCurrentItem = mFirstShowIndex;

        if (mCurrentItem > 0) {
            mShowPostList.get((mCurrentItem - 1) % VIEW_PAGER_COUNT).showPostPic(mPostBeanList.get(mCurrentItem - 1));
        }
        if (mCurrentItem + 1 < mPostBeanList.size()) {
            mShowPostList.get((mCurrentItem + 1) % VIEW_PAGER_COUNT).showPostPic(mPostBeanList.get(mCurrentItem + 1));
        }

        if (mShowPostType == SHOW_POST_BY_DRAWING_COMMENT_GCM) {
            mShowPostList.get(index).showDrawCommentsView();
        }
        if (!Preferences.getInstacne().getBoolByKey(Constants.TUTORIAL_IN_SHOW_POST_FRAGMENT)) {
            showTourialView();
            Preferences.getInstacne().setValues(Constants.TUTORIAL_IN_SHOW_POST_FRAGMENT, true);
        }
        mNeedDectHasNetFlag = true;
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (mNeedDectHasNetFlag) {
                    try {
                        Thread.sleep(30 * 1000);
                        if (mNeedDectHasNetFlag && (mShowPostList != null && !mShowPostList.isEmpty()
                                && mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).isDownloadedObjectTimeOut())) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!NetUtil.isConnected(getActivity())) {
                                            ((BaseActivity) getActivity()).showNoNetWorkError(PlatformErrorKeys.CONNECTTION_ERROR);
                                        } else {
                                            ((BaseActivity) getActivity()).showNoNetWorkError(PlatformErrorKeys.CONNECTTION_TIMEOUT);
                                        }
                                    }
                                });
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 浏览post
     *
     * @param postBean
     */
    private void readPost(PostBean postBean) {
        if (!postBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
            DataManager.getInstance().readPost(postBean);
        }
    }

    public void onPostItemClick(final boolean showNextPost) {
        getView().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsTouchFinish) {
                    return;
                }
                if (mPostBeanList == null || mPostBeanList.isEmpty()) {
                    goBack();
                    return;
                }
                if (showNextPost) {
                    if (mCurrentItem < mPostBeanList.size() - 1) {
                        ((OverscrollViewPager) getView().findViewById(R.id.viewPager)).getOverscrollView().setCurrentItem(mCurrentItem + 1);
                    } else {
                        goBack();
                    }
                } else {
                    if (mCurrentItem > 0) {
                        ((OverscrollViewPager) getView().findViewById(R.id.viewPager)).getOverscrollView().setCurrentItem(mCurrentItem - 1);
                    } else {
                        goBack();
                    }
                }
            }
        }, 200);
    }


    public void startReplyTagThread() {
        mIsStartThread = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mIsStartThread) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        mIsStartThread = false;
                        break;
                    }
                    if (mReplyTag == null || mReplyTag.getReplyNums() == 0) {
                        mIsStartThread = false;
                        break;
                    }
                    if (mStartCountTime + SEND_REPLYTAG_TIME < System.currentTimeMillis()) {
                        mStartCountTime = System.currentTimeMillis();
                        if (mReplyTag == null || mReplyTag.getReplyNums() == 0) {
                            mIsStartThread = false;
                            break;
                        }
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendReplyTagToServer();
                            }
                        });
                    }
                }
            }
        }).start();
    }

    private void sendReplyTagToServer() {
        PostBean postBean = mPostBeanList.get(mCurrentItem);
        DataManager.getInstance().createReplyTagForPost(mReplyTag, postBean);
        mReplyTag.setReplyNums(0);
    }

    public void removeAllPostByUser(String userId) {
        ArrayList<PostBean> deleteList = new ArrayList<>();
        for (PostBean postBean : mPostBeanList) {
            if (postBean.getOwner().getUserId().equals(userId)) {
                deleteList.add(postBean);
            }
        }
        mPostBeanList.removeAll(deleteList);
        refreshShowView();
    }

    public void removePost() {
        if (mPostBeanList.size() <= 0 || mCurrentItem >= mPostBeanList.size()) {
            goBack();
            return;
        }
        PostBean postBean = mPostBeanList.get(mCurrentItem);
        DataManager.getInstance().saveIds(postBean.getId());

        mPostBeanList.remove(mCurrentItem);
        refreshShowView();

    }

    private void refreshShowView() {
        if (mPostBeanList.size() <= 0) {
            goBack();
            return;
        }
        final OverscrollViewPager viewPager = (OverscrollViewPager) getView().findViewById(R.id.viewPager);
        viewPager.getOverscrollView().getAdapter().notifyDataSetChanged();

        ShowPostView showPostView = mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT);
        showPostView.destroyView();
        showPostView.resetWH();
        if (mCurrentItem < mPostBeanList.size()) {
            viewPager.getOverscrollView().setCurrentItem(mCurrentItem);
        } else {
            mCurrentItem = mPostBeanList.size() - 1;
            viewPager.getOverscrollView().setCurrentItem(mCurrentItem);
        }
        mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).showViewByType();
        readPost(mPostBeanList.get(mCurrentItem));
        if (mCurrentItem > 0) {
            mShowPostList.get((mCurrentItem - 1) % VIEW_PAGER_COUNT).showPostPic(mPostBeanList.get(mCurrentItem - 1));
        }
        if (mCurrentItem + 1 < mPostBeanList.size()) {
            mShowPostList.get((mCurrentItem + 1) % VIEW_PAGER_COUNT).showPostPic(mPostBeanList.get(mCurrentItem + 1));
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (mShowPostList == null || mShowPostList.isEmpty()) {
            return;
        }

        if (hidden) {
            if (getActivity() != null) {
                ((BaseActivity) getActivity()).setScreenFull(false);
            }
            if (mShowPostList != null && !mShowPostList.isEmpty()) {
                mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).destroyView();
            }
        } else {
            BasicUser basicUser = DataManager.getInstance().getCurOtherUser();
            if (basicUser != null && mPostBeanList != null) {
                for (PostBean postBean : mPostBeanList) {
                    if (postBean.getOwner().getUserId().equals(basicUser.getUserId())) {
                        postBean.getOwner().setRemarkName(basicUser.getRemarkName());
                    }
                }
            }
            ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
            if (basicUser != null && allFriends != null) {
                for (BasicUser friend : allFriends) {
                    if (friend.getUserId().equals(basicUser.getUserId())) {
                        friend.setRemarkName(friend.getRemarkName());
                        break;
                    }
                }
            }
            DataManager.getInstance().setCurOtherUser(null);
            ((BaseActivity) getActivity()).setScreenFull(true);
            if (mShowPostList != null && !mShowPostList.isEmpty()) {
                ShowPostView showPostView = mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT);
                if (!showPostView.isPlayingVideo()) {
                    showPostView.showViewByType();
                }
                showPostView.resetRemarkName();
                showPostView.resetRemarkName();
                if (mCurrentItem > 0) {
                    showPostView.resetRemarkName();
                }
            }

        }
    }

    /**
     * 判断需要显示的ShowPostView是否为当前的ShowPostView
     *
     * @param showPostView
     * @return
     */
    public boolean isShowPostViewCurrentItem(ShowPostView showPostView) {
        return showPostView == mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT);
    }


    public void onResume() {
        super.onResume();
        onHiddenChanged(false);
    }


    @Override
    public void onPause() {
        super.onPause();

        ((FlyEmoView) getView().findViewById(R.id.flyEmoView)).stopEmoFly();
        mIsStartThread = false;
        if (mReplyTag != null && mReplyTag.getReplyNums() > 0) {
            sendReplyTagToServer();
        }
        onHiddenChanged(true);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mNeedDectHasNetFlag = false;
        BitmapCacheManager.getInstance().evictAll();
        if (mShowPostList != null) {
            for (ShowPostView showPostView : mShowPostList) {
                showPostView.destroyView();
            }
        }
    }


    public void showPostViewPopup(final PushMessageBean pushMessageBean) {
        if (getActivity() == null || getView() == null || isAnimation) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isAnimation = true;
                initView(pushMessageBean);
                getPopupWindow().setVisibility(View.VISIBLE);
                ObjectAnimator animator = ObjectAnimator.ofFloat(getPopupWindow(), "translationY",
                        -getPopupWindow().getMeasuredHeight(), 0.0f);
                animator.setDuration(500).start();
                animator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        getPopupWindow().clearAnimation();
                        isAnimation = false;
                    }
                });
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }
                        hidePopupWindow();
                    }
                }, 4 * 1000);
            }
        });

    }

    private void hidePopupWindow() {
        if (isAnimation) {
            return;
        }
        isAnimation = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(getPopupWindow(), "translationY",
                0.0f, -getPopupWindow().getMeasuredHeight());
        animator.setDuration(500).start();
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getPopupWindow().setVisibility(View.GONE);
                getPopupWindow().clearAnimation();
                isAnimation = false;
            }
        });
    }

    private View getPopupWindow() {
        return getView().findViewById(R.id.popupWindow);
    }

    private void initView(final PushMessageBean pushMessageBean) {
        if (getView() == null) {
            return;
        }
        ((TextView) getView().findViewById(R.id.pop_message)).setText(pushMessageBean.getBody());
        getView().findViewById(R.id.pop_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
                if (allFriends != null) {
                    for (BasicUser basicUser : allFriends) {
                        if (basicUser.getUserId().equals(pushMessageBean.getCuid())) {
                            DataManager.getInstance().setCurOtherUser(basicUser);
                            gotoPager(ChatFragment.class, null);
                            return;
                        }
                    }
                }

                showLoadingDialog("", null, true);
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        final ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(false);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideLoadingDialog();
                                if (allFriends != null) {
                                    for (BasicUser basicUser : allFriends) {
                                        if (basicUser.getUserId().equals(pushMessageBean.getCuid())) {
                                            DataManager.getInstance().setCurOtherUser(basicUser);
                                            gotoPager(ChatFragment.class, null);
                                            return;
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    @Override
    public void goBack() {
        if (mShowPostList == null || mShowPostList.isEmpty()) {
            super.goBack();
            return;
        }
        if (mShowPostList.get(mCurrentItem % VIEW_PAGER_COUNT).isGoBack()) {
            onHiddenChanged(true);
            super.goBack();
        }
    }

    Handler oneBtnDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    DataManager.getInstance().setSelectObject(0);
                    goBack();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        if (dataType == 1) {
            if (oprateType == 0) {
                for (PostBean postBean : mPostBeanList) {
                    if (postBean.getId().equals(((PostBean) data).getId())) {
                        mPostBeanList.remove(postBean);
                        break;
                    }
                }
                refreshShowView();
            } else {
                BasicUser dataOwer = ((PostBean) data).getOwner();
                for (int i = 0; i < mPostBeanList.size(); i++) {
                    PostBean postBean = mPostBeanList.get(i);
                    if (postBean.getOwner().getUserId().equals(dataOwer.getUserId())) {
                        postBean.getOwner().resetBasicUser(dataOwer);
                    }
                }
            }
        }
    }

    public boolean isOldData() {
        return isOldData;
    }
}
