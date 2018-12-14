package com.yeemos.app.chat.bean;


import android.content.ContentValues;
import android.util.Log;

import com.bumptech.glide.util.Util;
import com.gigabud.core.database.DBOperate;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.DeviceUtil;
import com.google.gson.Gson;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BasicMessage extends IMsg {

    private long id; // 数据库ID
    private String msgID; // 消息唯一ID
    private long sUID; // 发送消息的用户ID
    private long rUID; // 收送消息的用户ID

    private int msgType = 0;
    private int btype = 0;

    private int isDel = 0;

    private int mStatus = 0;   // 0:实时消息(default) 1:离线消息 2:离线消息开始标记 3：离线消息结束标记

    private long cliTime;      // --发送/接收时的客户端GMT 毫秒时间
    private long svrTime;      // 发送/接收时的服务端GMT 毫秒时间
    private long updateTime;   // 消息修改的服务端GMT 毫秒时间


    private String seqNum;         //               -- 针对某人的发送的序列号
    public int sendStatus;       //                --发送状态(点击查看)
    public int sendConfimStatus; //          --消息的Server状态(预留)


    public int recvStatus;   //        -- 接收状态(点击查看)
    private long retryNums;  //        -- 客户端重试次数
    private long readNum;    //        -- 客户已读次数

    private String fName; // 文件名
    private String text;  // 消息内容


    private long fDuration;  //          -- 文件的长度
    private long fSize;      //          -- 文件的大小
    private String thumb;    //          -- 文件的缩略图
    private long imageMainColor;  //          -- 图片的主色

    private int progress;    //文件上传或者下载的进度

    private String readIds;        // 用于保存需要发送给对方已经收下来的序列号 (通知对方已经收下来的消息序列号，使用readIds属性)
    private String confirmReadIds; // confirm 接收到的 readIds 字段

    private int emptyMsg;


    public BasicMessage() {
        msgID = BaseUtils.getUUID();
        cliTime = Utils.getCurrentServerTime();
        updateTime = cliTime;
        emptyMsg = 0;
        setRetryNums(0);
    }

    public ContentValues getValues() {
        ContentValues values = super.getValues();
        values.remove("progress");
        values.remove("readIds");
        values.remove("confirmReadIds");
        values.remove("btype");

        return values;
    }


    public boolean insertOBject() {
        return false;

    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public long getfDuration() {
        return fDuration;
    }

    public void setfDuration(long fDuration) {
        this.fDuration = fDuration;
    }

    public long getfSize() {
        return fSize;
    }

    public void setfSize(long fSize) {
        this.fSize = fSize;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public long getImageMainColor() {
        return imageMainColor;
    }

    public void setImageMainColor(long imageMainColor) {
        this.imageMainColor = imageMainColor;
    }


    public int getSendConfimStatus() {
        return sendConfimStatus;
    }

    public void setSendConfimStatus(int sendConfimStatus) {
        this.sendConfimStatus = sendConfimStatus;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public long getDBID() {
        Log.i("AAA", "getDBID:" + this.id);
        return id;
    }


    public String getfName() {
        return fName;
    }

    public void setfName(String fName) {
        this.fName = fName;
    }


    public long getSvrTime() {
        return svrTime;
    }

    public void setSvrTime(long svrTime) {
        this.svrTime = svrTime;
    }


    public void setSendStatus(IMSG_SEND_STATUS sendStatus) {
        this.sendStatus = Math.max(this.sendStatus, sendStatus.GetValues());
    }

    public IMSG_SEND_STATUS getSendStatus() {
        return IMSG_SEND_STATUS.GetObject(sendStatus);
    }


    public void setRecvStatus(IMSG_RECV_STATUS recvStatus) {
        this.recvStatus = Math.max(this.recvStatus, recvStatus.GetValues());
    }

    public IMSG_RECV_STATUS getRecvStatus() {
        return IMSG_RECV_STATUS.GetObject(recvStatus);
    }

    public IMSG_DELETE_STATUS getIsDel() {
        return IMSG_DELETE_STATUS.GetObject(isDel);
    }

    public IMSG_DELETE_STATUS getDeleteStatus() {
        return IMSG_DELETE_STATUS.GetObject(isDel);
    }


    public void setIsDel(IMSG_DELETE_STATUS isDelStatus) {
        this.isDel = isDelStatus.GetValues();
    }

    public long getCliTime() {
        return cliTime;
    }

    public void setCliTime(long cliTime) {
        this.cliTime = cliTime;
    }

    /**
     * 得到重试次数
     *
     * @return
     */
    public long getRetryNums() {
        return retryNums;
    }

    /**
     * 设置重试次数
     *
     * @param retryNums
     */
    public void setRetryNums(long retryNums) {
        this.retryNums = retryNums;
    }


    /**
     * 返回主键的字段名，如：id
     *
     * @return
     */
    public String getPrimaryKeyName() {
        return "id";

    }


    /**
     * 返回这个类对应的表名
     *
     * @return
     */
    public String getTableName() {
        return "tb_msgs";
    }


    public void setMessageType(MES_TYPE msgType) {
        this.msgType = msgType.GetValues();
    }

    public MES_TYPE getMessageType() {
        return MES_TYPE.GetObject(msgType);
    }


    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }


    public boolean insert(DBOperate dbOperte) {
        return false;

    }

    public boolean delete(DBOperate dbOperte) {
        return false;
    }

    public boolean insertOrUpdate(DBOperate dbOperte) {
        return false;
    }

    public String toChatJson() {
        try {
            //Log.i("AAA", "TestMesage:" + new Gson().toJson(this));
            return new Gson().toJson(this);

        } catch (Exception expect) {

            //Log.i("AAA", "expect:" + expect.toString() );
            return "";
        }
    }


    public long getID() {
        return id;
    }

    public void setID(long id) {
        this.id = id;
        Log.i("AAA", "id:" + this.id);
    }


    public long getsUID() {
        return sUID;
    }

    public void setsUID(long sUID) {
        this.sUID = sUID;
    }


    public long getrUID() {
        return rUID;
    }

    public void setrUID(long rUID) {
        this.rUID = rUID;
    }


    public String getSendSeqNum() {
        return seqNum;
    }

    public void setSendSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }


    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }


    public long getReadNum() {
        return readNum;
    }

    public void setReadNum(long readNum) {
        this.readNum = readNum;
    }

    /**
     * 重发此送消息
     *
     * @return
     */
    public boolean reSendMsg() {
        IMSG_SEND_STATUS status = IMSG_SEND_STATUS.IMSG_SEND_STATUS_UNSEND;
        setSendStatus(status);
        return DatabaseFactory.getDBOper().updateMsgSendStatus(this, status);
    }

    public boolean updateSendStatus(IMSG_SEND_STATUS status) {
        setSendStatus(status);
        return DatabaseFactory.getDBOper().tryToUpdateMsgSendStatus(this, status);

    }

    public boolean updateRecvStatus(IMSG_RECV_STATUS status) {
        setRecvStatus(status);
        return DatabaseFactory.getDBOper().tryUpdateMsgRecvStatusByMsg(this, status);
    }

    public boolean updateDeleteStatus(IMSG_DELETE_STATUS status) {
        setIsDel(status);
        return DatabaseFactory.getDBOper().tryUpdateMsgDeleteStatusByMsg(this, status);
    }

    public int getmStatus() {
        return mStatus;
    }

    public void setmStatus(int mStatus) {
        this.mStatus = mStatus;
    }


    public String getConfirmReadIds() {
        return confirmReadIds;
    }

    public void setConfirmReadIds(String confirmReadIds) {
        this.confirmReadIds = confirmReadIds;
    }

    public String getReadIds() {
        return readIds;
    }

    public void setReadIds(String readIds) {
        this.readIds = readIds;
    }

    /**
     * 根据 givenSeqNum 设置 sendSeqNum
     */
    public void increaseqNumFromGivenString(String givenSeqNum) {
        //
        String[] givenSeqNums = null;
        if (givenSeqNum != null) {
            givenSeqNums = givenSeqNum.split("_");
        }


        if (givenSeqNums != null && givenSeqNums.length == 2) {
            String batchNum = givenSeqNums[0];
            String seqNum = givenSeqNums[1];
            String new_increaseSeqNum = String.format("%s_%d", batchNum, Integer.parseInt(seqNum) + 1);
            setSendSeqNum(new_increaseSeqNum);
        } else {
            // 可以认为是从来没有过seqNum
            String batchKey = "im_amqp_currentBatchnumber_key";
            String defautBatchValues = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
            String currentBatchNum = Preferences.getInstacne().getValues(batchKey, defautBatchValues);
            if (currentBatchNum == null) {
                currentBatchNum = defautBatchValues;
                Preferences.getInstacne().setValues(batchKey, currentBatchNum);
            }

            String new_increaseSeqNum = String.format("%s_1", currentBatchNum);
            setSendSeqNum(new_increaseSeqNum);
        }

    }

    /**
     * 分析 getReadIds 和 getConfirmReadIds
     */
    public void analystReadIds() {
        if (getReadIds() == null) return;

        try {

            // just for incoming
            // 1，对方已读我已经发送的消息
            long lcurrentTime = Utils.getCurrentServerTime();
            if (getReadIds() != null && getReadIds().length() > 0) {
                readIdsBean readidObject = new Gson().fromJson(getReadIds(), readIdsBean.class);
                IMSG_SEND_STATUS sendStatus = IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ;
                String strSQL = String.format("update tb_msgs set sendStatus = max(%d,sendStatus), updateTime = %d where seqNum IN(%s) AND rUID = '%s' AND sUID = '%s' AND sendStatus < %d;", sendStatus.GetValues(), lcurrentTime, readidObject.getReadIds(), readidObject.getrUID(), readidObject.getsUID(), sendStatus.GetValues());
                DatabaseFactory.getDBOper().execSQL(strSQL);
            }

            // 1，对方确认了 我已经收到的消息
            if (getConfirmReadIds() != null && getConfirmReadIds().length() > 0) {
                readIdsBean readidObject = new Gson().fromJson(getConfirmReadIds(), readIdsBean.class);
                IMSG_RECV_STATUS recvStatus = IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM;
                String strSQL = String.format("update tb_msgs set recvStatus = max(%d,recvStatus), updateTime = %d where seqNum IN(%s) AND rUID = '%s' AND sUID = '%s' AND recvStatus < %d;", recvStatus.GetValues(), lcurrentTime, readidObject.getReadIds(), readidObject.getrUID(), readidObject.getsUID(), recvStatus.GetValues());
                DatabaseFactory.getDBOper().execSQL(strSQL);
            }

        } catch (Exception ei) {
            ei.printStackTrace();
        }


    }

    /**
     * 将之前已读消息放入到readids
     *
     * @return
     */
    public void addReadIds() {
        // need added msgss
        if (getrUID() > 0 && getsUID() > 0) {
            ArrayList<IMsg> arrMsgs = DatabaseFactory.getDBOper().getPeerUnReadMsgList(getsUID(), getrUID());
            if (arrMsgs != null && arrMsgs.size() > 0) {
                StringBuffer readIdsBuffuer = new StringBuffer("");
                for (int i = 0; i < arrMsgs.size(); ++i) {
                    IMsg msg = arrMsgs.get(i);
                    if (i == arrMsgs.size() - 1) {
                        readIdsBuffuer.append(String.format("\'%s\'", msg.getSendSeqNum()));
                    } else {
                        readIdsBuffuer.append(String.format("\'%s\',", msg.getSendSeqNum()));
                    }
                }
                String readIds = String.format("{\"sUID\":\"%s\",\"rUID\":\"%s\",\"readIds\":\"%s\"}", getsUID(), getrUID(), readIdsBuffuer.toString());
                setReadIds(readIds);

                //Log.i("addReadIds", "addReadIds: getConfirmReadIds: " + getReadIds());
            }
        }
    }


    public int getEmptyMsg() {
        return emptyMsg;
    }

    public void setEmptyMsg(int emptyMsg) {
        this.emptyMsg = emptyMsg;
    }

    public int getBtype() {
        return btype;
    }

    public void setBtype(int btype) {
        this.btype = btype;
    }
}
