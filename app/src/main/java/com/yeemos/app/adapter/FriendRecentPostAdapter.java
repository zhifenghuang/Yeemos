package com.yeemos.app.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.BeansBase.TagBean;
import com.yeemos.app.R;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.interfaces.OnItemClickListener;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.CustomUrlImageView;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-3-18.
 */
public class FriendRecentPostAdapter extends RecyclerView.Adapter<FriendRecentPostAdapter.ViewHolder> {

    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private ArrayList<PostBean> mPostBeanList;

    public FriendRecentPostAdapter(Context context) {
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RelativeLayout rl = new RelativeLayout(mContext);
        ViewHolder viewHolder = new ViewHolder(rl);
        CustomUrlImageView imageView = new CustomUrlImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        int width = (int) (parent.getMeasuredWidth() * 1f / 3 + 0.5f);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(width, parent.getMeasuredHeight());
        int padding = Utils.dip2px(mContext, 1);
        rl.setPadding(padding, padding, padding, padding);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        rl.addView(imageView, lp);
        imageView.setViewWH(width - 2 * padding, parent.getMeasuredHeight() - 2 * padding);
        imageView.setNeedRouctRect(true);
        ImageView iv = new ImageView(mContext);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Utils.dip2px(mContext, 15), Utils.dip2px(mContext, 21));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.leftMargin = Utils.dip2px(mContext, 2);
        layoutParams.bottomMargin = Utils.dip2px(mContext, 2);
        rl.addView(iv, layoutParams);
        viewHolder.mImageView = imageView;
        viewHolder.mIvEmo = iv;
        return viewHolder;
    }

    public void setPostBeanList(ArrayList<PostBean> postBeanList) {
        mPostBeanList = postBeanList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PostBean postBean = mPostBeanList.get(position);
        holder.mImageView.setTag(R.id.post_bean,postBean);
        holder.mImageView.setPostBean(postBean);
        holder.mImageView.setNeedRouctRect(true);
//        if (postBean.getAttachDataType() == Constants.POST_ATTACH_DATA_TYPE.ONLY_TEXT.GetValue()) {
//            holder.mTvText.setVisibility(View.VISIBLE);
//            holder.mTvText.setText(postBean.getText());
//        } else {
//            holder.mTvText.setVisibility(View.GONE);
//        }
        ArrayList<TagBean> tagList = postBean.getTags();
        if (tagList != null && !tagList.isEmpty()) {
            holder.mIvEmo.setVisibility(View.VISIBLE);
            int resId = Constants.EMO_ID_COLOR[tagList.get(0).getId()][0];
            holder.mIvEmo.setImageResource(resId);
            String openIds = Preferences.getInstacne().getValues(HomeActivity.HAD_OPEN_POST_IDS, "");
            if (openIds.contains(postBean.getId())) {
                holder.mIvEmo.setAlpha(0.5f);
            } else {
                holder.mIvEmo.setAlpha(1f);
            }
        } else {
            holder.mIvEmo.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return mPostBeanList == null ? 0 : mPostBeanList.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(getAdapterPosition());

                        PostBean postBean = (PostBean) mImageView.getTag(R.id.post_bean);
                        if (postBean == null) {
                            return;
                        }
                        String openIds = Preferences.getInstacne().getValues(HomeActivity.HAD_OPEN_POST_IDS, "");
                        if (openIds.length() == 0) {
                            openIds += postBean.getId();
                        } else {
                            openIds += ("," + postBean.getId());
                        }
                        Preferences.getInstacne().setValues(HomeActivity.HAD_OPEN_POST_IDS, openIds);
                        mIvEmo.setAlpha(0.5f);
                    }
                }
            });
        }

        CustomUrlImageView mImageView;
        ImageView mIvEmo;
        //      TextView mTvText;
    }
}
