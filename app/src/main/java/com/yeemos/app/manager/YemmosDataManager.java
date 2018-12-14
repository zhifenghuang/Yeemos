package com.yeemos.app.manager;

/**
 * Created by gigabud on 15-12-9.
 */

import android.graphics.Bitmap;

import com.gbsocial.BeanRequest.FoodTypeBean;
import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.constants.GBSConstants.MenuObjectType;
import com.gbsocial.constants.GBSConstants.MenuOperateType;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.datamanage.GBSDataManagerByMem;
import com.gbsocial.server.ServerDataManager;
import com.gbsocial.server.ServerResultBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.interfaces.IDataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.TimeCount;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;

/**
 * 数据管理类，提供底层数据操作的方法和存储临时数据
 *
 * @author xiangwei.ma
 */

public class YemmosDataManager extends GBSDataManagerByMem implements IDataManager {
    public String TAG = "YemmosDataManager";
    public Bitmap mSettingPageBackgroundBm;
    public String curHtmlFragmentUrl;
    public TimeCount mTimeCount;
    /**
     * 缓存菜式
     */
    public ArrayList<FoodTypeBean> foodTypeList = null;

    /**
     * 性别列表
     */
    public ArrayList<String> sexList;


    public YemmosDataManager() {
        super();
    }

    public ArrayList<BasicUser> findFriendAddressBookUsers() {
        return MemberShipManager.getInstance().findFriendAddressBookUsers();
    }

    public ArrayList<BasicUser> findFriendFacebookUsers() {
        return MemberShipManager.getInstance().findFriendFacebookUsers();
    }

    public ArrayList<BasicUser> findFriendInstagramUsers() {
        return MemberShipManager.getInstance().findFriendInstagramUsers();
    }

    public ArrayList<BasicUser> findFriendTwitterUsers() {
        return MemberShipManager.getInstance().findFriendTwitterUsers();
    }

    public String getHTML() {

        return curHtmlFragmentUrl;
    }

    @Override
    public FoodTypeBean getSearchCurFoodType() {

        return curSearchFoodType;
    }

    public static FoodTypeBean defaultAllFoodTypeBean = null;


    public FoodTypeBean getDefaultAllFoodType() {
        if (defaultAllFoodTypeBean == null) {
            defaultAllFoodTypeBean = new FoodTypeBean().setId(0).setText("");
        }
        return defaultAllFoodTypeBean;
    }


    @Override
    public void getBaseDatas() {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            public void run() {
                // 获取restaurant Tag
                //getRestaurantTags();
                // 获取Menu
                getTopics();
                //
            }
        });
    }


    @Override
    public void like(PostBean pBean) {
        super.like(pBean);
//        if(!pBean.isLike()){
//            NotifyCenter.sendBoardcastByDataUpdate(Constants.UNLIKE_ACTION);
//        }
    }

    @Override
    public void follow(BasicUser user) {
//        saveIds(user.getUserId());
        super.follow(user);
//        if (!user.isFollowingOrRequest()) {
//            NotifyCenter.sendBoardcastByDataUpdate(Constants.UNFOLLOW_ACTION);
//        }
        if (user.isFollowingOrRequest()) {
            BaseApplication.getCurFragment().sendBroadcastMessage(3, user);
            DataChangeManager.getInstance().notifyDataChange(0, user, 5);
        } else {
            BaseApplication.getCurFragment().sendBroadcastMessage(2, user);
            DataChangeManager.getInstance().notifyDataChange(0, user, 4);
        }
    }

    @Override
    public void report(MenuObjectType menuObjectType,
                       MenuOperateType operateType) {
        super.report(menuObjectType, operateType);
        if (menuObjectType == MenuObjectType.Menu_Object_User) {
            BasicUser basicUser = GBSDataManager.getInstance().getCurOtherUser();
            DataChangeManager.getInstance().notifyDataChange(0, basicUser, 0);
        }
//        NotifyCenter.sendBoardcastByDataUpdate(Constants.REPORT_ACTION);
    }


    /**
     * 得到最近的聊天的联系人
     *
     * @return
     */
    public ArrayList<BasicUser> getRecentyChatUser() {
        ServerResultBean<BasicUser> rest = this.getUserDetailInfo(null, 0, 5, GBSConstants.UserDataType.User_Data_FriendUser, GBSConstants.SortType.SortType_HashTagNum, GBSConstants.SortWay.SortType_Ascending);
        return rest.getData().getFriendUsers();
    }

    /**
     * 得到所有朋友
     *
     * @return
     */
    public ArrayList<BasicUser> getAllFriends(boolean isFromCache) {
        ArrayList<BasicUser> allFriends = super.getAllFriends(isFromCache);
        if (isFromCache && (allFriends == null || allFriends.isEmpty())) {
            allFriends = Utils.getCache(BasicUser.class, HomeActivity.FRIEND_LIST);
        }
        return allFriends;
    }

    /**
     * 是否可以发匿名Post
     *
     * @return
     */
    public ServerResultBean<String> getStatusSendAnonymityPost() {
        ServerResultBean<String> result = ServerDataManager.getStatusSendAnonymityPost();
        errorCodeDo(result);
        return result;
    }

    /**
     * 修改好友备注
     *
     * @return
     */
    public ServerResultBean<String> updateFriendRemarkName(final String userId, final String remarkName) {
        ServerResultBean<String> result = ServerDataManager.updateFriendRemarkName(userId, remarkName);
        errorCodeDo(result);
        return result;
    }

    @Override
    public ArrayList<?> getPagerDataResource(int startIndex, Constants.MPagerListMode mode, boolean isGetNewData) {
        switch (mode) {
            case MPagerListMode_Search:
                return findFriendSearchUser(curKeyWord, startIndex, GBSConstants.SearchDataType.Search_Data_User);
            case MPagerListMode_HashTags:
                return searchHashTags(curKeyWord, startIndex, Constants.PAGE_NUMBER);
            case MPagerListMode_ACTIVITIES_You:
                return getMessageArrayList(startIndex, GBSConstants.UserDataType.User_Data_SystemMessage, isGetNewData);
        }
        return null;
    }


    @Override
    public void setTimeCount(TimeCount mTimeCount) {
        this.mTimeCount = mTimeCount;
    }

    @Override
    public TimeCount getTimeCount() {
        return mTimeCount;
    }

    @Override
    protected void errorCodeDo(final String errorCode) {
        super.errorCodeDo(errorCode);
        if (BaseApplication.getCurFragment() != null && MemberShipManager.getInstance().getUserInfo() != null) {
            BaseApplication.getCurFragment().errorCodeDo(errorCode);
        }
    }

    @Override
    public void blockUser(BasicUser user) {
//        saveIds(user.getUserId());
        super.blockUser(user);
        BaseApplication.getCurFragment().sendBroadcastMessage(2, user);
    }

    @Override
    public void saveIds(String idString) {
        String savedIds = Preferences.getInstacne().getValues(HomeActivity.NEED_REMOVE_POST_IDS, "");
        if (savedIds.contains(idString + ",")) {
            return;
        }
        savedIds += (idString + ",");
//        if (savedIds.length() == 0) {
//            savedIds += idString;
//        } else {
//            savedIds += ("," + idString);
//        }
        Preferences.getInstacne().setValues(HomeActivity.NEED_REMOVE_POST_IDS, savedIds);
    }

    @Override
    public void removeIds(String idString) {
        String savedIds = Preferences.getInstacne().getValues(HomeActivity.NEED_REMOVE_POST_IDS, "");
        if (savedIds.contains(idString + ",")) {
            return;
        }
        savedIds.replace(idString + ",", "");
        Preferences.getInstacne().setValues(HomeActivity.NEED_REMOVE_POST_IDS, savedIds);
    }
}
