package com.yeemos.app.chat.bean;

import android.util.Log;

import com.google.gson.Gson;

public class TextMessage extends BasicChatMessage {


	public TextMessage()
	{
		super();
		setMessageType(MES_TYPE.TEXT_MSG_TYPE);
		setNeedConfirmed(1);
	}



	public String toChatJson()
	{
		try
		{
			//Log.i("AAA", "TestMesage:" + new Gson().toJson(this));
			return new Gson().toJson(this);

		}
		catch (Exception expect)
		{

			Log.i("AAA", "expect:" + expect.toString() );
			return "";
		}
	}



}
