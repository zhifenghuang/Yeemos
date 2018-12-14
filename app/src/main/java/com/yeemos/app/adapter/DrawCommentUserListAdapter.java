package com.yeemos.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.DrawComentPopupWindow;
import com.yeemos.app.view.DrawCommentView;
import com.yeemos.app.view.DrawUserLongClickView;
import com.yeemos.app.view.FiveBtnPopupWindow;
import com.yeemos.app.view.RoundedImageView;

import java.util.ArrayList;


/**
 * Created by gigabud on 17-1-19.
 */

public class DrawCommentUserListAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;

    private boolean mIsAnonymity;
    private PostBean postBean;
    private Context mContext;
    private DrawCommentView mDrawCommentView;
    private DrawComentPopupWindow drawComentPopupWindow;

    public DrawCommentUserListAdapter(Context mContext) {
        this.mContext = mContext;
    }

    public void setPostBean(PostBean postBean, DrawCommentView mDrawCommentView, DrawComentPopupWindow drawComentPopupWindow) {
        this.postBean = postBean;
        this.mDrawCommentView = mDrawCommentView;
        this.drawComentPopupWindow = drawComentPopupWindow;
        mIsAnonymity = postBean.getIsAnonymity() == 1;
        notifyDataSetChanged();
    }

    public void hideOrShowAllUserDrawed(int hideAll) {
        for (BasicUser basicUser : arrayList) {
            basicUser.setHide(hideAll);
        }
        notifyDataSetChanged();
    }

    public void setArrayList(ArrayList<BasicUser> arrayList) {
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList == null ? 0 : arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHold viewHold;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.draw_comment_user_item, null);
            viewHold = new ViewHold(view);
            view.setTag(viewHold);
        } else {
            Object obj = view.getTag();
            if (obj != null) {
                viewHold = (ViewHold) obj;
            } else {
                view = LayoutInflater.from(mContext).inflate(R.layout.draw_comment_user_item, null);
                viewHold = new ViewHold(view);
                view.setTag(viewHold);
            }

        }
        viewHold.setBasicUser(arrayList.get(i));

        return view;
    }

    class ViewHold implements View.OnClickListener, View.OnLongClickListener {

        private RoundedImageView userAvater;
        private TextView tvShowOrHide, tvUserName;
        private BasicUser basicUser;

        public ViewHold(View view) {
            userAvater = (RoundedImageView) view.findViewById(R.id.userAvater);
            tvShowOrHide = (TextView) view.findViewById(R.id.tvShowOrHide);
            tvUserName = (TextView) view.findViewById(R.id.tvUserName);


            userAvater.setOnClickListener(this);
            tvShowOrHide.setOnClickListener(this);
            tvUserName.setOnClickListener(this);


            tvUserName.setOnLongClickListener(this);
            view.setOnLongClickListener(this);

        }


        public void setBasicUser(BasicUser basicUser) {
            this.basicUser = basicUser;
            if (basicUser.isHide()) {
                tvShowOrHide.setText(ServerDataManager.getTextFromKey("ddl_btn_show"));
                tvShowOrHide.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_rect_6_color_45_223_227));
            } else {
                tvShowOrHide.setText(ServerDataManager.getTextFromKey("ddl_btn_hide"));
                tvShowOrHide.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_rect_6_color_142_153_168));
            }
            if (mIsAnonymity) {
                String userId = basicUser.getUserId();
                String md5Str = Utils.MD5(basicUser.getUserId() + postBean.getId());
             //   tvUserName.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                tvUserName.setTextColor(mContext.getResources().getColor(R.color.color_187_187_187));
                if (userId.equals(MemberShipManager.getInstance().getUserID())) {
                    tvUserName.setText(Utils.getEmoText(md5Str) + ServerDataManager.getTextFromKey("pblc_txt_me"));
                } else {
                    tvUserName.setText(Utils.getEmoText(md5Str));
                }
                userAvater.setNeedDrawVipBmp(false);
                if (userId.equals(postBean.getOwner().getUserId())) {
                    userAvater.setImageResource(R.drawable.hide_on_bush_own);
                } else {
                    userAvater.setImageResource(R.drawable.hide_on_bush_other);
                }
            } else {
                userAvater.setNeedDrawVipBmp(basicUser.isAuthenticate());
                tvUserName.setText(basicUser.getRemarkName());
                //viewHolder.ivAvater.setImageUrl(Preferences.getAvatarUrl(replyTagBean.getReplyUser().getUserAvatar()));
                Utils.loadImage(mContext, R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()), userAvater);
            }


        }

        public void showPopupWindow() {
            DrawUserLongClickView mDrawUserLongClickView = new DrawUserLongClickView(BaseApplication.getCurFragment().getActivity(),
                    new FiveBtnPopupWindow.FiveBtnPopupWindowClickListener() {
                        @Override
                        public void onFirstBtnClicked() {
                            DataManager.getInstance().deleDrawComentByUser(postBean, basicUser);
                            arrayList.remove(basicUser);
                            mDrawCommentView.deleteImageCommentByUserId(Long.parseLong(basicUser.getUserId()));
                            notifyDataSetChanged();
                            if (getCount() == 0) {
                                drawComentPopupWindow.getTopButton().setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onSecondBtnClicked() {
                            mDrawCommentView.setReplyID(basicUser.getUserId());
                            drawComentPopupWindow.dismiss();
                        }

                        @Override
                        public void onThirdBtnClicked() {

                        }

                        @Override
                        public void onFourthBtnClicked() {

                        }

                        @Override
                        public void onFifthBtnClicked() {

                        }

                        @Override
                        public void onCancelBtnClicked() {

                        }
                    });
            mDrawUserLongClickView.setBasicUser(basicUser, postBean);
            mDrawUserLongClickView.initView();
            mDrawUserLongClickView.showAtLocation((View) mDrawCommentView.getParent(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.userAvater:
                case R.id.tvUserName:
                    if (postBean.getIsAnonymity() != 1) {
                        DataManager.getInstance().setCurOtherUser(basicUser);
                        if (basicUser.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                            BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
                        } else {
                            BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                        }
                    }
                    break;
                case R.id.tvShowOrHide:
                    if (basicUser.isHide()) {
                        tvShowOrHide.setText(ServerDataManager.getTextFromKey("ddl_btn_hide"));
                        tvShowOrHide.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_rect_6_color_142_153_168));
                    } else {
                        tvShowOrHide.setText(ServerDataManager.getTextFromKey("ddl_btn_show"));
                        tvShowOrHide.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_rect_6_color_45_223_227));
                    }
                    mDrawCommentView.showOrHideImageCommentByUserId(Long.valueOf(basicUser.getUserId()), !basicUser.isHide());
                    DataManager.getInstance().hide(postBean, basicUser);
                    break;
            }
        }

        @Override
        public boolean onLongClick(View view) {
            showPopupWindow();
            return true;
        }
    }

}
