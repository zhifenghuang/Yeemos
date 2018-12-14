package com.yeemos.app.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.ReplyTagBean;
import com.gbsocial.BeansBase.TagBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.MyInfoFragment;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.PillSreactView;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-5-26.
 */
public class PillSreactAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<ReplyTagBean> mReplyTagList;
    private PostBean mPosBean;
    private boolean mIsAnonymity;
    private PillSreactView pillSreactView;

    public void setAnonymity(boolean isAnonymity) {
        mIsAnonymity = isAnonymity;
        notifyDataSetChanged();
    }


    public PillSreactAdapter(Context context, PillSreactView pillSreactView) {
        mContext = context;
        this.pillSreactView = pillSreactView;
    }

    public void setReplyTagList(PostBean postBean, ArrayList<ReplyTagBean> replyTagList) {
        mPosBean = postBean;
        mReplyTagList = replyTagList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mReplyTagList == null ? 0 : mReplyTagList.size();
    }

    @Override
    public Object getItem(int position) {
        return mReplyTagList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.pill_sreact_item, null);
            viewHolder = new ViewHolder();
            viewHolder.ivAvater = (RoundedImageView) convertView.findViewById(R.id.userAvater);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
            viewHolder.ivEmo = (ImageView) convertView.findViewById(R.id.ivEmo);
            viewHolder.tvEmoNum = (TextView) convertView.findViewById(R.id.tvEmoNum);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final ReplyTagBean replyTagBean = mReplyTagList.get(position);
        viewHolder.tvUserName.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        viewHolder.tvUserName.setTextColor(mContext.getResources().getColor(R.color.color_142_153_168));
       // viewHolder.ivAvater.setDefaultImageResId(R.drawable.default_avater);
        if (mIsAnonymity) {
            String userId = replyTagBean.getReplyUser().getUserId();
            String md5Str = Utils.MD5(replyTagBean.getReplyUser().getUserId() + mPosBean.getId());
     //       viewHolder.tvUserName.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            viewHolder.tvUserName.setTextColor(mContext.getResources().getColor(R.color.color_187_187_187));
            if (userId.equals(MemberShipManager.getInstance().getUserID())) {
                viewHolder.tvUserName.setText(Utils.getEmoText(md5Str)+ ServerDataManager.getTextFromKey("pblc_txt_me"));
            } else {
                viewHolder.tvUserName.setText(Utils.getEmoText(md5Str));
            }
            viewHolder.ivAvater.setNeedDrawVipBmp(false);
            if (userId.equals(mPosBean.getOwner().getUserId())) {
                viewHolder.ivAvater.setImageResource(R.drawable.hide_on_bush_own);
            } else {
                viewHolder.ivAvater.setImageResource(R.drawable.hide_on_bush_other);
            }
        } else {
            viewHolder.ivAvater.setNeedDrawVipBmp(replyTagBean.getReplyUser().isAuthenticate());
            viewHolder.tvUserName.setText(replyTagBean.getReplyUser().getRemarkName());
            //viewHolder.ivAvater.setImageUrl(Preferences.getAvatarUrl(replyTagBean.getReplyUser().getUserAvatar()));
            Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(replyTagBean.getReplyUser().getUserAvatar()),viewHolder.ivAvater);
        }
//        viewHolder.tvUserName.setVisibility(mIsAnonymity ? View.GONE : View.VISIBLE);
        viewHolder.ivAvater.setTag(R.id.tag,replyTagBean);

        TagBean tagBean = replyTagBean.getTag();
        viewHolder.ivEmo.setImageResource(Constants.EMO_ID_COLOR[tagBean.getId()][0]);
        viewHolder.tvEmoNum.setText(Utils.transformNumber(replyTagBean.getReplyNums()));
        viewHolder.ivAvater.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mIsAnonymity) {
                    ReplyTagBean replyTagBean = (ReplyTagBean) v.getTag(R.id.tag);
                    DataManager.getInstance().setCurOtherUser(replyTagBean.getReplyUser());
                    if (replyTagBean.getReplyUser().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                        BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
                    } else {
                        BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                    }
                }
            }
        });
        viewHolder.tvUserName.setTag(replyTagBean);
        viewHolder.tvUserName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!mIsAnonymity) {
                    ReplyTagBean replyTagBean = (ReplyTagBean) v.getTag();
                    DataManager.getInstance().setCurOtherUser(replyTagBean.getReplyUser());
                    if (replyTagBean.getReplyUser().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                        BaseApplication.getCurFragment().gotoPager(MyInfoFragment.class, null);
                    } else {
                        BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                    }
                }
            }
        });
        getMoreData(position);
        return convertView;
    }

    class ViewHolder {
        RoundedImageView ivAvater;
        TextView tvUserName;
        ImageView ivEmo;
        TextView tvEmoNum;
    }

    private void getMoreData(int position) {
        if (position == getCount() / 2) {
            pillSreactView.loadMoreData();
        }
    }

}
