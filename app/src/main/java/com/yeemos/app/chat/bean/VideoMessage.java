package com.yeemos.app.chat.bean;


/**
 * 视频占位消息
 */
public class VideoMessage extends BasicChatFileMessage {


	public VideoMessage()
	{
		super();
		setMessageType(MES_TYPE.VIDEO_MSG_TYPE);
	}




}
