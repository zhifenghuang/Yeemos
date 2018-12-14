package com.yeemos.app.interfaces;

/**
 * Created by gigabud on 16-11-11.
 */

public interface DataChangeListener {
    /**
     *
     * @param dataType 为0时,表示data为User，dataType为１时,表示data为Post
     * @param data
     * @param oprateType 0举报, 1删除, 2拉黑, 3receivePostNotification, 4follow , 5unfollow
     */
    public void onDataChange(int dataType, Object data, int oprateType);
}
