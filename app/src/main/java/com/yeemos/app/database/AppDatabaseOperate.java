package com.yeemos.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.gigabud.core.database.DBOperate;
import com.gigabud.core.database.DatabaseHelper;
import com.gigabud.core.util.DeviceUtil;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangwei.ma on 15-12-23.
 */


public class AppDatabaseOperate extends DBOperate {

    public static AppDatabaseOperate mDBManager = null;

    static public long n24hours_in_millseconds = 24 * 3600 * 1000;
    static public int nMsg_keep_days = 7;

    public AppDatabaseOperate(SQLiteDatabase db) {
        super(db);
    }

    public static AppDatabaseOperate getInstance(Context context, String strDBName) {
        if (mDBManager == null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context, strDBName);
            SQLiteDatabase db = databaseHelper.getWritableDatabase();
            mDBManager = new AppDatabaseOperate(db);

            // ---------  升级数据库
            mDBManager.upgradeDb();
        }
        return mDBManager;
    }

    public void upgradeDb() {
//        String sql = "ALTER TABLE tb_msgs ADD COLUMN readNum long DEFAULT 0;";
//        DatabaseFactory.getDBOper().execSQL(sql);
//        //Log.i("AppDatabaseOperate", "upgradeDb SQL:" + sql + " flag:" + bFlag);
//
//        sql = "ALTER TABLE tb_msgs ADD COLUMN mStatus long DEFAULT 0;";
//        DatabaseFactory.getDBOper().execSQL(sql);
//
//        sql = "CREATE TABLE  IF NOT EXISTS \"tb_msgsSeqNum\" (\"id\" INTEGER PRIMARY KEY  NOT NULL ,\"seqNum\" TEXT DEFAULT '' ,\"sUID\" INTEGER DEFAULT (-1),\"rUID\" INTEGER DEFAULT (-1));";
//        DatabaseFactory.getDBOper().execSQL(sql);


    }

    public void initDBData() {
        //初始化数据库
        //删除超时的消息
        clearUpUnusefulMsgs();
        // 把上次被中断正在发送的消息 置为 失败
        resetSendindMsgStatus();
    }

    /**
     * 删除超时的消息
     */
    private boolean clearUpUnusefulMsgs() {
        long timeinterval_from_now = Utils.getCurrentServerTime() - n24hours_in_millseconds * nMsg_keep_days; //保留七天
        int peerunreadFlag = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues();
        int unreadFlag = IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues();
        int unreadconfirmFlag = IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM.GetValues();

        // 1,一天之外的消息 2，标记删除的消息 3，发送的消息对方没有已读 4，收到的消息没有标记为已读
        String strSQL = String.format("DELETE from tb_msgs WHERE updateTime < %d AND isDel < 2 AND (sendStatus = %d OR recvStatus = %d OR recvStatus = %d)", timeinterval_from_now, peerunreadFlag, unreadFlag, unreadconfirmFlag);

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        // Log.i("AppDatabaseOperate", "clearUpUnusefulMsgs SQL:" + strSQL + " flag:" + bFlag);
        return bFlag;
    }

    /**
     * 清空数据库
     */
    public boolean clearMsgData(long userID) {
        String strSQL = String.format("DELETE from tb_msgs WHERE sUID <> %d AND rUID <> %d", userID, userID);
        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        Log.i("AppDatabaseOperate", "clearMsgData SQL:" + strSQL + " flag:" + bFlag);
        return bFlag;
    }

    /**
     * 重置消息发送的消息状态
     */
    private boolean resetSendindMsgStatus() {
        String strSQL = String.format("UPDATE tb_msgs SET sendStatus = %d WHERE sendStatus < %d", IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues(), IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS.GetValues());

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        // Log.i("AppDatabaseOperate", "resetSendindMsgStatus SQL:" + strSQL + " flag:" + bFlag);
        return bFlag;
    }


    public IMsg getMsgByMsgId(String msgId) {
        String strSql = String.format("select * from tb_msgs where msgID = '%s'", msgId);
        BasicMessage baseMsg = getOne(strSql, BasicMessage.class);
        return baseMsg;
    }

    /**
     * 得到最近的聊天的消息
     */
    public ArrayList<IMsg> getRecentMsgList(long sUserID) {

        // 得到 最近七天内消息
        long timeinterval_from_now = Utils.getCurrentServerTime() - (n24hours_in_millseconds * nMsg_keep_days);

        //select * from tb_msgs where id > 0 and (sUID = 5 or rUID = 5)  group by (sUID+rUID)
        String strSql = String.format("select * from tb_msgs where id > 0 and (sUID = %d or rUID = %d) and (cliTime >= %d or isDel = 2) group by (sUID+rUID) order by id DESC", sUserID, sUserID, timeinterval_from_now);


        //Log.i("CRabbitMQChat", "getRecentMsgList:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        // Log.i("AppDatabaseOperate", "getRecentMsgList SQL:" + strSql + " size:" + msgList.size());
        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;

        return resultList;
    }


    /**
     * 得到与某人最后的聊天记录
     */
    public ArrayList<IMsg> getPeerUnReadMsgList(long sUserID, long rUserID) {
        String strSql = String.format("select * from tb_msgs where sUID = %d and rUID = %d and recvStatus < %d", sUserID, rUserID, IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM.GetValues());

        //Log.i("CRabbitMQChat", "getPeerLastChatMsgList:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        //Log.i("AppDatabaseOperate", "getPeerUnReadMsgList SQL:" + strSql + " size:" + msgList.size());
        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;


        return resultList;
    }


    /**
     * 直接修改消息的状态。不管是否正确，请注意使用
     *
     * @param msg
     * @param sendStatus
     * @return
     */
    public boolean updateMsgSendStatus(IMsg msg, IMsg.IMSG_SEND_STATUS sendStatus) {
        //通知服务器收到后，表示消息已经成功接收收到
        String strSQL = String.format("update tb_msgs set sendStatus = max(%d,sendStatus), updateTime = %d where msgID = '%s' and sendStatus < %d", sendStatus.GetValues(), Utils.getCurrentServerTime(), msg.getMsgID(), sendStatus.GetValues());

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        if (bFlag) {
            //Log.i("AppDatabaseOperate", "updateMsgSendStatus SQL:" + strSQL + " flag:" + bFlag);

            //IMsg tempMsg = DatabaseFactory.getDBOper().getMsgByMsgId(msg.getMsgID());
            //Log.i("AppDatabaseOperate", "tempMsg:" + tempMsg.toChatJson() );
            return true;
        } else {
            //Log.i("AppDatabaseOperate", "updateMsgSendStatus SQL:" + strSQL + " flag:" + bFlag);
            return false;
        }
    }

    /**
     * 修改消息发送的状态
     *
     * @param msgID
     * @param sendStatus
     * @return
     */
    public boolean updateMsgSendStatusByMsgID(String msgID, IMsg.IMSG_SEND_STATUS sendStatus) {
        //通知服务器收到后，表示消息已经成功接收收到
        String strSQL = String.format("update tb_msgs set sendStatus = max(%d,sendStatus), updateTime = %d where msgID = '%s' and sendStatus < %d", sendStatus.GetValues(), Utils.getCurrentServerTime(), msgID, sendStatus.GetValues());

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        if (bFlag) {
            //Log.i("AppDatabaseOperate", "updateMsgSendStatus SQL:" + strSQL + " flag:" + bFlag);

            //IMsg tempMsg = DatabaseFactory.getDBOper().getMsgByMsgId(msg.getMsgID());
            //Log.i("AppDatabaseOperate", "tempMsg:" + tempMsg.toChatJson() );
            return true;
        } else {
            //Log.i("AppDatabaseOperate", "updateMsgSendStatus SQL:" + strSQL + " flag:" + bFlag);
            return false;
        }
    }

    /**
     * 尝试修改消息的状态，如果要设置的状态要小于当前的状态，则忽略
     *
     * @param msg
     * @param sendStatus
     * @return
     */
    public boolean tryToUpdateMsgSendStatus(IMsg msg, IMsg.IMSG_SEND_STATUS sendStatus) {
        //通知服务器收到后，表示消息已经成功接收收到
        String strSQL = String.format("update tb_msgs set sendStatus = max(%d,sendStatus), updateTime = %d where msgID = '%s' and sendStatus < %d", sendStatus.GetValues(), Utils.getCurrentServerTime(), msg.getMsgID(), sendStatus.GetValues());

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        if (bFlag) {
            //Log.i("AppDatabaseOperate", "tryToUpdateMsgSendStatus SQL:" + strSQL + " flag:" + bFlag);

            //IMsg tempMsg = DatabaseFactory.getDBOper().getMsgByMsgId(msg.getMsgID());
            //Log.i("AppDatabaseOperate", "tempMsg:" + tempMsg.toChatJson() );
            return true;
        } else {
            //Log.i("AppDatabaseOperate", "tryToUpdateMsgSendStatus SQL:" + strSQL + " flag:" + bFlag);
            return false;
        }
    }

    /**
     * 修改消息删除状态
     *
     * @param msg
     * @param deleteStatus
     * @return
     */
    public boolean tryUpdateMsgDeleteStatusByMsg(IMsg msg, IMsg.IMSG_DELETE_STATUS deleteStatus) {
        //通知服务器收到后，表示消息已经成功接收收到
        return updateMsgDeleteStatusByMsgID(msg.getMsgID(), deleteStatus);
    }


    /**
     * 修改消息接收的状态
     *
     * @param msg
     * @param recvStatus
     * @return
     */
    public boolean tryUpdateMsgRecvStatusByMsg(IMsg msg, IMsg.IMSG_RECV_STATUS recvStatus) {
        //通知服务器收到后，表示消息已经成功接收收到
        return updateMsgRecvStatusByMsgID(msg.getMsgID(), recvStatus);
    }

    /**
     * 修改消息接收的状态
     *
     * @param msgID
     * @param recvStatus
     * @return
     */
    public boolean updateMsgRecvStatusByMsgID(String msgID, IMsg.IMSG_RECV_STATUS recvStatus) {
        //通知服务器收到后，表示消息已经成功接收收到
        String strSQL = String.format("update tb_msgs set recvStatus = max(%d,recvStatus), updateTime = %d  where msgID = '%s' and recvStatus < %d", recvStatus.GetValues(), Utils.getCurrentServerTime(), msgID, recvStatus.GetValues());

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        if (bFlag) {
            // Log.i("AppDatabaseOperate", "updateMsgRecvStatusByMsgID SQL:" + strSQL + " flag:" + bFlag);
            return true;
        } else {
            //Log.i("AppDatabaseOperate", "updateMsgRecvStatusByMsgID SQL:" + strSQL + " flag:" + bFlag);
            return false;
        }
    }


    /**
     * 修改消息接收的状态
     *
     * @param msgID
     * @param deleteStatus
     * @return
     */
    public boolean updateMsgDeleteStatusByMsgID(String msgID, IMsg.IMSG_DELETE_STATUS deleteStatus) {
        //
        String strSQL = String.format("UPDATE tb_msgs SET isDel = %d WHERE msgID = '%s'", deleteStatus.GetValues(), msgID);

        boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSQL);
        if (bFlag) {
            // Log.i("AppDatabaseOperate", "updateMsgDeleteStatusByMsgID SQL:" + strSQL + " flag:" + bFlag);
            return true;
        } else {
            //Log.i("AppDatabaseOperate", "updateMsgDeleteStatusByMsgID SQL:" + strSQL + " flag:" + bFlag);
            return false;
        }
    }

    /**
     * 标记用户又读了次消息
     *
     * @param lUserId
     * @return
     */
    public boolean markUserReadMsgsOnceMore(long lUserId) {
        //
        String strSQL = String.format("UPDATE tb_msgs SET readNum = readNum + 1 WHERE sUID = %d OR rUID = %d", lUserId, lUserId);

        return DatabaseFactory.getDBOper().execSQL(strSQL);
    }


    /**
     * 得到最后一条消息
     *
     * @param sendUserId
     * @param recvUserId
     * @return
     */
    public IMsg getMaxSeqNumMsg(long sendUserId, long recvUserId) {
        String strSql = String.format("select * from tb_msgs where sUID = %d and rUID = %d order by id DESC limit 1", sendUserId, recvUserId);
        // Log.i("AAA", "SQL getMaxSeqNumMsg:" + strSql);
        BasicMessage baseMsg = DatabaseFactory.getDBOper().getOne(strSql, BasicMessage.class);
        return baseMsg;
    }

    /**
     * 得到最大的发送序号，默认返回0
     *
     * @param sendUserId
     * @param recvUserId
     * @return
     */
    public String getMaxSendSeqNum(long sendUserId, long recvUserId) {
        String sendSeq = "";
        String strSql = String.format("SELECT seqNum FROM tb_msgsSeqNum where sUID = %d and rUID = %d", sendUserId, recvUserId);
        //Log.i("getMaxSendSeqNum", "getMaxSendSeqNum SQL:" + strSql);
        BasicMessage baseMsg = DatabaseFactory.getDBOper().getOne(strSql, BasicMessage.class);
        if (baseMsg != null) {
            sendSeq = baseMsg.getSendSeqNum();
            // Log.i("getMaxSendSeqNum1", "getMaxSendSeqNum SQL:" + strSql + "\n" + sendSeq );
        }

        if (sendSeq == null || sendSeq.length() <= 1) {
            strSql = String.format("SELECT seqNum FROM tb_msgs where sUID = %d and rUID = %d order by id DESC limit 1", sendUserId, recvUserId);
            BasicMessage baseMsginRecord = DatabaseFactory.getDBOper().getOne(strSql, BasicMessage.class);
            if (baseMsginRecord != null) {
                sendSeq = baseMsginRecord.getSendSeqNum();
                //Log.i("getMaxSendSeqNum2", "getMaxSendSeqNum SQL:" + strSql + "\n" + sendSeq);
            }
        }


        return sendSeq;
    }


    /**
     * 得到最大的发送序号，默认返回0
     *
     * @param userID
     * @return
     */
    public boolean detectIfEmptyDataBase(long userID) {
        String sendSeq = "";
        String strSql = String.format("SELECT COALESCE(MAX(id)+1, 0) AS id FROM tb_msgs WHERE rUID = %d OR sUID = %d;", userID, userID);
        boolean bifDBEmpty = true;
        BasicMessage baseMsg = DatabaseFactory.getDBOper().getOne(strSql, BasicMessage.class);
        if (baseMsg != null) {
            bifDBEmpty = baseMsg.getID() > 0 ? false : true;
        }

        return bifDBEmpty;
    }


    /**
     * 得到与某人的最有N条聊天记录（有可能是对方发的，也有可能是自己发的）
     *
     * @param sUID
     * @param overTime
     * @return
     */
    public ArrayList<IMsg> getSendOvertimeMsgList(long sUID, int overTime) {
        long curTime = Utils.getCurrentServerTime();
        String strSql = String.format("select * from tb_msgs where sUID = %d and (sendStatus < %d) and msgType = %d and (%d - updateTime) > %d ", sUID, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE.GetValues(), IMsg.MES_TYPE.TEXT_MSG_TYPE.GetValues(), curTime, overTime);
        //Log.i("CRabbitMQChat", "getSendOvertimeMsgList:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        //Log.i("CRabbitMQChat", "getSendOvertimeMsgList SQL:" + strSql + " size:" + msgList.size());
        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;
        return resultList;
    }


    /**
     * 得到与某人的聊天记录（有可能是对方发的，也有可能是自己发的）
     *
     * @param sUID
     * @param userID
     * @return
     */
    public ArrayList<IMsg> getPeerRecentChatRecordList(long sUID, long userID) {

        long timeinterval_from_now = Utils.getCurrentServerTime() - n24hours_in_millseconds;
        int peerunreadFlag = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues();
        int readFlag = IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues();

        // 1,一天之内的消息 2，保存下来的消息 3，发送的消息对方没有已读 4，收到的消息没有标记为已读
     //   String strSql = String.format("SELECT * FROM tb_msgs WHERE ((sUID = %d AND rUID = %d) OR (sUID = %d AND rUID = %d)) AND (updateTime >= %d OR isDel = 2 OR sendStatus < %d OR recvStatus < %d) order by cliTime ASC, id ASC", sUID, userID, userID, sUID, timeinterval_from_now, peerunreadFlag, readFlag);

  //      String strSql = String.format("SELECT * FROM tb_msgs WHERE ((sUID = %d AND rUID = %d) OR (sUID = %d AND rUID = %d)) AND (updateTime >= %d OR isDel = 2) order by cliTime ASC, id ASC", sUID, userID, userID, sUID, timeinterval_from_now);

        // 1,一天之内的消息 2，保存下来的消息 3，发送的消息对方没有已读 4，收到的消息没有标记为已读
        String strSql = String.format("SELECT * FROM tb_msgs WHERE ((sUID = %d AND rUID = %d) OR (sUID = %d AND rUID = %d)) AND (cliTime >= %d OR isDel = 2 OR sendStatus < %d OR recvStatus < %d) order by cliTime ASC, id ASC", sUID, userID, userID, sUID, timeinterval_from_now, peerunreadFlag, readFlag);


        //Log.i("CRabbitMQChat", "getPeerLastChatMsgList:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        Log.i("CRabbitMQChat", "getLastChatMsgList SQL:" + strSql + " size:" + msgList.size());
        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;
        return resultList;
    }


    /**
     * 得到与某人的最有N条聊天记录（有可能是对方发的，也有可能是自己发的）
     *
     * @param sUID
     * @param userID
     * @param nCount
     * @return
     */
    public ArrayList<IMsg> getPeerLastChatMsgList(long sUID, long userID, int nCount) {

        //  String strSql = String.format("select * from tb_msgs where id in ( select id from tb_msgs where (sUID = %d or sUID = %d ) and ( rUID = %d or rUID = %d ) and id <= (select max(id) as maxid from tb_msgs where (sUID = %d or sUID = %d ) and ( rUID = %d or rUID = %d ) ) order by id DESC limit %d ) order by cliTime ASC, id ASC", sUID, userID, sUID, userID, sUID, userID, sUID, userID, nCount);


        long timeinterval_from_now = Utils.getCurrentServerTime() - n24hours_in_millseconds;
        int peerunreadFlag = IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ.GetValues();
        int readFlag = IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues();

        // 1,一天之内的消息 2，保存下来的消息 3，发送的消息对方没有已读 4，收到的消息没有标记为已读
        String strSql = String.format("SELECT * FROM tb_msgs WHERE ((sUID = %d AND rUID = %d) OR (sUID = %d AND rUID = %d)) AND (cliTime >= %d OR isDel = 2 OR sendStatus < %d OR recvStatus < %d) order by cliTime DESC, id DESC limit %d", sUID, userID, userID, sUID, timeinterval_from_now, peerunreadFlag, readFlag, nCount);

        //Log.i("CRabbitMQChat", "getPeerLastChatMsgList:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        Log.i("CRabbitMQChat", "getLastChatMsgList SQL:" + strSql + " size:" + msgList.size());
        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;
        return resultList;
    }

    public boolean updateMsgsSeqNum(long sendUserId, IMsg msg) {
        //  String strSql = String.format("INSERT OR REPLACE INTO tb_msgsSeqNum(id,seqNum,sUID,rUID) VALUES ((select id from tb_msgsSeqNum where sUID = %d AND rUID = %d ),MAX('%s',(select seqNum from tb_msgsSeqNum where sUID = %d AND rUID = %d )),%d,%d)", sendUserId,msg.getrUID(),msg.getSendSeqNum(),sendUserId,msg.getrUID(),sendUserId,msg.getrUID() );

        boolean bflag = false;
        String strSql = String.format("select * from tb_msgsSeqNum where sUID = %d AND rUID = %d ", sendUserId, msg.getrUID());
        ArrayList<BasicMessage> msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);

        if (msgList != null && msgList.size() > 0) {
            strSql = String.format("UPDATE tb_msgsSeqNum SET seqNum = '%s' where sUID = %d AND rUID = %d", getLargerSeqNum(msgList.get(0).getSendSeqNum(), msg.getSendSeqNum()), sendUserId, msg.getrUID());
        } else {
            strSql = String.format("INSERT OR REPLACE INTO tb_msgsSeqNum(seqNum,sUID,rUID) VALUES ('%s',%d,%d)", msg.getSendSeqNum(), sendUserId, msg.getrUID());
        }
        bflag = DatabaseFactory.getDBOper().execSQL(strSql);

        Log.i("AAA", "updateMsgsSeqNum SQL:" + strSql + "\n" + bflag);

        return bflag;
    }

    private String getLargerSeqNum(String oldSeq, String newSeq) {
        try {

            String requrnString = newSeq;

            String[] oldSqeNums = oldSeq.split("_");
            String[] newSqeNums = newSeq.split("_");

            if (oldSqeNums.length >= 2 && newSqeNums.length >= 2) {

                if (oldSqeNums[0].equals(newSqeNums[0])) {
                    if (Integer.valueOf(newSqeNums[1]) < Integer.valueOf(oldSqeNums[1])) {
                        requrnString = oldSeq;
                    }
                }

            }

            return requrnString;
        } catch (Exception ep) {
            ep.printStackTrace();
            return newSeq;
        }


    }

    /**
     * 插入一条占位消息
     *
     * @param msg
     */
    public boolean insertAPositionMsg(IMsg msg) {

        String checkIfEist = String.format("select * from tb_msgs where sUID = %d AND rUID = %d AND seqNum = '%s'", msg.getsUID(), msg.getrUID(), msg.getSendSeqNum());
        // select seqNum from tb_msgsSeqNum where sUID = %ld AND rUID = %ld AND seqNum = '%@';
        ArrayList<BasicMessage> msgList = DatabaseFactory.getDBOper().getList(checkIfEist, BasicMessage.class);

        if (msgList == null || msgList.size() <= 0) {
            String strSql = String.format("INSERT INTO tb_msgs(seqNum,sUID,rUID,emptyMsg) VALUES ('%s',%d,%d,1)", msg.getSendSeqNum(), msg.getsUID(), msg.getrUID());
            Log.i("AAA", "insertAPositionMsg SQL:" + strSql);
            return DatabaseFactory.getDBOper().execSQL(strSql);
        }

        return false;


    }

    /**
     * 插入或者修改一条消息，如果存在，则修改，不存在则insert
     *
     * @param msg
     */
    public boolean insertOrUpdateSendMsg(long sendUserId, IMsg msg) {

        String strSql;
        long peerUid = sendUserId == msg.getsUID() ? msg.getrUID() : msg.getsUID();
        if (msg.getSendSeqNum() != null && msg.getSendSeqNum().length() > 0 && sendUserId > 0 && peerUid > 0) {
            strSql = String.format("select * from tb_msgs where msgID = '%s' OR (seqNum = '%s' AND sUID = %d AND rUID = %d)", msg.getMsgID(), msg.getSendSeqNum(), sendUserId, peerUid);
        } else {
            strSql = String.format("select * from tb_msgs where msgID = '%s' ", msg.getMsgID());
        }

        //Log.e("AAA", "SQL:" + strSql);
        ArrayList<BasicMessage> msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);

        boolean bResFalg = false;
        if (msgList != null && msgList.size() > 0) {
            // update message
            BasicMessage baseMsg = msgList.get(0);
            msg.setID(baseMsg.getID());

            if (sendUserId == baseMsg.getsUID()) {
                //send message
                if (msg.getSendStatus().GetValues() > baseMsg.getSendStatus().GetValues()) {
                    int nTemp = DatabaseFactory.getDBOper().update(msg);
                    bResFalg = nTemp > 0 ? true : false;
                }
            } else {
                // recv meesage
                if (msg.getRecvStatus().GetValues() > baseMsg.getRecvStatus().GetValues()) {
                    int nTemp = DatabaseFactory.getDBOper().update(msg);
                    bResFalg = nTemp > 0 ? true : false;
                }
            }

        } else {

            // insert mssage
            if (sendUserId == msg.getsUID()) {
                //send message

                // 得到最大的发送序号
                String maxSendSeqNum = DatabaseFactory.getDBOper().getMaxSendSeqNum(msg.getsUID(), msg.getrUID());
                msg.increaseqNumFromGivenString(maxSendSeqNum);

                // Log.i("AAA", "insertOrUpdateMsg: " + maxSendSeqNum + " seqnum " + msg.getSendSeqNum());

                long nIndex = insertWithNoPrimaryKey(msg);
                if (nIndex > 0) {
                    msg.setID(nIndex);
                    bResFalg = true;
                } else {
                    bResFalg = false;
                }
            } else {
                // recv meesage
                int nTemp = DatabaseFactory.getDBOper().update(msg);
                bResFalg = nTemp > 0 ? true : false;
            }
        }

        if (bResFalg) {
            Log.i("AppDatabaseOperate", "insertOrUpdateMsg database success");
        } else {
            Log.i("AppDatabaseOperate", "insertOrUpdateMsg database falied, msg is:" + msg.toChatJson());
        }
        return bResFalg;
    }


    /**
     * 插入一条收到的消息，如果存在，则修改，不存在则insert
     *
     * @param msg
     */
    public boolean insertOrUpdateRecvMsg(IMsg msg) {
        String strSql;
        if (msg.getSendSeqNum() != null && msg.getSendSeqNum().length() > 0 && msg.getsUID() > 0 && msg.getrUID() > 0) {
            strSql = String.format("select * from tb_msgs where msgID = '%s' OR (seqNum = '%s' AND sUID = %d AND rUID = %d)", msg.getMsgID(), msg.getSendSeqNum(), msg.getsUID(), msg.getrUID());
        } else {
            strSql = String.format("select * from tb_msgs where msgID = '%s' ", msg.getMsgID());
        }


        //String strSql = String.format("select * from tb_msgs where msgID = '%s' ", msg.getMsgID() );
        //Log.i("AAA", "SQL:" + strSql);
        ArrayList<BasicMessage> msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);


        boolean bResFalg = false;
        if (msgList != null && msgList.size() > 0) {
            // update message
            BasicMessage baseMsg = msgList.get(0);
            msg.setID(baseMsg.getID());

            // recv meesage
            if (msg.getRecvStatus().GetValues() > baseMsg.getRecvStatus().GetValues() ||
                    baseMsg.getEmptyMsg() >= 1) {
                // private int emptyMsg;

                int nTemp = DatabaseFactory.getDBOper().update(msg);
                bResFalg = nTemp > 0 ? true : false;

            }
        } else {
            long nIndex = insertWithNoPrimaryKey(msg);
            if (nIndex > 0) {
                msg.setID(nIndex);
                bResFalg = true;
            } else {
                bResFalg = false;
            }

        }

        if (bResFalg) {
            Log.i("AppDatabaseOperate", "insertOrUpdateMsg database success");
        } else {
            Log.i("AppDatabaseOperate", "insertOrUpdateMsg database falied, msg is:" + msg.toChatJson());
        }
        return bResFalg;
    }


}
