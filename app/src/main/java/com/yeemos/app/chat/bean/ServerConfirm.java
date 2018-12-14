package com.yeemos.app.chat.bean;

import android.util.Log;

import com.yeemos.app.database.DatabaseFactory;

public class ServerConfirm extends BasicStatusMessage {


	public ServerConfirm()
	{
		super();
		setMessageType(MES_TYPE.SEVR_CONFIRM_MSG_TYPE);
	}



	public boolean updateSendStatus(IMSG_SEND_STATUS status)
	{
		setSendStatus(status);

		return DatabaseFactory.getDBOper().updateMsgSendStatusByMsgID(getConfirmMsgID(), status);
	}

	public boolean updateRecvStatus(IMSG_RECV_STATUS status)
	{

		setRecvStatus(status);
		if ( IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_SUCCESS == status )
		{
			String strSql = String.format("update tb_msgs set sendStatus = %d where %d > sendStatus and msgID = '%s'", status.GetValues(),status.GetValues(),getConfirmMsgID());

			//Log.i("AAA", "ServerConfirm " + strSql);
			boolean bFlag = DatabaseFactory.getDBOper().execSQL(strSql);
			if( bFlag )
			{
				Log.i("ServerConfirm", "update database success(status:)" );
			}
			else
			{
				Log.i("ServerConfirm", "update database failed(status:)");
			}
			return bFlag;
		}
		else
		{
			Log.i("ServerConfirm", "updateRecvStatus:" + status + " not implement");
			return false;
		}


	}



}
