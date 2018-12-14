package com.yeemos.app.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.R;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.manager.BitmapCacheManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.DirectionalViewPager;
import com.yeemos.app.view.RoundedImageView;

/**
 * Created by gigabud on 16-5-27.
 */
public class MyInfoFragment extends UserInfoFragment {

    public void setParentDirectionalViewPager(DirectionalViewPager parentDirectionalViewPager) {
        mParentDirectionalViewPager = parentDirectionalViewPager;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mIsInHomeActivity) {
            BasicUser myUserInfo = ((HomeActivity) getActivity()).getMyUserInfo();
            initUserInfo(myUserInfo);
            ((ImageButton) view.findViewById(R.id.btnBack)).setImageDrawable(getResources().getDrawable(R.drawable.black_activities));
            ((ImageButton) view.findViewById(R.id.btnMore)).setImageResource(R.drawable.black_setting);
            mParentDirectionalViewPager.setRefreshView(view.findViewById(R.id.swipeRefreshLayout));
            view.findViewById(R.id.btnBackToCamera).setVisibility(View.VISIBLE);
            view.findViewById(R.id.btnBackToCamera).setOnClickListener(this);
            view.setAlpha(0.95f);
        }
        view.findViewById(R.id.friendsProfileBtn).setVisibility(View.GONE);
        view.findViewById(R.id.myProfileLy).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.tvFollowMe)).setText(ServerDataManager.getTextFromKey("prfl_btn_followedme"));
        ((TextView) view.findViewById(R.id.tvFindUser)).setText(ServerDataManager.getTextFromKey("prfl_btn_findfriend"));
        ((TextView) view.findViewById(R.id.tvMyFriend)).setText(ServerDataManager.getTextFromKey("prfl_btn_myfriend"));
    }

    public void setInHomeActivity(boolean isInHomeActivity) {
        mIsInHomeActivity = isInHomeActivity;
    }

    @Override
    public void initBottomView(BasicUser basicUser) {
        getView().findViewById(R.id.friendsProfileBtn).setVisibility(View.GONE);
        LinearLayout myProfileLy = (LinearLayout) getView().findViewById(R.id.myProfileLy);
        myProfileLy.setVisibility(View.VISIBLE);
        getView().findViewById(R.id.rlFollowMe).setOnClickListener(this);
        getView().findViewById(R.id.rlFindUser).setOnClickListener(this);
        getView().findViewById(R.id.rlMyFriend).setOnClickListener(this);
        getView().findViewById(R.id.ivAvater).setOnClickListener(this);
        ((TextView) getView().findViewById(R.id.tvFollowMe)).setText(ServerDataManager.getTextFromKey("prfl_btn_followedme"));
        ((TextView) getView().findViewById(R.id.tvFindUser)).setText(ServerDataManager.getTextFromKey("prfl_btn_findfriend"));
        ((TextView) getView().findViewById(R.id.tvMyFriend)).setText(ServerDataManager.getTextFromKey("prfl_btn_myfriend"));
    }

    @Override
    public void initUserFollowInfo(BasicUser basicUser) {
        ((TextView) getView().findViewById(R.id.tvFollowingNum)).setText(Utils.transformNumber(basicUser.getFollowingNums()));
        ((TextView) getView().findViewById(R.id.tvFollowersNum)).setText(Utils.transformNumber(basicUser.getFollowersNums()));
        ((TextView) getView().findViewById(R.id.tvPostNum)).setText(Utils.transformNumber(basicUser.getPostNums()));
        ((TextView) getView().findViewById(R.id.tvUserBio)).setText(basicUser.getBio());
        ((TextView) getView().findViewById(R.id.tvFollowingText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_following"));
        ((TextView) getView().findViewById(R.id.tvFollowersText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_follower"));
        ((TextView) getView().findViewById(R.id.tvPostText)).setText(ServerDataManager.getTextFromKey("usrprfl_btn_posts"));
        getView().findViewById(R.id.llFollowing).setOnClickListener(this);
        getView().findViewById(R.id.llFollowers).setOnClickListener(this);
        getView().findViewById(R.id.llPost).setOnClickListener(this);
    }

    public void showOrHideRedPoint() {
        if (mIsInHomeActivity) {
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
    }

    @Override
    public void onClick(View v) {
//        if (!mIsInHomeActivity) {
//            super.onClick(v);
//            return;
//        }
        switch (v.getId()) {
            case R.id.btnBack:
                if (mIsInHomeActivity) {
                    Preferences.getInstacne().setValues(HomeActivity.HAD_REQUEST_FOLLOW_YOU_MESSAGE, false);
                    Preferences.getInstacne().setValues(HomeActivity.HAD_NEW_NOTIFICATION_MESSAGE, false);
                    getActivity().sendBroadcast(new Intent(HomeActivity.NOTIFICATION_MESSAGE_LISTENER));
                    gotoPager(ActivitiesFragment.class, null);
                } else {
                    goBack();
                }
                break;
            case R.id.btnMore:
                if (mIsInHomeActivity) {
                    gotoPager(SettingsFragment.class, null);
                } else {
                    clickBtnMore();
                }
                break;
            case R.id.rlFollowMe:
                gotoPager(FollowedMeFragment.class, null);
                break;
            case R.id.rlFindUser:
                gotoPager(FindUserFragment.class, null);
                break;
            case R.id.rlMyFriend:
                gotoPager(MyFriendFragment.class, null);
                break;
            case R.id.llFollowing:
                DataManager.getInstance().setCurOtherUser(DataManager.getInstance().getBasicCurUser());
                gotoPager(FollowingFragment.class, null);
                break;
            case R.id.llFollowers:
                DataManager.getInstance().setCurOtherUser(DataManager.getInstance().getBasicCurUser());
                gotoPager(FollowedFragment.class, null);
                break;
            case R.id.llPost:
                if (basicUser == null) {
                    return;
                }
                DataManager.getInstance().setCurOtherUser(basicUser);
                gotoPager(SharesFragment.class, null);
                break;
            case R.id.ivAvater:
                Bundle bundle = new Bundle();
                bundle.putInt(CameraForChatOrAvaterFragment.USE_CAMERA_TYPE, CameraForChatOrAvaterFragment.TYPE_CAMERA_FOR_AVATER);
                gotoPager(CameraForChatOrAvaterFragment.class, bundle);
                break;
            case R.id.btnBackToCamera:
                if (mParentDirectionalViewPager != null) {
                    mParentDirectionalViewPager.setCurrentItem(1);
                }
                break;
        }
    }

    public void onStart() {
        super.onStart();
        BasicUser userinfo = MemberShipManager.getInstance().getUserInfo()
                .to(BasicUser.class);
        ((TextView) getView().findViewById(R.id.tvUserBio)).setText(userinfo.getBio());

        ((TextView) getView().findViewById(R.id.tvDisplayName)).setText(MemberShipManager.getInstance().getNickName());

        Bitmap bmp = BitmapCacheManager.getInstance().get(PhotoPreviewFragment.PREVIEW_PICTURE);
        if (bmp != null) {
            RoundedImageView roundedImageView = (RoundedImageView) getView().findViewById(R.id.ivAvater);
            roundedImageView.setImageBitmap(bmp);
            MemberShipManager.getInstance().updateUserInfoWithFile(null,
                    null, null, null, null, null,
                    bmp, null, null);

        }
        showOrHideRedPoint();
    }

    @Override
    public void clickPopupFirstBtn() {
        gotoPager(EditProfileFragment.class, null);
    }

    @Override
    public void onRefresh() {
        if (mIsInHomeActivity) {
            ((HomeActivity) getActivity()).getMyDetailInfo(mHandler);
        } else {
            super.onRefresh();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (getView() == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
                    swipeLayout.setRefreshing(false);
                    break;

            }
        }
    };

    @Override
    public void onDataChange(int dataType, Object data, int oprateType) {
        super.onDataChange(dataType, data, oprateType);
        if (dataType == 0) {
            if (oprateType == 0 || oprateType == 5) {
                basicUser.setFollowingNums(basicUser.getFollowingNums() - 1);
            } else if (oprateType == 2) {
                basicUser.setFollowingNums(basicUser.getFollowingNums() - 1);
                basicUser.setFollowersNums(basicUser.getFollowersNums() - 1);
            } else if (oprateType == 4) {
                basicUser.setFollowingNums(basicUser.getFollowingNums() + 1);
            }
        }
    }
}
