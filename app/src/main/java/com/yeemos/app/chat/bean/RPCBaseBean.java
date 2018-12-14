package com.yeemos.app.chat.bean;

import android.util.Log;

import com.gigabud.core.util.DeviceUtil;
import com.gigabud.core.util.VersionUtil;
import com.google.gson.Gson;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.manager.MemberShipManager;

/**
 * RPC消息
 */
public class RPCBaseBean
{
	private int apiVer;
	private String deviceID;
	private String sysType;
	private int appVer;
	private String language;
	private long sUID;
	private String token;

	public RPCBaseBean()
	{
		super();
		apiVer = 1;
		deviceID = DeviceUtil.getDeviceId( BaseApplication.getAppContext() );
		sysType = "android";
		setAppVer( (int)VersionUtil.convertVersionNumberToLong( BaseApplication.getAppContext() ));
		setLanguage("en_US");
		setToken(MemberShipManager.getInstance().getToken());
	}

	public String toJson()
	{
		try
		{
			return new Gson().toJson(this);
		}
		catch (Exception expect)
		{

			Log.i("AAA", "expect:" + expect.toString());
			return "";
		}
	}

	public int getApiVer() {
		return apiVer;
	}

	public void setApiVer(int apiVer) {
		this.apiVer = apiVer;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getSysType() {
		return sysType;
	}

	public void setSysType(String sysType) {
		this.sysType = sysType;
	}

	public int getAppVer() {
		return appVer;
	}

	public void setAppVer(int appVer) {
		this.appVer = appVer;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public long getsUID() {
		return sUID;
	}

	public void setsUID(long sUID) {
		this.sUID = sUID;
	}


	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
}
