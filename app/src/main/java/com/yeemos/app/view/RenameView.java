package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.constants.GBSConstants;
import com.gbsocial.memberShip.GBSMemberShipManager;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.common.platforms.GBUserInfo;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.fragment.BaseFragment;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Preferences;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by gigabud on 16-7-20.
 */
public class RenameView extends RelativeLayout implements View.OnClickListener {

    private BasicUser mEditUser;
    private BaseFragment baseFragment;

    public RenameView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.rename_layout, this);
        GradientDrawable bgShape = (GradientDrawable) findViewById(R.id.rlBg).getBackground();
        if (bgShape != null) {
            bgShape.setColor(Color.WHITE);
        }
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        findViewById(R.id.btnRename).setOnClickListener(this);
        findViewById(R.id.btnClose).setOnClickListener(this);
        ((EditText) findViewById(R.id.etDisplayName)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});

        ((TextView) findViewById(R.id.tvRenameTitle)).setText(ServerDataManager.getTextFromKey("pblc_ttl_customrename"));
        ((Button) findViewById(R.id.btnRename)).setText(ServerDataManager.getTextFromKey("pblc_btn_rename"));
    }

    public void setFragment(BaseFragment baseFragment) {
        this.baseFragment = baseFragment;
    }

    public void setEditUser(BasicUser user) {
        mEditUser = user;
        EditText etDisplayName = (EditText) findViewById(R.id.etDisplayName);
        etDisplayName.setText(user.getRemarkName());
    }

    private String getEditRemarkName() {
        return ((EditText) findViewById(R.id.etDisplayName)).getText().toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                removView();
                break;
            case R.id.btnRename:
                final String remarkName = getEditRemarkName();
                mEditUser.setRemarkName(remarkName);
                final BaseActivity activity = (BaseActivity) getContext();
                activity.showLoadingDialog("", null, true);
                if (mEditUser.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                    if (TextUtils.isEmpty(remarkName) || remarkName.trim().length() == 0) {
                        activity.hideLoadingDialog();
                        return;
                    }
                    MemberShipManager.getInstance().updateUserInfoWithFile(remarkName,
                            "", null, null, null, "", null,
                            null, new GBSMemberShipManager.memberShipCallBack<GBUserInfo>() {

                                @Override
                                public void timeOut() {
                                    activity.hideLoadingDialog();
                                }

                                @Override
                                public void success(GBUserInfo obj) {
                                    activity.hideLoadingDialog();
                                    removView();
                                }

                                @Override
                                public void fail(String errorStr) {
                                    activity.hideLoadingDialog();
                                    activity.errorCodeDo(errorStr);
                                }

                                @Override
                                public void cancel() {
                                    activity.hideLoadingDialog();
                                }
                            });
                } else {
                    GBExecutionPool.getExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            DataManager.getInstance().updateFriendRemarkName(mEditUser.getUserId(), remarkName);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    activity.hideLoadingDialog();
                                    Preferences.getInstacne().setValues(GBSConstants.SAVE_MARK_NAME_USER, modifySaveMarkUser(remarkName));
                                    removView();
                                }
                            });
                        }
                    });
                }
                break;
        }
    }

    private String modifySaveMarkUser(String remarkName) {
        Map<String, String> map = DataManager.getInstance().getMap();
        if (map.containsKey(mEditUser.getUserId())) {
            map.remove(mEditUser.getUserId());
        }
        if (!TextUtils.isEmpty(remarkName)) {
            map.put(mEditUser.getUserId(), remarkName);
        }
        if (map.size() == 0) {
            return "";
        }
        return new JSONObject(map).toString();
    }

    private void removView() {
        if (baseFragment != null) {
            baseFragment.refreshFromNextFragment(null);
        }
        hideInputMethodManager();
        postDelayed(new Runnable() {
            @Override
            public void run() {
                ((ViewGroup) getParent()).removeView(RenameView.this);
            }
        }, 300);

    }

    private void hideInputMethodManager() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(findViewById(R.id.etDisplayName).getWindowToken(), 0);
        }
    }
}
