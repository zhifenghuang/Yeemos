package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.viewholder.UserFollowSearchHolder;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-8-1.
 */
public class AddByFbAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;

    @Override
    public int getCount() {
        return arrayList == null || arrayList.isEmpty() ? 0 : arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setArrayList(ArrayList<BasicUser> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserFollowSearchHolder hold;
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.mpager_user_user_item, parent, false);
            hold = new UserFollowSearchHolder();
            hold.viewHolder(convertView, BaseApplication.getCurFragment());
            convertView.setTag(hold);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                hold = (UserFollowSearchHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.mpager_user_user_item, parent, false);
                hold = new UserFollowSearchHolder();
                hold.viewHolder(convertView, BaseApplication.getCurFragment());
                convertView.setTag(hold);
            }
        }
        hold.fill(arrayList.get(position), position);

        return convertView;
    }
}
