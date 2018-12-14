package com.yeemos.app.chat.manager;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.gbsocial.BeanResponse.BasicResponseBean;
import com.gbsocial.BeansBase.BasicUser;
import com.gigabud.common.platforms.errorkey.PlatformErrorKeys;
import com.gigabud.common.platforms.utils.PreferencesWrapper;
import com.gigabud.core.http.HttpUtil;
import com.gigabud.core.http.RequestBean;
import com.gigabud.core.util.ConnectedUtil;
import com.gigabud.core.util.DeviceUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.gigabud.core.util.NetUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.AudioMessage;
import com.yeemos.app.chat.bean.BasicChatMessage;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.BroadcastMessage;
import com.yeemos.app.chat.bean.ClientStructureMessage;
import com.yeemos.app.chat.bean.FileURLMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.ImageMessage;
import com.yeemos.app.chat.bean.PeerRead;
import com.yeemos.app.chat.bean.PeerRecieved;
import com.yeemos.app.chat.bean.RPCBaseBean;
import com.yeemos.app.chat.bean.RPCSumaryBean;
import com.yeemos.app.chat.bean.ReceivedRead;
import com.yeemos.app.chat.bean.SendFailed;
import com.yeemos.app.chat.bean.ServerConfirm;
import com.yeemos.app.chat.bean.TextMessage;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.chat.bean.UserOffline;
import com.yeemos.app.chat.bean.UserOnline;
import com.yeemos.app.chat.bean.UserStartTyping;
import com.yeemos.app.chat.bean.UserStopTyping;
import com.yeemos.app.chat.bean.VideoMessage;
import com.yeemos.app.chat.offlineMsgBlocking.offlineMsgBlocking;
import com.yeemos.app.chat.offlineMsgBlocking.offlineMsgListener;
import com.yeemos.app.chat.rpcBean.RPCOfflineBean;
import com.yeemos.app.chat.rpcBean.RPCPeerOfflineBean;
import com.yeemos.app.chat.rpcBean.RPCResponseBaseBean;
import com.yeemos.app.chat.rpcBean.RPGetMissMsgBean;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.chat.bean.IMsg.IMSG_RECV_STATUS;
import com.yeemos.app.chat.bean.IMsg.IMSG_SEND_STATUS;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by apple on 12/9/15.
 */
public class RabbitMQManager {

//    send msg:
//    exchange name : exchange_topic_msg
//    client queue name : queue_topic_userid
//    server queue name : queue_topic_server
//    bind name : to.topic.userid
//    client send msg route key : to.topic.userid
//
//    rpc get offline msg :
//    server queue name : queue_rpc_server

    static private String ROUTE_KEY_NAME_SERVER = "queue_topic_server";
    static private String EXCHANGE_NAME = "exchange_topic_msg";
    static private String QUEUE_KEY_NAME_PREFIX = "queue_topic_";
    static public String ROUTE_KEY_NAME_PREFIX = "to.topic.";
    static private int AUTO_CHECK_SLEEP_TIME = 10 * 1000;

    static public String LAST_GET_MSG_TIME = "LAST_GET_MSG_TIME";
    static public String LAST_SERVER_TIME = "LAST_SERVER_TIME";
    static public String CURRENT_DEVICE_TIME = "CURRENT_DEVICE_TIME";

    private BlockingDeque<IMsg> queue = new LinkedBlockingDeque<IMsg>();
    private ArrayList<IChatListener> arrListeners = new ArrayList<IChatListener>();
    private ConnectionFactory factory = new ConnectionFactory();
    private long userID = -1;
    private boolean isConnectServer = false;

    private Connection connection = null;
    private Channel publishChannel = null;
    private QueueingConsumer queueingConsumer = null;
    private ShutdownListener shutdownListener = null;
    private String lmsgSend_expiration_time = "180000";   //默认消息保存在queue里的时间 为3分钟 （单位:ms）
    private String lspemsgSend_expiration_time = "3000";  //特殊消息 如 typing 消息不需要保存那么久

    private boolean autoReSend = false;
    private boolean autoack = false;
    private boolean b_shutdown = false;
    //  private Thread m_ampqthread = null;
    private boolean b_initAMQP = false;

    private offlineMsgBlocking m_offlineMsgBlocking = null;


    public RabbitMQManager() {
        m_offlineMsgBlocking = new offlineMsgBlocking();
        m_offlineMsgBlocking.setOfflineListener(new offlineMsgListener() {
            @Override
            public void offlineMsgBlocking_finished(ArrayList<String> userArr) {

                ArrayList<IChatListener> chatListeners = getChatListeners();
                for (int i = 0; i < chatListeners.size(); ++i) {
                    chatListeners.get(i).offlineMsgRcvd(userArr);
                }

            }
        });

        //       initAutoCheck();
    }

    private ArrayList<IChatListener> getChatListeners() {
        if (arrListeners == null)  //防止内存回收
        {
            arrListeners = new ArrayList<IChatListener>();
        }
        return arrListeners;
    }


    private void setUserID(long userID) {
        this.userID = userID;
    }

    private long getUserID() {
        return this.userID;
    }


    private BlockingDeque getMsgQueue() {
        if (queue == null) {
            queue = new LinkedBlockingDeque();
        }
        return queue;
    }

    public boolean addChatListener(IChatListener chatListener) {

        ArrayList<IChatListener> listeners = getChatListeners();
        listeners.add(chatListener);
        // TODO
        // Remove repeat

        return true;
    }


    /**
     * 自动检测的检测，检测是否有没有发送成功的消息等
     */
//    public void initAutoCheck() {
//        GBExecutionPool.getExecutor().execute(new Runnable() {
//            @Override
//            public void run() {
//
//                while (true) {
//                    //Log.i("initAutoCheck", "1initAutoCheck");
//                    try {
//                        Thread.sleep(4000);
//
//                        // Log.i("initAutoCheck", "initAutoCheck");
//                        ArrayList<IMsg> arrMsg = DatabaseFactory.getDBOper().getSendOvertimeMsgList(getUserID(), AUTO_CHECK_SLEEP_TIME);
//                        for (int i = 0; i < arrMsg.size(); ++i) {
//                            updateSendMessageStatus(arrMsg.get(i), IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);
//                        }
//
//                    } catch (Exception e) {
//                    }
//                }
//
//            }
//        });
//
//    }

    /**
     * 得到ConnectionFactory 实例，防止被内存回收
     *
     * @return
     */
    public ConnectionFactory getConnectionFactory() {
        if (factory == null) {
            Log.i("AAA", "factory is null");
            factory = new ConnectionFactory();
        } else {
            Log.i("AAA", "factory is not null");
        }

        //factory = new ConnectionFactory();
        factory.setAutomaticRecoveryEnabled(true);
        factory.setRequestedHeartbeat(120);
        factory.setShutdownTimeout(60);

        try {
            //      HTTP   http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/

            // Admin :http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com:15672/#/queues/vhost1/queue_topic_client

            //factory.setUri(uri);
            //factory.setUri("amqp://admin:admin@192.168.1.126:5672/testVirHost");
            //final URI uri = URI.create("amqp://admin:admin@rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com:5672/%2F");
            //    final URI uri = URI.create("amqp://admin:admin@rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com:5672/vhost1");

            final URI uri = URI.create(Constants.DEBUG_MODE ? "amqp://admin:admin@rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com:5672/vhost1" :
                    "amqp://yeemos_rbmq:P5vgNgNDA2ZB@rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com:5672/vhost1");
            factory.setUri(uri);
        } catch (KeyManagementException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
        return factory;

    }

    public void initAMQP() {
        if (!b_initAMQP) {
            b_initAMQP = true;

            // 整合到一个connection 里
            Log.i("one_connection", "initAMQP");
            //           publishMsgsLooping();
            //           receiveMsgsLooping();
//            threadInitAMQP();
            startMsgLooping();
        } else {
            reStartAMQP();
        }


    }

//    public void threadInitAMQP() {
//        Log.e("aaaaa", "threadInitAMQP");
//        GBExecutionPool.getExecutor().execute(new Runnable() {
//            @Override
//            public void run() {
//
//                while (b_initAMQP) {
//                    if (!isConnecting() && !b_shutdown) {
//                        try {
//                            Log.i("one_connection", "A1");
//
//                            if (NetUtil.isConnected(BaseApplication.getAppContext())) {
//
//
//                                // set offline init
//                                if (m_offlineMsgBlocking != null) {
//                                    m_offlineMsgBlocking.setFlag(offlineMsgBlocking.OFFLINE_STATE.OFFLINE_STATE_CONNECTING);
//                                }
//
//                                Log.i("one_connection", "getConnectionFactory");
//                                ConnectionFactory connectionFactory = getConnectionFactory();
//                                if (connectionFactory == null) {
//                                    // link error
//                                    throw new Exception("init connectionFactory error ");
//                                }
//
//                                // 清除之前的管道状态
//                                clearConnection();
//
//                                shutdownListener = new ShutdownListener() {
//
//                                    @Override
//                                    public void shutdownCompleted(ShutdownSignalException arg0) {
//
//                                        Log.i("one_connection", "Bshotdown: ShutdownSignalException " + arg0.getClass().getName());
//                                        // notice connection error!
//                                        notifyForConnectFailure();
//                                    }
//                                };
//
//                                connection = connectionFactory.newConnection();
//                                connection.addShutdownListener(shutdownListener);
//
//                                // listen to shutdown listener
//                                if (connection == null) continue;
//
//                                //如果是短暂的重连，是否还应该收取RPC消息(RPC消耗时间比较长)20160314 by jxq
//                                //再收前，先获得消息概述
//                                Log.i("one_connection", "getAllOfflineMessageSummery");
//                                notifyMessageSummeryChange(getAllOfflineMessageSummery());
//
//                                publishChannel = connection.createChannel();
//                                if (publishChannel.isOpen()) {
//                                    publishChannel.basicQos(1);
//                                }
//
//                                String routingKey = String.format("%s%d", ROUTE_KEY_NAME_PREFIX, getUserID());
//                                String queueName = String.format("%s%d", QUEUE_KEY_NAME_PREFIX, getUserID());
//
//                                DeclareOk q = publishChannel.queueDeclare(queueName, true, false, false, null);
//                                publishChannel.queueBind(queueName, EXCHANGE_NAME, routingKey);
//                                queueingConsumer = new QueueingConsumer(publishChannel) {
//                                    @Override
//                                    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
//
//                                        Log.e("one_connection", "Bshotdown: queueingConsumer handleShutdownSignal reason: " + sig.getReason() + "\na:" + sig.isHardError() + "\na:" + sig.getReference());
//
//                                        notifyForConnectFailure();
//                                        throw sig;
//                                    }
//                                };
//                                publishChannel.basicConsume(queueName, autoack, queueingConsumer);
//
//                                Log.i("one_connection", "routingKey:" + routingKey);
//
//                                // set offline init
//                                if (m_offlineMsgBlocking != null) {
//                                    m_offlineMsgBlocking.setFlag(offlineMsgBlocking.OFFLINE_STATE.OFFLINE_STATE_RECEIVING);
//                                }
//
//                                // ready for publishing
//                                // ready for receiving
//                                notifyForConnectSuccess();
//                                Log.i("one_connection", "notifyForConnectSuccess");
//
//                            } else {
//                                Thread.sleep(3000);
//                            }
//
//                        } catch (IOException ioe) {
//                            try {
//                                Log.e("one_connection", "B1 threadInitAMQP broken: " + ioe.getClass().getName());
//                                Thread.sleep(500); //sleep and then try again
//                                notifyForConnectFailure();
//                            } catch (InterruptedException e) {
//                                notifyForConnectFailure();
//                            }
//                        } catch (AlreadyClosedException cause) {
//                            clearConnection();
//                            notifyForConnectFailure();
//                            Log.e("one_connection", "B3 threadInitAMQP broken: " + cause.getClass().getName());
//
//                        } catch (Exception e) {
//                            notifyForConnectFailure();
//                            Log.e("one_connection", "B2 threadInitAMQP broken: " + e.getClass().getName());
//                        }
//
//                    }
//                }
//
//            }
//
//        });
//    }

    public void startMsgLooping() {
        GBExecutionPool.getExecutor().execute(new Runnable() {

            @Override
            public void run() {
                while (b_initAMQP) {

                    if (!NetUtil.isConnected(BaseApplication.getAppContext())) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    //自动检测的检测，检测是否有没有发送成功的消息等
                    ArrayList<IMsg> arrMsg = DatabaseFactory.getDBOper().getSendOvertimeMsgList(getUserID(), AUTO_CHECK_SLEEP_TIME);
                    int size = arrMsg.size();
                    if (size > 0) {
                        for (int i = 0; i < size; ++i) {
                            updateSendMessageStatus(arrMsg.get(i), IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);
                        }
                    }

                    if (!isConnecting() && !b_shutdown) {
                        try {
                            Log.i("one_connection", "A1");
                            if (NetUtil.isConnected(BaseApplication.getAppContext())) {
                                // set offline init
                                if (m_offlineMsgBlocking != null) {
                                    m_offlineMsgBlocking.setFlag(offlineMsgBlocking.OFFLINE_STATE.OFFLINE_STATE_CONNECTING);
                                }

                                Log.i("one_connection", "getConnectionFactory");
                                ConnectionFactory connectionFactory = getConnectionFactory();
                                if (connectionFactory == null) {
                                    throw new Exception("init connectionFactory error ");
                                }

                                // 清除之前的管道状态
                                clearConnection();
                                shutdownListener = new ShutdownListener() {

                                    @Override
                                    public void shutdownCompleted(ShutdownSignalException arg0) {

                                        Log.i("one_connection", "Bshotdown: ShutdownSignalException " + arg0.getClass().getName());
                                        // notice connection error!
                                        notifyForConnectFailure();
                                    }
                                };
                                connection = connectionFactory.newConnection();
                                connection.addShutdownListener(shutdownListener);

                                // listen to shutdown listener
                                if (connection == null) continue;

                                //如果是短暂的重连，是否还应该收取RPC消息(RPC消耗时间比较长)20160314 by jxq
                                //再收前，先获得消息概述
                                Log.i("one_connection", "getAllOfflineMessageSummery");
                                notifyMessageSummeryChange(getAllOfflineMessageSummery());
                                publishChannel = connection.createChannel();
                                if (publishChannel.isOpen()) {
                                    publishChannel.basicQos(1);
                                }

                                String routingKey = String.format("%s%d", ROUTE_KEY_NAME_PREFIX, getUserID());
                                String queueName = String.format("%s%d", QUEUE_KEY_NAME_PREFIX, getUserID());

                                DeclareOk q = publishChannel.queueDeclare(queueName, true, false, false, null);
                                publishChannel.queueBind(queueName, EXCHANGE_NAME, routingKey);
                                queueingConsumer = new QueueingConsumer(publishChannel) {
                                    @Override
                                    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

                                        Log.e("one_connection", "Bshotdown: queueingConsumer handleShutdownSignal reason: " + sig.getReason() + "\na:" + sig.isHardError() + "\na:" + sig.getReference());

                                        notifyForConnectFailure();
                                        throw sig;
                                    }
                                };
                                publishChannel.basicConsume(queueName, autoack, queueingConsumer);

                                Log.i("one_connection", "routingKey:" + routingKey);

                                if (m_offlineMsgBlocking != null) {
                                    m_offlineMsgBlocking.setFlag(offlineMsgBlocking.OFFLINE_STATE.OFFLINE_STATE_RECEIVING);
                                }
                                // ready for publishing
                                // ready for receiving
                                notifyForConnectSuccess();
                                Log.i("one_connection", "notifyForConnectSuccess");

                            } else {
                                Thread.sleep(3000);
                            }

                        } catch (IOException ioe) {
                            try {
                                Log.e("one_connection", "B1 threadInitAMQP broken: " + ioe.getClass().getName());
                                Thread.sleep(500); //sleep and then try again
                                notifyForConnectFailure();
                            } catch (InterruptedException e) {
                                notifyForConnectFailure();
                            }
                        } catch (AlreadyClosedException cause) {
                            clearConnection();
                            notifyForConnectFailure();
                            Log.e("one_connection", "B3 threadInitAMQP broken: " + cause.getClass().getName());
                        } catch (Exception e) {
                            notifyForConnectFailure();
                            Log.e("one_connection", "B2 threadInitAMQP broken: " + e.getClass().getName());
                        }
                    }

                    if (!isConnecting()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        continue;
                    }

                    if (getMsgQueue().size() > 0) {    //publishMsgsLooping
                        try {
                            Object sendingOb = getMsgQueue().takeFirst();
                            IMsg message = null;
                            if (NetUtil.isConnected(BaseApplication.getAppContext())) {
                                // 有网络
                                try {
                                    // 单个消息发送
                                    if (sendingOb instanceof IMsg) {
                                        message = (IMsg) sendingOb;  //block until queue hava data
                                        // 发送给服务器的一些(已读，确认已读)状态消息 routingKey 放在 msg text 里
                                        String routingKey = String.format("%s%d", ROUTE_KEY_NAME_PREFIX, message.getrUID());
                                        if (message.getMessageType() == IMsg.MES_TYPE.PEER_READ_MSG_TYPE ||
                                                message.getMessageType() == IMsg.MES_TYPE.PEER_RCVD_READ_MSG_TYPE) {
                                            if (message.getText() != null)
                                                routingKey = message.getText();
                                        }

                                        String sendString = getSendStringFromMsg(message, true);
                                        String msg_expir = (message.getMessageType() == IMsg.MES_TYPE.TYPING_SEND_MSG_TYPE || message.getMessageType() == IMsg.MES_TYPE.STOP_TYPING_SEND_MSG_TYPE) ? lspemsgSend_expiration_time : lmsgSend_expiration_time;
                                        AMQP.BasicProperties msgpro = new AMQP.BasicProperties().builder().expiration(msg_expir).deliveryMode(1).contentEncoding("UTF-8").build();
                                        publishChannel.basicPublish(EXCHANGE_NAME, routingKey, msgpro, sendString.getBytes("UTF-8"));

                                        Log.d("one_connection", "Message Type:" + message.getMessageType().GetValues() + "\n send: " + sendString + "\n routingKey: " + routingKey);

                                        updateSendMessageStatus(message, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SENDING);

                                        // 通知外面
                                        notifySendingMsg(message);
                                    } else {
                                        // 批量发送simida
                                        HashMap<String, ArrayList<IMsg>> batchMsgsMapping = (HashMap<String, ArrayList<IMsg>>) sendingOb;
                                        Iterator entries = batchMsgsMapping.entrySet().iterator();

                                        while (entries.hasNext()) {
                                            HashMap.Entry<String, ArrayList<IMsg>> entry = (HashMap.Entry<String, ArrayList<IMsg>>) entries.next();

                                            String routingKey = entry.getKey();
                                            ArrayList<IMsg> sendingMsgArr = entry.getValue();

                                            StringBuffer sendingStringBuffuer = new StringBuffer("[");
                                            for (int i = 0; i < sendingMsgArr.size(); ++i) {
                                                IMsg sendingmsg = sendingMsgArr.get(i);
                                                String sendString = getSendStringFromMsg(sendingmsg, false);
                                                sendingStringBuffuer.append(sendString);
                                                sendingStringBuffuer.append(",");

                                                updateSendMessageStatus(sendingmsg, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SENDING);
                                            }
                                            sendingStringBuffuer.deleteCharAt(sendingStringBuffuer.length() - 1);
                                            sendingStringBuffuer.append("]");
                                            byte[] messageBodyBytes = sendingStringBuffuer.toString().getBytes("UTF-8");
                                            AMQP.BasicProperties msgpro = new AMQP.BasicProperties().builder().expiration(lmsgSend_expiration_time).deliveryMode(1).contentEncoding("UTF-8").build();

                                            publishChannel.basicPublish(EXCHANGE_NAME, routingKey, null, messageBodyBytes);

                                        }

                                    }

                                } catch (Exception e) {
                                    Log.d("one_connection", "Send message Exception " + e.toString());
                                    if (null != message && 0 == message.getRetryNums()) {
                                        message.setRetryNums(message.getRetryNums() + 1);
                                        DatabaseFactory.getDBOper().update(message);
                                        getMsgQueue().putFirst(message);
                                    } else {
                                        updateSendMessageStatus(message, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);
                                        throw e;
                                    }
                                }
                            } else {
                                //无网络
                                if (getNeedAutoResendMsg())  //需要自动重发
                                {
                                    updateSendMessageStatus(message, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);
                                    getMsgQueue().putFirst(message);
                                }
                            }
                        } catch (InterruptedException e) {
                            Log.e("one_connection", "B1 publish Connection broken: " + e.getClass().getName());
                        } catch (Exception e) {
                            notifyForConnectFailure();
                            Log.e("one_connection", "B2 publish Connection broken: " + e.getClass().getName());
                        }
                    }
        /*receiveMsgsLooping*/
                    if (NetUtil.isConnected(BaseApplication.getAppContext())) {
                        try {
                            QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery(3000); // block until receive message
                            Preferences.getInstacne().setValues(LAST_SERVER_TIME, delivery.getProperties().getTimestamp().getTime());
                            Preferences.getInstacne().setValues(CURRENT_DEVICE_TIME, System.currentTimeMillis());
        //                    Log.e("aaaaaa",delivery.getProperties().getTimestamp().getTime()+", "+System.currentTimeMillis());
                            String message = new String(delivery.getBody(), Charset.forName("UTF-8"));
                            if (!TextUtils.isEmpty(message)) {
                                try {
                                    Type listType = new TypeToken<ArrayList<ClientStructureMessage>>() {
                                    }.getType();
                                    List<ClientStructureMessage> arryMsgs = new Gson().fromJson(message, listType);
                                    if (arryMsgs.size() > 0) {
                                        receiveMsgs(arryMsgs, true);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.i("one_connection", "parse data exception:" + message);

                                } finally {
                                    if (!autoack) {
                                        publishChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                                    }
                                }
                            }
                        } catch (ShutdownSignalException se) {
                            Log.e("one_connection", "A Connection broken: ShutdownSignalException " + se.getClass().getName());
                            notifyForConnectFailure();
                        } catch (Exception e1) {
                            continue;
                        }
                    }
                }

            }


        });

    }


//    public void receiveMsgsLooping() {
//        Log.e("aaaaa", "receiveMsgsLooping");
//        GBExecutionPool.getExecutor().execute(new Runnable() {
//
//            @Override
//            public void run() {
//
//                while (b_initAMQP) {
//                    if (!isConnecting()) continue;
//
//                    try {
//
//                        QueueingConsumer.Delivery delivery = queueingConsumer.nextDelivery(3000); // block until receive message
//
//                        Preferences.getInstacne().setValues(LAST_SERVER_TIME, delivery.getProperties().getTimestamp().getTime());
//                        Preferences.getInstacne().setValues(CURRENT_DEVICE_TIME, System.currentTimeMillis());
//                        String message = new String(delivery.getBody(), Charset.forName("UTF-8"));
//                        Log.i("one_connection", "recv body: " + message);
//                        try {
//                            Type listType = new TypeToken<ArrayList<ClientStructureMessage>>() {
//                            }.getType();
//                            List<ClientStructureMessage> arryMsgs = new Gson().fromJson(message, listType);
//                            receiveMsgs(arryMsgs, true);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            Log.i("one_connection", "parse data exception:" + message);
//
//                        } finally {
//
//                            if (!autoack) {
//                                // ack to server since autoack = false
//                                publishChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//                            }
//                        }
//
//
//                    } catch (ShutdownSignalException se) {
//                        Log.e("one_connection", "A Connection broken: ShutdownSignalException " + se.getClass().getName());
//                        notifyForConnectFailure();
//                    } catch (Exception e1) {
//                        continue;
//                    }
//
//                }
//            }
//
//
//        });
//    }

    // 人为关闭连接
    public void shutDownAMQP() {
        b_shutdown = true;
        notifyForConnectFailure();
        clearConnection();
    }

    // 重启连接
    public void reStartAMQP() {
        b_shutdown = false;
        notifyForConnectFailure();
    }


    public void clearConnection() {

        try {
            // 清除之前的管道状态

            boolean bisnetwork = NetUtil.isConnected(BaseApplication.getAppContext());

            if (publishChannel != null) {
                publishChannel.clearConfirmListeners();
                if (bisnetwork && publishChannel.isOpen()) publishChannel.close();
                publishChannel = null;
            }


            if (connection != null) {
                connection.removeShutdownListener(shutdownListener);
                connection.clearBlockedListeners();
                if (bisnetwork) connection.close();
                connection = null;
            }


        } catch (Exception ioe) {
            //ioe.printStackTrace();
        }

    }


    /**
     * 预留函数 得到是否自动重发（已经发送失败的消息）
     */
    public boolean getNeedAutoResendMsg() {
        return this.autoReSend;
    }

    /**
     * 预留函数 设置是否自动重发（已经发送失败的消息）
     */
    public void setNeedAutoResendMsg(boolean autoReSend) {
        this.autoReSend = autoReSend;
    }


    /**
     * 修改发送消息的状态
     *
     * @param msg
     * @param status
     */
    private void updateSendMessageStatus(IMsg msg, IMSG_SEND_STATUS status) {
        boolean bFlag = msg.updateSendStatus(status); //修改DB状态
        if (bFlag) {

            // failed meesage
            if (IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE == status) {
                SendFailed sendFailed = new SendFailed();
                sendFailed.setConfirmMsgID(msg.getMsgID());
                notifyRecvMsg(sendFailed); // 通知UI更新状态
            }

            //IMSG_SEND_STATUS.IMSG_SEND_STATUS_SENDING


            // notice sending msg state


        } else {
            Log.i("RabbitMQManager", "updateSendMessageStatus failed" + msg.toChatJson() + "status:" + status);
        }

    }


    /**
     * 收到消息数组
     *
     * @param arryMsgs
     * @param isCheckMissMsg 标记收到消息后，如发现缺失的，是否要去获取
     */
    public void receiveMsgs(List<ClientStructureMessage> arryMsgs, boolean isCheckMissMsg) {
        for (int i = 0; arryMsgs != null && i < arryMsgs.size(); ++i) {
            receiveMsg(arryMsgs.get(i), isCheckMissMsg);
        }

    }

    /**
     * 检查缺少的消息，如果有就在数据库上为它预留位置
     */
    private void checkIfMissMsg(IMsg incomingmsg) {
        if (incomingmsg.getSendSeqNum() == null) return;

        String[] incominSeqNums = incomingmsg.getSendSeqNum().split("_");
        IMsg latestMsg = DatabaseFactory.getDBOper().getMaxSeqNumMsg(incomingmsg.getsUID(), incomingmsg.getrUID());
        String[] latesSeqNums = null;
        if (latestMsg != null) {
            latesSeqNums = latestMsg.getSendSeqNum().split("_");
        }

        if (latesSeqNums == null || latesSeqNums.length != 2) {
            // 从来没有过消息 则预留一些位置给有可能排在前面的消息
            if (incominSeqNums.length == 2) {
                int incomingSeqnum = Integer.parseInt(incominSeqNums[1]);
                if (incomingSeqnum > 1) {
                    preInsertApositionMsgIntoDb(incominSeqNums[0], incominSeqNums[1], incomingSeqnum - 1, -1, incomingmsg.getsUID(), incomingmsg.getrUID());
                }
            }
        } else {
            if (incominSeqNums.length == 2) {
                // 批次一样
                int incomingSeqnum = Integer.parseInt(incominSeqNums[1]);

                if (latesSeqNums[0].equals(incominSeqNums[0])) {
                    int latesSeqnum = Integer.parseInt(latesSeqNums[1]);
                    if (incomingSeqnum > latesSeqnum + 1) {
                        preInsertApositionMsgIntoDb(incominSeqNums[0], incominSeqNums[1], incomingSeqnum - (latesSeqnum + 1), -1, incomingmsg.getsUID(), incomingmsg.getrUID());
                    }
                } else {
                    // 预留5个消息位置给旧批次
                    preInsertApositionMsgIntoDb(latesSeqNums[0], latesSeqNums[1], 5, 1, incomingmsg.getsUID(), incomingmsg.getrUID());

                    // 再预留消息位置给新的
                    if (incomingSeqnum > 1) {
                        preInsertApositionMsgIntoDb(incominSeqNums[0], incominSeqNums[1], incomingSeqnum - 1, -1, incomingmsg.getsUID(), incomingmsg.getrUID());
                    }
                }
            }
        }

    }

    /**
     * 在数据库上预留位置
     */
    private void preInsertApositionMsgIntoDb(String batchNum, String beginSeqNum, int nInsertNum, int nIncreaseFlag, long sUID, long rUID) {
        int ntruelyInsertNum = nInsertNum > 30 ? 30 : nInsertNum;
        int n_begin_seqNum = 0;
        int n_end_seqNum = 0;

        if (nIncreaseFlag > 0) {
            // 自然递增
            n_begin_seqNum = Integer.parseInt(beginSeqNum) + 1;
            n_end_seqNum = Integer.parseInt(beginSeqNum) + ntruelyInsertNum;

        } else {
            // 递减
            n_begin_seqNum = Integer.parseInt(beginSeqNum) - ntruelyInsertNum;
            n_begin_seqNum = n_begin_seqNum >= 1 ? n_begin_seqNum : 1;

            n_end_seqNum = Integer.parseInt(beginSeqNum) - 1;
            n_end_seqNum = n_end_seqNum >= 1 ? n_end_seqNum : 1;
        }

        for (int i = n_begin_seqNum; i <= n_end_seqNum; i++) {
            BasicChatMessage baseChatMsg = new BasicChatMessage();
            baseChatMsg.setsUID(sUID);
            baseChatMsg.setrUID(rUID);
            baseChatMsg.setRecvStatus(IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_FAILURE);
            baseChatMsg.setSendSeqNum(String.format("%s_%d", batchNum, i));

            DatabaseFactory.getDBOper().insertAPositionMsg(baseChatMsg);
        }

    }


    /**
     * 收到消息
     *
     * @param struckMsg:数据交互的数据结构
     */
    public void receiveMsg(ClientStructureMessage struckMsg, boolean isCheckMissMsg) {
        //Log.i("RabbitMQManager", "receiveMsg:");
        TextMessage textMessage = struckMsg.getTextMessage();
        if (textMessage != null) {
            // Log.i("RabbitMQManager", "receiveMsg textMssage:");
            ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
            if (allFriends != null && !allFriends.isEmpty()) {
                boolean isFriend = false;
                for (BasicUser basicUser : allFriends) {
                    if (basicUser.getUserId().equals(String.valueOf(textMessage.getsUID())) && basicUser.getFollowedStatus() == 1 && basicUser.getFollowStatus() == 1) {
                        isFriend = true;
                        break;
                    }
                }
                if (!isFriend) {
                    //              DataManager.getInstance().getAllFriends(false);
                    return;
                }
            }
            if (isCheckMissMsg) {
                // get miss message:
                checkIfMissMsg(textMessage);
            }
            textMessage.setEmptyMsg(0);
            DatabaseFactory.getDBOper().insertOrUpdateRecvMsg(textMessage);
            textMessage.updateRecvStatus(IMSG_RECV_STATUS.IMSG_RECV_STATUS_WAIT_RECV);
            textMessage.analystReadIds();
            notifyRecvMsg(textMessage);
        }


        ServerConfirm serverConfirm = struckMsg.getServerConfirm();
        if (serverConfirm != null) {
            //表明消息成功发送到服务器
            serverConfirm.updateSendStatus(IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS);
            notifyRecvMsg(serverConfirm);
        }

        PeerRecieved peerRecvMsg = struckMsg.getPeerRecieved();
        if (peerRecvMsg != null) {
            //表明消息成功发送到对方 （即对方已收））
            peerRecvMsg.updateSendStatus(IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_RECEIVED);
            peerRecvMsg.analystReadIds();
            notifyRecvMsg(peerRecvMsg);
        }

        PeerRead peerRead = struckMsg.getPeerRead();
        if (peerRead != null) {
            //对方已读
            peerRead.updateSendStatus(IMSG_SEND_STATUS.IMSG_SEND_STATUS_PEER_READ);
            peerRead.analystReadIds();
            notifyRecvMsg(peerRead);

            // 需要告诉服务器 和 对方 我已经确认收到 已读状态 消息
            // 发送给服务器
            ReceivedRead receivedRead = new ReceivedRead();
            receivedRead.setConfirmMsgID(peerRead.getConfirmMsgID());
            receivedRead.setText(ROUTE_KEY_NAME_SERVER);
            this.sendMsg(receivedRead);

            // 发送给对方
            BasicMessage bmsg = receivedRead.getPeerUId();
            if (bmsg != null) {
                ReceivedRead toPeerRcvdRead = new ReceivedRead();
                toPeerRcvdRead.setConfirmMsgID(peerRead.getConfirmMsgID());
                long lpeerUid = getUserID() == bmsg.getrUID() ? bmsg.getsUID() : bmsg.getrUID();
                String routingKey = String.format("%s%d", ROUTE_KEY_NAME_PREFIX, lpeerUid);
                toPeerRcvdRead.setText(routingKey);
                toPeerRcvdRead.setConfirmReadIds(peerRead.getReadIds());
                //Log.i("RabbitMQManager", "set routingKey: " + routingKey + " msgid" + toPeerRcvdRead.getConfirmMsgID() + "userid" + getUserID());
                this.sendMsg(toPeerRcvdRead);
            }
        }

        ReceivedRead receivedRead = struckMsg.getReceivedRead();
        if (receivedRead != null) {
            // Log.i("RabbitMQManager", "get receivedRead confirmid" + receivedRead.getConfirmMsgID() + "msgid" + receivedRead.getMsgID());
            // 表明对方已经收到我发的已读状态）
            receivedRead.updateRecvStatus(IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_READ_CONFIRM);
            receivedRead.analystReadIds();
            notifyRecvMsg(receivedRead);

        }


        AudioMessage audioMessage = struckMsg.getAudioMessage();
        if (audioMessage != null) {

        }

        VideoMessage videoMsg = struckMsg.getVideoMessage();
        if (videoMsg != null) {

        }

        ImageMessage imgMsg = struckMsg.getImageMessage();
        if (imgMsg != null) {

        }

        FileURLMessage fileURLMessage = struckMsg.getFileURLMessage();
        if (fileURLMessage != null) {
            ArrayList<BasicUser> allFriends = DataManager.getInstance().getAllFriends(true);
            if (allFriends != null && !allFriends.isEmpty()) {
                boolean isFriend = false;
                for (BasicUser basicUser : allFriends) {
                    if (basicUser.getUserId().equals(String.valueOf(fileURLMessage.getsUID())) && basicUser.getFollowedStatus() == 1 && basicUser.getFollowStatus() == 1) {
                        isFriend = true;
                        break;
                    }
                }
                if (!isFriend) {
                    //   DataManager.getInstance().getAllFriends(false);
                    return;
                }
            }
            if (isCheckMissMsg) {
                // get miss message:
                checkIfMissMsg(fileURLMessage);
            }

            String[] filename = fileURLMessage.getfName().split("\\.");

            int length = filename.length;
            String thumb = "";
            for (int i = 0; i < length - 1; ++i) {
                thumb += filename[i];
            }
            thumb += ("_s." + filename[length - 1]);
            fileURLMessage.setThumb(thumb);
            fileURLMessage.setEmptyMsg(0);

            //Log.i("RabbitMQManager", "receive file message:");
            DatabaseFactory.getDBOper().insertOrUpdateRecvMsg(fileURLMessage);
            fileURLMessage.updateRecvStatus(IMSG_RECV_STATUS.IMSG_RECV_STATUS_WAIT_RECV);
            fileURLMessage.analystReadIds();

            //下载文件
//            FileHtttpManager.getInstance().downloadMsg( fileURLMessage );

//            // 发送已收到的confim消息
//            PeerRecieved peerRecv = new PeerRecieved();
//            peerRecv.setrUID(fileURLMessage.getsUID());
//            peerRecv.setConfirmMsgID(fileURLMessage.getMsgID());
//            this.sendMsg(peerRecv);


            notifyRecvMsg(fileURLMessage);
        }


        UserOffline offLineMsg = struckMsg.getUserOffline();
        if (offLineMsg != null) {

        }

        UserOnline onLineMsg = struckMsg.getUserOnline();
        if (onLineMsg != null) {

        }


        UserStartTyping userStartTyping = struckMsg.getUserStartTyping();
        if (userStartTyping != null) {
            //Log.i("RabbitMQManager", "Receive userStartTyping");
            notifyRecvMsg(userStartTyping);

        }

        UserStopTyping userStopTyping = struckMsg.getUserStopTyping();
        if (userStopTyping != null) {
            //Log.i("RabbitMQManager", "Receive userStopTyping");
            notifyRecvMsg(userStopTyping);
        }

        BroadcastMessage broadcastMessage = struckMsg.getBroadcastMessage();
        if (broadcastMessage != null) {
            if (broadcastMessage.getBtype() == 1 && !broadcastMessage.getText().equals(DeviceUtil.getDeviceId(BaseApplication.getAppContext()))) {  //表示被踢出登陆
                BaseApplication.getCurFragment().errorCodeDo(PlatformErrorKeys.CODE_TOKEN_TIME_EXPIRATION);
                return;
            }
            notifyRecvMsg(broadcastMessage);
        }
    }

    private String getSendStringFromMsg(IMsg msg, boolean needaFlag) {
        IMsg.MES_TYPE type = msg.getMessageType();

        //Log.i("AAA", "Message type:" + type.GetValues());
        String strKey = "";
        switch (type) {
            case TEXT_MSG_TYPE:
                strKey = "textMessage";
                break;
            case IMAGE_MSG_TYPE:
                strKey = "imageMessage";
                break;
            case VIDEO_MSG_TYPE:
                strKey = "videoMessage";
                break;
            case ADUID_MSG_TYPE:
                strKey = "audioMessage";
                break;
            case PEER_RECV_MSG_TYPE:
                strKey = "peerRecieved";
                break;
            case PEER_READ_MSG_TYPE:
                strKey = "peerRead";
                break;
            case PEER_RCVD_READ_MSG_TYPE:
                strKey = "receivedRead";
                break;
            case TYPING_SEND_MSG_TYPE:
                strKey = "userStartTyping";
                break;
            case STOP_TYPING_SEND_MSG_TYPE:
                strKey = "userStopTyping";
                break;
            case BROADCAST_MSG_TYPE:
                strKey = "broadcastMessage";
                break;
        }

        String sendString = "";
        if (needaFlag) {
            sendString = String.format("[{\"%s\":%s}]", strKey, msg.toChatJson());
        } else {
            sendString = String.format("{\"%s\":%s}", strKey, msg.toChatJson());
        }

        //Log.i("AAA", "Message type:" + type.GetValues() + "getSendStringFromMsg:" + sendString);
        return sendString;
    }

    public void connect(String token, long userID) {
        // begin connect
        setUserID(userID);
        notifyForBeginConnect();
        initAMQP();
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


    public void getOfflineNotifyMessageSummeryChange() {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                notifyMessageSummeryChange(getAllOfflineMessageSummery());
            }
        });
    }


    /**
     * 通知Listener 收到新消息
     *
     * @param msg
     */
    private boolean notifyRecvMsg(IMsg msg) {
        if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.ADUID_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.VIDEO_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.IMAGE_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
            m_offlineMsgBlocking.addOfflineUser(String.valueOf(msg.getsUID()));
        }
        // 还处在离线消息的接收中，则暂不通知
        if (m_offlineMsgBlocking.offlineFlag() != offlineMsgBlocking.OFFLINE_STATE.OFFLINE_STATE_RECEIVING) {
            ArrayList<IChatListener> chatListeners = getChatListeners();
            for (int i = 0; i < chatListeners.size(); ++i) {
                chatListeners.get(i).receiveMsg(msg);
            }

            return chatListeners.size() >= 2;
        }

        return true;

    }

    /**
     * 通知Listener 收到新消息
     *
     * @param msg
     */
    private void notifySendingMsg(IMsg msg) {
        if (msg.getMessageType() == IMsg.MES_TYPE.TEXT_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.ADUID_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.VIDEO_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.IMAGE_MSG_TYPE ||
                msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {
            ArrayList<IChatListener> chatListeners = getChatListeners();
            for (int i = 0; i < chatListeners.size(); ++i) {
                chatListeners.get(i).sendingMsg(msg);
            }
        }

    }


    private void notifyForBeginConnect() {
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).beginConnect();
        }
    }

    private void notifyForConnectSuccess() {

        isConnectServer = true;

        Log.i("RabbitMQManager", "connect success:");
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).connectSuccess();
        }
    }

    private void notifyForConnectFailure() {

        BlockingDeque<IMsg> quque = getMsgQueue();
        for (int i = 0; i < quque.size(); ++i) {
            //quque.removeFirst()

        }
        isConnectServer = false;
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).connectFailure();
        }


    }

    private void notifyForDisconnect() {
        isConnectServer = false;
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).disconnect();
        }
    }


    public boolean disconnect() {
        try {
            shutDownAMQP();

            if (publishChannel != null) {
                publishChannel.close();
            }

            if (connection != null) {
                connection.close();
            }

            if (factory != null) {
                factory = null;
            }
        } catch (Exception e) {
            Log.i("AAA", "Disconnect failed");
        }
        notifyForDisconnect();
        return true;
    }

    public boolean isConnecting() {
        return isConnectServer;
    }


    public boolean sendMsg(IMsg msg) {

        if (NetUtil.isConnected(BaseApplication.getAppContext())) {
            if (msg instanceof TextMessage || msg instanceof FileURLMessage) {
                // 有网络
                httpSendMsg(msg);
            }
            // 加到发送的消息队列中，等连接上的时候，只自动发送消息
            return this.getMsgQueue().add(msg);
        } else {
            Log.i("RabbitMQManager", "sendMsg: no network");
            // 无网络。则立即通知UI发送失败
            updateSendMessageStatus(msg, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);

            //是否需要自动发送
            if (getNeedAutoResendMsg()) {
                return this.getMsgQueue().add(msg);
            }
            return false;
        }

    }

    private void httpSendMsg(final IMsg msg) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String requesturl;
                if (Constants.DEBUG_MODE) {
                    requesturl = Constants.DEBUG_SEND_MSG_URL;
                } else {
                    requesturl = Constants.PRODUCT_SEND_MSG_URL;
                }
                String postBody = getSendStringFromMsg(msg, true);
                RequestBean request = new RequestBean(requesturl, postBody, 10, 10,
                        BasicResponseBean.class, RequestBean.HttpMethod.POST);

                HttpUtil httpUtil = new HttpUtil();
                if (httpUtil.httpPost(request.getUrl(), request.getData(),
                        request.getAppType(), request.getTimeout(), null) == 0) {
                    updateSendMessageStatus(msg, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS);
                }
            }
        });
    }

    public boolean batchsendMsgs(HashMap<String, ArrayList<IMsg>> sendMapping) {
        if (NetUtil.isConnected(BaseApplication.getAppContext())) {
            // 有网络
            // 加到发送的消息队列中，等连接上的时候，只自动发送消息
            return this.getMsgQueue().add(sendMapping);
        } else {
            // 无网络。则立即通知UI发送失败
            for (Object msgarrOb : sendMapping.values()) {
                ArrayList<IMsg> msgsArray = (ArrayList<IMsg>) msgarrOb;
                for (int i = 0; i < msgsArray.size(); ++i) {
                    IMsg sendingmsg = msgsArray.get(i);
                    updateSendMessageStatus(sendingmsg, IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);
                }
            }

            //是否需要自动发送
            if (getNeedAutoResendMsg()) {
                return this.getMsgQueue().add(sendMapping);
            }
            return false;
        }

    }


    /**
     * 标记已读此用户的所有消息
     *
     * @param userId
     * @return
     */
    public boolean readUserMsg(long userId) {
        if (isConnecting()) {
            // get all unread msgs
        }
        return true;
    }

    /**
     * http & rpc Api 请求
     *
     * @param apiName,postBodyBean,loopTimes
     * @return
     */
    private String im_API_httpAndRpc_Requesting(String apiName, String beanName, RPCBaseBean postBodyBean, int loopTimes) {
        if (!NetUtil.isConnected(BaseApplication.getAppContext())) {
            return null;
        }

        String responseStr = null;

        String requesturl = String.format("%s/%s", Constants.DEBUG_MODE ? Constants.DEBUG_YEEMOS_URL : Constants.PRODUCT_YEEMOS_URL, apiName);
        String postBody = String.format("{\"%s\":%s}", beanName, postBodyBean.toJson());

        int result = 0;
        for (int i = 0; i < loopTimes; ++i) {
            // first using http request
            RequestBean request = new RequestBean(requesturl, postBody, 10, 10,
                    BasicResponseBean.class, RequestBean.HttpMethod.POST);

            HttpUtil httpUtil = new HttpUtil();
            result = httpUtil.httpPost(request.getUrl(), request.getData(),
                    request.getAppType(), request.getTimeout(), null);

            // second using rpc way if first one failed!
            if (result != 0) {
                try {
                    RPCClient rpcClient = new RPCClient(connection, getUserID());
                    //RPCClient rpcClient = new RPCClient(null, getUserID()); // 针对rpc使用新的connection
                    responseStr = rpcClient.call(postBody);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                responseStr = httpUtil.getResponseStr();
            }

            // detect to break;
            if (responseStr != null) {
                break;
            }
        }

        Log.i("RabbitMQManager", "apiName:" + apiName + " beanName:" + beanName + "\npostBody:" + postBody + "\nresult:" + result + "\nresponseBody:" + responseStr);

        return responseStr;
    }


    /**
     * 得到离线消息的概述
     *
     * @return
     */
    public List<UserMsgSummery> getAllOfflineMessageSummery() {

        RPCSumaryBean sumaryBean = new RPCSumaryBean();
        sumaryBean.setsUID(getUserID());
        sumaryBean.setSearchUserId(getUserID());
        String responseBody = im_API_httpAndRpc_Requesting("getOfflineMsgSummary.do", "offlineMsgSummary", sumaryBean, 2);
        if (responseBody != null) {
            Type clsType = new TypeToken<RPCResponseBaseBean<ArrayList<RPCOfflineBean>>>() {
            }.getType();
            RPCResponseBaseBean<ArrayList<RPCOfflineBean>> responseBaseBean = null;
            try {
                responseBaseBean = new Gson().fromJson(responseBody, clsType);
            } catch (Exception e) {
                return new ArrayList<UserMsgSummery>();
            }
            if (responseBaseBean != null && responseBaseBean.getSuccess() == 1) {
                List tempList = responseBaseBean.getData();
                List<UserMsgSummery> listUserSummer = (List<UserMsgSummery>) tempList;

                return listUserSummer;
            } else {
                return new ArrayList<UserMsgSummery>();
            }
        } else {
            return new ArrayList<UserMsgSummery>();
        }

    }

    /**
     * 得到与某人的离线消息
     *
     * @return
     */
    public boolean getPeerOfflineMessage(long peerUserId, String lastMsgID) {

        boolean bsuccess = false;
        RPCPeerOfflineBean rpcPeerOfflineBaseBean = new RPCPeerOfflineBean();
        rpcPeerOfflineBaseBean.setrUID(getUserID());
        rpcPeerOfflineBaseBean.setsUID(peerUserId);
        if (!TextUtils.isEmpty(lastMsgID)) {
            rpcPeerOfflineBaseBean.setMsgID(lastMsgID);
        }

        String responseBody = im_API_httpAndRpc_Requesting("getPeerOfflineMessages.do", "peerOfflineMessages", rpcPeerOfflineBaseBean, 2);
        if (responseBody != null) {
            Type clsType = new TypeToken<RPCResponseBaseBean<ArrayList<ClientStructureMessage>>>() {
            }.getType();
            RPCResponseBaseBean<ArrayList<ClientStructureMessage>> responseBaseBean = new Gson().fromJson(responseBody, clsType);
            if (responseBaseBean.getSuccess() == 1) {
                ArrayList<ClientStructureMessage> arrayListMsg = responseBaseBean.getData();

                this.receiveMsgs(arrayListMsg, false);
                bsuccess = true;
                Preferences.getInstacne().setValues(LAST_SERVER_TIME, responseBaseBean.getTimestamp());
                Preferences.getInstacne().setValues(CURRENT_DEVICE_TIME, System.currentTimeMillis());
            } else {
                bsuccess = false;
            }
        } else {
            bsuccess = false;
        }

        return bsuccess;
    }

    /**
     * 获取所有未接收的消息
     *
     * @return
     */
    public void httpGetUnReceiverMessage(final Context context, final long userId) {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                String requesturl;
                if (Constants.DEBUG_MODE) {
                    requesturl = Constants.DEBUG_GET_MSG_URL;
                } else {
                    requesturl = Constants.PRODUCT_GET_MSG_URL;
                }
                JSONObject jsonObject = new JSONObject();

                try {
                    long lastGetTime = Preferences.getInstacne().getValues(LAST_GET_MSG_TIME, 0l);
                    lastGetTime = lastGetTime == 0l ? Utils.getCurrentServerTime() : lastGetTime;
                    jsonObject.put("sUID", userId);
                    jsonObject.put("lastTime", lastGetTime);
                    RequestBean request = new RequestBean(requesturl, jsonObject.toString(), 10, 10,
                            BasicResponseBean.class, RequestBean.HttpMethod.POST);
                    HttpUtil httpUtil = new HttpUtil();
                    int result = httpUtil.httpPost(request.getUrl(), request.getData(),
                            request.getAppType(), request.getTimeout(), null);
                    String responseStr = httpUtil.getResponseStr();
                    Log.e("aaaaa", jsonObject.toString() + "\nresponseStr: " + responseStr);
                    if (result == 0 && !TextUtils.isEmpty(responseStr)) {
                        Type clsType = new TypeToken<RPCResponseBaseBean<ArrayList<ClientStructureMessage>>>() {
                        }.getType();
                        RPCResponseBaseBean<ArrayList<ClientStructureMessage>> responseBaseBean = new Gson().fromJson(responseStr, clsType);
                        if (responseBaseBean.getSuccess() == 1) {
                            ArrayList<ClientStructureMessage> arrayListMsg = responseBaseBean.getData();
                            receiveMsgs(arrayListMsg, false);
                            Preferences.getInstacne().setValues(LAST_GET_MSG_TIME, responseBaseBean.getTimestamp());
                            Preferences.getInstacne().setValues(LAST_SERVER_TIME, responseBaseBean.getTimestamp());
                            Preferences.getInstacne().setValues(CURRENT_DEVICE_TIME, System.currentTimeMillis());
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

//        String responseBody = im_API_httpAndRpc_Requesting("getPeerOfflineMessages.do", "peerOfflineMessages", rpcPeerOfflineBaseBean, 2);
//        if (responseBody != null) {
//            Type clsType = new TypeToken<RPCResponseBaseBean<ArrayList<ClientStructureMessage>>>() {
//            }.getType();
//            RPCResponseBaseBean<ArrayList<ClientStructureMessage>> responseBaseBean = new Gson().fromJson(responseBody, clsType);
//            if (responseBaseBean.getSuccess() == 1) {
//                ArrayList<ClientStructureMessage> arrayListMsg = responseBaseBean.getData();
//                this.receiveMsgs(arrayListMsg, false);
//                bsuccess = true;
//            } else {
//                bsuccess = false;
//            }
//        } else {
//            bsuccess = false;
//        }
//
//        return bsuccess;
//    }


    /**
     * 得到与某人的缺失的离线消息
     *
     * @return
     */
    protected ArrayList<ClientStructureMessage> getPeerMissMessage(long peerUserId, ArrayList<Long> arrMsgId) {
        RPGetMissMsgBean rpcGetMissMsgBean = new RPGetMissMsgBean();
        rpcGetMissMsgBean.setsUID(peerUserId);
        rpcGetMissMsgBean.setrUID(getUserID());
        rpcGetMissMsgBean.setrSeqNums(arrMsgId);

        String responseBody = im_API_httpAndRpc_Requesting("getMissingMsssages.do", "missingMsssages", rpcGetMissMsgBean, 2);
        if (responseBody != null) {
            Type clsType = new TypeToken<RPCResponseBaseBean<ArrayList<ClientStructureMessage>>>() {
            }.getType();
            RPCResponseBaseBean<ArrayList<ClientStructureMessage>> responseBaseBean = new Gson().fromJson(responseBody, clsType);
            if (responseBaseBean.getSuccess() == 1) {
                ArrayList<ClientStructureMessage> arrayListMsg = responseBaseBean.getData();
                if (arrayListMsg != null) {
                    return arrayListMsg;
                } else {
                    return new ArrayList<ClientStructureMessage>();
                }
            } else {
                return new ArrayList<ClientStructureMessage>();
            }
        } else {
            return new ArrayList<ClientStructureMessage>();
        }

    }


}