package com.yeemos.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.gbsocial.BeansBase.message;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.viewholder.ActivitiesViewHolder;
import com.yeemos.app.R;

import java.util.ArrayList;


/**
 * Created by gigabud on 16-7-21.
 */
public class ActivitiesAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<message> mMessageList;

    public ActivitiesAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mMessageList == null ? 0 : mMessageList.size();
    }

    @Override
    public Object getItem(int position) {
        return mMessageList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setArrayList(ArrayList<message> arrayList) {
        mMessageList=arrayList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ActivitiesViewHolder hold;
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.listview_activities_you_item, parent, false);
            hold = new ActivitiesViewHolder();
            hold.viewHolder(convertView, BaseApplication.getCurFragment());
            convertView.setTag(hold);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                hold = (ActivitiesViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.listview_activities_you_item, parent, false);
                hold = new ActivitiesViewHolder();
                hold.viewHolder(convertView, BaseApplication.getCurFragment());
                convertView.setTag(hold);
            }
        }
        int width = parent.getWidth() / 5;
        hold.setViewWH(width, width);
        hold.fill(mMessageList.get(position));
        return convertView;
    }
}
