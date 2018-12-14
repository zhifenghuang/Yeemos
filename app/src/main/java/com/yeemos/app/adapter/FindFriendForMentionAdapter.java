package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.FollowImageView;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-8-23.
 */
public class FindFriendForMentionAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;


    public void setArrayList(ArrayList<BasicUser> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList == null ? 0:arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.mpager_user_user_item, parent, false);
            holder = new ViewHolder();
            holder.viewHolder(convertView);
            convertView.setTag(holder);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                holder = (ViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.mpager_user_user_item, parent, false);
                holder = new ViewHolder();
                holder.viewHolder(convertView);
                convertView.setTag(holder);
            }
        }
        holder.fill(arrayList.get(position));
        return convertView;
    }
    class ViewHolder {
        protected View convertView;
        protected RoundedImageView imgIcon;
        protected TextView tvTitle;
        protected TextView tvContent;
        protected FollowImageView btnFollow;

        public void viewHolder(View convertView) {
            this.convertView=convertView;
            tvContent = (TextView) convertView.findViewById(R.id.tvContent);
            tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            imgIcon = (RoundedImageView) convertView.findViewById(R.id.imgIcon);
            btnFollow = (FollowImageView) convertView.findViewById(R.id.btnFollow);
            btnFollow.setVisibility(View.GONE);
        }

        public void fill(final BasicUser bBean) {
            tvContent.setText("@" + bBean.getUserName());
            tvTitle.setText(bBean.getRemarkName());
            imgIcon.setNeedDrawVipBmp(bBean.isAuthenticate());
//            imgIcon.setDefaultImageResId(R.drawable.default_avater);
//            imgIcon.setImageUrl(Preferences.getAvatarUrl(bBean.getUserAvatar()));

            Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(bBean.getUserAvatar()),imgIcon);

        }
    }
}
