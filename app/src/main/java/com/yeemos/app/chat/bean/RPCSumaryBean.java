package com.yeemos.app.chat.bean;

import com.yeemos.app.database.DatabaseFactory;

/**
 * Created by iosdeviOSDev on 6/16/16.
 */
public class RPCSumaryBean extends RPCBaseBean {

    private String nstatus;

    public void setSearchUserId(long userId)
    {
        boolean bifDBempty = DatabaseFactory.getDBOper().detectIfEmptyDataBase(userId);

        setNstatus(bifDBempty?"3":"");

    }


    public String getNstatus() {
        return nstatus;
    }

    public void setNstatus(String nstatus) {
        this.nstatus = nstatus;
    }
}
