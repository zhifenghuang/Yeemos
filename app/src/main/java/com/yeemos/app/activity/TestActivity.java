package com.yeemos.app.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.gigabud.core.task.TaskManager;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.FileURLMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.TextMessage;
import com.yeemos.app.chat.bean.UserMsgSummery;
import com.yeemos.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestActivity extends FragmentActivity implements IChatListener
{
    private ArrayList<IMsg> mArryMsg = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);



        //初始化Task模块
        //TaskManager.init(getApplicationContext());


        IChat chat = IChat.getInstance();
        chat.addChatListener(this);

        EditText editText = (EditText)findViewById(R.id.edit_sendUser);
        editText.setText("6");


        EditText editText1 = (EditText)findViewById(R.id.edit_recvUser);
        editText1.setText("180");


        ImageView imgView = (ImageView)findViewById(R.id.imageView);


        String youFilePath = Environment.getExternalStorageDirectory().toString()+"/test/test.png";

        Log.i("AAA","PATH:" + youFilePath);
        File imgFile = new  File(youFilePath);

        if (imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imgView.setImageBitmap(myBitmap);
        }

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = (EditText) findViewById(R.id.edit_sendUser);
                long nSendId = (long) Integer.parseInt(editText.getText().toString());
                IChat.getInstance().connectServer("s7mmc6hFLXNhhKVFqpYsgPSwTTWGVKjscPecI1BavFDC4cJL6bX6i76XvUFWr2FVH278cxwo8wFBy4/IBh5CFOi8A5jFapp9I90GtYZtQV16eTFbTCAs7Ipk1evD8dkLz1iq8ti9mkt5pJ/gTLYmqnQZ+beKDpLGLPLvHCHr+XDJ0tcUo8sEgH21vG9fBFGhNie1s+KPoe6wv2XoiXO1qODPa0SHaSp2BP/aAb5+6FRuulz06Xo3JNQ7bdYVPnHz"
                        , nSendId);

            }
        });



        findViewById(R.id.btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IChat.getInstance().disconnectServer();

            }
        });



        findViewById(R.id.btn_sendText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                TextMessage textMsg = new TextMessage();

                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
                long nRecvId = (long)Integer.parseInt(editText.getText().toString());
                textMsg.setrUID( nRecvId );
                textMsg.setText(String.format("Test messge %s", editText.getText()));

                IChat.getInstance().sendMsg(textMsg);

                //
                boolean bFlag = IChat.getInstance().isHaveUnReadMsg();
                if(bFlag)
                {
                    Log.i("AAA", "have unread message" );
                }
                else
                {
                    Log.i("AAA", "have no read message");
                }
            }
        });



        findViewById(R.id.btn_sendImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
                long nRecvId = (long)Integer.parseInt(editText.getText().toString());


                FileURLMessage fileURLMsg = new FileURLMessage();
                fileURLMsg.setrUID(nRecvId);
                fileURLMsg.setThumb("ICAgICAgICAKICMKICMg/kcov2xiOkJhc2U2NAz7qADRAUhUTUw1ANEBxQeERGF0YVVSSeV3CiAjIC8BIFBORwFHSUYBSlBHAUJNUAFUSUYBUFNEAUlDTyBJPA8KICMKICAgICAgICA="); //
               // msg.setImageMainColor(Color.red());
                fileURLMsg.setfSize(2014 * 10);

                String youFilePath = Environment.getExternalStorageDirectory().toString()+"/test/test.png";

                File imgFile = new File(youFilePath);

                if (imgFile.exists())
                {
                    //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    fileURLMsg.setfName(youFilePath);//和自己的文件缓存机制放到一起，以保证统一
                    fileURLMsg.setfSize(imgFile.length());
                    IChat.getInstance().sendMsg(fileURLMsg);

                }



//                ImageMessage msg = new ImageMessage();
//                msg.setrUID(nRecvId);
//                msg.setThumb("ICAgICAgICAKICMKICMg/kcov2xiOkJhc2U2NAz7qADRAUhUTUw1ANEBxQeERGF0YVVSSeV3CiAjIC8BIFBORwFHSUYBSlBHAUJNUAFUSUYBUFNEAUlDTyBJPA8KICMKICAgICAgICA="); //
//               // msg.setImageMainColor(Color.red());
//                msg.setfSize(2014 * 10);
//
//                String youFilePath = Environment.getExternalStorageDirectory().toString()+"/test/test.png";
//
//                File imgFile = new File(youFilePath);
//
//                if (imgFile.exists())
//                {
//                    //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
//                    msg.setfName(youFilePath);//和自己的文件缓存机制放到一起，以保证统一
//                    msg.setfSize(imgFile.length());
//                    IChat.getInstance().sendMsg(msg);
//                }



            }
        });




        findViewById(R.id.btn_getLastMsg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
                long nRecvId = (long)Integer.parseInt(editText.getText().toString());




                if( mArryMsg == null)
                {
                    mArryMsg = new ArrayList<IMsg>();
                }
                else
                {
                    mArryMsg.clear();
                }

                List<IMsg> arryMsg = IChat.getInstance().getPeerLastChatMsgList(nRecvId, 2);
                mArryMsg.addAll( arryMsg );
                logMsgs();
            }
        });


        findViewById(R.id.btn_getBeforeMsg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
                long nRecvId = (long)Integer.parseInt(editText.getText().toString());


                if( mArryMsg != null)
                {
                    IMsg msg = mArryMsg.get(0);
                    ArrayList<IMsg> arryMsg = IChat.getInstance().getPeerMsgListBeforeMsg(msg, nRecvId, 2);
                    mArryMsg.addAll(0, arryMsg);
                    logMsgs();
                }

            }
        });


        findViewById(R.id.btn_getAfterMsg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
                long nRecvId = (long)Integer.parseInt(editText.getText().toString());

                if( mArryMsg != null && mArryMsg.size() > 0)
                {
                    IMsg msg = mArryMsg.get( 0 );
                    ArrayList<IMsg> arryMsg = IChat.getInstance().getPeerMsgListAfterMsg(msg, nRecvId, 2);

                    mArryMsg.addAll(arryMsg);
                    logMsgs();
                }

            }
        });




//        findViewById(R.id.btn_getPeerOffline).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view)
//            {
//                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
//                long nRecvId = (long)Integer.parseInt(editText.getText().toString());
//
//
//                IChat.getInstance().getPeerOfflineMessage(nRecvId);
//                Log.i("AAA", "getPeerOfflineMessage:" );
//            }
//        });


        findViewById(R.id.btn_getMissMsg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText editText = (EditText)findViewById(R.id.edit_recvUser);
                long nRecvId = (long)Integer.parseInt(editText.getText().toString());

                ArrayList<Long> arrMsgSeqs = new ArrayList<Long>();
                arrMsgSeqs.add( new Long(1));
                arrMsgSeqs.add( new Long(2));
                IChat.getInstance().getPeerMissMessage(nRecvId, arrMsgSeqs);

                Log.i("AAA", "getPeerOfflineMessage:" );
            }
        });







    }


    private void logMsgs()
    {

        Log.i("AAA", "messsage size:" + mArryMsg.size() );
        for (int i = 0; i < mArryMsg.size(); ++i )
        {
            Log.i("AAA", "Message DB ID:" + mArryMsg.get(i).getDBID());
            //Log.i("AAA", "Message json:" + mArryMsg.get(i).toChatJson());
        }
    }
    private void updateTipText(final String textTip)
    {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView ttvTip = (TextView) findViewById(R.id.ttv_tips);
                ttvTip.setText(textTip);
                ttvTip.invalidate();
            }
        });
    }


    /**
     * begin connect chat server
     */
    public void beginConnect() {

        Log.i("AAA", "开始登陆中");
        updateTipText("登陆中");

    }

    /**
     * 连接成功
     */
    public void connectSuccess()
    {
        updateTipText("登陆成功");

    }


    /**
     * 连接失败
     */
    public void connectFailure()
    {
        updateTipText("登陆失败");
    }

    /**
     * 断开连接 或者连接失败
     */
    public void disconnect()
    {
        updateTipText("已经断开");

    }

    /**
     * 收到消息
     * @param msg
     */
    public void receiveMsg(IMsg msg)
    {
        Log.i("TestActivity", "receiveMsg:" + msg.toChatJson());
    }

    @Override
    public void sendingMsg(IMsg msg) {

    }

    public void offlineMsgRcvd(ArrayList<String> userArr)
    {

    }

    /**
     * 文件上传中
     * @param msg
     */
    public void msgUploading(IMsg msg, int progress)
    {

    }

    /**
     * 文件在下载中
     * @param msg
     */
    public void msgDownloading(IMsg msg, int progress)
    {

    }

    /**
     * 发、收的消息失败了
     * @param msg
     */
    public void msgError(IMsg msg)
    {

    }



    /**
     * 消息概述发生变化
     */
    @Override
    public void msgSummeryChange(List<UserMsgSummery> msgSummery)
    {

    }


}
