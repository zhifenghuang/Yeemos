package com.yeemos.app.chat.rpcBean;

import com.yeemos.app.chat.bean.RPCBaseBean;

import java.util.ArrayList;

/**
 * RPC消息
 */
public class RPGetMissMsgBean extends RPCBaseBean
{


	private long rUID;
	private ArrayList<Long> rSeqNums;


	public ArrayList<Long> getrSeqNums() {
		return rSeqNums;
	}

	public void setrSeqNums(ArrayList<Long> rSeqNums) {
		this.rSeqNums = rSeqNums;
	}


	public long getrUID() {
		return rUID;
	}

	public void setrUID(long rUID) {
		this.rUID = rUID;
	}
}
