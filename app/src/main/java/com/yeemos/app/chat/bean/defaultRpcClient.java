package com.yeemos.app.chat.bean;


import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.RpcClient;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.manager.CRabbitMQChat;

import java.util.UUID;

/**
 * Created by iosdeviOSDev on 4/15/16.
 */
public class defaultRpcClient {

    private static final String TAG = "defaultRpcClient";
    private String rpc_routing_key = "queue_rpc_server";
    private String replyQueueName ;
    private RpcClient ampqRpcClient;
    private Connection connection;
    private Channel channel;
    private boolean bcreateconnection = false;



    public defaultRpcClient( Connection connection, long sendUserId ) throws Exception {

        if ( connection != null )
        {
            this.connection = connection;
            bcreateconnection = false;
        }
        else
        {
            CRabbitMQChat cRabbitMQChat = (CRabbitMQChat) IChat.getInstance();
            ConnectionFactory connectionFactory = cRabbitMQChat.getRabbitMQManager().getConnectionFactory();
            this.connection = connectionFactory.newConnection();
            bcreateconnection = true;
        }

        this.connection.addShutdownListener(new ShutdownListener()
        {
            @Override
            public void shutdownCompleted(ShutdownSignalException arg0) {

                Log.i(TAG, "Bshotdown: ShutdownSignalException" + arg0.getClass().getName());
            }

        });

        channel = connection.createChannel();

        String strReplyQueueName = String.format("Temp_%d", sendUserId);
        AMQP.Queue.DeclareOk q = channel.queueDeclare(strReplyQueueName, true, false, false, null);
        replyQueueName = q.getQueue();

        ampqRpcClient = new RpcClient(channel,"",rpc_routing_key,replyQueueName);
        ampqRpcClient.checkConsumer();

        Log.i(TAG, "RPCClient ampqRpcClient " + ampqRpcClient);

    }


    public String rpcCall(String message) throws Exception {

        String response = null;

        String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        try
        {
            byte[] rpcMsg = ampqRpcClient.primitiveCall(props, message.getBytes("UTF-8"));
            response = new String(rpcMsg,"UTF-8");

        }catch ( Exception e)
        {
            e.printStackTrace();
        }

        //response = ampqRpcClient.stringCall(message);


        Log.i(TAG, "rpcCall " + response);

        // response = ampqRpcClient.stringCall(message);

        return response;
    }

    public void close() throws Exception {
        ampqRpcClient.close();
    }

}
