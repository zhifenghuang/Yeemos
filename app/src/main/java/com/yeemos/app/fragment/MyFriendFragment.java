package com.yeemos.app.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.ConnectedUtil;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.adapter.MyFriendAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.view.FiveBtnPopupWindow;
import com.yeemos.app.view.MorePopupWindow;
import com.yeemos.app.view.RenameView;
import com.yeemos.app.view.RoundedImageView;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-7-20.
 */
public class MyFriendFragment extends BaseFragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ArrayList<BasicUser> allFriends = null;
    private BasicUser mSelectUser;
    private Dialog dialog;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_my_friend;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
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
        setOnlineText(R.id.tvTitle, "myfrnd_ttl_myfriend");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setSize(SwipeRefreshLayout.DEFAULT);
        ((ListView) view.findViewById(R.id.listView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BasicUser user = getMyFriendAdapter().getItem(position);
                if (user == null) {
                    return;
                }
                showSmallProfileByUser(user);
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (allFriends == null || allFriends.isEmpty()) {
            getAllFriends();
        } else {
            getMyFriendAdapter().setMyFriends(allFriends);
        }
    }

    public MyFriendAdapter getMyFriendAdapter() {
        ListView listView = (ListView) getView().findViewById(R.id.listView);
        if (listView.getAdapter() == null) {
            MyFriendAdapter adapter = new MyFriendAdapter(getActivity());
            listView.setAdapter(adapter);
        }
        return (MyFriendAdapter) listView.getAdapter();
    }

    private void showSmallProfileByUser(BasicUser basicUser) {
        mSelectUser = basicUser;
        getView().findViewById(R.id.smallProfile).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.smallProfile).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                getView().findViewById(R.id.smallProfile).setVisibility(View.GONE);
                mSelectUser = null;
                return true;
            }
        });
        getView().findViewById(R.id.rlBg).setOnClickListener(this);
        GradientDrawable bgShape = (GradientDrawable) getView().findViewById(R.id.rlBg).getBackground();
        if (bgShape != null) {
            bgShape.setColor(Color.WHITE);
        }

        ((TextView) getView().findViewById(R.id.tvSmallDisplayName)).setText(basicUser.getRemarkName());
        ((TextView) getView().findViewById(R.id.tvSmallUserName)).setText("@" + basicUser.getUserName());
        RoundedImageView ivAvater=(RoundedImageView) getView().findViewById(R.id.ivSmallAvater);
        ivAvater.setNeedDrawVipBmp(basicUser.isAuthenticate());
//        ivAvater.setDefaultImageResId(R.drawable.default_avater);
//        ivAvater.setImageUrl(Preferences.getAvatarUrl(basicUser.getUserAvatar()));
        Utils.loadImage(BaseApplication.getAppContext(),R.drawable.default_avater, Preferences.getAvatarUrl(basicUser.getUserAvatar()),ivAvater);
        getView().findViewById(R.id.btnChat).setOnClickListener(this);
        getView().findViewById(R.id.btnProfile).setOnClickListener(this);
        getView().findViewById(R.id.btnSetting).setOnClickListener(this);
        getView().findViewById(R.id.ivSmallAvater).setOnClickListener(this);
    }

    @Override
    public void refreshFromNextFragment(Object obj) {
        getMyFriendAdapter().setMyFriends(allFriends);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                goBack();
                break;
            case R.id.btnChat:
                if (mSelectUser == null) {
                    return;
                }
                getView().findViewById(R.id.smallProfile).setVisibility(View.GONE);
                DataManager.getInstance().setCurOtherUser(mSelectUser);
                gotoPager(ChatFragment.class,null);
                break;
            case R.id.btnProfile:
                if (mSelectUser == null) {
                    return;
                }
                DataManager.getInstance().setCurOtherUser(mSelectUser);
                gotoPager(UserInfoFragment.class, null);
                getView().findViewById(R.id.smallProfile).setVisibility(View.GONE);
                break;
            case R.id.btnSetting:
                if (mSelectUser == null) {
                    return;
                }
                getView().findViewById(R.id.smallProfile).setVisibility(View.GONE);
                showPopupWindow();
        }
    }

//    private void showRenameView() {
//        RenameView renameView = new RenameView(getActivity());
//        renameView.setEditUser(mSelectUser);
//        renameView.setFragment(this);
//        RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.layout);
//        rl.addView(renameView);
//    }

    @Override
    public void onRefresh() {
        getAllFriends();
    }

    private void getAllFriends() {
        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
        if (!swipeLayout.isRefreshing()) {
            swipeLayout.setRefreshing(true);
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                allFriends = DataManager.getInstance().getAllFriends(false);
                if (allFriends == null) {
                    allFriends = new ArrayList<>();
                }
                if (getActivity() == null) {
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (getView() == null) {
                            return;
                        }

                        SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
                        if (swipeLayout.isRefreshing()) {
                            swipeLayout.setRefreshing(false);
                        }
                        getMyFriendAdapter().setMyFriends(allFriends);
                    }
                });
            }
        });
    }

    private void clickFirstBtn(){
        RenameView renameView = new RenameView(getActivity());
        renameView.setEditUser(mSelectUser);
        renameView.setFragment(this);
        RelativeLayout rl = (RelativeLayout) getView().findViewById(R.id.layout);
        rl.addView(renameView);
    }

    public void showPopupWindow() {
        if (mSelectUser == null) {
            return;
        }
        MorePopupWindow morePopupWindow = new MorePopupWindow(getActivity(), new MorePopupWindow.MorePopupWindowClickListener() {
            @Override
            public void onFirstBtnClicked() {
                clickFirstBtn();
            }

            @Override
            public void onSecondBtnClicked() {
                String content = ServerDataManager.getTextFromKey("mssg_block_confirmblock");
                String cancel = ServerDataManager.getTextFromKey("pblc_btn_no");
                String Okey = ServerDataManager.getTextFromKey("pblc_btn_yes");
                showPublicDialog(null, content, cancel, Okey, blockDialog);
            }

            @Override
            public void onThirdBtnClicked() {
                reportTypeWindow();
            }

            @Override
            public void onFourthBtnClicked() {
            }

            @Override
            public void onCancelBtnClicked() {

            }
        }, Constants.MORE_POPUPWINDOW_OTHERUSER_MORE);
        morePopupWindow.setBasicUser(mSelectUser);
        morePopupWindow.initView(null);
        morePopupWindow.getFourthBtn().setVisibility(View.GONE);
        morePopupWindow.getPopupThirdLine().setVisibility(View.GONE);
        morePopupWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    Handler blockDialog = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                case Constants.DIALOG_RIGHY_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    DataManager.getInstance().setCurOtherUser(mSelectUser);
                    DataManager.getInstance().blockUser(mSelectUser);
                    allFriends.remove(mSelectUser);
                    getMyFriendAdapter().setMyFriends(allFriends);
                    String content = ServerDataManager.getTextFromKey("pblc_txt_blocksuccessful");
                    String Okey = ServerDataManager.getTextFromKey("pub_btn_ok");
                    showPublicDialog(null, content, Okey, null, oneBtnDialoghandler);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    private void reportTypeWindow() {

        FiveBtnPopupWindow popUpWindow = new FiveBtnPopupWindow(
                (Activity) getContext(), new FiveBtnPopupWindow.FiveBtnPopupWindowClickListener() {
            @Override
            public void onFirstBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Porn);
            }

            @Override
            public void onSecondBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Scam);
            }

            @Override
            public void onThirdBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Abuse);
            }

            @Override
            public void onFourthBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_CommercialSpam);
            }

            @Override
            public void onFifthBtnClicked() {
                // TODO Auto-generated method stub
                reportSubmit(GBSConstants.MenuOperateType.Menu_Operate_Report_Offensive);
            }

            @Override
            public void onCancelBtnClicked() {
                // TODO Auto-generated method stub
            }
        });
        popUpWindow.initView();
        popUpWindow.showAtLocation(getView(), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    private void reportSubmit(GBSConstants.MenuOperateType opType) {
        DataManager.getInstance().setCurOtherUser(mSelectUser);
        DataManager.getInstance().setReportId(mSelectUser.getUserId());
        if (!ConnectedUtil.isConnected(getContext())) {
            return;
        }
//			operateType = opType;
//			String content = ServerDataManager.getTextFromKey("pub_txt_reportphoto");
//			String cancel = ServerDataManager.getTextFromKey("pub_btn_no");
//			String Okey = ServerDataManager.getTextFromKey("pub_btn_yes");
//			frg.showPublicDialog(null, content, cancel, Okey, reportHandler);

        DataManager.getInstance().report(GBSConstants.MenuObjectType.Menu_Object_User, opType);
        allFriends.remove(mSelectUser);
        getMyFriendAdapter().setMyFriends(allFriends);
        String content = ServerDataManager.getTextFromKey("pblc_txt_reportsuccess");
        String OK = ServerDataManager.getTextFromKey("pub_btn_ok");//"OK";
        showPublicDialog(null, content, OK, null, oneBtnDialoghandler);
    }

    protected Handler oneBtnDialoghandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.DIALOG_LEFT_BTN:
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
                default:
                    break;
            }
        }

        ;
    };
}
