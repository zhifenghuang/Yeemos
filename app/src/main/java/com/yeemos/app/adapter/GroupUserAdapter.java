package com.yeemos.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 17-3-2.
 */

public class GroupUserAdapter extends BaseAdapter {
    private ArrayList<BasicUser> allFriendList;

    @Override
    public int getCount() {
        return allFriendList == null ? 0 : allFriendList.size();
    }

    @Override
    public Object getItem(int i) {
        return allFriendList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setArrayList(ArrayList<BasicUser> arrayList) {
        allFriendList = new ArrayList<>();
        for (BasicUser basicUser : arrayList) {
            BasicUser basicUser1 = new BasicUser();
            basicUser1.copyUser(basicUser);
            basicUser1.setSelect(false);
            allFriendList.add(basicUser1);
        }
        notifyDataSetChanged();
    }

    public ArrayList<BasicUser> getArrayList() {
        return allFriendList;
    }

    public boolean hasSelect() {
        boolean flag = false;
        for (BasicUser basicUser : allFriendList) {
            if (basicUser.isSelect()) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 重新排序
     */
    public void reSortList() {
        ArrayList<BasicUser> newArrayList = new ArrayList<>();

        for (int i = 0; i < allFriendList.size(); i++) {
            BasicUser mBasicUser = allFriendList.get(i);

            if (mBasicUser.isSelect()) {
                newArrayList.add(mBasicUser);
                allFriendList.remove(mBasicUser);
                i--;
            }
        }
        Collections.sort(newArrayList, new Comparator<BasicUser>() {
            @Override
            public int compare(BasicUser o1, BasicUser o2) {
                return o1.getPinyinName().toUpperCase().compareTo(o2.getPinyinName().toUpperCase());
            }
        });

        Collections.sort(allFriendList, new Comparator<BasicUser>() {
            @Override
            public int compare(BasicUser o1, BasicUser o2) {
                return o1.getPinyinName().toUpperCase().compareTo(o2.getPinyinName().toUpperCase());
            }
        });

        allFriendList.addAll(0, newArrayList);
        notifyDataSetChanged();

    }

    public void setGroupUserAndRefresh(ArrayList<Integer> userIdArrayList) {
        if (userIdArrayList == null) {
            return;
        }
        ArrayList<BasicUser> newArrayList = new ArrayList<>();

        for (Integer integer : userIdArrayList) {

            for (int i = 0; i < allFriendList.size(); i++) {
                BasicUser basicUser = allFriendList.get(i);

                if (String.valueOf(integer).equals(basicUser.getUserId())) {

                    basicUser.setSelect(true);
                    allFriendList.remove(basicUser);
                    newArrayList.add(basicUser);

                    break;
                }
            }
        }
        Collections.sort(newArrayList, new Comparator<BasicUser>() {
            @Override
            public int compare(BasicUser o1, BasicUser o2) {
                return o1.getPinyinName().toUpperCase().compareTo(o2.getPinyinName().toUpperCase());
            }
        });
        allFriendList.addAll(0, newArrayList);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.layout_group_user_item, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            Object obj = view.getTag();
            if (obj != null) {
                viewHolder = (ViewHolder) obj;
            } else {
                view = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.layout_group_user_item, viewGroup, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }

        }
        viewHolder.setData(allFriendList.get(i));
        return view;
    }

    class ViewHolder {
        private TextView tvText1, tvText2;
        private RoundedImageView imgIcon;
        private ImageView tickBtn;

        public ViewHolder(View view) {
            tvText1 = (TextView) view.findViewById(R.id.tvText1);
            tvText2 = (TextView) view.findViewById(R.id.tvText2);

            imgIcon = (RoundedImageView) view.findViewById(R.id.imgIcon);
            tickBtn = (ImageView) view.findViewById(R.id.tickBtn);
        }

        public void setData(BasicUser basicUser) {
            tvText1.setText(basicUser.getUserName());
            tvText2.setText("@" + basicUser.getRemarkName());
            Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater,
                    Preferences.getAvatarUrl(basicUser.getUserAvatar()), imgIcon);
            tickBtn.setSelected(basicUser.isSelect());
        }
    }
}
