package com.yeemos.app.chat.manager;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PushMessageBean;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.http.HttpUtil;
import com.gigabud.core.util.DeviceUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.ClientStructureMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.PeerRead;
import com.yeemos.app.chat.bean.PeerRecieved;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.chat.rpcBean.User;
import com.yeemos.app.database.AppDatabaseOperate;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.fragment.ChatFragment;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xiangwei.ma on 12/9/15.
 */
public class CRabbitMQChat extends IChat implements IChatListener {
    static private String TAG = "CRabbitMQChat";
    private RabbitMQManager rabbitMQManager = null;
    private ArrayList<IChatListener> arrListeners = null;
    private long sendUserId = -1;
    private String token = "";

    private Map<Long, Integer> mapGetOffLineMsg = null; //标记用户是否已经取过类似记录  1:或者其他数字表示没有获取 2:正在获取  3:表示已经取过


    private List<UserMsgSummery> userMsgInfos = new ArrayList<UserMsgSummery>();

    public CRabbitMQChat() {
        getRabbitMQManager();
    }

    public RabbitMQManager getRabbitMQManager() {
        if (rabbitMQManager == null) {
            rabbitMQManager = new RabbitMQManager();
            rabbitMQManager.addChatListener(this);
        }
        return rabbitMQManager;
    }


    private ArrayList<IChatListener> getChatListeners() {
        if (arrListeners == null)  //防止内存回收
        {
            arrListeners = new ArrayList<IChatListener>();
        }
        return arrListeners;
    }


    /**
     * 得到消息概述
     *
     * @return
     */
    @Override
    public List<UserMsgSummery> getUserMsgSummery() {
        if (userMsgInfos == null) {
            userMsgInfos = new ArrayList<UserMsgSummery>();
            getRecentyChatFromDB();
        }
        return userMsgInfos;
    }

    /**
     * 得到消息概述
     *
     * @return
     */
    protected void setUserMsgSummery(List<UserMsgSummery> arrUserMsgInfos) {
        userMsgInfos = arrUserMsgInfos;
    }


    private void setUserId(long sendUserId) {
        this.sendUserId = sendUserId;
    }

    private long getUserId() {
        return this.sendUserId;
    }


    private void setToken(String token) {
        this.token = token;
    }

    private String getToken() {
        return this.token;
    }


    private boolean isNeedGetOffLineMessage(long peerUserId) {
        Log.i("CRabbitMQChat", "isNeedGetOffLineMessage:" + peerUserId);
        if (mapGetOffLineMsg == null) {
            mapGetOffLineMsg = new HashMap<Long, Integer>();
        }

        Integer integer = mapGetOffLineMsg.get(new Long(peerUserId));

        if (integer != null) {
            Log.i("CRabbitMQChat", "integer.intValue():" + integer.intValue());
        }

        if (integer == null)
            return true;
        else if (integer.intValue() == 3 //已经取过了
                || integer.intValue() == 2) //正在获取
        {
            return false;
        }

        return false;
    }


    /**
     * 设置用户是否获取历史消息的状态
     *
     * @param peerUserId
     * @param nStatus    1:或者其他数字表示没有获取 2:正在获取  3:表示已经取过
     */
    private void setIsNeedGetOffLineMessageStatus(long peerUserId, int nStatus) {
        if (mapGetOffLineMsg == null) {
            mapGetOffLineMsg = new HashMap<Long, Integer>();
        }

        mapGetOffLineMsg.put(new Long(peerUserId), new Integer(3));
    }


    /**
     * begin connect chat server
     */
    public void beginConnect() {

    }

    /**
     * 连接成功
     */
    public void connectSuccess() {
        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.connectSuccess();
        }
    }

    /**
     * 连接失败
     */
    public void connectFailure() {
        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.connectFailure();
        }
    }

    /**
     * 断开连接 或者连接失败
     */
    public void disconnect() {
        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.disconnect();
        }
    }


    /**
     * 从DB读取没有读的消息
     */
    public void getRecentyChatFromDB() {
        long selfUserId = getUserId();
        ArrayList<IMsg> arrMsgs = DatabaseFactory.getDBOper().getRecentMsgList(selfUserId);

        // Log.i("CRabbitMQChat", "getRecentyChatFromDB:" + selfUserId + " arrMsgs.size:" + arrMsgs.size());
        for (int i = 0; i < arrMsgs.size(); ++i) {
            IMsg msg = arrMsgs.get(i);

            UserMsgSummery userMsgSummery = new UserMsgSummery();

            // 只保留一天之内的消息
            BasicMessage baseMsg = (BasicMessage) msg;
            long ldetectTime = Utils.getCurrentServerTime() - AppDatabaseOperate.n24hours_in_millseconds;
            //          if (baseMsg.getCliTime() >= ldetectTime) {
            //设置最后一条消息
            userMsgSummery.setLastMsg(msg);
            //          }

            //设置对方用户信息
            User user = new User();
            long peerUserId = msg.getrUID();
            if (msg.getsUID() != selfUserId) {
                peerUserId = msg.getsUID();
            }
            user.setUserId(peerUserId);
            userMsgSummery.setUser(user);


            dealwithUserMsgSummery(userMsgSummery);
        }

        notifyUserMsgSummeryChange(getUserMsgSummery());

    }

    /**
     * 处理一个用户的消息概述
     *
     * @param userMsgSummery
     */
    private void dealwithUserMsgSummery(UserMsgSummery userMsgSummery) {
        List<UserMsgSummery> arrUserSummery = getUserMsgSummery();
        for (int i = 0; i < arrUserSummery.size(); ++i) {
            UserMsgSummery temp = arrUserSummery.get(i);
            if (temp.getUser().getUserId() == userMsgSummery.getUser().getUserId()) {
                arrUserSummery.remove(i);
                break;
            }
        }
        arrUserSummery.add(userMsgSummery);
    }


    /**
     * 接收离线消息结束
     *
     * @param userArr
     */
    public void offlineMsgRcvd(ArrayList<String> userArr) {
        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.offlineMsgRcvd(userArr);
        }
    }

    /**
     * 收到消息
     *
     * @param msg
     */
    public void receiveMsg(IMsg msg) {
        if (msg.getsUID() != 0
                && !String.valueOf(msg.getsUID()).equals(MemberShipManager.getInstance().getUserID())
                && (msg.getMessageType().GetValues() == IMsg.MES_TYPE.TEXT_MSG_TYPE.GetValues() || msg.getMessageType().GetValues() == IMsg.MES_TYPE.FILE_MSG_TYPE.GetValues())
                && msg.getRecvStatus() == IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_WAIT_RECV) {

            if (Preferences.getInstacne().isRunning()) {
                if (BaseApplication.getCurFragment() != null && BaseApplication.getCurFragment().getClass().isAssignableFrom(ShowPostViewPagerFragment.class)) {
                    final PushMessageBean pushMessageBean = new PushMessageBean();
                    pushMessageBean.setTuid(String.valueOf(msg.getrUID()));
                    pushMessageBean.setCuid(String.valueOf(msg.getsUID()));
                    pushMessageBean.setType(String.valueOf(MyFirebaseMessagingService.PushType.TYPE_RECEIVE_FRIEND_MESSAGE.value()));
                    ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
                    String body = null;
                    if (allFriends != null) {
                        for (BasicUser basicUser : allFriends) {
                            if (basicUser.getUserId().equals(pushMessageBean.getCuid())) {
                                if (basicUser.getFollowedStatus() == 0) {
                                    return;
                                }
                                body = ServerDataManager.getTextFromKey("pf_txt_receivemessage").replace("{0}", basicUser.getRemarkName());
                                break;
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(body)) {
                        //body = ServerDataManager.getTextFromKey("pf_txt_receivemessage").replace("{0}", "");
                        pushMessageBean.setBody(body);
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                ((ShowPostViewPagerFragment) BaseApplication.getCurFragment()).showPostViewPopup(pushMessageBean);
                            }
                        });
                    }

                }
                BaseApplication.getAppContext().sendBroadcast(new Intent(HomeActivity.NOTIFICATION_MESSAGE_LISTENER));
            }
//            else {
//                PushMessageBean pushMessageBean = new PushMessageBean();
//                pushMessageBean.setType(String.valueOf(GCMIntentService.PushType.TYPE_RECEIVE_FRIEND_MESSAGE.value()));
//                pushMessageBean.setTuid(String.valueOf(msg.getrUID()));
//                pushMessageBean.setCuid(String.valueOf(msg.getsUID()));
//                pushMessageBean.setType("1001");
//                ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
//                String body = null;
//                if (allFriends != null) {
//                    for (BasicUser basicUser : allFriends) {
//                        if (basicUser.getUserId().equals(pushMessageBean.getCuid())) {
//                            if (basicUser.getFollowedStatus() == 0) {
//                                return;
//                            }
//                            body = ServerDataManager.getTextFromKey("pf_txt_receivemessage").replace("{0}", basicUser.getRemarkName());
//                            break;
//                        }
//                    }
//                }
//                if (!TextUtils.isEmpty(body)) {
//                    //body = ServerDataManager.getTextFromKey("pf_txt_receivemessage").replace("{0}", "");
//                    pushMessageBean.setBody(body);
//                    GCMIntentService.showNotification(BaseApplication.getAppContext(), pushMessageBean);
//                }
//
//            }
            int unreadMsgNum = IMsg.getUnReadMsgFriendNum(Long.parseLong(MemberShipManager.getInstance().getUserID()));
            BadgeUtil.setBadgeCount(BaseApplication.getAppContext(), unreadMsgNum);
        }

        //加入到消息概述里面
        addToUserMsgSummery(msg, true);

        boolean isChatFrameListening = false;
        ArrayList<IChatListener> listeners = getChatListeners();
        for (
                int i = 0;
                i < listeners.size(); ++i)

        {
            IChatListener listener = listeners.get(i);
            listener.receiveMsg(msg);
            if (!isChatFrameListening) {
                isChatFrameListening = listener instanceof ChatFragment;
            }
        }

        if (!isChatFrameListening)

        {
            if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE) {
                // 正在和该用户聊天时，直接发 已读消息过去，省略已收这一步，所以放在 UI 上判断比较好
                // 发送已收到的confim消息
                PeerRecieved peerRecv = new PeerRecieved();
                peerRecv.setrUID(msg.getsUID());
                peerRecv.setConfirmMsgID(msg.getMsgID());

                RabbitMQManager rabbitMQ = getRabbitMQManager();
                rabbitMQ.sendMsg(peerRecv);
            }
        }


    }

    /**
     * 通知Listener message summery 发生改变
     *
     * @param msgSummery
     */
    private void notifyMessageSummeryChange(List<UserMsgSummery> msgSummery) {
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).msgSummeryChange(msgSummery);
        }
    }

    @Override
    public void sendingMsg(IMsg msg) {

    }


    /**
     * 文件上传中
     *
     * @param msg
     */
    @Override
    public void msgUploading(IMsg msg, int progress) {

        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.msgUploading(msg, progress);
        }

    }


    /**
     * 文件在下载中
     *
     * @param msg
     */
    @Override
    public void msgDownloading(IMsg msg, int progress) {


        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.msgDownloading(msg, progress);
        }

    }


    public void msgError(IMsg msg) {
        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.msgError(msg);
        }

    }


    public void notifyUserMsgSummeryChange(List<UserMsgSummery> msgSummery) {

        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            listener.msgSummeryChange(msgSummery);
        }

    }


    /**
     * 消息概述发生变化
     */
    public void msgSummeryChange(List<UserMsgSummery> msgSummery) {

        //Log.i(TAG, "msgSummeryChange");
        if (msgSummery != null) {
            Log.i(TAG, "msgSummeryChange is not null");

            //dealWithGetOfflineMessage(msgSummery);
            //setUserMsgSummery(msgSummery); 不需要

            for (int i = 0; i < msgSummery.size(); ++i) {
                UserMsgSummery userMsgSummner = msgSummery.get(i);

//                Log.i(TAG, "user id:" + userMsgSummner.getUser().getUserId() + " unRead message:" + userMsgSummner.getUnReadNum());
//
//                IMsg tempMsg = userMsgSummner.getLastMsg();
//                if ( tempMsg != null )
//                {
//                    Log.i(TAG, " text:" + tempMsg.getText() + "message type:" + tempMsg.getMessageType() );
//                }

                // 和现有的进行整合
                dealwithUserMsgSummery(userMsgSummner);

            }
        }

//        if (true) // test only
//        {
//            try
//            {
//                Log.i("CRabbitMQChat", "msgSummeryChange:" + new Gson().toJson(msgSummery));
//            }
//            catch (Exception expect)
//            {
//                Log.i("CRabbitMQChat", "msgSummeryChange expect:" + expect.toString() );
//            }
//        }
        notifyUserMsgSummeryChange(getUserMsgSummery());

    }


    public void connectServer(String token, long userID) {
        synchronized (CRabbitMQChat.class) {
            setUserId(userID);
            setToken(token);

            getRecentyChatFromDB();

            // clear database  not beyong to userID
            DatabaseFactory.getDBOper().clearMsgData(userID);

            RabbitMQManager rabbitManager = getRabbitMQManager();
            ArrayList<IChatListener> listeners = getChatListeners();
            boolean bConnectFlag = isConnecting();
            if (!bConnectFlag) {
                rabbitManager.connect(token, userID);
            } else {
                connectSuccess();
            }
        }
    }

    public void disconnectServer() {
        synchronized (CRabbitMQChat.class) {
            RabbitMQManager rabbitManager = getRabbitMQManager();

            if (isConnecting()) {
                rabbitManager.disconnect();
            } else {
                disconnect();
            }

        }
    }

    /**
     *
     */
    public void logOut() {
        disconnectServer();

        // empty memeory
        setUserMsgSummery(null);

    }


    public boolean isConnecting() {
        RabbitMQManager rabbitManager = getRabbitMQManager();
        return rabbitManager.isConnecting();
    }


    /**
     * 发送消息
     *
     * @param msg
     * @return
     */
    public boolean sendMsgImp(IMsg msg) {
        if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.IMAGE_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.ADUID_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.VIDEO_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
            // insert meesage
            DatabaseFactory.getDBOper().insertOrUpdateSendMsg(getUserId(), msg);

            // update tb_msgsSeqNum
            DatabaseFactory.getDBOper().updateMsgsSeqNum(getUserId(), msg);
        }

        // get max send seq
        // 1 未发送状态
        boolean bFlag = msg.updateSendStatus(IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_UNSEND);
        if (!bFlag) {
            Log.i(TAG, "update unSend Status failed");
            return bFlag;
        }


        if (msg.getMessageType() == IMsg.MES_TYPE.IMAGE_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.ADUID_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.VIDEO_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
            //上传附件
            FileHtttpManager.getInstance().uploadMsg(msg, getToken());
        }


        if (msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
            return true;
        }

        RabbitMQManager rabbitMQ = getRabbitMQManager();
        return rabbitMQ.sendMsg(msg);

    }

    /**
     * 得到对方的UserID
     *
     * @param msg
     * @return
     */
    private long getPeerUserIdFromMsg(IMsg msg) {
        long selfUserID = getUserId();
        long peerUserID = msg.getrUID();
        if (selfUserID == peerUserID) {
            peerUserID = msg.getsUID();
        }
        return peerUserID;
    }

    /**
     * 增加消息到消息概述里面
     *
     * @param msg
     */
    private void addToUserMsgSummery(IMsg msg, boolean bIsRecvMsg) {
        if (msg.getMessageType().GetValues() == IMsg.MES_TYPE.TEXT_MSG_TYPE.GetValues()
                || msg.getMessageType().GetValues() == IMsg.MES_TYPE.FILE_MSG_TYPE.GetValues()
                || msg.getMessageType().GetValues() == IMsg.MES_TYPE.ADUID_MSG_TYPE.GetValues()
                || msg.getMessageType().GetValues() == IMsg.MES_TYPE.VIDEO_MSG_TYPE.GetValues()
                || msg.getMessageType().GetValues() == IMsg.MES_TYPE.PEER_READ_MSG_TYPE.GetValues()) {
            long peerUserID = getPeerUserIdFromMsg(msg);

            if (peerUserID <= 0) return;

            boolean bIsFindFlag = false;
            List<UserMsgSummery> listUserSummery = getUserMsgSummery();

            // Log.i("CRabbitMQChat", "addToUserMsgSummery size: " + listUserSummery.size() + " peer id:" + peerUserID );

            for (int i = 0; i < listUserSummery.size(); ++i) {
                UserMsgSummery userMsgSummery = listUserSummery.get(i);
                if (userMsgSummery.getUser().getUserId() == peerUserID) {
                    bIsFindFlag = true;
                    userMsgSummery.setLastMsg(msg);

//                    if (bIsRecvMsg) // 收到的的消息
//                    {
//                        userMsgSummery.setUnReadNum( userMsgSummery.getUnReadNum() + 1);
//                    }
//                    else
//                    {
//                        if ( msg.getMessageType().GetValues() == IMsg.MES_TYPE.PEER_READ_MSG_TYPE.GetValues() )
//                        {
//                            long nTemp = userMsgSummery.getUnReadNum();
//                            nTemp = nTemp < 0 ? 0 : nTemp;
//                            userMsgSummery.setUnReadNum( nTemp );
//                        }
//                    }
                    break;
                }
            }

            if (!bIsFindFlag)  //增加一条消息概述
            {
                UserMsgSummery userMsgSummery = new UserMsgSummery();

                userMsgSummery.setLastMsg(msg);

                User user = new User();
                user.setUserId(peerUserID);
                userMsgSummery.setUser(user);
                //userMsgSummery.setUnReadNum(DatabaseFactory.getDBOper().getUnReadMsgNumbers( getUserId(), peerUserID));

                // 加入到最近聊天列表中去 added by jxq 20160412
                listUserSummery.add(0, userMsgSummery);

                //Log.i("CRabbitMQChat", "addToUserMsgSummery add a record size: " + listUserSummery.size());
            }


            //通知用户概述变化
            notifyUserMsgSummeryChange(getUserMsgSummery());
        }


    }

    /**
     * 发送消息
     *
     * @param msg
     * @return
     */
    public boolean sendMsg(IMsg msg) {

        msg.setsUID(getUserId());

        // add readIds into msgs
        msg.addReadIds();

        //加入到消息概述里面
        addToUserMsgSummery(msg, false);

        return sendMsgImp(msg);

    }


    /**
     * 重发
     *
     * @param msg
     * @return
     */
    public boolean reSendMsg(IMsg msg) {
        Log.i("CRabbitMQChat", "reSendMsg:" + msg.getsUID() + " send userid:" + getUserId());

        //首先清除此消息的状态
        msg.reSendMsg();

        return sendMsgImp(msg);

    }

    /**
     * 读了这条消息
     *
     * @param msg
     * @return
     */
    public boolean readMsg(IMsg msg) {

        if (msg.getsUID() == getUserId()) {
            return false;
        } else {
            // IMSG_RECV_STATUS_RECV_READ_CONFIRM
            if (msg.getRecvStatus().GetValues() < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues()) {
                msg.updateRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ); //修改消息状态，避免重复发送
                // Log.i("CRabbitMQChat", "readMessage receive:" + msg.getsUID() + " send userid:" + getUserId() );

                PeerRead peerRead = new PeerRead();  //TODO ：如果没有网络，则此消息会发送不出去
                peerRead.setConfirmMsgID(msg.getMsgID());
                peerRead.setrUID(msg.getsUID());
                peerRead.setConfirmReadIds(msg.getReadIds());

                BasicMessage baseMsg = (BasicMessage) msg;

                return sendMsg(peerRead);
            }
        }


        return false;

    }

    /**
     * 批量读了这条消息
     *
     * @param sendMapping
     * @return
     */
    public boolean batchReadMsgs(HashMap<String, ArrayList<IMsg>> sendMapping) {
        RabbitMQManager rabbitMQ = getRabbitMQManager();
        return rabbitMQ.batchsendMsgs(sendMapping);
    }


    /**
     * 收了这条消息
     *
     * @param msg
     * @return
     */
    public boolean receivedMsg(IMsg msg) {

        if (msg.getsUID() == getUserId()) {
            return false;
        } else {
            if (msg.getRecvStatus().GetValues() < IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_SUCCESS.GetValues()) {
                msg.updateRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_SUCCESS); //修改消息状态，避免重复发送
                // 发送已收到的confim消息
                PeerRecieved peerRecv = new PeerRecieved();
                peerRecv.setrUID(msg.getsUID());
                peerRecv.setConfirmMsgID(msg.getMsgID());
                peerRecv.setConfirmReadIds(msg.getReadIds());
                return sendMsg(peerRecv);
            }

            return false;
        }


    }

    /**
     * 正在收消息，适用于 文件
     *
     * @param msg
     * @return
     */
    public boolean receivingMsg(IMsg msg) {
        msg.setRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECEING);
        return msg.updateRecvStatus(IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECEING);
    }


    /**
     * 下载一个消息
     *
     * @param msg
     * @return
     */
    public boolean downloadMsg(IMsg msg) {
        return FileHtttpManager.getInstance().downloadMsg(msg);
    }

    public boolean gotoBackground() {
        return true;
    }

    public boolean gotoForeground() {
        return true;
    }

    public boolean addChatListener(IChatListener chatListener) {

        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); i++) {
            IChatListener chectlisten = listeners.get(i);
            if (chectlisten.equals(chatListener)) {
                listeners.remove(chectlisten);
                break;
            }
        }

        listeners.add(chatListener);

        //监听文件上传和下载进度
        FileHtttpManager.getInstance().addChatListener(chatListener);

        return true;
    }

    public boolean removeChatListener(IChatListener chatListener) {

        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            if (listener != null && listener == chatListener) {
                listeners.remove(i);
                break;
            }

        }


        //监听文件上传和下载进度
        FileHtttpManager.getInstance().removeChatListener(chatListener);
        return true;
    }

    // abstract ArrayList<BasicUser> getRectMsg();
    // abstract ArrayList<IMsg*>getCharMsgList(BasicUser user, IMsg msg, int nCount);


    /**
     * 获取所有的离线消息
     */
    public boolean getAllOfflineMessageSummery() {

        RabbitMQManager rabbitMQ = getRabbitMQManager();
        rabbitMQ.getAllOfflineMessageSummery();
        return true;
    }

    /**
     * 标记已读此用户的所有消息
     *
     * @param userId
     * @return
     */
    public boolean readUserMsg(long userId) {

        // 取得所以对方都没有确认已读消息的发一遍
        ArrayList<IMsg> arrMsgs = DatabaseFactory.getDBOper().getPeerUnReadMsgList(userId, getUserId());
        if (arrMsgs != null && arrMsgs.size() > 0) {
            ArrayList<IMsg> arrReadMsgs = new ArrayList();
            for (int i = 0; i < arrMsgs.size(); ++i) {
                IMsg msg = arrMsgs.get(i);

                // 针对 文本消息
                if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE) {
                    PeerRead peerRead = new PeerRead();  //TODO ：如果没有网络，则此消息会发送不出去
                    peerRead.setConfirmMsgID(msg.getMsgID());
                    peerRead.setrUID(msg.getsUID());
                    arrReadMsgs.add(peerRead);
                }

            }

            String routingKey = String.format("%s%d", RabbitMQManager.ROUTE_KEY_NAME_PREFIX, userId);
            HashMap<String, ArrayList<IMsg>> sendingMap = new HashMap();
            sendingMap.put(routingKey, arrReadMsgs);
            boolean baddtoqueue = batchReadMsgs(sendingMap);
            // Log.i(TAG, "_____readUserMsg " + baddtoqueue + " routing " + routingKey + " array " + arrReadMsgs);
        }


        /*
         // 取得所以对方都没有确认已读消息的发一遍
        ArrayList<IMsg> arrMsgs = DatabaseFactory.getDBOper().getPeerUnReadMsgList(userId, getUserId());
        if (arrMsgs != null)
        {
            RabbitMQManager rabbitMQ = getRabbitMQManager();
            for (int i = 0; i < arrMsgs.size(); ++i )
            {
                readMsg(arrMsgs.get(i));
            }
        }
        */


        return true;
    }

    /**
     * 标记用户又读了次消息
     *
     * @param userId
     * @return
     */
    public boolean markUserReadMsgsOnceMore(long userId) {
        return DatabaseFactory.getDBOper().markUserReadMsgsOnceMore(userId);

    }
//    /**
//     * 得到与某人的离线消息
//     * @return
//     */
//    public boolean getPeerOfflineMessage(long peerUserId)
//    {
//
//        RabbitMQManager rabbitMQ = getRabbitMQManager();
//        return rabbitMQ.getPeerOfflineMessage(peerUserId);
//    }

    /**
     * 得到与某人的缺失消息
     *
     * @return
     */
    public boolean getPeerMissMessage(long peerUserId, ArrayList<Long> arrMisMsgSeqs) {

        RabbitMQManager rabbitMQ = getRabbitMQManager();
        rabbitMQ.getPeerMissMessage(peerUserId, arrMisMsgSeqs);
        return false;
    }


    /**
     * 得到与某人最后的聊天记录
     */
    public ArrayList<IMsg> getPeerLastChatMsgList(final long userID, int nCount) {

        return DatabaseFactory.getDBOper().getPeerLastChatMsgList(getUserId(), userID, nCount);

    }


    /**
     * 得到与某人聊天记录
     */
    public ArrayList<IMsg> getPeerRecentChatRecordList(long userID) {
        return DatabaseFactory.getDBOper().getPeerRecentChatRecordList(getUserId(), userID);
    }


    /**
     * 得到某条消息前面的消息
     *
     * @param msg
     * @param userID
     * @param nCount
     * @return
     */
    public ArrayList<IMsg> getPeerMsgListBeforeMsg(IMsg msg, long userID, int nCount) {

        // 是否需要获取离线消息
        // 1,一天之内的消息 2，保存下来的消息 3，发送的消息对方没有已读 4，收到的消息没有标记为已读

        long onedayAgeTime = Utils.getCurrentServerTime() - 86400000;

        String strSql = String.format("SELECT * FROM tb_msgs WHERE (rUID = %d OR sUID = %d) AND (updateTime >= %d OR isDel = 2 OR sendStatus < %d OR recvStatus < %d) AND emptyMsg < 1 limit %d order by id ASC", getUserId(), userID, onedayAgeTime, IMsg.IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ, IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ, nCount);

        //String strSql = String.format("select * from tb_msgs where id in (select id from tb_msgs where (sUID = %d or sUID = %d ) and ( rUID = %d or rUID = %d ) and id < %d order by id DESC limit %d) order by id ASC", getUserId(), userID, getUserId(), userID, msg.getDBID(),  nCount);
        //Log.i("AAA", "SQL:" + strSql);

        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        //Log.i("AAA", "getPeerMsgListBeforeMsg SQL:" + strSql + " size:" + msgList.size());

        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;
        return resultList;

    }


    /**
     * 得到某条消息后面的消息
     *
     * @param msg
     * @param userID
     * @param nCount
     * @return
     */
    public ArrayList<IMsg> getPeerMsgListAfterMsg(IMsg msg, final long userID, int nCount) {

        String strSql = String.format("select * from tb_msgs where (sUID = %d or sUID = %d ) and ( rUID = %d or rUID = %d )  and id > %d limit %d", getUserId(), userID, getUserId(), userID, msg.getDBID(), nCount);

        //Log.i("AAA", "SQL:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null)
            msgList = new ArrayList<BasicMessage>();

        // Log.i("AAA", "getPeerMsgListAfterMsg SQL:" + strSql + " size:" + msgList.size());


        ArrayList<IMsg> resultList = (ArrayList<IMsg>) msgList;

        return resultList;
    }

    /**
     * 是否有未读消息
     *
     * @return
     */
    public boolean isHaveUnReadMsg() {
        //是否有为读消息
        String strSql = String.format("select * from tb_msgs where rUID = %d and recvStatus < %d", getUserId(), IMsg.IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ.GetValues());
        // Log.i("AAA", "SQL:" + strSql);
        List msgList = DatabaseFactory.getDBOper().getList(strSql, BasicMessage.class);
        if (msgList == null || msgList.size() == 0)
            return false;
        else {
            //Log.i("AAA", "isHaveUnReadMsg size:" + msgList.size());
            return true;
        }
    }


    /**
     * 检查是否有缺失的消息并取回来
     *
     * @param userID
     * @return
     */
    public void detectMissingAndGetMsgs(long userID) {

        final long fuserID = userID;
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {

                // 从 getUserMsgSummery 里寻找
                UserMsgSummery getUserS = null;
                List<UserMsgSummery> arrUserSummery = getUserMsgSummery();
                for (int i = 0; i < arrUserSummery.size(); ++i) {
                    UserMsgSummery temp = arrUserSummery.get(i);
                    if (fuserID == temp.getUser().getUserId()) {
                        getUserS = temp;
                        // Log.i("CRabbitMQChat", "detectMissingAndGetMsgs:aa " + fuserID + " " + getUserS.getUnReadNum());
                        break;
                    }
                }

                if (getUserS == null) return;

                // 确认有缺失消息
                if (getUserS.getUnReadNum() > 0) {
                    IMsg latestMsg = DatabaseFactory.getDBOper().getMaxSeqNumMsg(fuserID, getUserId());
                    if (latestMsg == null) {
                        latestMsg = new BasicMessage();
                        latestMsg.setsUID(fuserID);
                        latestMsg.setrUID(getUserId());
                        latestMsg.setMsgID("");
                    }

                    RabbitMQManager rabbitMQ = getRabbitMQManager();
                    if (rabbitMQ.getPeerOfflineMessage(fuserID, latestMsg.getMsgID())) {
                        getUserS.setUnReadNum(0);
                    }
                }
            }
        });

    }


    /**
     * 删除与某些人的聊天纪录
     *
     * @param userIds
     * @param myUserID
     * @return
     */
    public void deleteMsgsRecordByUserIds(final ArrayList<Long> userIds, final long myUserID, final String token) {
        // delete from database
        StringBuffer deleteSqlBuffuer = new StringBuffer("DELETE FROM tb_msgs WHERE ");
        String sql_OR_str = " OR ";
        long lcuurrentUserId = myUserID;
        for (int i = 0; i < userIds.size(); ++i) {
            Long lsenuserId = userIds.get(i);
            String addingStr = String.format("(sUID = %d AND rUID = %d) OR (sUID = %d AND rUID = %d)", lsenuserId, lcuurrentUserId, lcuurrentUserId, lsenuserId);
            deleteSqlBuffuer.append(addingStr);
            deleteSqlBuffuer.append(sql_OR_str);
        }

        deleteSqlBuffuer.delete(deleteSqlBuffuer.length() - sql_OR_str.length(), deleteSqlBuffuer.length());
        DatabaseFactory.getDBOper().execSQL(deleteSqlBuffuer.toString());

        // remove from memory
        List<UserMsgSummery> arrUserSummery = getUserMsgSummery();
        for (int i = 0; i < arrUserSummery.size(); ++i) {
            UserMsgSummery temp = arrUserSummery.get(i);

            for (int j = 0; j < userIds.size(); ++j) {
                Long lsenuserId = userIds.get(j);
                if (temp.getUser().getUserId() == lsenuserId) {
                    arrUserSummery.remove(i);
                    break;
                }
            }
        }

        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String url;
                if (Constants.DEBUG_MODE) {
                    url = Constants.DEBUG_DELETE_MSG_RECORD_URL + "token=" + token + "&rUID=" + myUserID;
                } else {
                    url = Constants.PRODUCT_DELETE_MSG_RECORD_URL + "token=" + token + "&rUID=" + myUserID;
                }
                for (int i = 0; i < userIds.size(); ++i) {
                    url += ("&sUID=" + userIds.get(i));
                }
                if (new HttpUtil().httpGetResultDirect(url, 5, "text/plain") == 0) {
                    Preferences.getInstacne().setValues(RabbitMQManager.LAST_GET_MSG_TIME, Utils.getCurrentServerTime());
                }
            }
        });

        // 需要通知否
        // notifyUserMsgSummeryChange(arrUserSummery);

    }

    /**
     * 预留函数 得到是否自动重发（已经发送失败的消息）
     */
    public boolean getNeedAutoResendMsg() {
        return getRabbitMQManager().getNeedAutoResendMsg();
    }

    /**
     * 预留函数 设置是否自动重发（已经发送失败的消息）
     */
    public void setNeedAutoResendMsg(boolean autoReSend) {
        getRabbitMQManager().setNeedAutoResendMsg(autoReSend);
    }


}
