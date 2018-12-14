package com.yeemos.app.manager;

import android.util.Log;

import com.yeemos.app.interfaces.DataChangeListener;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-11-11.
 */

public class DataChangeManager {
    private static DataChangeManager mDataChangeManager;
    private static Object mLockObject = new Object();

    private ArrayList<DataChangeListener> mDataChangeListeners;

    public static DataChangeManager getInstance() {
        if (mDataChangeManager == null) {
            synchronized (mLockObject) {
                if (mDataChangeManager == null) {
                    mDataChangeManager = new DataChangeManager();
                }
            }
        }
        return mDataChangeManager;
    }

    /**
     * 添加dataChangeListener
     *
     * @param dataChangeListener
     */
    public void addDataChangeListener(DataChangeListener dataChangeListener) {
        if (mDataChangeListeners == null) {
            mDataChangeListeners = new ArrayList<>();
        }
        synchronized (mLockObject) {
            if (!mDataChangeListeners.contains(dataChangeListener)) {
                mDataChangeListeners.add(dataChangeListener);
            }
        }
    }


    public void clearDataChangeListener() {

        synchronized (mLockObject) {
            if (mDataChangeListeners != null) {
                mDataChangeListeners.clear();
            }
        }
    }

    /**
     * 移除dataChangeListener
     *
     * @param dataChangeListener
     */
    public void removeDataChangeListener(DataChangeListener dataChangeListener) {
        synchronized (mLockObject) {
            if (mDataChangeListeners != null) {
                mDataChangeListeners.remove(dataChangeListener);
            }
        }
    }

    /**
     * @param dataType   为0时,表示data为User，dataType为１时,表示data为Post
     * @param data
     * @param oprateType 0举报, 1删除, 2拉黑, 3receivePostNotification, 4follow, 5unfollow,6编辑post
     */
    public void notifyDataChange(int dataType, Object data, int oprateType) {
        synchronized (mLockObject) {
            if (mDataChangeListeners == null || mDataChangeListeners.isEmpty()) {
                return;
            }
            DataChangeListener dataChangeListener;
            for (int i = 0; i < mDataChangeListeners.size(); ) {
                dataChangeListener = mDataChangeListeners.get(i);
                if (dataChangeListener == null) {
                    mDataChangeListeners.remove(i);
                    continue;
                }
                dataChangeListener.onDataChange(dataType, data, oprateType);
                ++i;
            }
        }
    }
}


