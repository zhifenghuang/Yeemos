package com.yeemos.app.chat.offlineMsgBlocking;

import android.util.Log;

import com.gigabud.core.util.DeviceUtil;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by iosdeviOSDev on 5/31/16.
 */
public class offlineMsgBlocking {


    public enum OFFLINE_STATE {
        OFFLINE_STATE_INIT(-1),
        OFFLINE_STATE_CONNECTING(0),
        OFFLINE_STATE_RECEIVING(1),
        OFFLINE_STATE_DONE(2),
        OFFLINE_STATE_ALL(3);

        int nValues;

        private OFFLINE_STATE(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static OFFLINE_STATE GetObject(int nNum) {
            OFFLINE_STATE[] As = OFFLINE_STATE
                    .values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return OFFLINE_STATE_INIT;
        }
    }

    private int nOfflineFlag;
    private ArrayList<String> offlineUserArr;
    private long  incomingMsgDate;
    private Timer timer;
    private int timerCounter;
    private static int imOfflineMsgBlocking_default_fire_timeinterval = 1000;
    private static int imOfflineMsgBlocking_incoming_max_waiting = 2000;
    private static int imOfflineMsgBlocking_nomsg_max_waiting = 2000;

    private offlineMsgListener m_offlineMsgListener = null;

    public offlineMsgBlocking()
    {
        incomingMsgDate = 0;
        offlineUserArr = new ArrayList<String>();
        nOfflineFlag = OFFLINE_STATE.OFFLINE_STATE_INIT.GetValues();
        timer = new Timer();
        timerCounter = 0;
    }


    public void setOfflineListener(offlineMsgListener listener)
    {
        m_offlineMsgListener = listener;
    }

    public void setFlag(OFFLINE_STATE emFlag )
    {
        nOfflineFlag = emFlag.GetValues();

        switch ( emFlag )
        {
            case OFFLINE_STATE_CONNECTING:
            {
                offlineUserArr.clear();
                incomingMsgDate = 0;
                timerCounter = 0;
            }
            break;

            case OFFLINE_STATE_RECEIVING:
            {
                reStartTimeer();
            }
            break;

            default:
                break;
        }
    }

    public OFFLINE_STATE offlineFlag()
    {
        return OFFLINE_STATE.GetObject(nOfflineFlag);
    }

    public void addOfflineUser(String uID)
    {
        if (uID == null) return;

        if (! offlineUserArr.contains(uID))
        {
            offlineUserArr.add(uID);
        }

        incomingMsgDate = Utils.getCurrentServerTime();

    }

    private void reStartTimeer()
    {
        try {

            timerCounter = 0;

            if (timer==null)
            {
                timer = new Timer();
            }

            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {

                    if (incomingMsgDate > 0)
                    {

                        long cuurentTime = Utils.getCurrentServerTime();
                        if (cuurentTime - incomingMsgDate >= imOfflineMsgBlocking_incoming_max_waiting)
                        {
                            // do something
                            if ( m_offlineMsgListener!= null )
                            {
                                m_offlineMsgListener.offlineMsgBlocking_finished(offlineUserArr);
                            }
                            //---------
                            finish();
                        }

                    }
                    else {

                        if ( timerCounter * imOfflineMsgBlocking_default_fire_timeinterval >= imOfflineMsgBlocking_nomsg_max_waiting )
                        {
                            // do something
                            if ( m_offlineMsgListener!= null )
                            {
                                m_offlineMsgListener.offlineMsgBlocking_finished(offlineUserArr);
                            }
                            //---------
                            finish();
                        }
                    }

                    timerCounter++;

                }
            },imOfflineMsgBlocking_default_fire_timeinterval,imOfflineMsgBlocking_default_fire_timeinterval);
        }
        catch (RuntimeException en)
        {
            Log.i("offlineMsgBlocking", "reStartTimeer error" );
        }

    }


    private void finish()
    {
        timerCounter = 0;
        incomingMsgDate = 0;
        nOfflineFlag = OFFLINE_STATE.OFFLINE_STATE_INIT.GetValues();
        timer.cancel();
        timer = null;

        Log.i("offlineMsgBlocking", "offlineMsgRcvd finish:");
    }

}
