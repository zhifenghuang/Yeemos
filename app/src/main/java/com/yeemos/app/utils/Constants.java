package com.yeemos.app.utils;

import com.gbsocial.constants.GBSConstants;
import com.yeemos.app.R;

/**
 * Created by gigabud on 15-12-4.
 */
public class Constants {

    public static final String DB_NAME = "chat.db";
    // 是否测试模式
    public static final boolean DEBUG_MODE = false;


    // 正式环境
    public static final String PRODUCT_APPCONFIG_URL = "http://member.yeemos.com/message/ws/rest/messages/url.json";
    public static final String PRODUCT_UPGRADE_URL = "http://member.yeemos.com/message/ws/rest/messages/inform.json";
    public static final String PRODUCT_MEMBERSHIP_URL = "https://app.yeemos.com/yeemos";
    public static final String PRODUCT_i18n_URL = "http://member.yeemos.com";
    public static final String PRODUCT_YEEMOS_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat";
    public static final String PRODUCT_FILE_HTTP_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat/uploadFile";
    public static final String PRODUCT_FILE_DOWND_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat/downloadFile.do?fileName=";
    public static final String PRODUCT_DELETE_MSG_RECORD_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat/deleteMsg.do?";
    public static final String PRODUCT_SEND_MSG_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat/sendmsg.do";
    public static final String PRODUCT_GET_MSG_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat/getmsg.do";
    //    public static final String PRODUCT_DOWNLOAD_POST_FILE_URL = "http://app.yeemos.com/yeemos/user/downloads.do?key=%s&token=%s";
    public static final String PRODUCT_PUSH_MSG_URL = "http://rabbitmq-52652949.ap-southeast-1.elb.amazonaws.com/yeemosChat/pushmsg.do";
    public static final String PRODUCT_DOWNLOAD_POST_FILE_URL = "https://d2zyh4u1auj2ov.cloudfront.net/%s";

    // 测试环境
    public static final String DEBUG_APPCONFIG_URL = "http://member.yeemos.com/message/ws/rest/messages/url.json";
    public static final String DEBUG_UPGRADE_URL = "http://awstestjp.gigabud.com:7272/message/ws/rest/messages/inform.json";
    public static final String DEBUG_MEMBERSHIP_URL = "http://awstestjp.gigabud.com:7272/yeemos";
    public static final String DEBUG_i18n_URL = "http://member.yeemos.com";
    public static final String DEBUG_YEEMOS_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat";
    public static final String DEBUG_FILE_HTTP_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/uploadFile";
    public static final String DEBUG_FILE_DOWND_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/downloadFile.do?fileName=";
    public static final String DEBUG_SEND_MSG_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/sendmsg.do";
    public static final String DEBUG_GET_MSG_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/getmsg.do";
    public static final String DEBUG_DELETE_MSG_RECORD_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/deleteMsg.do?";
    public static final String DEBUG_PUSH_MSG_URL = "http://rabbitmq-2035253031.ap-northeast-1.elb.amazonaws.com/yeemosChat/pushmsg.do";
    public static final String DEBUG_DOWNLOAD_POST_FILE_URL = "http://awstestjp.gigabud.com:7272/yeemos/user/downloads.do?key=%s&token=%s";

    public final static String PROJECT_NAME = "Yeemos";
    public final static String VIDEO_EXTENSION = ".mp4";
    public final static String IMAGE_EXTENSION = ".jpg";
    public final static String GIF_EXTENSION = ".gif";
    public final static String VIDEO_CONTENT_URI = "content://media/external/video/media";

    public static final int ONLY_TEXT = 0;   //只有文字
    public static final int PIC_SHADER_FILTER = 1;  //图片滤镜
    //    public static final int BACK_CAMERAE_VIDEO_SHADER_FILTER = 2;  //后置摄像头滤镜
//    public static final int FRONT_CAMERA_VIDEO_SHADER_FILTER = 3;  //前置摄像头滤镜
    public static final int VIDEO_DEGREE_0 = 2;  //视屏旋转0度
    public static final int VIDEO_DEGREE_90 = 3;  //视屏转90度
    public static final int VIDEO_DEGREE_180 = 4;  //视屏转180度
    public static final int VIDEO_DEGREE_270 = 5;  //视屏转270度

    public static final int COMMON_SHADER_TYPE = 0;  //普通滤镜
    public static final int BEAUTIFY_SKIN_SHADER_TYPE = 20;  //美肤滤镜

    public static final int MORE_POPUPWINDOW_MYPOST = 0;
    public static final int MORE_POPUPWINDOW_OTHERPOST = 1;
    public static final int MORE_POPUPWINDOW_MENU = 2;
    public static final int MORE_POPUPWINDOW_CHANGE_AVATAR = 3;
    public static final int MORE_POPUPWINDOW_CHANGE_BG = 4;
    public static final int MORE_POPUPWINDOW_OTHERUSER_MORE = 5;
    public static final int MORE_POPUPWINDOW_UNFOLLOWREQUEST = 6;
    public static final int MORE_POPUPWINDOW_COMMENT = 7;
    public static final int MORE_POPUPWINDOW_OTHERUSER_FOLLOWING = 8;
    public static final int MORE_POPUPWINDOW_UNLINK = 9;
    public static final int MORE_POPUPWINDOW_COMMENT_OPERATE = 10;
    public static final int MORE_POPUPWINDOW_FRIEND_GROUP_OPERATE = 11;
    public static final int MORE_POPUPWINDOW_HANDLE_FAILED_POST = 12;

    public static final int PLATFORM_ANDROID = 1;  // 安卓平台

    public static final String DELETE_POST = "DELETE_POST";

    public static final String ROME_USER = "REMOVE_USER";

    public static final String FLIP_TUTORIAL_IN_CAMERA_FRAGMENT = "flip_tutorial_in_camera_fragment";
    public static final String TUTORIAL_SEARCH_FRIEND = "tutorial_search_friend";
    public static final String TUTORIAL_IN_CAMERA_FRAGMENT = "tutorial_in_camera_fragment";
    public static final String TUTORIAL_IN_HOME_FRAGMENT = "tutorial_in_home_fragment";
    public static final String TUTORIAL_IN_EDIT_POST_FRAGMENT = "tutorial_in_edit_post_fragment";
    public static final String TUTORIAL_IN_SHOW_POST_FRAGMENT = "tutorial_in_show_post_fragment";
    public static final String TUTORIAL_IN_CHAT_LIST_FRAGMENT = "tutorial_in_chat_list_fragment";
    //  public static final String TUTORIAL_IN_CHAT_LIST_FRAGMENT_2="tutorial_in_chat_list_fragment_2";
    public static final String TUTORIAL_IN_FIND_USERS_FRAGMENT = "tutorial_in_find_users_fragment";

    public static final String TUTORIAL_IN_DRAW_COMMENT_VIEW = "tutorial_in_draw_comment_view";
    /**
     * 谷歌推送相关key
     */
    public static final String SENDER_ID = "369087697276";  //AIzaSyAT6ktT-iJtEcL3FzpGbBrBQMzg-C0Kw80

    public static final int[][] EMO_ID_COLOR = {   //表情资源ID和对应的颜色
            {R.drawable.emo_happy, -541169, R.drawable.round_emo_happy_on, R.drawable.round_emo_happy_off, R.drawable.sent_happy_on, R.drawable.sent_happy_off},
            {R.drawable.emo_love, -45671, R.drawable.round_emo_love_on, R.drawable.round_emo_love_off, R.drawable.sent_love_on, R.drawable.sent_love_off},
            {R.drawable.emo_wow, -13576977, R.drawable.round_emo_wow_on, R.drawable.round_emo_wow_off, R.drawable.sent_wow_on, R.drawable.sent_wow_off},
            {R.drawable.emo_bad, -15821568, R.drawable.round_emo_bad_on, R.drawable.round_emo_bad_off, R.drawable.sent_bad_on, R.drawable.sent_bad_off},
            {R.drawable.emo_cry, -16756318, R.drawable.round_emo_cry_on, R.drawable.round_emo_cry_off, R.drawable.sent_cry_on, R.drawable.sent_cry_off},
            {R.drawable.emo_fear, -7403909, R.drawable.round_emo_fear_on, R.drawable.round_emo_fear_off, R.drawable.sent_fear_on, R.drawable.sent_fear_off},
            {R.drawable.emo_angry, -1434589, R.drawable.round_emo_angry_on, R.drawable.round_emo_angry_off, R.drawable.sent_angry_on, R.drawable.sent_angry_off},
    };


    public static final String[][] STRICK_NAMES = {
            {"mith_000", "mith_010", "mith_020", "mith_030", "mith_040", "mith_050", "mith_060", "mith_070"},
            {"svg_birthdaycake", "svg_candy", "svg_confetti",
                    "svg_confetti_2", "svg_dollar", "svg_donut","svg_eyemask", "svg_garland", "svg_like",
                    "svg_mask", "svg_masks", "svg_sunglasses"},
            {"s1_01", "s1_02", "s1_03", "s1_04", "s1_05", "s1_06", "s1_07", "s1_08", "s1_09", "s1_10", "s1_11", "s1_12", "s1_13", "s1_14", "s1_15",
                    "s1_16", "s1_17", "s1_18", "s1_19", "s1_20", "s1_21", "s1_22", "s1_23", "s1_24", "s1_25", "s1_26", "s1_27", "s1_28", "s1_29",
                    "s1_30", "s1_31", "s1_32", "s1_33", "s1_34", "s1_35", "s1_36", "s1_37", "s1_38", "s1_39", "s1_40", "s1_41", "s1_42",
                    "s1_43", "s1_44", "s1_45", "s1_46", "s1_47", "s1_48", "s1_49", "s1_50", "s1_51", "s1_52", "s1_53", "s1_54", "s1_55", "s1_56", "s1_57",
                    "s1_58", "s1_59", "s1_60", "s1_61", "s1_62", "s1_63", "s1_64", "s1_65", "s1_66", "s1_67", "s1_68"}
    };

    public static int getEmoIdByTagStr(String tagStr) {
        if (tagStr.equals("happy")) {
            return 0;
        } else if (tagStr.equals("love")) {
            return 1;
        } else if (tagStr.equals("wow")) {
            return 2;
        } else if (tagStr.equals("bad")) {
            return 3;
        } else if (tagStr.equals("cry")) {
            return 4;
        } else if (tagStr.equals("fear")) {
            return 5;
        } else if (tagStr.equals("angry")) {
            return 6;
        }
        return 0;
    }


    public static final int[] SYSTEM_EMO_IDS = {  //系统自带表情Id
            0x1F600, 0x1F601, 0x1F602, 0x1F60E, 0x1F60D, 0x1F914, 0x1F61D, 0x1F631, 0x1F621, 0x1F608,
            0x1F479, 0x1F480, 0x1F47B, 0x1F47E, 0x1F916, 0x1F4A9, 0x1F63A, 0x1F64A, 0x1F466, 0x1F467,
            0x1F468, 0x1F469, 0x1F476, 0x1F47C, 0x1F471, 0x1F46E, 0x1F478, 0x1F482, 0x1F575, 0x1F385,
            0x1F486, 0x1F487, 0x1F64B, 0x1F6B6, 0x1F483, 0x1F46F, 0x1F46B, 0x1F48F, 0x1F491, 0x1F46A,
            0x1F4AA, 0x1F448, 0x1F449, 0x1F446, 0x1F595, 0x1F447, 0x1F918, 0x1F44C, 0x1F44D, 0x1F44E,
            0x1F64F, 0x1F485, 0x1F442, 0x1F443, 0x1F463, 0x1F441, 0x1F445, 0x1F444, 0x1F494, 0x1F4A4,
            0x1F4A2, 0x1F4A3, 0x1F4A6, 0x1F4A8, 0x1F4AC, 0x1F453, 0x1F576, 0x1F454, 0x1F459, 0x1F45B,
            0x1F45E, 0x1F460, 0x1F451, 0x1F3A9, 0x1F484, 0x1F48E, 0x1F435, 0x1F436, 0x1F43A, 0x1F42F,
            0x1F434, 0x1F42E, 0x1F437, 0x1F42D, 0x1F439, 0x1F43B, 0x1F424, 0x1F438, 0x1F40D, 0x1F433,
            0x1F419, 0x1F34E, 0x1F37C, 0x1F302, 0x1F525, 0x1F383, 0x1F384, 0x1F389, 0x1F380, 0x1F3C0
    };

    public static final int[] WEEKDAYS = {  //星期几
            R.string.edtpst_txt_weekday7, R.string.edtpst_txt_weekday1, R.string.edtpst_txt_weekday2, R.string.edtpst_txt_weekday3,
            R.string.edtpst_txt_weekday4, R.string.edtpst_txt_weekday5, R.string.edtpst_txt_weekday6,
    };

    public static final int[] MONTHS = {  //月份
            R.string.edtpst_txt_month1, R.string.edtpst_txt_month2, R.string.edtpst_txt_month3, R.string.edtpst_txt_month4,
            R.string.edtpst_txt_month5, R.string.edtpst_txt_month6, R.string.edtpst_txt_month7, R.string.edtpst_txt_month8,
            R.string.edtpst_txt_month9, R.string.edtpst_txt_month10, R.string.edtpst_txt_month11, R.string.edtpst_txt_month12
    };

    /***
     * 支持的Mode
     */
    public enum MPagerListMode {
        MPagerListMode_AddressBook(1),
        MPagerListMode_Facebook(2),
        MPagerListMode_Search(3),
        MPagerListMode_Users(4),
        MPagerListMode_HashTags(5),
        MPagerListMode_ACTIVITIES_You(10);
        int nValues;

        private MPagerListMode(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static MPagerListMode GetObject(int nNum) {
            MPagerListMode[] As = MPagerListMode.values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return MPagerListMode_Search;
        }
    }


    public enum EMO_IDS {
        EMO_HAPPY(0),
        EMO_LOVE(1),
        EMO_NORMAL(2),
        EMO_BAD(3),
        EMO_CRY(4),
        EMO_FEAR(5),
        EMO_ANGRY(6);

        int value;

        private EMO_IDS(int i) {
            value = i;
        }

        public int GetValue() {
            return value;
        }
    }

    public enum POST_ATTACH_DATA_TYPE {
        IMAGE_TEXT(0),
        GIF_TEXT(1),
        ONLY_TEXT(2),
        VIDEO_TEXT(3);

        int value;

        private POST_ATTACH_DATA_TYPE(int i) {
            value = i;
        }

        public int GetValue() {
            return value;
        }
    }

    public enum PHONE_FRAGMENT_UI_POSITION {
        PHONE_FRAGMENT_UI_NONE(0), // 默认值
        PHONE_FRAGMENT_UI_EXCHANGE_POSITION(1), // 替换之前的
        PHONE_FRAGMENT_UI_POPUP_POSITION(2), // 弹窗
        PHONE_FRAGMENT_UI_ALONE_POSITION(3), // 单独一个页面 使用EmptyActivity
        PHONE_FRAGMENT_UI_ALONE_POSITION_TWO(4), // 单独一个页面 使用EmptyTwoActivity
        PHONE_FRAGMENT_UI_ALONE_POSITION_THREE(5); // 单独一个页面 使用EmptyTwoActivity

        int nValues;

        private PHONE_FRAGMENT_UI_POSITION(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static PHONE_FRAGMENT_UI_POSITION GetObject(int nNum) {
            PHONE_FRAGMENT_UI_POSITION[] As = PHONE_FRAGMENT_UI_POSITION
                    .values();
            for (int i = 0; i < As.length; i++) {
                if (As[i].Compare(nNum))
                    return As[i];
            }
            return PHONE_FRAGMENT_UI_EXCHANGE_POSITION;
        }

    }

    /*
     * |-----------------------------|-----------------------|--------------------
     * |
     * --------------------------------------------------------------------------
     * ------------------------------------------ | | 描述 | BROADCAST_TYPE |
     * BROADCAST_CONTENT
     * |-----------------------------|-----------------------|--
     * ------------------
     * |--------------------------------------------------------
     * ------------------------------------------------------------ | | | | | |
     * | | | | 推 送 相 关 | | | | | | | | | | | | | | |
     * |-----------------------|----
     * ----------------|----------------------------
     * ------------------------------
     * ---------------------------------------------------------- | |
     * |BROADCAST_REFRESHUI_CATEGORY | 数据更新，刷新UI | DATA_UPDATE | | | | | | | | |
     * | | | | | | | |
     * |-----------------------------|-----------------------|----
     * ----------------
     * |----------------------------------------------------------
     * -------------------------------------- | | eg:
     *
     * 广播的消息结构 BROADCAST_REFRESHUI_CATEGORY {
     * BROADCAST_TYPE:BROADCAST_RECEIVE_PUSH_TYPE/BROADCAST_DATA_UPDATE_TYPE
     * BROADCAST_CONTENT:CREATE_MENU/CREATE_POST BROADCAST_ERROR:GBxxxxxx
     * BROADCAST_SUCCESS:true/false }
     */
    public static final String BROADCAST_REFRESHUI_CATEGORY = "BROADCAST_REFRESHUI_CATEGORY";
    public static final String BROADCAST_TYPE = "BROADCAST_TYPE";
    public static final String BROADCAST_CONTENT = "BROADCAST_CONTENT";
    public static final String BROADCAST_ERROR = "BROADCAST_ERROR";
    public static final String BROADCAST_SUCCESS = "BROADCAST_SUCCESS";

    // 收到推送
    public static final String BROADCAST_RECEIVE_PUSH_TYPE = "BROADCAST_RECEIVE_PUSH_TYPE";

    // 数据修改
    public static final String BROADCAST_DATA_UPDATE_TYPE = "BROADCAST_DATA_UPDATE_TYPE";

    /*
 一级数据为:	HOME,USER,Restaurant  每个1级别数据下的二级数据为:POST Menu Location
*/
    public static final int BASEOBJ_LIST_OWNER_MY_FOLLOWER = 0;    //我关注的人   发的POST Menu Location
    public static final int BASEOBJ_LIST_OWNER_USER = 1;        //自己或者别人 发的POST Menu Location
    public static final int BASEOBJ_LIST_OWNER_RESTAURANT = 2;    //所属餐厅    发的POST Menu Location
    public static final int BASEOBJ_LIST_OWNER_MENU = 3;//Menu 下的POST Menu Location
    public static final int BASEOBJ_LIST_OWNER_FAVORITE = 4;//自己喜欢的POST

    public static final int BASEOBJ_LIST_DATA_TYPE_POST = 0;    // POST
    public static final int BASEOBJ_LIST_DATA_TYPE_MENU = 1;    // Menu
    public static final int BASEOBJ_LIST_DATA_TYPE_LOCATION = 2;// location

    /**
     * 用户列表数据类型
     */
    public static final int USERLIST_LIST_DATA_TYPE_FOLLOWER = 41;        // FOLLOWER页面
    public static final int USERLIST_LIST_DATA_TYPE_FOLLOWING = 42;        // FOLLOW页面

    public static final int DIALOG_LEFT_BTN = 0;
    public static final int DIALOG_CENTER_BTN = 1;
    public static final int DIALOG_RIGHY_BTN = 2;

    public static final String REFRESH_MESSAGE_PAGE = "REFRESH_MESSAGE_PAGE";
    public static final int PAGE_NUMBER = GBSConstants.PAGE_NUMBER;


    //传递数据的KEY
    public static final String KEY_SEND_STRING_TO_HASHTAGSFRAGMENT = "send_string_to_hashtagsfragment";

    /**
     * 药丸图片正常时缩放比例
     */
    public static final float NORMAL_EMOS_SCALE = 0.8f;

    /**
     * APP是否处于打开状态
     */
    public static final String APP_IS_RUNNING = "APP_IS_RUNNING";


    public static final String DEFAULT_PHONE_ZIP = "852";
    public static final String DEFAULT_COUNTRY_ZIP = "HK";

    public static final String LAST_SELECT_GROUP = "Last_Select_Group";

}