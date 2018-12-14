package com.yeemos.app.adapter;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.FriendGroup;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.ChooseFriendView;
import com.yeemos.app.view.EditGroupView;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.NewGroupView;
import com.yeemos.app.view.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 17-3-1.
 */

public class ChooseFriendAdapter extends BaseAdapter {


    private ArrayList<Object> objectArrayList;
    private ArrayList<FriendGroup> groupArrayList;
    private ArrayList<BasicUser> allFriend;
    private ChooseFriendView mChooseFriendView;
    private ChooseItemListener chooseItemListener;

    public interface ChooseItemListener {
        void onChooseItem(boolean noSelectItem);
    }

    public void setChooseItemListener(ChooseItemListener chooseItemListener) {
        this.chooseItemListener = chooseItemListener;
    }

    public ChooseFriendAdapter(ChooseFriendView mChooseFriendView) {
        this.mChooseFriendView = mChooseFriendView;
    }

    @Override
    public int getCount() {
        return objectArrayList == null ? 0 : objectArrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return objectArrayList.get(i);

    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void setObjectArrayList(ArrayList<FriendGroup> allFriendGroup, ArrayList<BasicUser> allFriend) {
        this.groupArrayList = allFriendGroup;
        this.allFriend = allFriend;
        objectArrayList = new ArrayList<>();
        objectArrayList.clear();
        for (FriendGroup friendGroup : allFriendGroup) {
            FriendGroup newFriendGroup = new FriendGroup();
            newFriendGroup.copyGroup(friendGroup);
            newFriendGroup.setSelect(false);
            objectArrayList.add(newFriendGroup);
        }
        ArrayList<BasicUser> userList = new ArrayList<>();
        for (BasicUser basicUser : allFriend) {
            BasicUser newBasicUser = new BasicUser();
            newBasicUser.copyUser(basicUser);
            newBasicUser.setSelect(false);
            userList.add(newBasicUser);
        }
        if (userList.size() > 0) {
            Collections.sort(userList, new Comparator<BasicUser>() {
                @Override
                public int compare(BasicUser user1, BasicUser user2) {
                    return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
                }
            });
        }
        objectArrayList.addAll(userList);
        userList.clear();
        userList = null;
        notifyDataSetChanged();
    }

    public ArrayList<Object> getObjectArrayList() {
        return objectArrayList;
    }

    /**
     * 选中
     *
     * @param groupArrayList
     * @param userIdArrayList
     */
    public void setGroupUserAndRefresh(ArrayList<FriendGroup> groupArrayList, ArrayList<Integer> userIdArrayList) {
        if (userIdArrayList == null && groupArrayList == null) {
            //如果为空,时新建Post,默认选中上一次选中的分享对象
            String sharedID = Preferences.getInstacne().getLastSharedID();
            if (!TextUtils.isEmpty(sharedID) && !sharedID.equals("")) {
                for (Object object : objectArrayList) {
                    if (object.getClass().isAssignableFrom(FriendGroup.class)) {
                        String objID = ((FriendGroup) object).getId() + ",";
                        if (sharedID.contains(objID)) {
                            ((FriendGroup) object).setSelect(true);
                        }
                    } else {
                        String objID = ((BasicUser) object).getUserId() + ",";
                        if (sharedID.contains(objID)) {
                            ((BasicUser) object).setSelect(true);
                        }
                    }
                }
            }
        } else {
            if (userIdArrayList != null) {
                for (Integer integer : userIdArrayList) {
                    for (Object object : objectArrayList) {
                        if (object.getClass().isAssignableFrom(BasicUser.class)
                                && String.valueOf(integer).equals(((BasicUser) object).getUserId())) {
                            ((BasicUser) object).setSelect(true);
                            break;
                        }
                    }
                }
            }
            if (groupArrayList != null) {
                for (FriendGroup friendGroup : groupArrayList) {
                    for (Object object : objectArrayList) {
                        if (object.getClass().isAssignableFrom(FriendGroup.class)
                                && friendGroup.getId().equals(((FriendGroup) object).getId())) {
                            ((FriendGroup) object).setSelect(true);
                            break;
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (chooseItemListener != null) {
            chooseItemListener.onChooseItem(isNoSelect());
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if (view == null) {
            view = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.layout_choose_friend_list_item, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            Object obj = view.getTag();
            if (obj != null) {
                viewHolder = (ViewHolder) obj;
            } else {
                view = LayoutInflater.from(BaseApplication.getAppContext()).inflate(R.layout.layout_choose_friend_list_item, viewGroup, false);
                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            }

        }
        viewHolder.setObject(position);
        return view;
    }

    private boolean isFirstGroup(FriendGroup friendGroup) {
        if (friendGroup.equals(objectArrayList.get(0))) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFisrtBasicUser(BasicUser basicUser) {
        Object obj = null;
        for (int i = 0; i < objectArrayList.size(); i++) {
            obj = objectArrayList.get(i);
            if (obj.getClass().isAssignableFrom(BasicUser.class)) {
                break;
            }
        }
        if (basicUser.equals(obj)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isUserNoSelectAll() {
        boolean flag = false;
        for (Object object : objectArrayList) {
            if (object.getClass().isAssignableFrom(BasicUser.class)) {

                if (!((BasicUser) object).isSelect()) {
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }

    private void selectAll() {
        for (Object object : objectArrayList) {
            if (object.getClass().isAssignableFrom(BasicUser.class)) {
                ((BasicUser) object).setSelect(true);
            }
        }
        notifyDataSetChanged();
    }

    private void deSelectAll() {
        for (Object object : objectArrayList) {
            if (object.getClass().isAssignableFrom(BasicUser.class)) {
                ((BasicUser) object).setSelect(false);
            }
        }
        notifyDataSetChanged();
    }

    public boolean isNoSelect() {
        boolean flag = true;
        for (Object object : objectArrayList) {
            if (object.getClass().isAssignableFrom(BasicUser.class)) {
                if (((BasicUser) object).isSelect()) {
                    flag = false;
                    break;
                }
            } else {
                if (((FriendGroup) object).isSelect()) {
                    flag = false;
                    break;
                }
            }
        }
        return flag;
    }

    private String getGroupName(FriendGroup friendGroup) {

        String groupName = friendGroup.getGroupName();

        if (TextUtils.isEmpty(groupName)) {
            groupName = "";
            ArrayList<Integer> basicUserIDList = friendGroup.getGroupUsers();
            for (int i = 0; i < basicUserIDList.size(); i++) {
                Integer userID = basicUserIDList.get(i);
                for (BasicUser basicUser : allFriend) {
                    if (userID.equals(Integer.valueOf(basicUser.getUserId()))) {
                        groupName += basicUser.getRemarkName() + ",";
                    }
                }
            }
            if (groupName.length() > 50) {
                groupName = groupName.substring(0, 49);
            }
            char[] groupNamechar = groupName.toCharArray();
            if (groupNamechar[groupName.length() - 1] == ',') {
                groupName = groupName.substring(0, groupName.length() - 1);
            }
        }
        return groupName;
    }

    private FriendGroup getFriendGroupByID(String groupID) {
        FriendGroup friendGroup = null;
        for (int i = 0; i < groupArrayList.size(); i++) {
            String groupid = groupArrayList.get(i).getId();
            if (groupid.equals(groupID)) {
                friendGroup = groupArrayList.get(i);
                break;
            }
        }
        return friendGroup;
    }

    class ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private RelativeLayout topView, imgView, tvContentLy;
        private TextView friendGroup, createGroup, tvText1, tvText2, noData;
        private ImageView friendGroupImg, tickBtn;
        private RoundedImageView imgIcon;
        private Object object;

        public ViewHolder(View view) {
            topView = (RelativeLayout) view.findViewById(R.id.topView);
            imgView = (RelativeLayout) view.findViewById(R.id.imgView);
            tvContentLy = (RelativeLayout) view.findViewById(R.id.tvContentLy);

            friendGroup = (TextView) view.findViewById(R.id.friendGroup);
            createGroup = (TextView) view.findViewById(R.id.createGroup);
            tvText1 = (TextView) view.findViewById(R.id.tvText1);
            tvText2 = (TextView) view.findViewById(R.id.tvText2);
            noData = (TextView) view.findViewById(R.id.noData);

            friendGroupImg = (ImageView) view.findViewById(R.id.friendGroupImg);
            tickBtn = (ImageView) view.findViewById(R.id.tickBtn);

            imgIcon = (RoundedImageView) view.findViewById(R.id.imgIcon);

            createGroup.setOnClickListener(this);
            tickBtn.setOnClickListener(this);
            imgView.setOnClickListener(this);
            tvContentLy.setOnClickListener(this);


        }

        private void setObject(int position) {
            object = objectArrayList.get(position);
            if (object.getClass().isAssignableFrom(FriendGroup.class)) {
                imgIcon.setVisibility(View.GONE);
                imgIcon.setNeedDrawVipBmp(false);
                friendGroupImg.setVisibility(View.VISIBLE);

                if (isFirstGroup((FriendGroup) object)) {
                    topView.setVisibility(View.VISIBLE);
                    friendGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_txt_group"));
                    createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_creategroup"));
                } else {
                    topView.setVisibility(View.GONE);
                }
                if (!((FriendGroup) object).isTrueGroup()) {
                    noData.setVisibility(View.VISIBLE);
                    imgView.setVisibility(View.GONE);
                    tvContentLy.setVisibility(View.GONE);
                    tickBtn.setVisibility(View.GONE);
                    noData.setText(ServerDataManager.getTextFromKey("chsfrnd_txt_nogroup"));
                    tvContentLy.setOnLongClickListener(null);
                    imgView.setOnLongClickListener(null);
                    tickBtn.setOnLongClickListener(null);
                } else {
                    tvContentLy.setOnLongClickListener(this);
                    imgView.setOnLongClickListener(this);
                    tickBtn.setOnLongClickListener(this);
                    imgView.setVisibility(View.VISIBLE);
                    tvContentLy.setVisibility(View.VISIBLE);
                    tickBtn.setVisibility(View.VISIBLE);
                    tvText1.setText(getGroupName((FriendGroup) object));
                    tvText2.setText(((FriendGroup) object).getGroupUsers().size() + ServerDataManager.getTextFromKey("chsfrnd_txt_friends"));
                    tickBtn.setSelected(((FriendGroup) object).isSelect());
                    noData.setVisibility(View.GONE);
                }
            } else {
                imgIcon.setVisibility(View.VISIBLE);
                friendGroupImg.setVisibility(View.GONE);
                noData.setVisibility(View.GONE);
                imgView.setVisibility(View.VISIBLE);
                tvContentLy.setVisibility(View.VISIBLE);
                tickBtn.setVisibility(View.VISIBLE);

                tvContentLy.setOnLongClickListener(null);
                imgView.setOnLongClickListener(null);
                tickBtn.setOnLongClickListener(null);

                tvText1.setText(((BasicUser) object).getUserName());
                tvText2.setText("@" + ((BasicUser) object).getRemarkName());
                imgIcon.setNeedDrawVipBmp(((BasicUser) object).isAuthenticate());
                Utils.loadImage(BaseApplication.getAppContext(), R.drawable.default_avater,
                        Preferences.getAvatarUrl(((BasicUser) object).getUserAvatar()), imgIcon);
                tickBtn.setSelected(((BasicUser) object).isSelect());

                if (isFisrtBasicUser((BasicUser) object)) {
                    topView.setVisibility(View.VISIBLE);
                    friendGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_txt_myfriend"));
                    if (isUserNoSelectAll()) {
                        createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_selectall"));
                    } else {
                        createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_deselectall"));
                    }
                    createGroup.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                } else {
                    topView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.createGroup:
                    if (object.getClass().isAssignableFrom(FriendGroup.class)) {
                        NewGroupView mNewGroupView = new NewGroupView(BaseApplication.getCurFragment().getActivity(), R.style.ActionSheetDialogStyle);
                        DisplayMetrics displayMetrics = BaseApplication.getCurFragment().getDisplaymetrics();
                        mNewGroupView.setDialogSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
                        mNewGroupView.setOnAddNewGroupListener(new NewGroupView.OnAddNewGroupListener() {
                            @Override
                            public void addNewGroup(FriendGroup friendGroup) {
                                if (!((FriendGroup) objectArrayList.get(0)).isTrueGroup()) {
                                    objectArrayList.remove(object);
                                    groupArrayList.remove(getFriendGroupByID(((FriendGroup) object).getId()));
                                }
                                objectArrayList.add(0, friendGroup);
                                groupArrayList.add(0, friendGroup);
                                notifyDataSetChanged();
                            }
                        });
                        mNewGroupView.setFriendGroupsList(groupArrayList);
                        mNewGroupView.getData(true);
                        mNewGroupView.show();
                    } else {
                        if (isUserNoSelectAll()) {
                            createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_deselectall"));
                            selectAll();
                        } else {
                            createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_selectall"));
                            deSelectAll();
                        }
                    }
                    break;
                case R.id.tickBtn:
                    if (object.getClass().isAssignableFrom(FriendGroup.class)) {
                        ((FriendGroup) object).setSelect(!((FriendGroup) object).isSelect());
                        tickBtn.setSelected(((FriendGroup) object).isSelect());
                    } else {
                        ((BasicUser) object).setSelect(!((BasicUser) object).isSelect());
                        tickBtn.setSelected(((BasicUser) object).isSelect());
                        if (isUserNoSelectAll()) {
                            createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_selectall"));
                        } else {
                            createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_deselectall"));
                        }
                    }
                    chooseItemListener.onChooseItem(isNoSelect());
                    break;
                case R.id.imgView:
                case R.id.tvContentLy:
                    selectItem();
                    break;
            }
        }

        private void selectItem() {
            if (object.getClass().isAssignableFrom(FriendGroup.class)) {
                editFriendGroup();
            } else {
                ((BasicUser) object).setSelect(!((BasicUser) object).isSelect());
                tickBtn.setSelected(((BasicUser) object).isSelect());
                if (isUserNoSelectAll()) {
                    createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_selectall"));
                } else {
                    createGroup.setText(ServerDataManager.getTextFromKey("chsfrnd_btn_deselectall"));
                }
                chooseItemListener.onChooseItem(isNoSelect());
            }
        }

        private void editFriendGroup() {
            EditGroupView mEditGroupView = new EditGroupView(BaseApplication.getCurFragment().getActivity(), R.style.ActionSheetDialogStyle);
            DisplayMetrics displayMetrics = BaseApplication.getCurFragment().getDisplaymetrics();
            mEditGroupView.setDialogSize(displayMetrics.widthPixels, displayMetrics.heightPixels);
            mEditGroupView.setOnAddNewGroupListener(new NewGroupView.OnAddNewGroupListener() {
                @Override
                public void addNewGroup(FriendGroup friendGroup) {
                    ((FriendGroup) object).setGroupUsers(friendGroup.getGroupUsers());
                    ((FriendGroup) object).setGroupName(friendGroup.getGroupName());
                    FriendGroup group = getFriendGroupByID(((FriendGroup) object).getId());
                    group.setGroupName(friendGroup.getGroupName());
                    group.setGroupUsers(friendGroup.getGroupUsers());
                    notifyDataSetChanged();
                }
            });
            mEditGroupView.setIdArrayList((FriendGroup) object);
            mEditGroupView.setFriendGroupsList(groupArrayList);
            mEditGroupView.getData(true);
            mEditGroupView.show();
        }

        @Override
        public boolean onLongClick(View view) {
            switch (view.getId()) {
                case R.id.imgView:
                case R.id.tvContentLy:
                case R.id.tickBtn:
                    longClickItem();
                    return true;
            }
            return false;
        }

        private void longClickItem() {
            MorePopupWindow morePopupWindow = new MorePopupWindow(BaseApplication.getCurFragment().getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
                @Override
                public void onFirstBtnClicked() {
                    deleteFriendGroup();
                }

                @Override
                public void onSecondBtnClicked() {
                    editFriendGroup();
                }

                @Override
                public void onThirdBtnClicked() {

                }

                @Override
                public void onFourthBtnClicked() {

                }

                @Override
                public void onCancelBtnClicked() {

                }
            }, Constants.MORE_POPUPWINDOW_FRIEND_GROUP_OPERATE);

            morePopupWindow.initView(null);
            morePopupWindow.showAtLocation(mChooseFriendView.getContentView(), Gravity.BOTTOM | Gravity.CENTER, 0, -50);
        }

        private void deleteFriendGroup() {
            DataManager.getInstance().deleteFriendGroup((FriendGroup) object);
            objectArrayList.remove(object);
            for (FriendGroup friendGroup : groupArrayList) {
                if (friendGroup.getId() != null &&
                        friendGroup.getId().equals(((FriendGroup) object).getId())) {
                    groupArrayList.remove(friendGroup);
                    break;
                }
            }
            if (groupArrayList.size() == 0) {
                FriendGroup friendGroup = new FriendGroup();
                friendGroup.setTrueGroup(false);
                groupArrayList.add(friendGroup);//FriendGroup 确保第一行显示
                objectArrayList.add(0, friendGroup);
            }
            notifyDataSetChanged();
        }
    }
}
