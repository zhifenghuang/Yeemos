package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-15.
 */
public class BlackListAdapter extends BaseAdapter {

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
        ViewHolder viewHolder;
        if ( convertView == null ) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.black_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            Object obj = convertView.getTag();
            if ( obj != null ) {
                viewHolder = (ViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.black_list_item, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
        }
        viewHolder.fill(position);
        return convertView;
    }

    class ViewHolder implements View.OnClickListener {
        private RoundedImageView imgIcon;
        private TextView tvTitle;
        private TextView tvContent;
        private TextView btnRemove;
        private int position;

        public ViewHolder(View cView) {
            tvContent = (TextView) cView.findViewById(R.id.tvContent);
            tvTitle = (TextView) cView.findViewById(R.id.tvTitle);
            imgIcon = (RoundedImageView) cView.findViewById(R.id.imgIcon);
            btnRemove = (TextView) cView.findViewById(R.id.btnRemove);
            btnRemove.setOnClickListener(this);
        }

        public void fill(int position) {
            this.position = position;
            BasicUser basicUser = arrayList.get(position);
            btnRemove.setText(ServerDataManager.getTextFromKey("blcklst_btn_remove"));
            tvTitle.setText(basicUser.getRemarkName());
            tvContent.setText("@"+basicUser.getUserName());
            imgIcon.setNeedDrawVipBmp(basicUser.isAuthenticate());
//            imgIcon.setDefaultImageResId(R.drawable.default_avater);
//            imgIcon.setImageUrl(Preferences.getAvatarUrl(basicUser.getUserAvatar()));

            Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()),imgIcon);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnRemove:
                    BasicUser basicUser=arrayList.get(position);
                    DataManager.getInstance().blockUser(basicUser);
                    arrayList.remove(position);
                    notifyDataSetChanged();
                    break;
            }
        }
    }
}
