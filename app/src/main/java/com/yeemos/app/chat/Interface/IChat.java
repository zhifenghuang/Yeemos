package com.yeemos.app.chat.Interface;


import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.chat.manager.CRabbitMQChat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 12/9/15.
 */
public abstract class IChat
{

    static private IChat g_chat = null;

    /**
     * 得到IChat实例
     * @return
     */
    static public IChat getInstance()
    {
        if(g_chat == null)
        {
            g_chat = new CRabbitMQChat();
        }
        return g_chat;
    }


    /**
     * 连接服务器
     * @param token
     * @param userID
     */
    public abstract void connectServer(String token, long userID);

    /**
     * 断开连接
     */
    public abstract void disconnectServer();

    /**
     *
     */
    public abstract void logOut();

    /**
     * 判断是否连接上IM 服务器
     * @return
     */
    public abstract boolean isConnecting();

    /**
     * 发送一条消息。如果不需要占位符消息，则不发送ImageMessage 和 VideoMessage AudioMessage
     * @param msg
     * @return
     */
    public abstract boolean sendMsg(IMsg msg);


    /**
     * 重发
     * @param msg
     * @return
     */
    public abstract boolean reSendMsg(IMsg msg);

    /**
     * 读了这条消息
     * @param msg
     * @return
     */
    public abstract boolean readMsg(IMsg msg);

    /**
     * 收了这条消息
     * @param msg
     * @return
     */
    public abstract boolean receivedMsg(IMsg msg);

    /**
     * 正在收消息，适用于 文件
     * @param msg
     * @return
     */
    public abstract boolean receivingMsg(IMsg msg);

    /**
     * 标记已读此用户的所有消息
     * @param userId
     * @return
     */
    public abstract boolean readUserMsg(long userId);

    /**
     * 标记用户又读了次消息
     * @param userId
     * @return
     */
    public abstract boolean markUserReadMsgsOnceMore(long userId);


    /**
     * 下载消息
     *
     * @param msg
     * @return
     */
    public abstract boolean downloadMsg(IMsg msg);

    /**
     * 前台到后台
     * @return
     */
    public abstract boolean gotoBackground();

    /**
     * 后台到前台
     * @return
     */
    public abstract boolean gotoForeground();

    /**
     * 增加Listener
     * @param chatListener
     * @return
     */
    public abstract boolean addChatListener(IChatListener chatListener);

    /**
     * 移除Listener
     * @param chatListener
     * @return
     */
    public abstract boolean removeChatListener(IChatListener chatListener);

    /**
     * 是否还有未读消息
     * @return
     */
    public abstract boolean isHaveUnReadMsg();



   // abstract ArrayList<BasicUser> getRectMsg();



    /**
     * 预留函数 得到是否自动重发
     */
    public abstract boolean getNeedAutoResendMsg();


    /**
     * 预留函数 设置是否自动重发（已经发送失败的消息）
     */
    public abstract void setNeedAutoResendMsg(boolean autoReSend);


    /**
     * 获取最近的聊天概述
     */
    public  abstract List<UserMsgSummery> getUserMsgSummery();







//    /**
//     * 得到与某人的所有离线消息
//     * @return
//     */
//    public abstract boolean getPeerOfflineMessage(long peerUserId);


    /**
     * 得到与某人的缺失消息(测试用)
     * @return
     */
    public abstract boolean getPeerMissMessage(long peerUserId, ArrayList<Long> arrMisMsgSeqs);




    /**
     * 得到与某人聊天记录
     */
    public abstract ArrayList<IMsg> getPeerRecentChatRecordList(long userID);



    /**
     * 得到与某人最后N条聊天记录
     */
    public abstract ArrayList<IMsg> getPeerLastChatMsgList(long userID, int nCount);

    /**
     * 得到某条消息前面的消息
     * @param msg
     * @param userID
     * @param nCount
     * @return
     */
    public abstract ArrayList<IMsg> getPeerMsgListBeforeMsg(IMsg msg, long userID, int nCount);


    /**
     * 得到某条消息后面的消息
     * @param msg
     * @param userID
     * @param nCount
     * @return
     */
    public abstract ArrayList<IMsg> getPeerMsgListAfterMsg(IMsg msg, long userID, int nCount);



    /**
     * 检查是否有缺失的消息并取回来
     * @param userID
     * @return
     */
    public abstract void detectMissingAndGetMsgs(long userID);


    /**
     * 删除与某些人的聊天纪录
     * @param userIds
     * @param myUserID
     * @return
     */
    public abstract void deleteMsgsRecordByUserIds(ArrayList<Long> userIds,long myUserID,String toke);
}
