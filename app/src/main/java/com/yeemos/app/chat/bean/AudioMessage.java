package com.yeemos.app.chat.bean;

/**
 * 声音占位消息
 */
public class AudioMessage extends BasicChatFileMessage {



	public AudioMessage()
	{
		super();
		setMessageType(MES_TYPE.ADUID_MSG_TYPE);
	}

}
