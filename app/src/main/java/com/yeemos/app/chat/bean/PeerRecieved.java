package com.yeemos.app.chat.bean;

import com.yeemos.app.database.DatabaseFactory;

public class PeerRecieved extends BasicStatusMessage {


    public PeerRecieved()
    {
        super();
        setMessageType(MES_TYPE.PEER_RECV_MSG_TYPE);
    }




    public boolean updateSendStatus(IMSG_SEND_STATUS status)
    {
        setSendStatus(status);

        return DatabaseFactory.getDBOper().updateMsgSendStatusByMsgID(getConfirmMsgID(), status);
    }
}
