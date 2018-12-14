package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.viewholder.UserFollowSearchHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 16-7-20.
 */
public class FindUserAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;
    private BaseFragment baseFragment;


    public FindUserAdapter(){
    }

    public void setArrayList(ArrayList<BasicUser> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        sortAscendList(arrayList);
        super.notifyDataSetChanged();
    }

    private void sortAscendList(ArrayList<BasicUser> arrayList) {
        Collections.sort(arrayList, new Comparator<BasicUser>() {
            @Override
            public int compare(BasicUser user1, BasicUser user2) {
                return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
            }
        });
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
        getMoreData(position);
        return convertView;
    }

    private void getMoreData(int position) {
        if (position == arrayList.size() / 2) {
            if (baseFragment != null) {
                baseFragment.loadMoreData();
            }
        }
    }

    public void setBaseFragment(BaseFragment baseFragment) {
        this.baseFragment = baseFragment;
    }
}
