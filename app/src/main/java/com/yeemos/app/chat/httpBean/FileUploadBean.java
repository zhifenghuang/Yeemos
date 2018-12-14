package com.yeemos.app.chat.httpBean;

import android.util.Log;

import com.gigabud.core.util.DeviceUtil;
import com.google.gson.Gson;
import com.yeemos.app.chat.bean.RPCBaseBean;
import com.yeemos.app.utils.Utils;

/**
 * RPC消息
 */
public class FileUploadBean extends RPCBaseBean {
    private long rUID;
    private long msgType;
    private long cliTime;
    private String occupiedMsgID;


    private String seqNum;
    private long fDuration;
    private long fSize;
    private String thumb;
    private long imageMainColor;

    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
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

    public FileUploadBean() {
        super();
        cliTime = Utils.getCurrentServerTime();
    }

    public long getrUID() {
        return rUID;
    }

    public void setrUID(long rUID) {
        this.rUID = rUID;
    }

    public long getMsgType() {
        return msgType;
    }

    public void setMsgType(long msgType) {
        this.msgType = msgType;
    }

    public long getCliTime() {
        return cliTime;
    }

    public void setCliTime(long cliTime) {
        this.cliTime = cliTime;
    }

    public String getOccupiedMsgID() {
        return occupiedMsgID;
    }

    public void setOccupiedMsgID(String occupiedMsgID) {
        this.occupiedMsgID = occupiedMsgID;
    }

    public String toJson() {
        try {
            return new Gson().toJson(this);
        } catch (Exception expect) {

            Log.i("AAA", "expect:" + expect.toString());
            return "";
        }
    }
}
