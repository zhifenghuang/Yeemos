package com.yeemos.app.fragment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gbsocial.constants.GBSConstants;
import com.gbsocial.datamanage.GBSDataManager;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.preferences.GBSPreferences;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.membership_v2.GBMemberShip_V2;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.common.platforms.utils.PreferencesWrapper;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.chat.manager.RabbitMQManager;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.BadgeUtil;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.PagerDotView;

import java.util.ArrayList;

/**
 * Created by gigabud on 15-12-8.
 */
public class FirstFragment extends BaseFragment implements View.OnClickListener {

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_first;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        BadgeUtil.resetBadgeCount(getActivity());
        view.findViewById(R.id.btnSignUp).setOnClickListener(this);
        view.findViewById(R.id.btnLogin).setOnClickListener(this);
        view.findViewById(R.id.facebookBtn).setOnClickListener(this);
        //      getView().findViewById(R.id.instagramBtn).setOnClickListener(this);

        final ArrayList<View> viewList = new ArrayList<>();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        for (int i = 0; i < 4; ++i) {
            viewList.add(inflater.inflate(R.layout.first_pager_item, null));
        }
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        final PagerDotView pagerDotView = getActivity().findViewById(R.id.pagerDotView);
        pagerDotView.setTotalPage(viewList.size());
        pagerDotView.setCurrentPageIndex(0);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView(viewList.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                View itemView = viewList.get(position);
                TextView tv1 = itemView.findViewById(R.id.tv1);
                TextView tv2 = itemView.findViewById(R.id.tv2);
                ImageView iv = itemView.findViewById(R.id.iv);
                int resId = getResources().getIdentifier("reg" + (position + 1) + "_con", "drawable", getActivity().getPackageName());
                iv.setImageResource(resId);
                if (position == 0) {
                    tv1.setText("");
                    tv2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    tv2.setText(ServerDataManager.getTextFromKey("intl_txt_anonymoussocialnetwork"));
                } else if (position == 1) {
                    tv1.setText(ServerDataManager.getTextFromKey("intl_txt_stayanonymous"));
                    tv2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    tv2.setText(ServerDataManager.getTextFromKey("intl_txt_shareyourstore"));
                } else if (position == 2) {
                    tv1.setText(ServerDataManager.getTextFromKey("intl_txt_doodlewithfriends"));
                    tv2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    tv2.setText(ServerDataManager.getTextFromKey("intl_txt_newwaytointeract"));
                } else {
                    tv1.setText(ServerDataManager.getTextFromKey("intl_txt_expressyourself"));
                    tv2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    tv2.setText(ServerDataManager.getTextFromKey("intl_txt_withemotion"));
                }
                container.addView(itemView);
                return itemView;
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                pagerDotView.setCurrentPageIndex(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(0);

        Preferences.getInstacne().setValues(
                HomeActivity.HAD_SET_FIREBASE_PUSH_TOKEN, false);
        Preferences.getInstacne().setValues(EditPostFragment.LAST_ANONYMOUS_TIME, 0l);

        Preferences.getInstacne().setValues(RabbitMQManager.LAST_GET_MSG_TIME, 0l);
        Preferences.getInstacne().setValues(RabbitMQManager.LAST_SERVER_TIME, 0l);
        Preferences.getInstacne().setValues(RabbitMQManager.CURRENT_DEVICE_TIME, 0l);

        Utils.saveArrayCache(HomeActivity.TAG_LIST, null);
        Utils.saveArrayCache(HomeActivity.TOPIC_LIST, null);
        Utils.saveArrayCache(HomeActivity.RECENT_POST_LIST, null);
        Utils.saveArrayCache(HomeActivity.FRIEND_LIST, null);
        GBSPreferences.getInstacne().setValues(GBSConstants.SAVE_MARK_NAME_USER, "");
        GBSDataManager.reset();
    }


    @Override
    protected void initFilterForBroadcast() {

    }


    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {

        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.btnSignUp, "intl_btn_createaccount");
        setOnlineText(R.id.btnLogin, "intl_btn_signin");
        setOnlineText(R.id.tvFacebook, "intl_btn_loginfb");
//        setOnlineText(R.id.instagramBtn, "intl_btn_loginig");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignUp:
                gotoPager(SignUpFragment.class, null);
                break;
            case R.id.btnLogin:
                gotoPager(LoginFragment.class, null);
                break;
            case R.id.facebookBtn:
                showLoadingDialog(null, null, true);
                MemberShipManager.getInstance().loginByFaceBook(getActivity(),
                        new GBSMemberShipManager.memberShipThirdPartyCallBack<GBUserInfo>() {
                            @Override
                            public void timeOut() {
                                hideLoadingDialog();
                                Logs("FB登录超时");
                            }

                            @Override
                            public void success(GBUserInfo obj) {
                                Logs("FB登录成功 用户信息为:" + obj);
                                if (getView() == null) {
                                    return;
                                }
                                Preferences.getInstacne().setThirdPartyType(
                                        GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram.GetValues());
                                startActivity(new Intent(getActivity(), HomeActivity.class));
                                getActivity().finish();
                            }

                            @Override
                            public void fail(String errorStr) {
                                hideLoadingDialog();
                                MemberShipManager.getInstance().getFacebook(getActivity()).logout(null);
                                Logs("FB登录失败");
                            }

                            @Override
                            public void cancel() {
                                hideLoadingDialog();
                                Logs("FB登录取消");
                            }

                            @Override
                            public void needToMatchDisplayName() {
                                hideLoadingDialog();
                                Logs("此次FB登录需要绑定用户名来注册,所以需要设置displayName");
                                Preferences.getInstacne().setThirdPartyType(
                                        GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook.GetValues());
                                gotoPager(ThirdPartySignUpFragment.class, null);
//                                gotoMatchDisplayNamePageWithAccessTokenForInPage(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_Facebook);
                            }
                        });
                break;
//            case R.id.instagramBtn:
//                showLoadingDialog(null, null, false);
//                MemberShipManager.getInstance().loginByInstagram(getActivity(),
//                        new GBSMemberShipManager.memberShipThirdPartyCallBack<GBUserInfo>() {
//                            @Override
//                            public void timeOut() {
//                                hideLoadingDialog();
//                                Logs("Instagram登录超时");
//                            }
//
//                            @Override
//                            public void success(GBUserInfo obj) {
//                                Logs("Instagram登录成功 用户信息为:" + obj);
//                                Preferences.getInstacne().setThirdPartyType(
//                                        GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram.GetValues());
//                                startActivity(new Intent(getActivity(),
//                                        HomeActivity.class));
//                                getActivity().finish();
//                            }
//
//                            @Override
//                            public void fail(String errorStr) {
//                                hideLoadingDialog();
//                                Logs("Instagram登录失败");
//                            }
//
//                            @Override
//                            public void cancel() {
//                                hideLoadingDialog();
//                                Logs("Instagram登录取消");
//                            }
//
//                            @Override
//                            public void needToMatchDisplayName() {
//                                hideLoadingDialog();
//                                Logs("此次Instagram登录需要绑定用户名来注册,所以需要设置displayName");
//                                Preferences.getInstacne().setThirdPartyType(
//                                        GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram.GetValues());
//                                gotoPager(ThirdPartySignUpFragment.class, null);
////                                gotoMatchDisplayNamePageWithAccessTokenForInPage(GBMemberShip_V2.MemberShipThirdPartyType.MemberShip_ThirdParty_instagram);
//                            }
//                        });
//                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideLoadingDialog();
    }
}
