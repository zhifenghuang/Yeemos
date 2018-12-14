package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.HashTagBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-8-23.
 */
public class HotTagAdapter extends BaseAdapter {

    private ArrayList<HashTagBean> arrayList;


    public void setArrayList(ArrayList<HashTagBean> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return arrayList == null? 0 : arrayList.size();
    }

    @Override
    public HashTagBean getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AllHashTagHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.listview_hashtag, parent, false);
            holder = new AllHashTagHolder();
            holder.viewHolder(convertView);
            convertView.setTag(holder);
        } else {
            Object obj = convertView.getTag();
            if (obj != null) {
                holder = (AllHashTagHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.listview_hashtag, parent, false);
                holder = new AllHashTagHolder();
                holder.viewHolder(convertView);
                convertView.setTag(holder);
            }
        }
        holder.fill(arrayList.get(position));
        return convertView;
    }
    class AllHashTagHolder {
        private TextView tvHashTag;
        protected TextView tvDes;
        protected TextView tvPhotoUnit;
        protected HashTagBean bean;
        protected RelativeLayout hashTagRe;

        public void fill(HashTagBean bean) {
            this.bean = bean;
            tvHashTag.setText(" " + bean.getHasTag());
            tvHashTag.setCompoundDrawablesWithIntrinsicBounds(BaseApplication.getAppContext().getResources().getDrawable(R.drawable.hashtag_label),null,null,null);
            tvDes.setText("" + bean.getAssociatePostNums());
            tvPhotoUnit.setText(ServerDataManager.getTextFromKey("srchtag_txt_posts"));
        }
        public void viewHolder( View convertView) {
            tvHashTag = (TextView) convertView.findViewById(R.id.tvHashTag);
            tvDes = (TextView) convertView.findViewById(R.id.tvDes);
            tvPhotoUnit = (TextView) convertView.findViewById(R.id.tvPhotoDes);
            hashTagRe = (RelativeLayout)convertView.findViewById(R.id.hashTagRe);
        }

    }
}
