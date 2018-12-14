package com.yeemos.app.manager;

import android.app.Activity;
import android.content.Context;

import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBInstagram;
import com.gigabud.common.platforms.GBPlatform;
import com.gigabud.common.platforms.GBUserInfo;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.R;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

/***
 * MemberShip管理类(Singleton)
 *
 * @author Damon
 */
public class MemberShipManager extends GBSMemberShipManager {

    public static MemberShipManager getInstance() {
        if (GBSMemberShipManager.membershipManager == null || !(GBSMemberShipManager.membershipManager instanceof MemberShipManager)) {
            GBSMemberShipManager.membershipManager = new MemberShipManager();
        }
        return (MemberShipManager) GBSMemberShipManager.membershipManager;
    }

    public boolean isAppNeedFacebook() {
        return true;
    }

    public boolean isAppNeedTwitter() {
        return false;
    }

    public boolean isAppNeedGooglePlus() {
        return false;
    }

    /**
     * 有没有绑定第三方
     *
     * @return
     */
    public boolean hasBindThirdParty() {
        return getFacebookAccessToken() != null || getTwitterAccessToken() != null || getInstagramAccessToken() != null ? true : false;
    }

    public void thirdPartyChangePassWord(String newPsw, memberShipCallBack<GBUserInfo> listener) {
        GBMemberShip_V2.MemberShipThirdPartyType thridtyPartyType = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook;

        if (getFacebookAccessToken() != null) {
            thridtyPartyType = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook;
        } else if (getTwitterAccessToken() != null) {
            thridtyPartyType = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Twitter;
        } else if (getInstagramAccessToken() != null) {
            thridtyPartyType = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram;
        } else if (getGooglePlusAccessToken() != null) {
            thridtyPartyType = GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_GooglePlus;
        }
        changePasswordByThirdParty(thridtyPartyType, newPsw, listener);

    }

    /**
     * 构造
     */
    private MemberShipManager() {
        super();
    }


    /**
     * 检测用户名是否有效
     *
     * @return
     * @version 创建时间：2015-9-23 下午5:45:53
     */
    public static boolean isValidUsername(String userName) {
        userName = userName.toLowerCase(Locale.getDefault());
        String regEx = "[A-Za-z0-9._\\-]{4,20}$";
        return userName != null && matchRegEx(regEx, userName);
    }

    /**
     * 检测密码是否有效
     *
     * @return
     * @version 创建时间：2015-9-23 下午5:45:53
     */
    public static boolean isValidPsw(String psw) {
        return psw != null && psw.length() >= 4 && psw.length() <= 20;
    }

    /**
     * 检测邮箱是否有效
     *
     * @return
     * @version 创建时间：2015-9-23 下午5:45:53
     */
    public static boolean isVaildEmail(String email) {
        String regEx = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+([A-Za-z0-9]{2,4}|museum)$";
        return email != null && matchRegEx(regEx, email) && email.length() >= 5
                && email.length() <= 100 && !email.contains(" ");
    }

    /**
     * 显示Username格式错误提示框
     *
     * @param activity
     * @version 创建时间：2015-9-24 下午2:51:14
     */
    public static void showInVaildUsernameTipDialog(BaseActivity activity) {
        activity.showPublicDialog(activity.getString(R.string.app_name),
                ServerDataManager.getTextFromKey("sgn_up_err_username"),
                ServerDataManager.getTextFromKey("pub_btn_ok"), null, null);
    }

    /**
     * 显示Password格式错误提示框
     *
     * @param activity
     * @version 创建时间：2015-9-24 下午2:51:14
     */
    public static void showInVaildPswTipDialog(BaseActivity activity) {
        activity.showPublicDialog(activity.getString(R.string.app_name),
                ServerDataManager.getTextFromKey("sgn_up_err_password"),
                ServerDataManager.getTextFromKey("pub_btn_ok"), null, null);
    }

    /**
     * 显示Email格式错误提示框
     *
     * @param activity
     * @version 创建时间：2015-9-24 下午2:51:14
     */
    public static void showInVaildEmailTipDialog(BaseActivity activity) {
        activity.showPublicDialog(activity.getString(R.string.app_name),
                ServerDataManager.getTextFromKey("sgn_up_err_email"),
                ServerDataManager.getTextFromKey("pub_btn_ok"), null, null);
    }

    public GBPlatform getFacebook(Activity activity) {
        //need to overwrite
        if (mFacebook == null) {
            Class cls = GBPlatform.getGBPlatformClass("com.gigabud.common.platforms.GBFacebook");
            try {
                Constructor con = cls.getDeclaredConstructor(Context.class);
                con.setAccessible(true);
                mFacebook = (GBPlatform) con.newInstance(getApplicationContext()); //new GBFacebook(getApplicationContext());;
                mFacebook.setAppId("597941957078740");
                mFacebook.setStrAppSecret("aa44bea6358ee3196ca0eeacce03740a");
                mFacebook.setStrRedirectURL("http://www.gigabud.com/");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (mFacebook != null && activity != null) {
            mFacebook.setActivity(activity);
        }
        return mFacebook;
    }

    public GBInstagram getInstagram(Context context) {
        if (mInstagram == null) {
            mInstagram = new GBInstagram(BaseApplication.getAppContext());
            mInstagram.setStrAppKey("e5737bd0820c419ba7cdc29911edefe1");
            mInstagram.setStrAppSecret("bcdbb30abc3d4f649f9cfe75d7728b9c");
            mInstagram.setStrRedirectURL("http://www.yeemos.com");
        }
        if (context != null) {
            mInstagram.setContext(context);
        }
        return mInstagram;
    }

    public GBPlatform getTwitter(Context context) {
        if (mTwitter == null) {
            Class cls = GBPlatform.getGBPlatformClass("com.gigabud.common.platforms.GBTwitter");
            try {
                Constructor con = cls.getDeclaredConstructor(Context.class);
                con.setAccessible(true);
                mTwitter = (GBPlatform) con.newInstance(getApplicationContext()); //new GBTwitter(getApplicationContext());;
                mTwitter.setStrAppKey("7k9lUZrOFgoyI4qedVyXky0oJ");
                mTwitter.setStrAppSecret("9GqiPbS6B0xbN8v7vHoEub4SJHgaqyYa6pSrb7codZFHvj0QIt");
                mTwitter.setStrRedirectURL("http://www.google.com");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        if (mTwitter != null && context != null) {
            mTwitter.setContext(context);
        }
        return mTwitter;
    }
}

