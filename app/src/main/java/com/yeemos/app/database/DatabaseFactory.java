package com.yeemos.app.database;

import com.yeemos.app.BaseApplication;
import com.yeemos.app.utils.Constants;

/**
 * Created by gigabud on 15-12-23.
 */
public class DatabaseFactory
{
    static public AppDatabaseOperate getDBOper()
    {
        return AppDatabaseOperate.getInstance(BaseApplication.getAppContext(), Constants.DB_NAME);
    }
}
