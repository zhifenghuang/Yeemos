package com.yeemos.app.chat.bean;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yeemos.app.database.DatabaseFactory;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class BasicStatusMessage extends BasicMessage {


	private String confirmMsgID; // 被 Confirmed 消息ID

	public String getConfirmMsgID() {
		return confirmMsgID;
	}

	public void setConfirmMsgID(String confirmMsgID) {
		this.confirmMsgID = confirmMsgID;
	}

	private static final Type TT_mapStringString = new TypeToken<Map<String,String>>(){}.getType();





	public String toChatJson()
	{
		Map<String, String> statusMapping = new HashMap<String, String>();
		statusMapping.put("msgID",getMsgID());
		statusMapping.put("confirmMsgID",getConfirmMsgID());
		statusMapping.put("readIds",getReadIds());
		statusMapping.put("confirmReadIds",getConfirmReadIds());
		//Log.i("AAA",new Gson().toJson(statusMapping,TT_mapStringString));
		return new Gson().toJson(statusMapping,TT_mapStringString);

		//return new Gson().toJson(this);
	}

	public BasicMessage getPeerUId()
	{
		String strSql = String.format("SELECT sUID,rUID FROM tb_msgs WHERE msgID = '%s';", this.confirmMsgID);

		BasicMessage baseMsg = DatabaseFactory.getDBOper().getOne(strSql, BasicMessage.class);

		return baseMsg;
	}

	public ContentValues getValues()
	{
		ContentValues values = super.getValues();
		values.remove("confirmMsgID");
		return values;
	}



}
