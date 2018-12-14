package com.yeemos.app.chat.bean;

import com.yeemos.app.database.DatabaseFactory;

public class PeerRead extends BasicStatusMessage {

    public PeerRead()
    {
        super();
        setMessageType(MES_TYPE.PEER_READ_MSG_TYPE);
    }



    public boolean updateSendStatus(IMSG_SEND_STATUS status)
    {
        setSendStatus(status);

        return DatabaseFactory.getDBOper().updateMsgSendStatusByMsgID(getConfirmMsgID(), status);
    }



    public boolean updateRecvStatus(IMSG_RECV_STATUS status)
    {
        this.setRecvStatus(status);
        return DatabaseFactory.getDBOper().updateMsgRecvStatusByMsgID(getConfirmMsgID(), status);

    }
}
