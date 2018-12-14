package com.yeemos.app.chat.bean;

/**
 * Created by iosdeviOSDev on 5/26/16.
 */
public class readIdsBean {

    private String sUID;      // 发送消息的用户ID
    private String rUID;      // 收送消息的用户ID
    private String readIds; // 收到的已读ids


    public String getReadIds() {
        return readIds;
    }

    public void setReadIds(String readIds) {
        this.readIds = readIds;
    }

    public String getsUID() {
        return sUID;
    }

    public void setsUID(String sUID) {
        this.sUID = sUID;
    }

    public String getrUID() {
        return rUID;
    }

    public void setrUID(String rUID) {
        this.rUID = rUID;
    }
}
