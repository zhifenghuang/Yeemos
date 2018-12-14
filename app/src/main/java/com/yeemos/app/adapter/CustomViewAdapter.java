package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;

import java.util.ArrayList;

/**
 * Created by gigabud on 17-3-28.
 */

public class CustomViewAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;


    public void setArrayList(ArrayList<BasicUser> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList == null ? 0 : arrayList.size();
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
        ViewHolder hold;
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.custom_view_list_item, parent, false);
            hold = new ViewHolder(convertView);
            convertView.setTag(hold);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                hold = (ViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.custom_view_list_item, parent, false);
                hold = new ViewHolder(convertView);
                convertView.setTag(hold);
            }
        }
        hold.setData(arrayList.get(position));

        return convertView;
    }

    class ViewHolder {
        private RoundedImageView ivAvater;
        private TextView tvDisplayName, tvUserName;

        public ViewHolder(View convertView) {
            ivAvater = (RoundedImageView) convertView.findViewById(R.id.ivAvater);
            tvDisplayName = (TextView) convertView.findViewById(R.id.tvDisplayName);
            tvUserName = (TextView) convertView.findViewById(R.id.tvUserName);
        }

        public void setData(BasicUser userBean) {
            tvDisplayName.setText(userBean.getRemarkName());
            tvUserName.setText("@" + userBean.getUserName());
            Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater, Preferences.getAvatarUrl(userBean.getUserAvatar()), ivAvater);
        }
    }
}
