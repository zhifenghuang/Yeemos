package com.yeemos.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.fragment.UserInfoFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-7-19.
 */
public class RequestAdapter extends BaseAdapter {

    private ArrayList<BasicUser> arrayList;
    private Context context;
    public RequestAdapter(Context context){
        this.context = context;
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

    public void setArrayList(ArrayList<BasicUser> arrayList){
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if ( convertView == null ) {
            convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.layout_request_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            Object obj = convertView.getTag();
            if ( obj != null ) {
                viewHolder = (ViewHolder) obj;
            } else {
                convertView = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.layout_request_item, parent, false);
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
        private ImageView requestBtn;
        private ImageView rejectBtn;
        private BasicUser basicUser;

        public ViewHolder(View cView) {
            tvContent = (TextView) cView.findViewById(R.id.tvContent);
            tvTitle = (TextView) cView.findViewById(R.id.tvTitle);
            imgIcon = (RoundedImageView) cView.findViewById(R.id.imgIcon);
            requestBtn = (ImageView) cView.findViewById(R.id.requestBtn);
            rejectBtn = (ImageView) cView.findViewById(R.id.rejectBtn);

            requestBtn.setOnClickListener(this);
            rejectBtn.setOnClickListener(this);
            imgIcon.setOnClickListener(this);
            tvTitle.setOnClickListener(this);
            tvContent.setOnClickListener(this);
        }

        public void fill(int position) {
            basicUser = arrayList.get(position);
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
                case R.id.rejectBtn:
                    basicUser.setAgreeOrDisagree(false);
                    DataManager.getInstance().agreeOrDisagree(basicUser);
                    arrayList.remove(basicUser);
                    notifyDataSetChanged();
                    break;
                case R.id.requestBtn:
                    basicUser.setAgreeOrDisagree(true);
                    DataManager.getInstance().agreeOrDisagree(basicUser);
                    arrayList.remove(basicUser);
                    notifyDataSetChanged();
                    break;
                case R.id.tvContent:
                case R.id.tvTitle:
                case R.id.imgIcon:
                    DataManager.getInstance().setCurOtherUser(basicUser);
                    BaseApplication.getCurFragment().gotoPager(UserInfoFragment.class, null);
                    break;
            }
        }
    }
}
