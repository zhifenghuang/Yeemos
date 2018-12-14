package com.yeemos.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.gbsocial.BeansBase.PushMessageBean;
import com.gbsocial.memberShip.GBSMemberShipManager.memberShipCallBack;
import com.gigabud.common.membership_v2.GBMemberShip_V2.MemberShipPushServerType;
import com.gigabud.core.util.ConnectedUtil;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.chat.Interface.IChat;
import com.yeemos.app.chat.bean.IMsg;
import com.yeemos.app.chat.manager.CRabbitMQChat;
import com.yeemos.app.fragment.ShowPostViewPagerFragment;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.receiver.NotificationBroadcastReceiver;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Preferences;

import java.util.Map;

/*
 * 推送服务
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FCM";
    public static final String PUSH_MESSAGE_BEAN = "PushMessageBean";
    private static int mNotificationID = -1;

    public MyFirebaseMessagingService() {
    }

    public enum PushType {
        TYPE_SYSTEM_MESSAGE(0),
        TYPE_OTHER_FOLLOW_YOU(1),
        TYPE_OTHER_AGREE_YOUR_REQUEST(3),
        TYPE_OTHER_COMMENT_YOUR_POST(5),
        TYPE_OTHER_REPLY_TAG_FOR_YOUR_POST(6),
        TYPE_OTHER_AT_YOU(8),
        TYPE_OTHER_REQUEST_FOLLOW_YOU(13),
        TYPE_YOUR_FOLLOWER_SEND_POST(14),
        TYPE_FACEBOOK_FRIENDS_JOIN(15),
        TYPE_INSTAGRAM_FRIENDS_JOIN(16),
        TYPE_OTHER_DRAWING_COMMENT(18),
        TYPE_OTHER_REPLY_YOUR_DRAWING_COMMENT(19),
        TYPE_OPEN_POST_NOTIFICATION_FOR_OTHER(23),
        TYPE_OPEN_A_POST(31),
        TYPE_SEE_A_USER(32),
        TYPE_OPEN_A_WEBSITE(33),
        TYPE_OPEN_A_TOPIC(34),
        TYPE_OPEN_A_HASHTAG(35),
        TYPE_OPEN_A_EMO(36),
        TYPE_OPEN_APP_STORE(37),
        TYPE_OPEN_CAMERA(38),
        TYPE_RECEIVE_FRIEND_MESSAGE(1001),
        TYPE_FRIEND_IS_TYPING(1002);

        private int value;

        private PushType(int value) {
            this.value = value;
        }

        public static PushType valueOf(int value) {
            switch (value) {
                case 0:
                    return TYPE_SYSTEM_MESSAGE;
                case 1:
                    return TYPE_OTHER_FOLLOW_YOU;
                case 3:
                    return TYPE_OTHER_AGREE_YOUR_REQUEST;
                case 5:
                    return TYPE_OTHER_COMMENT_YOUR_POST;
                case 6:
                    return TYPE_OTHER_REPLY_TAG_FOR_YOUR_POST;
                case 8:
                    return TYPE_OTHER_AT_YOU;
                case 13:
                    return TYPE_OTHER_REQUEST_FOLLOW_YOU;
                case 14:
                    return TYPE_YOUR_FOLLOWER_SEND_POST;
                case 15:
                    return TYPE_FACEBOOK_FRIENDS_JOIN;
                case 16:
                    return TYPE_INSTAGRAM_FRIENDS_JOIN;
                case 18:
                    return TYPE_OTHER_DRAWING_COMMENT;
                case 19:
                    return TYPE_OTHER_REPLY_YOUR_DRAWING_COMMENT;
                case 23:
                    return TYPE_OPEN_POST_NOTIFICATION_FOR_OTHER;
                case 31:
                    return TYPE_OPEN_A_POST;
                case 32:
                    return TYPE_SEE_A_USER;
                case 33:
                    return TYPE_OPEN_A_WEBSITE;
                case 34:
                    return TYPE_OPEN_A_TOPIC;
                case 35:
                    return TYPE_OPEN_A_HASHTAG;
                case 36:
                    return TYPE_OPEN_A_EMO;
                case 37:
                    return TYPE_OPEN_APP_STORE;
                case 38:
                    return TYPE_OPEN_CAMERA;
                case 1001:
                    return TYPE_RECEIVE_FRIEND_MESSAGE;
                case 1002:
                    return TYPE_FRIEND_IS_TYPING;

                default:
                    return null;
            }
        }

        public int value() {
            return this.value;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public void onNewToken(String registrationId) {
        super.onNewToken(registrationId);
        sendRegistrationToServer(registrationId);
    }


//    @Override
//    protected void onUnregistered(Context context, String registrationId) {
//        logs("onUnregistered");
//        Preferences.getInstacne().setValues("hadSetPushToken", false);
//        MemberShipManager.getInstance().pushOff(
//                MemberShipPushServerType.MemberShip_PushServerType_Official,
//                null);
//    }

    /**
     * 处理消息
     *
     * @param pushMessageBean
     */
    private void handlePushMessageByType(PushMessageBean pushMessageBean) {
        int type = 0;
        try {
            type = Integer.parseInt(pushMessageBean.getType());
        } catch (Exception e) {
            Log.e(TAG, "Push message type error: " + e.toString());
            return;
        }

        if (type < 0) {
            return;
        }
        showNotification(this, pushMessageBean);
    }

    public static void showNotification(Context context, PushMessageBean pushMessageBean) {

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context).setDefaults(Notification.DEFAULT_ALL).setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(context.getString(R.string.app_name));
        // 显示通知
        PushType pushType = PushType.valueOf(Integer.parseInt(pushMessageBean
                .getType()));
        int notificationId;
        if (pushType == PushType.TYPE_RECEIVE_FRIEND_MESSAGE) {
            int num = Preferences.getInstacne().getValues(pushMessageBean.getCuid(), 0);
            if (num > 0) {
                builder.setContentText(pushMessageBean.getBody() + "(" + (num + 1) + ")");
            } else {
                builder.setContentText(pushMessageBean.getBody());
            }
            Preferences.getInstacne().setValues(pushMessageBean.getCuid(), ++num);
            notificationId = Integer.parseInt(pushMessageBean.getCuid());
            Intent intentClick = new Intent(context, NotificationBroadcastReceiver.class);
            intentClick.setAction("notification_clicked");
            intentClick.putExtra(PUSH_MESSAGE_BEAN, pushMessageBean);
            PendingIntent pendingIntentClick = PendingIntent.getBroadcast(context, notificationId, intentClick, PendingIntent.FLAG_ONE_SHOT);

            Intent intentCancel = new Intent(context, NotificationBroadcastReceiver.class);
            intentCancel.setAction("notification_cancelled");
            intentCancel.putExtra(PUSH_MESSAGE_BEAN, pushMessageBean);
            PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(context, notificationId, intentCancel, PendingIntent.FLAG_ONE_SHOT);

            builder.setContentIntent(pendingIntentClick);
            builder.setDeleteIntent(pendingIntentCancel);
        }
//        else if (pushType == PushType.TYPE_SYSTEM_MESSAGE) {
//            builder.setContentText(pushMessageBean.getBody());
//            notificationId = (++mNotificationID);
//
//        }
        else if (pushType == PushType.TYPE_OPEN_APP_STORE) {
            builder.setContentText(pushMessageBean.getBody());
            notificationId = (++mNotificationID);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (!TextUtils.isEmpty(pushMessageBean.getId())) {
                intent.setData(Uri.parse("market://details?id=" + pushMessageBean.getId()));
            } else {
                intent.setData(Uri.parse("market://details?id=" + context.getPackageName()));
            }
            PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
        } else {
            builder.setContentText(pushMessageBean.getBody());
            notificationId = (++mNotificationID);
            Intent intent = new Intent(context, HomeActivity.class);
            intent.putExtra(PUSH_MESSAGE_BEAN, pushMessageBean);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.drawable.push_notification_icon);
            builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.icon))
                    .setColor(Color.WHITE);
        } else {
            builder.setSmallIcon(R.mipmap.icon);
        }

        notificationManager.notify(notificationId, builder.build());
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage != null && Preferences.getInstacne().getBoolByKey(HomeActivity.HAD_SET_FIREBASE_PUSH_TOKEN)) {
            Map map = remoteMessage.getData();
            if (map != null) {
                Gson gson = new Gson();
                PushMessageBean pushMessageBean = gson.fromJson(gson.toJson(map),
                        PushMessageBean.class);
                if (pushMessageBean == null) {
                    return;
                }
                PushType pushType = PushType.valueOf(Integer.parseInt(pushMessageBean
                        .getType()));
                if (pushType == PushType.TYPE_SYSTEM_MESSAGE || pushType == PushType.TYPE_OPEN_APP_STORE) {
                    handlePushMessageByType(pushMessageBean);
                    return;
                }
                String userId = MemberShipManager.getInstance().getUserID();
                if (userId == null || userId.trim().length() == 0) {
                    return;
                }
                if (pushMessageBean.getTuid() != null && pushMessageBean.getTuid().equals(userId)) { // 当接到推送消息的用户与本地登陆用户不一致时不做处理

                    if (pushType.value() <= PushType.TYPE_OPEN_POST_NOTIFICATION_FOR_OTHER.value()) {
                        if (pushType == PushType.TYPE_OTHER_REQUEST_FOLLOW_YOU) {
                            Preferences.getInstacne().setValues(HomeActivity.HAD_REQUEST_FOLLOW_YOU_MESSAGE, true);
                        } else {
                            Preferences.getInstacne().setValues(HomeActivity.HAD_NEW_NOTIFICATION_MESSAGE, true);
                        }
                        Intent i = new Intent(HomeActivity.NOTIFICATION_MESSAGE_LISTENER);
                        i.putExtra(PUSH_MESSAGE_BEAN, pushMessageBean);
                        sendBroadcast(i);
                    } else if (pushType == PushType.TYPE_RECEIVE_FRIEND_MESSAGE) {
                        String msgId = pushMessageBean.getMsgid();
                        boolean isMsgExist = IMsg.isMessageExist(msgId);
                        if (!isMsgExist && ConnectedUtil.isConnected(this)) {
                            IChat.getInstance().connectServer(MemberShipManager.getInstance().getToken(),
                                    Long.parseLong(userId));
                            ((CRabbitMQChat) CRabbitMQChat.getInstance()).getRabbitMQManager().httpGetUnReceiverMessage(this, Long.parseLong(userId));
                        }
                    }

                    if (pushType != null && pushType == PushType.TYPE_RECEIVE_FRIEND_MESSAGE) {
                        int unreadMsgNum = IMsg.getUnReadMsgFriendNum(Long.parseLong(MemberShipManager.getInstance().getUserID()));
                        BadgeUtil.setBadgeCount(this, unreadMsgNum == 0 ? 1 : unreadMsgNum);
                    }


//            NotifyCenter.sendBoardcastByDataUpdate(Constants.DELETE_POST);
                    if (Preferences.getInstacne().isRunning()) {
                        if (PushType.valueOf(Integer.valueOf(pushMessageBean.getType())) == PushType.TYPE_RECEIVE_FRIEND_MESSAGE
                                && BaseApplication.getCurFragment() != null && BaseApplication.getCurFragment().getClass().isAssignableFrom(ShowPostViewPagerFragment.class)) {
                            ((ShowPostViewPagerFragment) BaseApplication.getCurFragment()).showPostViewPopup(pushMessageBean);
                        }
                    } else {
                        handlePushMessageByType(pushMessageBean);
                    }
                } else {
                    if (pushType.value() >= PushType.TYPE_OPEN_A_POST.value() && !Preferences.getInstacne().isRunning()) {
                        handlePushMessageByType(pushMessageBean);
                    }
                }
            }
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    private void sendRegistrationToServer(String token) {
        if (TextUtils.isEmpty(token)) {
            return;
        }
        String userId = MemberShipManager.getInstance().getUserID();
        if (userId == null || userId.trim().length() == 0) {  //未登录
            return;
        }
        MemberShipManager.getInstance().pushOn(token,
                MemberShipPushServerType.MemberShip_PushServerType_Official,
                new memberShipCallBack<Object>() {

                    @Override
                    public void timeOut() {
                    }

                    @Override
                    public void success(Object obj) {
                        Preferences.getInstacne().setValues(HomeActivity.HAD_SET_FIREBASE_PUSH_TOKEN,
                                true);
                    }

                    @Override
                    public void fail(String errorStr) {
                    }

                    @Override
                    public void cancel() {
                    }
                });
    }

}
