package com.yeemos.app.chat.manager;

import android.util.Log;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.yeemos.app.chat.Interface.IChat;

import java.util.UUID;


public class RPCClient {

    private Connection connection = null;
    private Channel channel = null;
    private String requestQueueName = "queue_rpc_server";
    private String replyQueueName ;
    private QueueingConsumer consumer;
    private boolean bcreateconnection = false;

    private RPCClient()  {
    }


    public RPCClient( Connection connection, long sendUserId) throws Exception {

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
        channel = connection.createChannel();

        String strReplyQueueName = String.format("Temp_%d", sendUserId);
        //String strReplyQueueName = String.format("Temp_%s", BaseUtils.getUUID() );

       // channel.basicQos(1);

//        String routingKey = String.format("%s%d", ROUTE_KEY_NAME_PREFIX, getUserID());
//        String queueName = String.format("%s%d", QUEUE_KEY_NAME_PREFIX, getUserID());


        AMQP.Queue.DeclareOk q = channel.queueDeclare(strReplyQueueName, true, false, false, null);
        replyQueueName = q.getQueue();

        Log.i("jxq testing", "RPCClient replyQueueName " + replyQueueName);

        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    public String call(String message) throws Exception {
        String response = null;
        String corrId = UUID.randomUUID().toString();


        BasicProperties props = new BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
        //channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

            response = new String(delivery.getBody(),"UTF-8");
            break;
//            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
//                response = new String(delivery.getBody(),"UTF-8");
//                break;
//            }
        }

        return response;
    }

    public void close() throws Exception {
        if(bcreateconnection)
        {
            connection.close();
            connection = null;
        }
        if( consumer != null) {
            channel.basicCancel(consumer.getConsumerTag());
            consumer = null;
        }
       // channel.close();
    }

//    public static void main(String[] argv) {
//        RPCClient fibonacciRpc = null;
//        String response = null;
//        try {
//            fibonacciRpc = new RPCClient();
//
//            System.out.println(" [x] Requesting fib(30)");
//            response = fibonacciRpc.call("30");
//            System.out.println(" [.] Got '" + response + "'");
//        }
//        catch  (Exception e) {
//            e.printStackTrace();
//        }
//        finally {
//            if (fibonacciRpc!= null) {
//                try {
//                    fibonacciRpc.close();
//                }
//                catch (Exception ignore) {}
//            }
//        }
//    }
}