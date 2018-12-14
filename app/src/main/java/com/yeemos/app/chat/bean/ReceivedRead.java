package com.yeemos.app.chat.bean;

import android.util.Log;

import com.gigabud.core.util.DeviceUtil;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.utils.Utils;

/**
 * Created by iosdeviOSDev on 4/18/16.
 */
public class ReceivedRead extends BasicStatusMessage {


    public ReceivedRead()
    {
        super();
        setMessageType(MES_TYPE.PEER_RCVD_READ_MSG_TYPE);
    }




    public boolean updateSendStatus(IMSG_SEND_STATUS status)
    {
        setSendStatus(status);

        return DatabaseFactory.getDBOper().updateMsgSendStatusByMsgID(getConfirmMsgID(), status);
    }


    public boolean updateRecvStatus(IMSG_RECV_STATUS status)
    {
        this.setRecvStatus(status);

        if ( getReadIds()!=null && getReadIds().length() > 0 )
        {
            String[] array = getReadIds().split(":");
            if ( array.length >= 2 )
            {
                String strSQL = String.format("UPDATE tb_msgs SET recvStatus = MAX(%d, recvStatus),updateTime = %d WHERE seqNum IN %s AND sUID = %s AND recvStatus < %d;",status.GetValues(), Utils.getCurrentServerTime(),array[1],array[0],status.GetValues());
                DatabaseFactory.getDBOper().execSQL(strSQL);
            }
        }

        return DatabaseFactory.getDBOper().updateMsgRecvStatusByMsgID(getConfirmMsgID(), status);
    }

}
