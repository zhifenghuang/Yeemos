package com.yeemos.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 16-7-20.
 */
public class MyFriendAdapter extends BaseAdapter {

    private ArrayList<BasicUser> mMyFriendList;
    private Context mContext;

    public MyFriendAdapter(Context context) {
        mContext = context;
    }

    public void setMyFriends(ArrayList<BasicUser> myFriendList) {
        if (mMyFriendList == null) {
            mMyFriendList = new ArrayList<>();
        }
        mMyFriendList.clear();
        mMyFriendList.addAll(myFriendList);
        if (!mMyFriendList.isEmpty()) {
            Collections.sort(mMyFriendList, new Comparator<BasicUser>() {
                @Override
                public int compare(BasicUser user1, BasicUser user2) {
                    return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
                }
            });
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mMyFriendList == null ? 0 : mMyFriendList.size();
    }

    @Override
    public BasicUser getItem(int position) {
        return mMyFriendList == null ? null : mMyFriendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.my_friend_item, null);
            convertView.setTag(viewHolder);
            viewHolder.tvChar = (TextView) convertView.findViewById(R.id.tvChar);
            viewHolder.line = convertView.findViewById(R.id.line);
            viewHolder.ivAvater = (RoundedImageView) convertView.findViewById(R.id.ivAvater);
            viewHolder.tvDisplayName = (TextView) convertView.findViewById(R.id.tvDisplayName);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BasicUser basicUser = mMyFriendList.get(position);
        char firstChar = basicUser.getPinyinName().toUpperCase().charAt(0);
        viewHolder.line.setVisibility(View.GONE);
        if (position > 0) {
            char lastFirstChar = mMyFriendList.get(position - 1).getRemarkName().toUpperCase().charAt(0);
            if (firstChar == lastFirstChar) {
                viewHolder.tvChar.setVisibility(View.GONE);
            } else {
                viewHolder.tvChar.setVisibility(View.VISIBLE);
                viewHolder.tvChar.setText(String.valueOf(firstChar));
                viewHolder.line.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.tvChar.setVisibility(View.VISIBLE);
            viewHolder.tvChar.setText(String.valueOf(firstChar));
        }
        viewHolder.tvDisplayName.setText(basicUser.getRemarkName());
        viewHolder.tvUserName.setText("@" + basicUser.getUserName());
        viewHolder.ivAvater.setNeedDrawVipBmp(basicUser.isAuthenticate());
 //       viewHolder.ivAvater.setDefaultImageResId(R.drawable.default_avater);
//        viewHolder.ivAvater.setTag(basicUser);
//        viewHolder.ivAvater.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                BasicUser user=(BasicUser) v.getTag();
//                if(user!=null){
//                    DataManager.getInstance().setCurOtherUser(user);
//
//                }
//            }
//        });
 //       viewHolder.ivAvater.setImageUrl(Preferences.getAvatarUrl(basicUser.getUserAvatar()));
        Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()),viewHolder.ivAvater);
        return convertView;
    }

    class ViewHolder {
        public TextView tvChar;
        public View line;
        public RoundedImageView ivAvater;
        public TextView tvDisplayName;
        public TextView tvUserName;
    }
}
