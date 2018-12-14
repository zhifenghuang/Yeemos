package com.yeemos.app.chat.bean;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class UserStartTyping extends BasicChatMessage {


    public UserStartTyping()
    {
        super();
        setMessageType(MES_TYPE.TYPING_SEND_MSG_TYPE);
        setNeedConfirmed(0);
    }

    private static final Type TT_mapStringString = new TypeToken<Map<String,Object>>(){}.getType();


    public String toChatJson()
    {
        Map<String, Object> statusMapping = new HashMap<String, Object>();
        statusMapping.put("msgID",getMsgID());
        statusMapping.put("sUID",getsUID());

        //Log.i("AAA",new Gson().toJson(statusMapping,TT_mapStringString));
        return new Gson().toJson(statusMapping,TT_mapStringString);

        //return new Gson().toJson(this);
    }


}


