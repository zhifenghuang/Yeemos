package com.yeemos.app.chat.rpcBean;

import com.yeemos.app.chat.bean.ClientStructureMessage;
import com.yeemos.app.chat.bean.UserMsgSummery;

import java.util.ArrayList;

/**
 * RPC消息
 */
public class RPCOfflineBean extends UserMsgSummery
{
	public ArrayList<ClientStructureMessage> msgs;


	public RPCOfflineBean()
	{
		super();
	}


	public ArrayList<ClientStructureMessage> getMsgs() {
		return msgs;
	}

	public void setMsgs(ArrayList<ClientStructureMessage> msgs) {
		this.msgs = msgs;
	}



}
