package com.yeemos.app.chat.bean;

/**
 * 图片占位消息
 */
public class ImageMessage extends BasicChatFileMessage {

	public ImageMessage()
	{
		super();
		setMessageType(MES_TYPE.IMAGE_MSG_TYPE);
		setNeedConfirmed(1);
	}

}
