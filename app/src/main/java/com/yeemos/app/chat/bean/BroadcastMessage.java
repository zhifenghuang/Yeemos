package com.yeemos.app.chat.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by iosdeviOSDev on 6/29/16.
 */
public class BroadcastMessage extends BasicChatMessage {

    public BroadcastMessage()
    {
        super();
        setMessageType(MES_TYPE.BROADCAST_MSG_TYPE);
        setNeedConfirmed(0);
    }

    private static final Type TT_mapStringString = new TypeToken<Map<String,Object>>(){}.getType();


    public String toChatJson()
    {
        Map<String, Object> statusMapping = new HashMap<String, Object>();
        statusMapping.put("msgID",getMsgID());
        statusMapping.put("sUID",getsUID());
        statusMapping.put("btype",getBtype());
        statusMapping.put("rUID",getrUID());
        statusMapping.put("text",getsUID());

        //Log.i("AAA",new Gson().toJson(statusMapping,TT_mapStringString));
        return new Gson().toJson(statusMapping,TT_mapStringString);

        //return new Gson().toJson(this);
    }
}
