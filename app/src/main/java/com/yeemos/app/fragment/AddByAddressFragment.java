package com.yeemos.app.fragment;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.BaseUtils;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.adapter.AddByAddressAdapter;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by gigabud on 16-8-1.
 */
public class AddByAddressFragment extends BaseFragment implements View.OnClickListener {

    private ArrayList<BasicUser> arrayList;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_add_by_fb_fragment;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getAddFromFb().setVisibility(View.GONE);
        getBtnCancelText().setOnClickListener(this);
        getBtnCancel().setOnClickListener(this);
        getSearchEditText().setHint(ServerDataManager.getTextFromKey("addbyaddrss_txt_nameornumber"));
        getSearchEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                initFilterSearchText(s.toString());
            }
        });
        if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.READ_CONTACTS)) {
            ((BaseActivity) getActivity()).requestPermission(BaseActivity.PERMISSION_READ_CONTACTS_REQ_CODE, Manifest.permission.READ_CONTACTS);
        }
    }

    private void initFilterSearchText(String str) {
        if (arrayList == null || arrayList.isEmpty()) {
            return;
        }
        if (TextUtils.isEmpty(str) || str.equals("")) {
            getAddByAddressAdapter().setArrayList(arrayList);
            return;
        }
        ArrayList<BasicUser> filterArrayList = new ArrayList<>();
        for (BasicUser basicUser : arrayList) {
            if (basicUser.getRemarkName().toLowerCase().contains(str.toLowerCase()) || basicUser.getMobile().contains(str)) {
                filterArrayList.add(basicUser);
            }
        }
        getAddByAddressAdapter().setArrayList(filterArrayList);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.READ_CONTACTS)) {
            return;
        }
        if (arrayList == null || arrayList.isEmpty()) {
            initData();
        } else {
            getAddByAddressAdapter().setArrayList(arrayList);
        }
    }


    private void initData() {
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getDataAndShow();
            }
        });
    }

    private synchronized void getDataAndShow() {
        ArrayList<BasicUser> newArrayList = MemberShipManager.getInstance().findFriendAddressBookUsers();
        ArrayList<BasicUser> contactsInfos = Utils.getLocalContactsInfos(BaseApplication.getAppContext());
        if (!newArrayList.isEmpty()) {
            Collections.sort(newArrayList, new Comparator<BasicUser>() {
                @Override
                public int compare(BasicUser user1, BasicUser user2) {
                    return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
                }
            });
        }
        if (!contactsInfos.isEmpty()) {
            Collections.sort(contactsInfos, new Comparator<BasicUser>() {
                @Override
                public int compare(BasicUser user1, BasicUser user2) {
                    return user1.getPinyinName().toUpperCase().compareTo(user2.getPinyinName().toUpperCase());
                }
            });
        }
        arrayList = new ArrayList<BasicUser>();
        arrayList.addAll(newArrayList);
        for (int i = 0; i < contactsInfos.size(); i++) {
            BasicUser contacts = contactsInfos.get(i);
            for (BasicUser basicUser : newArrayList) {
                if ((!TextUtils.isEmpty(basicUser.getEmail()) && basicUser.getEmail().equals(contacts.getEmail()))
                        || (!TextUtils.isEmpty(basicUser.getMobile()) && basicUser.getMobile().contains(contacts.getMobile()))) {
                    contactsInfos.remove(i);
                    --i;
                }
            }
        }
        arrayList.addAll(contactsInfos);
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (getView() != null) {
                    getAddByAddressAdapter().setArrayList(arrayList);
                }
            }
        });
    }

    private ListView getListView() {
        return (ListView) getView().findViewById(R.id.listView);
    }

    private TextView getAddFromFb() {
        return (TextView) getView().findViewById(R.id.addFromFb);
    }

    private TextView getBtnCancelText() {
        return (TextView) getView().findViewById(R.id.btnCancelText);
    }

    private ImageView getBtnCancel() {
        return (ImageView) getView().findViewById(R.id.btnCancel);
    }

    private EditText getSearchEditText() {
        return (EditText) getView().findViewById(R.id.searchEditText);
    }


    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        return false;
    }

    @Override
    public void updateUIText() {
        setOnlineText(R.id.btnCancelText, "pblc_btn_cancel");
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCancel:
                getSearchEditText().setText("");
                break;
            case R.id.btnCancelText:
                goBack();
                break;
        }
    }

    public AddByAddressAdapter getAddByAddressAdapter() {
        if (getListView().getAdapter() == null) {
            AddByAddressAdapter addByAddressAdapter = new AddByAddressAdapter();
            getListView().setAdapter(addByAddressAdapter);
        }
        return (AddByAddressAdapter) getListView().getAdapter();
    }
}
