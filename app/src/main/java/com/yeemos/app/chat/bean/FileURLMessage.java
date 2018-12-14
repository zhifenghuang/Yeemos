package com.yeemos.app.chat.bean;

import android.content.ContentValues;

public class FileURLMessage extends BasicChatMessage {


	private String occupiedMsgID; // Occupied File message ID

	public FileURLMessage()
	{
		super();
		setMessageType(MES_TYPE.FILE_MSG_TYPE);
		setNeedConfirmed(0);
	}

	public MES_TYPE getMessageType()
	{
		return MES_TYPE.FILE_MSG_TYPE;
	}

	public String getOccupiedMsgID() {
		return occupiedMsgID;
	}

	public void setOccupiedMsgID(String occupiedMsgID) {
		this.occupiedMsgID = occupiedMsgID;
	}


	public ContentValues getValues()
	{
		ContentValues values = super.getValues();
		values.remove("occupiedMsgID");
		return values;
	}


}
