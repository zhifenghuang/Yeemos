package com.yeemos.app.interfaces;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.interfaces.GBSIDataManager;
import com.gbsocial.server.ServerResultBean;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.TimeCount;

import java.util.ArrayList;

public interface IDataManager extends GBSIDataManager {

    /**
     * 得到最近的聊天的联系人
     *
     * @return
     */
    public ArrayList<BasicUser> getRecentyChatUser();


    /**
     * 修改好友备注
     *
     * @return
     */
    public ServerResultBean<String> updateFriendRemarkName(final String userId, final String remarkName);

    /**
     * 是否可以发倪敏Post
     * @return
     */
    public ServerResultBean<String> getStatusSendAnonymityPost();

    /**
     * 获取Pager系列的数据资源
     *
     * @return
     */
    public ArrayList<?> getPagerDataResource(int startIndex, Constants.MPagerListMode mode, boolean isGetNewData);

    /**
     *
     * @param mTimeCount
     */
    public void setTimeCount(TimeCount mTimeCount);

    public TimeCount getTimeCount();


}


