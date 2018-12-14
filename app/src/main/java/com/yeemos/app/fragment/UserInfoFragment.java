package com.yeemos.app.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.PushMessageBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.ConnectedUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.R;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.DataChangeManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.DirectionalViewPager;
import com.yeemos.app.view.FiveBtnPopupWindow;
import com.yeemos.app.view.FollowButton;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.RenameView;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.view.VerticalSwipeRefreshLayout;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-5-24.
 */
public class UserInfoFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    //    protected ArrayList<View> mUserInfoViews;
//    private ArrayList<UserRecentPostViewGroup> mUserPostViews;
    //    private ArrayList<PostBean> mUserAllPosts;
    protected boolean mIsInHomeActivity = false;
    protected BasicUser mInitUser, basicUser;
    private Dialog dialog;


//    private int mMoveType = 0; //１表示向上，２向下
//    private int mFirstY;

    protected DirectionalViewPager mParentDirectionalViewPager;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_user_profile;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);

        ((TextView) view.findViewById(R.id.tvDisplayName)).setText("");
        ((TextView) view.findViewById(R.id.tvFollowingNum)).setText("0");
        ((TextView) view.findViewById(R.id.tvFollowersNum)).setText("0");
        ((TextView) view.findViewById(R.id.tvPostNum)).setText("0");
        ((TextView) view.findViewById(R.id.tvFollowingText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_following"));
        ((TextView) view.findViewById(R.id.tvFollowersText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_follower"));
        ((TextView) view.findViewById(R.id.tvPostText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_posts"));
        RoundedImageView ivAvater = (RoundedImageView) view.findViewById(R.id.ivAvater);
        ivAvater.setImageResource(R.drawable.default_avater);
        ((TextView) getView().findViewById(R.id.tvUserName)).setText("");

        Bundle bundle = getArguments();
        if (bundle != null) {
            PushMessageBean pushMessageBean = (PushMessageBean) bundle
                    .getSerializable(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN);
            if (pushMessageBean != null) {
                MyFirebaseMessagingService.PushType pushType = MyFirebaseMessagingService.PushType.valueOf(Integer.parseInt(pushMessageBean
                        .getType()));
                BasicUser user = new BasicUser();
                if (pushType == MyFirebaseMessagingService.PushType.TYPE_SEE_A_USER) {
                    if (pushMessageBean.getId().equals(MemberShipManager.getInstance().getUserName())) {
                        user.setUserId(MemberShipManager.getInstance().getUserID());
                    } else {
                        user.setUserName(pushMessageBean.getId());
                    }
                } else {
                    user.setUserId(pushMessageBean.getCuid());
                }
                DataManager.getInstance().setCurOtherUser(user);
            }
        }
        ((ImageButton) view.findViewById(R.id.btnMore)).setImageResource(R.drawable.black_more);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.btnMore).setOnClickListener(this);
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        if (!mIsInHomeActivity) {
            mInitUser = DataManager.getInstance().getCurOtherUser();
            if (mInitUser == null) {
                return;
            }
            getUserDetailInfo(mInitUser);
        }
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
    }

    private void getUserDetailInfo(final BasicUser user) {
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServerResultBean<BasicUser> serverBean = DataManager.getInstance().getUserDetailInfo(user, 0,
                        GBSConstants.UserDataType.User_Data_AllPost, GBSConstants.SortType.SortType_Time, GBSConstants.SortWay.SortWay_Descending);
                if (getView() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getSwipeRefreshLayout().setRefreshing(false);
                        }
                    });
                }
                if (serverBean != null) {
                    basicUser = serverBean.getData();
                    if (basicUser == null) {
                        return;
                    }
                    if (basicUser.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                        DataManager.getInstance().cacheCurrentUser(serverBean);
                    }
                    DataManager.getInstance().setCurOtherUser(basicUser);
                    if (getView() != null) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ((TextView) getView().findViewById(R.id.tvDisplayName)).setText(basicUser.getRemarkName());
                                initUserInfo(basicUser);
                            }
                        });
                    }
                }
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
        //    showUserPostViewPage(mUserAllPosts);
        return false;
    }

    @Override
    public void updateUIText() {
        //       ((TextView) getView().findViewById(R.id.tvNoPost)).setText(ServerDataManager.getTextFromKey("frndprfl_txt_nopost"));
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }


    public void initUserInfo(BasicUser basicUser) {
        if (getView() == null) {
            return;
        }
        this.basicUser = basicUser;

        initUserFollowInfo(basicUser);

        RoundedImageView ivAvater = (RoundedImageView) getView().findViewById(R.id.ivAvater);
        ivAvater.setNeedDrawVipBmp(basicUser.isAuthenticate());
        Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()), ivAvater);
        ((TextView) getView().findViewById(R.id.tvUserName)).setText("@" + basicUser.getUserName());

        initBottomView(basicUser);

        ((VerticalSwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout))
                .setParentDirectionalViewPager(mParentDirectionalViewPager);

        getView().findViewById(R.id.tvUserBio).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ScrollView scrollView = (ScrollView) v.getParent();
                if (mParentDirectionalViewPager != null) {
                    mParentDirectionalViewPager.setCanScroll(false);
                }

                ((VerticalSwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout))
                        .setCanRefresh(scrollView.getScrollY() <= 5);
                if (scrollView.getScrollY() <= 5) {
                    getView().findViewById(R.id.swipeRefreshLayout).onTouchEvent(event);
                }
                return v.onTouchEvent(event);
            }
        });
    }

    public void initUserFollowInfo(BasicUser basicUser) {
        ((TextView) getView().findViewById(R.id.tvFollowingNum)).setText(Utils.transformNumber(basicUser.getFollowingNums()));
        ((TextView) getView().findViewById(R.id.tvFollowersNum)).setText(Utils.transformNumber(basicUser.getFollowersNums()));
        ((TextView) getView().findViewById(R.id.tvPostNum)).setText(Utils.transformNumber(basicUser.getPostNums()));
        ((TextView) getView().findViewById(R.id.tvUserBio)).setText(basicUser.getBio());
        ((TextView) getView().findViewById(R.id.tvFollowingText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_following"));
        ((TextView) getView().findViewById(R.id.tvFollowersText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_follower"));
        ((TextView) getView().findViewById(R.id.tvPostText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_posts"));
        if ((basicUser.getSetting() == null || basicUser.getSetting().getPrivateFollowList() == 0)) {
            if (basicUser.isPrivate() && basicUser.getFollowStatus() != 1) {
                return;
            }
            getView().findViewById(R.id.llFollowing).setOnClickListener(this);
            getView().findViewById(R.id.llFollowers).setOnClickListener(this);
        }
        getView().findViewById(R.id.llPost).setOnClickListener(this);
    }


//    public void initUserInfoViewPage(BasicUser basicUser) {
//        if (getView() == null) {
//            return;
//        }
//        this.basicUser = basicUser;
//        ViewPager userInfoPager = (ViewPager) getView().findViewById(R.id.userInfoViewPager);
//        if (mUserInfoViews == null || mUserInfoViews.isEmpty()) {
//            mUserInfoViews = new ArrayList<>();
//            LayoutInflater lf = LayoutInflater.from(getActivity());
//            View view = lf.inflate(R.layout.user_profile_page1, null);
//            mUserInfoViews.add(view);
//            view = lf.inflate(R.layout.user_profile_page2, null);
//            mUserInfoViews.add(view);
//            userInfoPager.setAdapter(new PagerAdapter() {
//                @Override
//                public boolean isViewFromObject(View arg0, Object arg1) {
//                    return arg0 == arg1;
//                }
//
//                @Override
//                public int getCount() {
//                    return mUserInfoViews.size();
//                }
//
//                @Override
//                public void destroyItem(ViewGroup container, int position, Object object) {
//                    container.removeView(mUserInfoViews.get(position));
//                }
//
//                @Override
//                public int getItemPosition(Object object) {
//                    return super.getItemPosition(object);
//                }
//
//                @Override
//                public CharSequence getPageTitle(int position) {
//                    return "";
//                }
//
//                @Override
//                public Object instantiateItem(ViewGroup container, int position) {
//                    container.addView(mUserInfoViews.get(position));
//                    return mUserInfoViews.get(position);
//                }
//            });
//
//            userInfoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//                @Override
//                public void onPageSelected(int arg0) {
//                    ((PagerDotView) getView().findViewById(R.id.pagerDotView)).setCurrentPageIndex(arg0);
//                }
//
//                @Override
//                public void onPageScrolled(int arg0, float arg1, int arg2) {
//
//                }
//
//                @Override
//                public void onPageScrollStateChanged(int arg0) {
//                }
//            });
//        }
//        View view = mUserInfoViews.get(0);
//        initProfilePage1(view, basicUser);
//
//        RoundedImageView ivAvater = (RoundedImageView) view.findViewById(R.id.ivAvater);
//
//        if (mIsInHomeActivity) {
//            ViewGroup.LayoutParams lp = ivAvater.getLayoutParams();
//            lp.width = lp.height = Utils.dip2px(getActivity(), getActivity().getResources().getDisplayMetrics().heightPixels < 1000 ? 60 : 100);
//        }
//        ivAvater.setNeedDrawVipBmp(basicUser.isAuthenticate());
//        Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()), ivAvater);
//        ((TextView) view.findViewById(R.id.tvUserName)).setText("@" + basicUser.getUserName());
//        view = mUserInfoViews.get(1);
//
//        initProfilePage2(view, basicUser);
//
//        ((PagerDotView) getView().findViewById(R.id.pagerDotView)).setTotalPage(mUserInfoViews.size());
//
//        ((VerticalSwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout))
//                .setParentDirectionalViewPager(mParentDirectionalViewPager);
//
//        view.findViewById(R.id.tvUserBio).setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                ScrollView scrollView = (ScrollView) v.getParent();
//                if (mParentDirectionalViewPager != null) {
//                    mParentDirectionalViewPager.setCanScroll(false);
//                }
//
//                ((VerticalSwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout))
//                        .setCanRefresh(scrollView.getScrollY() <= 5);
//                if (scrollView.getScrollY() <= 5) {
//                    getView().findViewById(R.id.swipeRefreshLayout).onTouchEvent(event);
//                }
//                return v.onTouchEvent(event);
//            }
//        });
//
//    }


    public void initBottomView(final BasicUser basicUser) {
        final FollowButton btnFollow = (FollowButton) getView().findViewById(R.id.btnFollow);
        final ImageView btnNotification = (ImageView) getView().findViewById(R.id.btnNotification);
        btnFollow.setVisibility(View.VISIBLE);
        btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(basicUser.getFollowStatus()));
        btnFollow.setFollowButtonClickListener(new FollowButton.onFollowBtnClickListener() {
            public void onClick() {
                if (btnFollow.getStatus() == FollowButton.FollowButtonStatus.FollowButtonStatus_Follow) {
                    followOperate(basicUser, btnFollow, btnNotification);

                } else {
                    MorePopupWindow popUpWindow = new MorePopupWindow(
                            getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
                        @Override
                        public void onThirdBtnClicked() {
                        }

                        @Override
                        public void onSecondBtnClicked() {
                        }

                        @Override
                        public void onFirstBtnClicked() {
                            followOperate(basicUser, btnFollow, btnNotification);
                        }

                        @Override
                        public void onFourthBtnClicked() {

                        }

                        @Override
                        public void onCancelBtnClicked() {
                            // TODO Auto-generated method stub
                        }
                    }, Constants.MORE_POPUPWINDOW_UNFOLLOWREQUEST);
                    popUpWindow.initView(null);
                    popUpWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                }
            }
        });
        btnNotification.setSelected(basicUser.getIsRecvHisNotification() && basicUser.getFollowStatus() == 1);
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (basicUser.getBlockStatus() == 1) {
                    //如果这个用户已经将当前用户Block
                    getFollowButton().setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Following);
                    getBtnNotification().setSelected(true);
                    if (basicUser.getBlockStatus() == 1) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getFollowButton().setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Follow);
                                getBtnNotification().setSelected(false);
                            }
                        }, 1000);
                    }
                } else {
                    DataManager.getInstance().receivePostNotification(basicUser);
                    btnNotification.setSelected(basicUser.getIsRecvHisNotification() && basicUser.getFollowStatus() == 1);
                    btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(basicUser.getFollowStatus()));
                    DataChangeManager.getInstance().notifyDataChange(0, basicUser, 3);
                    DataChangeManager.getInstance().notifyDataChange(1, new PostBean().setOwner(basicUser), 3);
                }
            }
        });
    }


    private FollowButton getFollowButton() {
        return (FollowButton) getView().findViewById(R.id.btnFollow);
    }

    private ImageView getBtnNotification() {
        return (ImageView) getView().findViewById(R.id.btnNotification);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.btnMore:
                clickBtnMore();
                break;
            case R.id.llFollowing:
                if (basicUser == null) {
                    return;
                }
                DataManager.getInstance().setCurOtherUser(basicUser);
                gotoPager(FollowingFragment.class, null);
                break;
            case R.id.llFollowers:
                if (basicUser == null) {
                    return;
                }
                DataManager.getInstance().setCurOtherUser(basicUser);
                gotoPager(FollowedFragment.class, null);
                break;
            case R.id.llPost:
                if (basicUser == null) {
                    return;
                }
                DataManager.getInstance().setCurOtherUser(basicUser);
                gotoPager(SharesFragment.class, null);
                break;
        }
    }

    public void clickPopupFirstBtn() {
        RenameView renameView = new RenameView(getActivity());
        renameView.setEditUser(basicUser);
        renameView.setFragment(this);
        RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.layout);
        rl.addView(renameView);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            DataManager.getInstance().setCurOtherUser(basicUser);
        } else {
            if (basicUser != null && basicUser.getBlockStatus() == 1) {
                String content = ServerDataManager.getTextFromKey("pblc_txt_blocksuccessful");
                String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                showPublicDialog(null, content, Okey, null, oneBtnBlockDialoghandler);
            } else if (basicUser != null) {
                initBottomView(basicUser);
                initUserFollowInfo(basicUser);
            }
        }
    }

    @Override
    public void refreshFromNextFragment(Object obj) {
        ((TextView) getView().findViewById(R.id.tvDisplayName)).setText(basicUser.getRemarkName());
    }

    public void clickBtnMore() {
        if (basicUser == null) {
            return;
        }
        MorePopupWindow morePopupWindow = new MorePopupWindow(getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
            @Override
            public void onFirstBtnClicked() {
                clickPopupFirstBtn();
            }

            @Override
            public void onSecondBtnClicked() {
                String content = ServerDataManager.getTextFromKey("mssg_block_confirmblock");
                String cancel = ServerDataManager.getTextFromKey("pblc_btn_no");
                String Okey = ServerDataManager.getTextFromKey("pblc_btn_yes");
                showPublicDialog(null, content, cancel, Okey, blockDialog);
            }

            @Override
            public void onThirdBtnClicked() {
                reportTypeWindow();
            }

            @Override
            public void onFourthBtnClicked() {
                if (basicUser.getBlockStatus() == 1) {
                    //如果这个用户已经将当前用户Block
                    getFollowButton().setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Following);
                    getBtnNotification().setSelected(true);
                    if (basicUser.getBlockStatus() == 1) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getFollowButton().setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Follow);
                                getBtnNotification().setSelected(false);
                            }
                        }, 1000);
                    }
                } else {
                    DataManager.getInstance().receivePostNotification(basicUser);
                    getBtnNotification().setSelected(basicUser.getIsRecvHisNotification() && basicUser.getFollowStatus() == 1);
                    getFollowButton().setStatus(FollowButton.FollowButtonStatus.GetObject(basicUser.getFollowStatus()));
                    DataChangeManager.getInstance().notifyDataChange(0, basicUser, 3);
                    DataChangeManager.getInstance().notifyDataChange(1, new PostBean().setOwner(basicUser), 3);
                }
            }

            @Override
            public void onCancelBtnClicked() {

            }
        }, Constants.MORE_POPUPWINDOW_OTHERUSER_MORE);
        morePopupWindow.setBasicUser(basicUser);
        morePopupWindow.initView(null);
        morePopupWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

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
                    DataManager.getInstance().setCurOtherUser(basicUser);
                    DataManager.getInstance().blockUser(basicUser);
                    DataChangeManager.getInstance().notifyDataChange(0, basicUser, 2);
                    String content = ServerDataManager.getTextFromKey("pblc_txt_blocksuccessful");
                    String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                    showPublicDialog(null, content, Okey, null, oneBtnBlockDialoghandler);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    protected Handler oneBtnBlockDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    getActivity().finish();
                    break;
                default:
                    break;
            }
        }
    };

    private void followOperate(BasicUser bBean, final FollowButton btnFollow, ImageView btnNotification) {
        if (bBean.getBlockStatus() == 1) {
            //如果这个用户已经将当前用户Block
            btnFollow.setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Following);
            if (bBean.getBlockStatus() == 1) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btnFollow.setStatus(FollowButton.FollowButtonStatus.FollowButtonStatus_Follow);
                    }
                }, 1000);
            }
        } else {
            DataManager.getInstance().follow(bBean);
            btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(bBean.getFollowStatus()));
            btnNotification.setSelected(bBean.getIsRecvHisNotification());
        }


    }

    private void reportTypeWindow() {

        FiveBtnPopupWindow popUpWindow = new FiveBtnPopupWindow(
                (Activity) getContext(), new FiveBtnPopupWindow.FiveBtnPopupWindowClickListener() {
            @Override
            public void onFirstBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Porn);
            }

            @Override
            public void onSecondBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Scam);
            }

            @Override
            public void onThirdBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Abuse);
            }

            @Override
            public void onFourthBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_CommercialSpam);
            }

            @Override
            public void onFifthBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Offensive);
            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub
            }
        });
        popUpWindow.initView();
        popUpWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void reportSubmit(GBSConstants.MenuOperateType opType) {
        DataManager.getInstance().setCurOtherUser(basicUser);
        DataManager.getInstance().setReportId(basicUser.getUserId());
        if (!ConnectedUtil.isConnected(getContext())) {
            return;
        }

        DataManager.getInstance().report(GBSConstants.MenuObjectType.Menu_Object_User, opType);
        getBtnNotification().setSelected(basicUser.getIsRecvHisNotification() && basicUser.getFollowStatus() == 1);
        getFollowButton().setStatus(FollowButton.FollowButtonStatus.GetObject(basicUser.getFollowStatus()));
        String content = ServerDataManager.getTextFromKey("pblc_txt_reportsuccess");
        String OK = ServerDataManager.getTextFromKey("pub_btn_ok");//"OK";
        showPublicDialog(null, content, OK, null, oneBtnDialoghandler);
    }


    protected Handler oneBtnDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onRefresh() {
        if (basicUser == null) {
            if (mInitUser != null) {
                getUserDetailInfo(mInitUser);
            }
        } else {
            getUserDetailInfo(basicUser);
        }
    }

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        if (dataType == 0) {
            if (basicUser.getUserId().equals(((BasicUser) data).getUserId())) {
                basicUser.setBlockStatus(((BasicUser) data).getBlockStatus());
                basicUser.setFollowedStatus(((BasicUser) data).getFollowedStatus());
                basicUser.setIsRecvHisNotification(((BasicUser) data).getIsRecvHisNotification());
            }
        } else {
            ArrayList<PostBean> userAllPosts = basicUser.getObjectsPosts();
            if (userAllPosts != null && userAllPosts.size() > 0) {
                if (oprateType == 0 || oprateType == 1 || oprateType == 2) {
                    for (PostBean postBean : userAllPosts) {
                        if (postBean.getId().equals(((PostBean) data).getId())) {
                            userAllPosts.remove(postBean);
                            break;
                        }
                    }
                } else if (oprateType == 6) {   //编辑post
                    PostBean postData = (PostBean) data;
                    String postId = postData.getId();
                    for (int i = 0; i < userAllPosts.size(); i++) {
                        PostBean postBean = userAllPosts.get(i);
                        if (postBean.getId().equals(postId)) {
                            postBean.setIsPrivate(postData.getIsPrivate());
                            postBean.setIsAnonymity(postData.getIsAnonymity());
                            postBean.setText(postData.getText());
                            postBean.setTags(postData.getTags());
                            postBean.setExpiredType(postData.getExpiredType());
                            postBean.setCreateTime(postData.getCreateTime());
                            break;
                        }
                    }
                } else {
                    BasicUser dataOwer = ((PostBean) data).getOwner();
                    for (int i = 0; i < userAllPosts.size(); i++) {
                        PostBean postBean = userAllPosts.get(i);
                        if (postBean.getOwner().getUserId().equals(dataOwer.getUserId())) {
                            postBean.getOwner().resetBasicUser(dataOwer);
                        }
                    }
                }
            }
        }
        if (getView() != null) {
            initBottomView(basicUser);
            initUserFollowInfo(basicUser);
        }
    }
}
