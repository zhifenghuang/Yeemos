package com.yeemos.app.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.gbsocial.BeansBase.PushMessageBean;
import com.yeemos.app.MyFirebaseMessagingService;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.utils.Preferences;

/**
 * Created by gigabud on 16-11-2.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        PushMessageBean pushMessageBean = (PushMessageBean) intent
                .getSerializableExtra(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN);
        if (pushMessageBean == null) {
            return;
        }
        Preferences.getInstacne().setValues(pushMessageBean.getCuid(), 0);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if (action.equals("notification_clicked")) {
            Intent i = new Intent(context, HomeActivity.class);
            i.putExtra(MyFirebaseMessagingService.PUSH_MESSAGE_BEAN, pushMessageBean);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        }
    }
}
