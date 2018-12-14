package com.yeemos.app.chat.bean;

import com.gbsocial.BeansBase.BasicUser;
import com.gigabud.core.database.IDBItemOperation;
import com.gigabud.core.util.DeviceUtil;
import com.yeemos.app.database.AppDatabaseOperate;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;

public abstract class IMsg extends IDBItemOperation {


    /*
    详细解释
    0:未发送:  如无网络情况下，发送的消息
    1:等待发送:在等待发送队列中的消息
    2:发送中: 调用API，提交消息到exchange
    3:发送失败:
            1:突然网络断开，客户端抓到异常。
            2:客户端间隔扫描1min内，没有收到Server confirm的。
            3:server告诉漏掉的消息(server再回 send confirm的时候，告诉我Server没有收到的消息)
    4:发送成功(server确认):
    服务器确认该消息已经收到。
    5:对方已接收:
    6:对方已读:
    */
    public enum IMSG_SEND_STATUS {
        IMSG_SEND_STATUS_DEFAULT(-1),
        IMSG_SEND_STATUS_UNSEND(0),
        IMSG_SEND_STATUS_WAIT_SEND(1),
        IMSG_SEND_STATUS_SENDING(2),
        IMSG_SEND_STATUS_SEND_FAILURE(3),
        IMSG_SEND_STATUS_SEND_SUCCESS(4),
        IMSG_SEND_STATUS_PEER_RECEIVED(5),
        IMSG_SEND_STATUS_PEER_READ(6);

        int nValues;

        private IMSG_SEND_STATUS(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static IMSG_SEND_STATUS GetObject(int nNum) {
            IMSG_SEND_STATUS[] As = IMSG_SEND_STATUS
                    .values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return IMSG_SEND_STATUS_UNSEND;
        }

    }

    /*
    0:等待接收:
        文本类：客户端收到消息，但是还没有发送confirm
        文件类：客户端收到消息，但是还没有开始下载
    1:接收中:
        文本类：调用API，提交confirm消息到exchange
        文件类：客户端下载中。
    2:接收失败：
        文本类：
            1:突然网络断开，客户端抓到异常。
            2:客户端间隔扫描1min内，没有发送Server confirm的。
            3:server告诉没有Confirm的消息(server再回 send confirm的时候，告诉我之前我漏掉的消息)
        文件类：客户端下载失败。
    3:接收成功（confirm）
        文本类：服务器确认该消息已经收到。
        文件类：客户端下载完成。
    4:已读 (主动标记)
    4:已读确认 (对方发送过来已读确认)

     */

    public enum IMSG_RECV_STATUS {
        IMSG_RECV_STATUS_WAIT_RECV(0),
        IMSG_RECV_STATUS_RECEING(1),
        IMSG_RECV_STATUS_RECV_FAILURE(2),
        IMSG_RECV_STATUS_RECV_SUCCESS(3),
        IMSG_RECV_STATUS_RECV_READ(4),
        IMSG_RECV_STATUS_RECV_READ_CONFIRM(5);

        int nValues;

        private IMSG_RECV_STATUS(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static IMSG_RECV_STATUS GetObject(int nNum) {
            IMSG_RECV_STATUS[] As = IMSG_RECV_STATUS
                    .values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return IMSG_RECV_STATUS_WAIT_RECV;
        }

    }


      /*
      一般情况消息如果超过24小时需要删除处理，只有当用户长按消息后进行保存就可以永久保存
    0:默认字段
    1:需要删除
    2:需要保存
     */

    public enum IMSG_DELETE_STATUS {
        IMSG_DELETE_DEFAULT(0),
        IMSG_DELETE_CONFIRM(1),
        IMSG_DELETE_NEEDSAVE(2);

        int nValues;

        private IMSG_DELETE_STATUS(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static IMSG_DELETE_STATUS GetObject(int nNum) {
            IMSG_DELETE_STATUS[] As = IMSG_DELETE_STATUS
                    .values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return IMSG_DELETE_DEFAULT;
        }

    }


//  ios字段Type
//    typedef NS_ENUM(NSInteger, enimMsgType)
//    {
//        enimMsgType_None = -1,
//                enimMsgType_Broadcast 0,
//                enimMsgType_SerReceived 1,
//                enimMsgType_PeerReceived 2,
//                enimMsgType_PeerRead 3,
//                enimMsgType_PeerRcveRead 4,
//                enimMsgType_FileUrl , 5
//                enimMsgType_Audio,6
//                enimMsgType_Image,7
//                enimMsgType_Video,8
//                enimMsgType_Text,9
//                enimMsgType_StartTyping,10
//                enimMsgType_StopTyping,11
//                enimMsgType_Online,12
//                enimMsgType_Offline,13
//                enimMsgType_ALL 14
//    };


    public enum MES_TYPE {
        UNKNOW_MSG_TYPE(-1),
        TEXT_MSG_TYPE(9),
        ADUID_MSG_TYPE(6),
        VIDEO_MSG_TYPE(8),
        PEER_RCVD_READ_MSG_TYPE(4),
        PEER_READ_MSG_TYPE(3),
        PEER_RECV_MSG_TYPE(2),
        SEVR_CONFIRM_MSG_TYPE(1),
        IMAGE_MSG_TYPE(7),
        FILE_MSG_TYPE(5),
        MSG_SEND_FAILED(100),
        TYPING_SEND_MSG_TYPE(10),
        STOP_TYPING_SEND_MSG_TYPE(11),
        BROADCAST_MSG_TYPE(12);

        int nValues;

        private MES_TYPE(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static MES_TYPE GetObject(int nNum) {
            MES_TYPE[] As = MES_TYPE
                    .values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return UNKNOW_MSG_TYPE;
        }

    }


    /**
     * 插入一条新记录
     *
     * @return
     */
    public abstract boolean insertOBject();


    public abstract String toChatJson();


    /**
     * 设置发送的消息序号，针对一个人
     */
    public abstract void addReadIds();

    public abstract void setSendSeqNum(String seqNum);

    public abstract String getSendSeqNum();

    public abstract String getConfirmReadIds();

    public abstract void setConfirmReadIds(String confirmReadIds);

    public abstract String getReadIds();

    public abstract void setReadIds(String readIds);

    /**
     * 根据 givenSeqNum 设置 sendSeqNum
     */
    public abstract void increaseqNumFromGivenString(String givenSeqNum);

    /**
     * 分析 getReadIds 和 getConfirmReadIds
     */
    public abstract void analystReadIds();


    public abstract void setsUID(long sUID);

    public abstract long getsUID();

    public abstract void setrUID(long sUID);

    public abstract long getrUID();


    public abstract long getDBID();

    public abstract String getMsgID();

    public abstract void setMsgID(String msgID);


    public abstract MES_TYPE getMessageType();

    public abstract void setMessageType(MES_TYPE msgType);


    /**
     * 得到文字
     *
     * @return
     */
    public abstract String getText();

    /**
     * 设置文字
     *
     * @param text
     */
    public abstract void setText(String text);


    /**
     * 设置文件名
     *
     * @return
     */
    public abstract String getfName();

    /**
     * 得打文件名
     *
     * @param fName
     */
    public abstract void setfName(String fName);

    public abstract long getfDuration();

    public abstract void setfDuration(long fDuration);

    public abstract long getfSize();

    public abstract void setfSize(long fSize);

    public abstract String getThumb();

    public abstract void setThumb(String thumb);

    public abstract long getImageMainColor();

    public abstract void setImageMainColor(long imageMainColor);


    /**
     * 设置发送的状态（只修改成员变量）
     *
     * @return
     */
    public abstract void setSendStatus(IMSG_SEND_STATUS sendStatus);

    /**
     * 得到发送的状态
     *
     * @return
     */
    public abstract IMSG_SEND_STATUS getSendStatus();


    /**
     * 设置接收的状态（只修改成员变量）
     *
     * @return
     */
    public abstract void setRecvStatus(IMSG_RECV_STATUS recvStatus);

    /**
     * 得到接收的状态
     *
     * @return
     */
    public abstract IMSG_RECV_STATUS getRecvStatus();


    /**
     * 预留消息标记位，1 时代表这个消息是个预留消息，没有实体内容
     *
     * @return
     */
    public abstract int getEmptyMsg();

    public abstract void setEmptyMsg(int emptyMsg);


    /**
     * 重发此送消息
     *
     * @return
     */
    public abstract boolean reSendMsg();

    /**
     * 根据状态，处理消息逻辑（存储DB等）
     *
     * @param status
     * @return
     */
    public abstract boolean updateSendStatus(IMSG_SEND_STATUS status);

    /**
     * 根据状态，处理消息逻辑（存储DB等）
     *
     * @param status
     * @return
     */
    public abstract boolean updateRecvStatus(IMSG_RECV_STATUS status);

    /**
     * 更新消息删除的状态
     *
     * @param status
     * @return
     */
    public abstract boolean updateDeleteStatus(IMSG_DELETE_STATUS status);

    public abstract IMSG_DELETE_STATUS getDeleteStatus();


    /**
     * 得到重试次数
     *
     * @return
     */
    public abstract long getRetryNums();

    /**
     * 设置重试次数
     *
     * @param retryNums
     */
    public abstract void setRetryNums(long retryNums);


    /**
     * 得到消息的DB id
     *
     * @return
     */
    public abstract long getID();

    /**
     * 得到
     *
     * @param id
     */
    public abstract void setID(long id);


    /**
     * 得到文件上传或者下载的进度
     *
     * @return
     */
    public abstract int getProgress();

    /**
     * 设置文件上传和下载的进度
     *
     * @param progress
     */
    public abstract void setProgress(int progress);


    /**
     * 得到未读消息总数
     *
     * @param userId
     */
    static public int getUnReadMsgNum(long userId) {
        long ldetectTime = Utils.getCurrentServerTime() - AppDatabaseOperate.n24hours_in_millseconds;
        String strSQL = String.format("SELECT count(*) AS 'id' FROM tb_msgs WHERE recvStatus < %d AND rUID = %d AND readNum=0 AND isDel=0 AND cliTime>=%d;", IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues(), userId, ldetectTime);

        BasicMessage baseMsg = DatabaseFactory.getDBOper().getOne(strSQL, BasicMessage.class);

        if (baseMsg != null) {
            return (int) baseMsg.getID();
        }

        return 0;
    }


    /**
     * 得到未读消息相关好友人数
     *
     * @param userId
     */
    static public int getUnReadMsgFriendNum(long userId) {
        long ldetectTime = Utils.getCurrentServerTime() - AppDatabaseOperate.n24hours_in_millseconds;
        String strSQL = String.format("SELECT sUID FROM tb_msgs WHERE recvStatus < %d AND rUID = %d AND readNum=0 AND isDel=0 AND cliTime>=%d group by sUID;", IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues(), userId, ldetectTime);

        ArrayList<BasicMessage> baseMsgs = DatabaseFactory.getDBOper().getList(strSQL, BasicMessage.class);
        if (baseMsgs != null) {
            ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
            if (allFriends != null && !allFriends.isEmpty()) {
                String sUID;
                for (int i = 0; i < baseMsgs.size(); ) {
                    boolean isFriend = false;
                    sUID = String.valueOf(baseMsgs.get(i).getsUID());
                    for (BasicUser user : allFriends) {
                        if (user.getUserId().equals(sUID) && user.getFollowedStatus() == 1 && user.getFollowStatus() == 1) {
                            isFriend = true;
                            break;
                        }
                    }
                    if (!isFriend) {
                        baseMsgs.remove(i);
                    } else {
                        ++i;
                    }
                }
            }
            return baseMsgs.size();
        }
        return 0;
    }

    /**
     * 判断消息是否存在
     *
     * @param msgId
     */
    static public boolean isMessageExist(String msgId) {
        String strSQL = String.format("SELECT * FROM tb_msgs WHERE msgID='%s';", msgId);
        ArrayList<BasicMessage> baseMsgs = DatabaseFactory.getDBOper().getList(strSQL, BasicMessage.class);
        return baseMsgs != null && baseMsgs.size()>0;
    }

}
