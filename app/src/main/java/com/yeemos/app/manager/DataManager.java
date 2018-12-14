package com.yeemos.app.manager;

import com.gbsocial.datamanage.GBSDataManager;
import com.yeemos.app.interfaces.IDataManager;

/**
 * 数据管理类，提供底层数据操作的方法和存储临时数据
 *
 * @author xiangwei.ma
 */

public abstract class DataManager implements IDataManager {
    private static boolean controlsVisible = true;
    // 测试开关
    protected boolean _debug = false;


    static public IDataManager getInstance() {
        if (null == GBSDataManager.gDataManager) {
            GBSDataManager.gDataManager = new YemmosDataManager();
        }
        return (IDataManager) GBSDataManager.gDataManager;
    }

    static public void reset() {
        GBSDataManager.gDataManager = null;
    }

    public DataManager() {
    }

    @Override
    public void recoverDefault() {
    }


    public static boolean isControlsVisible() {
        return controlsVisible;
    }

    public static void setControlsVisible(boolean isVisible) {
        controlsVisible = isVisible;
    }


}