package com.yeemos.app.chat.manager;

import android.text.TextUtils;
import android.util.Log;

import com.gbsocial.server.YeemosTask;
import com.gigabud.core.JobDaddy.JobDaddy;
import com.gigabud.core.http.DownloadFileManager;
import com.gigabud.core.http.RequestBean;
import com.gigabud.core.task.ITask;
import com.gigabud.core.task.ITaskListener;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.chat.Interface.IChatListener;
import com.yeemos.app.chat.bean.BasicMessage;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.bean.SendFailed;
import com.yeemos.app.chat.httpBean.FileUploadBean;
import com.yeemos.app.chat.httpBean.FileUploadResponseBean;
import com.yeemos.app.database.DatabaseFactory;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.chat.bean.IMsg.IMSG_RECV_STATUS;
import com.yeemos.app.chat.bean.IMsg.IMSG_SEND_STATUS;
import com.yeemos.app.utils.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by apple on 12/9/15.
 */
public class FileHtttpManager implements ITaskListener {

    static private FileHtttpManager g_FileHtttpManager = null;

    private ArrayList<IMsg> mArrayMsgs = null;
    private ArrayList<IChatListener> arrListeners = new ArrayList<IChatListener>();

    private FileHtttpManager() {
        // 上传管理
        //      TaskManager.addTaskListener(this);

        //下载管理
        //      DownloadFileManager.getInstance().addDownloadListener(this);
    }

    public static FileHtttpManager getInstance() {
        if (g_FileHtttpManager == null) {
            g_FileHtttpManager = new FileHtttpManager();

        }
        return g_FileHtttpManager;
    }

    private ArrayList<IChatListener> getChatListeners() {
        if (arrListeners == null)  //防止内存回收
        {
            arrListeners = new ArrayList<IChatListener>();
        }
        return arrListeners;
    }

    public void start(ITask task) {

    }

    /**
     * 得到要上传和下载的消息数组
     *
     * @return
     */
    protected ArrayList<IMsg> getArrayMsgs() {
        if (mArrayMsgs == null) {
            mArrayMsgs = new ArrayList<IMsg>();
        }
        return mArrayMsgs;
    }

    /**
     * 上传附件
     *
     * @param msg
     * @return
     */
    public boolean uploadMsg(IMsg msg, String token) {
        if (msg.getMessageType() == IMsg.MES_TYPE.IMAGE_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.ADUID_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.VIDEO_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {

            getArrayMsgs().add(msg);
            //上传附件  // file

            FileUploadBean fileBean = new FileUploadBean();
            fileBean.setsUID(msg.getsUID());
            fileBean.setrUID(msg.getrUID());
            fileBean.setToken(token);
            fileBean.setMsgType(msg.getMessageType().GetValues());
            fileBean.setOccupiedMsgID(msg.getMsgID());
            fileBean.setSeqNum(msg.getSendSeqNum());
            fileBean.setCliTime(Utils.getCurrentServerTime());


            RequestBean rqstBean = new RequestBean(
                    Preferences.getInstacne().getFileHttpURL(),
                    fileBean.toJson(),
                    3,
                    30,
                    FileUploadResponseBean.class,
                    RequestBean.HttpMethod.POST_FORM_WITH_PROGRESS);

            BasicMessage baseMsg = (BasicMessage) msg;
            rqstBean.addUploadChatFile("image", baseMsg.getfName(), new File(DownloadFileManager.getInstance().getFilePath(BaseApplication.getAppContext(), baseMsg.getfName())));
            rqstBean.addUploadChatFile("image", baseMsg.getThumb(), new File(DownloadFileManager.getInstance().getFilePath(BaseApplication.getAppContext(), baseMsg.getThumb())));


            YeemosTask task = new YeemosTask(rqstBean);
            task.setTag(msg.getMsgID());
            task.setYeemosTask(YeemosTask.YeemosTaskType.UPLOAD_CHAT_FILE);
            //TaskManager.addTask(BaseApplication.getAppContext(), task);
            JobDaddy.getInstance().addJob(task);

            task.addListener(this);

            //修改消息的发送状态为发送中
            msg.updateSendStatus(IMSG_SEND_STATUS.IMSG_SEND_STATUS_SENDING);

        }
        return true;
    }


    /**
     * 下载附件
     *
     * @param msg
     * @return
     */
    public boolean downloadMsg(IMsg msg) {
        if (msg.getMessageType() == IMsg.MES_TYPE.IMAGE_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.ADUID_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.VIDEO_MSG_TYPE
                || msg.getMessageType() == IMsg.MES_TYPE.FILE_MSG_TYPE) {

            getArrayMsgs().add(msg);
            //下载file

            //修改消息的发送状态为发送中
            msg.updateRecvStatus(IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECEING);

        }
        return true;
    }

    public boolean addChatListener(IChatListener chatListener) {

        ArrayList<IChatListener> listeners = getChatListeners();

        for (int i = 0; i < listeners.size(); i++) {
            IChatListener chectlisten = listeners.get(i);
            if (chectlisten.equals(chatListener)) {
                listeners.remove(chectlisten);
                break;
            }
        }

        listeners.add(chatListener);


        return true;
    }

    public boolean removeChatListener(IChatListener chatListener) {

        ArrayList<IChatListener> listeners = getChatListeners();
        for (int i = 0; i < listeners.size(); ++i) {
            IChatListener listener = listeners.get(i);
            if (listener != null && listener == chatListener) {
                listeners.remove(i);
                break;
            }

        }
        return true;
    }


    /**
     * 通知Listener 收到新消息
     *
     * @param msg
     */
    private void notifyDownloadRecvMsg(IMsg msg, int progress) {
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).msgDownloading(msg, progress);
        }
    }

    private void notifyUploadRecvMsg(IMsg msg, int progress) {

        if (msg != null) {

            ArrayList<IChatListener> chatListeners = getChatListeners();
            for (int i = 0; i < chatListeners.size(); ++i) {
                chatListeners.get(i).msgUploading(msg, progress);
            }

        }
    }

    protected IMsg getMsgByTaskTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            Log.i("FileHtttpManager", "getMsgByTaskTag msg is null, because task tag is null");
            return null;
        }
        IMsg msg = null;
        ArrayList<IMsg> arrMsgs = getArrayMsgs();
        for (int i = 0; i < arrMsgs.size(); ++i) {
            if (tag.equalsIgnoreCase(arrMsgs.get(i).getMsgID())) {
                msg = arrMsgs.get(i);
                break;
            }
        }

        if (msg == null) {
            // 根据消息ID得到Msg对象
            msg = DatabaseFactory.getDBOper().getMsgByMsgId(tag);
            addMsg(msg);
        }

        if (msg == null) {
            Log.i("FileHtttpManager", "getMsgByTaskTag msg is null, task tag is:" + tag);
        }
        return msg;


    }


    protected void removeMsg(IMsg msg) {
        if (msg != null) {
            ArrayList<IMsg> arrMsgs = getArrayMsgs();
            for (int i = 0; i < arrMsgs.size(); ++i) {
                if (msg == arrMsgs.get(i)) {
                    arrMsgs.remove(i);
                    break;
                }
            }
        }
    }

    protected void addMsg(IMsg msg) {
        if (msg != null) {
            getArrayMsgs().add(msg);
        }
    }


    /**
     * 失败
     *
     * @param task
     */
    public void error(ITask task) {
        IMsg msg = getMsgByTaskTag(task.getTag());
        if (msg != null) {
            msg.updateSendStatus(IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_FAILURE);

            // failed meesage

            SendFailed sendFailed = new SendFailed();
            sendFailed.setConfirmMsgID(msg.getMsgID());
            notifyRecvMsg(sendFailed); // 通知UI更新状态

        }
    }

    /**
     * 上传文件 通知Listener 收到新消息
     *
     * @param msg
     */
    private void notifyRecvMsg(IMsg msg) {
        ArrayList<IChatListener> chatListeners = getChatListeners();
        for (int i = 0; i < chatListeners.size(); ++i) {
            chatListeners.get(i).receiveMsg(msg);
        }
    }


    /**
     * 上传文件 成功
     *
     * @param task
     */
    public void success(ITask task) {
        IMsg msg = getMsgByTaskTag(task.getTag());
        if (msg != null) {
            msg.updateSendStatus(IMSG_SEND_STATUS.IMSG_SEND_STATUS_SEND_SUCCESS);
        }
    }

    /**
     * 上传文件 进度
     *
     * @param task
     * @param progress
     */
    public void progress(ITask task, int progress) {
        notifyUploadRecvMsg(getMsgByTaskTag(task.getTag()), progress);

    }


    /**
     * 下载文件
     *
     * @param tag
     * @param url
     * @param fileSize
     * @param currentDownloadSize
     */
    public void notifyDownloadInfo(String tag, String url, long fileSize, long currentDownloadSize) {
        Log.i("FileHtttpManager", "currentDownloadSize:" + currentDownloadSize + " fileSize" + fileSize);
        if (getMsgByTaskTag(tag) == null) {
            return;
        }
        int progress = 0;
        if (fileSize == 0) {
            progress = 0;
        } else {
            float fTemp = currentDownloadSize / fileSize;
            progress = (int) (fTemp * 100);

            IMsg msg = getMsgByTaskTag(tag);
            Log.i("FileHtttpManager", "download progress:" + progress + " currentDownloadSize:" + currentDownloadSize + " fileSize:" + fileSize + " msg download statuse" + msg.getRecvStatus());

            notifyDownloadRecvMsg(msg, progress);

            if (currentDownloadSize == fileSize) {

                Log.i("FileHtttpManager", "notifyDownloadInfo success:" + progress + " currentDownloadSize:" + currentDownloadSize + " fileSize:" + fileSize);
                //下载完成
                if (msg != null) {
                    msg.updateRecvStatus(IMSG_RECV_STATUS.IMSG_RECV_STATUS_RECV_SUCCESS);
                }
            }
        }
    }

}
