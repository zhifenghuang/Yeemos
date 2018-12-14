package com.yeemos.app.chat.rpcBean;

import com.yeemos.app.chat.bean.RPCBaseBean;

/**
 * RPC消息
 */
public class RPCPeerOfflineBean extends RPCBaseBean
{
	private long rUID;
	private String  msgID;

	public String getMsgID() {
		return msgID;
	}

	public void setMsgID(String msgID) {
		this.msgID = msgID;
	}

	public long getrUID() {
		return rUID;
	}

	public void setrUID(long rUID) {
		this.rUID = rUID;
	}
}
