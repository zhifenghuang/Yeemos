package com.yeemos.app.chat.Interface;

import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.UserMsgSummery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by apple on 12/9/15.
 */
public interface IChatListener
{

    /**
     * begin connect chat server
     */
    void beginConnect();

    /**
     * 连接成功
     */
    void connectSuccess();


    /**
     * 连接失败
     */
    void connectFailure();



    /**
     * 断开连接
     */
    void disconnect();


    /**
     * 收到消息
     * @param msg
     */
    void receiveMsg(IMsg msg);

    /**
     * 发送消息
     * @param msg
     */
    void sendingMsg(IMsg msg);

    /**
     * 接收离线消息结束
     * @param userArr
     */
    void offlineMsgRcvd(ArrayList<String> userArr);


    /**
     * 文件上传中
     * @param msg
     */
    void msgUploading(IMsg msg, int progress);

    /**
     * 文件在下载中
     * @param msg
     */
    void msgDownloading(IMsg msg, int progress);


    /**
     * 错误消息
     * @param msg
     */
    void msgError(IMsg msg);


    /**
     * 消息概述发 生变化
     */
    void msgSummeryChange(List<UserMsgSummery> msgSummery);
}
