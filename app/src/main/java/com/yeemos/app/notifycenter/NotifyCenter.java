package com.yeemos.app.notifycenter;

import android.content.Intent;
import android.util.Log;

import com.yeemos.app.BaseApplication;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;


public class NotifyCenter {

	public static void sendBoardcastByDataUpdate(String strContent) {		
		NotifyCenter.sendBoardcastForDataUpdate(Constants.BROADCAST_DATA_UPDATE_TYPE, strContent, "", true);
	}

	public static void sendBoardcastForDataUpdate(String strType,
			String strContent, String strError, boolean bIsSuccess) {

		Preferences prefer = Preferences.getInstacne();

	//	String packageName = prefer.getPackageName();
		
		//Intent intent = new Intent(packageName + Constants.BROADCAST_REFRESHUI_CATEGORY);
		Intent intent = new Intent(Constants.BROADCAST_REFRESHUI_CATEGORY);
		
		
		intent.putExtra(Constants.BROADCAST_TYPE, strType);
		intent.putExtra(Constants.BROADCAST_CONTENT, strContent);
		intent.putExtra(Constants.BROADCAST_ERROR, strError);
		intent.putExtra(Constants.BROADCAST_SUCCESS, bIsSuccess);

		BaseApplication.getAppContext().sendBroadcast(intent);
	}

}
