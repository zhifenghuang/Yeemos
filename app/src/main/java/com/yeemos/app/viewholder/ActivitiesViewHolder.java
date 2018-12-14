package com.yeemos.app.viewholder;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.message;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.R;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomUrlImageView;
import com.yeemos.app.view.FollowButton;
import com.yeemos.app.view.FollowImageView;
import com.yeemos.app.view.RoundedImageView;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-7-21.
 */
public class ActivitiesViewHolder implements View.OnClickListener {
    private Dialog dialog;
    private TextView tvContent;
    private RoundedImageView roundedImageView;
    private FollowImageView btnFollow;
    private CustomUrlImageView activitiesYouPost;
    private LinearLayout agreeOrDisagree;
    private ImageView agree, disagree;
    private BasicUser basicUser;
    private message bean;
    private PostBean postBean = null;
    private Fragment frg;
    private int i = 0;
    private int mViewWidth, mViewHeight;

    public void viewHolder(View convertView, Fragment frg) {
        tvContent = (TextView) convertView.findViewById(R.id.tvContent);

        roundedImageView = (RoundedImageView) convertView.findViewById(R.id.roundedImageView);
        btnFollow = (FollowImageView) convertView.findViewById(R.id.btnFollow);
        activitiesYouPost = (CustomUrlImageView) convertView.findViewById(R.id.activitiesYouPost);
        agreeOrDisagree = (LinearLayout) convertView.findViewById(R.id.agreeOrDisagree);
        agree = (ImageView) convertView.findViewById(R.id.agree);
        disagree = (ImageView) convertView.findViewById(R.id.disagree);
        this.frg = frg;
        roundedImageView.setOnClickListener(this);
        activitiesYouPost.setOnClickListener(this);
        agree.setOnClickListener(this);
        disagree.setOnClickListener(this);
        btnFollow.setOnClickListener(this);
    }

    /**
     * 1:actvty_txt_startfollowingyou:               %s 关注您了。
     * <p>
     * 3:actvty_txt_acceptyourfollowrequest:         %s 同意了您的关注请求。
     * <p>
     * 5:actvty_txt_leftcommentonyourphoto:          %s 评论了您的帖子。
     * actvty_txt_someoneleftcommentonyourphoto    某人评论了您的帖子。（匿名的情况）
     * <p>
     * 6:actvty_txt_likeyourphoto:                   %s 分享了心情给你。
     * actvty_txt_someonelikeyourphoto             某人分享了心情给你。（匿名的情况）
     * <p>
     * 8:actvty_txt_mentionyou:                      %s 提到了您。
     * actvty_txt_someonementionyou                某人提到了您。（匿名的情况）
     * <p>
     * 9:actvty_txt_leftdrawingcomment                    %s 绘画评论了您的帖子。
     * actvty_txt_someoneleftdrawingcomment(匿名的情况)  某人绘画评论了您的帖子。（匿名的情况）
     * <p>
     * 13:actvty_txt_requestfollowyou:                  %s 请求关注您。
     * <p>
     * 14:actvty_txt_postphoto:                         %s 分享了新的心情。
     * actvty_txt_someonepostphoto                   某人分享了新的心情。（匿名的情况）
     */
    public void fill(message bean) {
        this.bean = bean;
        basicUser = bean.getCreateUser();
        roundedImageView.setImageResource(R.drawable.default_avater);
//        BasicUser targetUser = DataManager.getInstance().getBasicCurUser();
//        if (bean.getTargetUser() != null) {
//            targetUser = bean.getTargetUser();
//        }
        BasicUser createUser = basicUser;
        postBean = null;
        long createTime = bean.getCreateTime();
        activitiesYouPost.setViewWH(mViewWidth, mViewHeight);
        ArrayList<BasicUser> operatorUsers = bean.getOperatorUsers();
        if (operatorUsers == null) {
            operatorUsers = new ArrayList<>();
        }
        if (operatorUsers.isEmpty()) {
            operatorUsers.add(createUser);
        }
        String comment = "";
        MyFirebaseMessagingService.PushType pushType = MyFirebaseMessagingService.PushType.valueOf(bean.getMeesageType());
        if (pushType == null) {
            return;
        }
        switch (pushType) {
            case TYPE_OTHER_FOLLOW_YOU:
                if (operatorUsers.size() > 1) {
                    btnFollow.setVisibility(View.GONE);
                    int size = operatorUsers.size();
                    if (size == 2) {
                        comment = btnFollow.getResources().getString(R.string.actvty_txt_twostartfollowingyou,
                                operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName());
                    } else if (size == 3) {
                        comment = btnFollow.getResources().getString(R.string.actvty_txt_threestartfollowingyou,
                                operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), operatorUsers.get(2).getRemarkName());
                    } else {
                        comment = btnFollow.getResources().getString(R.string.actvty_txt_morestartfollowingyou,
                                operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), String.valueOf(operatorUsers.size() - 2));
                    }
                    comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                } else {
                    comment = ServerDataManager.getTextFromKey("actvty_txt_startfollowingyou");
                    comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    btnFollow.setVisibility(View.VISIBLE);
                }
                activitiesYouPost.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                break;
            case TYPE_OTHER_AGREE_YOUR_REQUEST:
                comment = ServerDataManager.getTextFromKey("actvty_txt_acceptyourfollowrequest");
                comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                btnFollow.setVisibility(View.INVISIBLE);
                activitiesYouPost.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                break;
            case TYPE_OTHER_COMMENT_YOUR_POST:
                postBean = bean.getTargetObject();
                if (postBean.getIsAnonymity() == 0) {
                    if (operatorUsers.size() > 1) {
                        int size = operatorUsers.size();
                        if (size == 2) {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_twoleftcommentonyourphoto,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName());
                        } else if (size == 3) {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_threeleftcommentonyourphoto,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), operatorUsers.get(2).getRemarkName());
                        } else {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_moreleftcommentonyourphoto,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), String.valueOf(operatorUsers.size() - 2));
                        }
                        comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    } else {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_leftcommentonyourphoto");
                        comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    }
                } else {
                    if (operatorUsers.size() > 1) {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_moreanonyleftcommentonyourphoto");
                        comment = String.format(comment, String.valueOf(operatorUsers.size()));
                    } else {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_someoneleftcommentonyourphoto");
                    }
                }
                btnFollow.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                activitiesYouPost.setVisibility(View.VISIBLE);
                activitiesYouPost.setPostBean(postBean);
                break;
            case TYPE_OTHER_REPLY_TAG_FOR_YOUR_POST:
                postBean = bean.getTargetObject();
                if (postBean.getIsAnonymity() == 0) {
                    if (operatorUsers.size() > 1) {
                        int size = operatorUsers.size();
                        if (size == 2) {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_twolikeyourphoto,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName());
                        } else if (size == 3) {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_threelikeyourphoto,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), operatorUsers.get(2).getRemarkName());
                        } else {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_morelikeyourphoto,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), String.valueOf(operatorUsers.size() - 2));
                        }
                        comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    } else {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_likeyourphoto");
                        comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    }
                } else {
                    if (operatorUsers.size() > 1) {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_moreanonylikeyourphoto");
                        comment = String.format(comment, String.valueOf(operatorUsers.size()));
                    } else {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_someonelikeyourphoto");
                    }
                }
                btnFollow.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                activitiesYouPost.setVisibility(View.VISIBLE);
                activitiesYouPost.setPostBean(postBean);
                break;
            case TYPE_OTHER_AT_YOU:
            case TYPE_OTHER_REPLY_YOUR_DRAWING_COMMENT:
                postBean = bean.getTargetObject();
                if (postBean.getIsAnonymity() == 0) {
                    comment = ServerDataManager.getTextFromKey("actvty_txt_mentionyou");
                    comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                } else {
                    comment = ServerDataManager.getTextFromKey("actvty_txt_someonementionyou");
                }
                //   comment += postBean.getCommentObjects().get(0).getText();
                btnFollow.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                activitiesYouPost.setVisibility(View.VISIBLE);
                activitiesYouPost.setPostBean(postBean);
                break;
            case TYPE_OTHER_DRAWING_COMMENT:
                postBean = bean.getTargetObject();

                if (postBean.getIsAnonymity() == 0) {
                    if (operatorUsers.size() > 1) {
                        int size = operatorUsers.size();
                        if (size == 2) {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_twoleftdrawingcomment,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName());
                        } else if (size == 3) {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_threeleftdrawingcomment,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), operatorUsers.get(2).getRemarkName());
                        } else {
                            comment = btnFollow.getResources().getString(R.string.actvty_txt_moreleftdrawingcomment,
                                    operatorUsers.get(0).getRemarkName(), operatorUsers.get(1).getRemarkName(), String.valueOf(operatorUsers.size() - 2));
                        }
                        comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    } else {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_leftdrawingcomment");
                        comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                    }
                } else {
                    if (operatorUsers.size() > 1) {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_moreanonyleftdrawingcomment");
                        comment = String.format(comment, String.valueOf(operatorUsers.size()));
                    } else {
                        comment = ServerDataManager.getTextFromKey("actvty_txt_someoneleftdrawingcomment");
                    }
                }
                btnFollow.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                activitiesYouPost.setVisibility(View.VISIBLE);
                activitiesYouPost.setPostBean(postBean);
                break;
//            case Constants.MESSAGE_TYPE_FOLLOWING_OTHER:
//                comment = ServerDataManager.getTextFromKey("actvty_txt_startfollowing");
//                comment = String.format(comment, name[0], name[1]);
//                btnFollow.setVisibility(View.GONE);
//                activitiesYouPost.setVisibility(View.GONE);
//                agreeOrDisagree.setVisibility(View.GONE);
//                break;
//            case Constants.MESSAGE_TYPE_FOLLOWING_LIKE:
//                comment = ServerDataManager.getTextFromKey("actvty_txt_likephoto");
//                comment = String.format(comment, name[0], name[1]);
//                postBean = bean.getTargetObject();
//                btnFollow.setVisibility(View.GONE);
//                agreeOrDisagree.setVisibility(View.GONE);
//                activitiesYouPost.setVisibility(View.VISIBLE);
//                activitiesYouPost.setPostBean(postBean);
//                break;
//            case Constants.MESSAGE_TYPE_FOLLOWING_COMMENT:
//                comment = ServerDataManager.getTextFromKey("actvty_txt_leftcomment");
//                postBean = bean.getTargetObject();
//                comment = String.format(comment, name[0], name[1])+postBean.getCommentObjects().get(0).getText();
//                btnFollow.setVisibility(View.GONE);
//                agreeOrDisagree.setVisibility(View.GONE);
//                activitiesYouPost.setVisibility(View.VISIBLE);
//                activitiesYouPost.setPostBean(postBean);
//                break;
            case TYPE_OTHER_REQUEST_FOLLOW_YOU:
                comment = ServerDataManager.getTextFromKey("actvty_txt_requestfollowyou");
                comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                btnFollow.setVisibility(View.GONE);
                activitiesYouPost.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.VISIBLE);
                break;
            case TYPE_YOUR_FOLLOWER_SEND_POST:
                postBean = bean.getTargetObject();
                if (postBean.getIsAnonymity() == 0) {
                    comment = ServerDataManager.getTextFromKey("actvty_txt_postphoto");
                    comment = String.format(comment, operatorUsers.get(0).getRemarkName());
                } else {
                    comment = ServerDataManager.getTextFromKey("actvty_txt_someonepostphoto");
                }
                btnFollow.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                activitiesYouPost.setVisibility(View.VISIBLE);
                activitiesYouPost.setPostBean(postBean);
                break;
//            case Constants.MESSAGE_TYPE_FACEBOOK_YOU:
//                comment = ServerDataManager.getTextFromKey("activty_txt_fbfriendonmenu");
//                comment = String.format(comment,bean.getContent(), name[0]);
//                btnFollow.setVisibility(View.VISIBLE);
//                activitiesYouPost.setVisibility(View.GONE);
//                agreeOrDisagree.setVisibility(View.GONE);
//                break;
//            case Constants.MESSAGE_TYPE_INSTAGRAM_YOU:
//                comment = ServerDataManager.getTextFromKey("activty_txt_igfriendonmenu");
//                comment = String.format(comment,bean.getContent(), name[0]);
//                btnFollow.setVisibility(View.VISIBLE);
//                activitiesYouPost.setVisibility(View.GONE);
//                agreeOrDisagree.setVisibility(View.GONE);
//                break;
//            case Constants.MESSAGE_TYPE_FOLLOW_SYSTEM:
//                break;
            default:
                break;

        }
        if (postBean == null || postBean.getIsAnonymity() == 0) {
            roundedImageView.setNeedDrawVipBmp(basicUser.isAuthenticate());
            Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()), roundedImageView);
        } else if (postBean.getIsAnonymity() == 1) {
            roundedImageView.setNeedDrawVipBmp(false);
            if (MemberShipManager.getInstance().getUserID().equals(postBean.getOwner().getUserId())) {
                roundedImageView.setImageResource(R.drawable.hide_on_bush_own);
            } else {
                roundedImageView.setImageResource(R.drawable.hide_on_bush_other);
            }
        }
        String date = Utils.getTime(createTime);
        tvContent.setMovementMethod(LinkMovementMethod.getInstance());
        tvContent.setText(Utils.getKeywordClickable(comment + "  " + date, operatorUsers, date, R.color.color_142_153_168));
        if (bean.getCreateUser().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
            btnFollow.setVisibility(View.GONE);
            btnFollow.setClickable(false);
            return;
        } else {
            btnFollow.setClickable(true);
        }
        btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(basicUser.getFollowStatus()));
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.activitiesYouPost:
                if (postBean != null) {
                    BasicUser ower = postBean.getOwner();
                    if (!ower.getUserId().equals(MemberShipManager.getInstance().getUserID())
                            && (ower.isPrivate()
                            || ower.getFollowStatus() != 1)) {
                        String content = ServerDataManager.getTextFromKey("GB3305107");
                        String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                        BaseApplication.getCurFragment().showPublicDialog(null, content, Okey, null, oneBtnDialoghandler);
                    } else {
                        ArrayList<PostBean> arrayList = new ArrayList<>();
                        arrayList.add(postBean);
                        DataManager.getInstance().setShowPostList(arrayList);

                        Bundle bundle = new Bundle();
                        bundle.putInt(ShowPostViewPagerFragment.SHOW_POST_TYPE, ShowPostViewPagerFragment.SHOW_POST_BY_GCM);
                        bundle.putString(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN, postBean.getId());
                        BaseApplication.getCurFragment().gotoPager(ShowPostViewPagerFragment.class, bundle);
                    }
                }
                break;
            case R.id.roundedImageView:
                if (postBean == null || postBean.getIsAnonymity() == 0) {
                    if (basicUser.getUserId().equals(MemberShipManager.getInstance().getUserInfo().getUserId())) {
                        BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
                    } else {
                        DataManager.getInstance().setCurOtherUser(basicUser);
                        BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                    }
                }
                break;
            case R.id.agree:
                btnFollow.setVisibility(View.VISIBLE);
                activitiesYouPost.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                basicUser.setAgreeOrDisagree(true);
                DataManager.getInstance().agreeOrDisagree(basicUser);
                bean.setMeesageType(1);
                btnFollow.setOnClickListener(this);
                break;
            case R.id.disagree:
                btnFollow.setVisibility(View.VISIBLE);
                activitiesYouPost.setVisibility(View.GONE);
                agreeOrDisagree.setVisibility(View.GONE);
                basicUser.setAgreeOrDisagree(false);
                DataManager.getInstance().agreeOrDisagree(basicUser);
                bean.setMeesageType(1);
                btnFollow.setOnClickListener(this);
                break;
            case R.id.btnFollow:
                DataManager.getInstance().follow(basicUser);
                btnFollow.setStatus(FollowButton.FollowButtonStatus.GetObject(basicUser.getFollowStatus()));
                break;
            default:
                break;
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
                    break;
                default:
                    break;
            }
        }
    };

    public void setViewWH(int viewWidth, int viewHeight) {
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
    }
}
