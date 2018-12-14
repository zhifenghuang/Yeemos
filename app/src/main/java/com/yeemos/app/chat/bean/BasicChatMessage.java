package com.yeemos.app.chat.bean;

import android.content.ContentValues;

public class BasicChatMessage extends BasicStatusMessage {


	private int needConfirmed; // 是否需要confim 0:不需要 1:需要

	public BasicChatMessage()
	{
		super();
	}


	public int getNeedConfirmed() {
		return needConfirmed;
	}

	public void setNeedConfirmed(int needConfirmed) {
		this.needConfirmed = needConfirmed;
	}



	public ContentValues getValues()
	{
		ContentValues values = super.getValues();
		values.remove("needConfirmed");
		return values;
	}
}
