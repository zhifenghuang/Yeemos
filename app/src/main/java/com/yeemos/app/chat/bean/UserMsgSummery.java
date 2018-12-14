package com.yeemos.app.chat.bean;

import com.yeemos.app.chat.rpcBean.User;

/**
 * RPC消息
 */
public class UserMsgSummery
{
	private long unReadNum; // 用于确认缺失多少条消息
	private IMsg lastMsg;
	private User user;

	public UserMsgSummery()
	{
		super();
	}

	public long getUnReadNum() {
		return unReadNum;
	}

	public void setUnReadNum(long unReadNum) {
		this.unReadNum = unReadNum;
	}

	public IMsg getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(IMsg lastMsg) {
		this.lastMsg = lastMsg;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
