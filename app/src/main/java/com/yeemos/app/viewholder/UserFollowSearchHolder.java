package com.yeemos.app.viewholder;


import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.FollowButton;
import com.yeemos.app.view.FollowImageView;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

public class UserFollowSearchHolder implements MPagerViewHolder<BasicUser> {
    protected View convertView;
    protected RoundedImageView imgIcon;
    protected TextView tvTitle;
    protected TextView tvContent;
    protected FollowImageView btnFollow;
    private Fragment frg;

    @Override
    public void viewHolder(View convertView, Fragment frg) {
        this.convertView=convertView;
        tvContent = (TextView) convertView.findViewById(R.id.tvContent);
        tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
        imgIcon = (RoundedImageView) convertView.findViewById(R.id.imgIcon);
        btnFollow = (FollowImageView) convertView.findViewById(R.id.btnFollow);
        this.frg = frg;
    }

    public void clickBConvertView(BasicUser bBean){
        String name = bBean.getUserName();
        DataManager.getInstance().setCurOtherUser(bBean);
        if (name.equals(MemberShipManager.getInstance().getUserInfo().getUserName())) {
            BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
        } else {
            BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
        }
    }

    @Override
    public void fill(final BasicUser bBean, int position) {
        tvContent.setText("@" + bBean.getUserName());
        tvTitle.setText(bBean.getRemarkName());
        imgIcon.setNeedDrawVipBmp(bBean.isAuthenticate());
//        imgIcon.setDefaultImageResId(R.drawable.default_avater);
//        imgIcon.setImageUrl(Preferences.getAvatarUrl(bBean.getUserAvatar()));
        Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(bBean.getUserAvatar()),imgIcon);
        convertView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
//				DataManager.getInstance().setCurOtherUser(bBean);
//				BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                clickBConvertView(bBean);
            }
        });
//        tvTitle.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
////				DataManager.getInstance().setCurOtherUser(bBean);
////				BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
//                String name = bBean.getUserName();
//                DataManager.getInstance().setCurOtherUser(bBean);
//                if (name.equals(MemberShipManager.getInstance().getUserInfo().getUserName())) {
//                    BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
//                } else {
//                    BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
//                }
//            }
//        });
//        imgIcon.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                DataManager.getInstance().setCurOtherUser(bBean);
////				BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
//                String name = bBean.getUserName();
//                if (name.equals(MemberShipManager.getInstance().getUserInfo().getUserName())) {
//                    BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
//                } else {
//                    DataManager.getInstance().setCurOtherUser(bBean);
//                    BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
//                }
//            }
//        });
        if (bBean.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
            btnFollow.setVisibility(View.INVISIBLE);
            return;
        }
        btnFollow.setVisibility(View.VISIBLE);
        btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(bBean.getFollowStatus()));
        btnFollow.setFollowImageClickListener(new FollowImageView.OnFollowImgClickListener() {
            public void onClick() {
                if (btnFollow.getStatus() == FollowButton.FollowButtonStatus.FollowButtonStatus_Follow) {
                    followOperate(bBean);
                } else {
                    MorePopupWindow popUpWindow = new MorePopupWindow(frg.getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
                        @Override
                        public void onThirdBtnClicked() {
                        }

                        @Override
                        public void onSecondBtnClicked() {
                        }

                        @Override
                        public void onFirstBtnClicked() {
                            followOperate(bBean);
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
                    popUpWindow.showAtLocation(frg.getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
                }
            }
        });
    }

    private void followOperate(BasicUser bBean) {
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
        }
    }
}
