package com.yeemos.app.chat.bean;

public class SendFailed extends BasicStatusMessage {

	public SendFailed()
	{
		super();
		setMessageType(MES_TYPE.MSG_SEND_FAILED);
	}
}
