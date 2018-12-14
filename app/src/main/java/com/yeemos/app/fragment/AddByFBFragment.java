package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.server.ServerDataManager;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.adapter.AddByFbAdapter;
import com.yeemos.app.manager.MemberShipManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-8-1.
 */
public class AddByFBFragment extends BaseFragment implements View.OnClickListener{

    private ArrayList<BasicUser> arrayList;

    @Override
    protected int getLayoutId() {
        return R.layout.layout_add_by_fb_fragment;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSearchEditText().setHint(ServerDataManager.getTextFromKey("addbyfb_txt_name"));
        getBtnCancel().setOnClickListener(this);
        getBtnCancelText().setOnClickListener(this);
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
    }
    private void initFilterSearchText(String filterStr) {
        if(arrayList == null || arrayList.isEmpty()){
            return;
        }
        if(filterStr.equals("") || TextUtils.isEmpty(filterStr)){
            getAddByFbAdapter().setArrayList(arrayList);
            return;
        }
        ArrayList<BasicUser> filterArrayList = new ArrayList<>();
        for(BasicUser basicUser : arrayList){
            if(basicUser.getRemarkName().toLowerCase().contains(filterStr.toLowerCase())
                    || basicUser.getUserName().toLowerCase().contains(filterStr.toLowerCase())){
                filterArrayList.add(basicUser);
            }
        }
        getAddByFbAdapter().setArrayList(filterArrayList);
    }
    @Override
    public void onStart() {
        super.onStart();
        if(arrayList == null || arrayList.isEmpty()){
            initData();
        }else {
            getAddByFbAdapter().setArrayList(arrayList);
        }
    }

    private AddByFbAdapter getAddByFbAdapter() {
        if(getListView().getAdapter() == null) {
            AddByFbAdapter addByFbAdapter = new AddByFbAdapter();
            getListView().setAdapter(addByFbAdapter);
        }
        return (AddByFbAdapter) getListView().getAdapter();
    }

    private void initData() {
        showLoadingDialog(null, null, true);
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getDataAndShow();
            }
        });
    }

    private synchronized void getDataAndShow(){
        arrayList = MemberShipManager.getInstance().findFriendFacebookUsers();
        if(arrayList == null || arrayList.isEmpty() ) {
            arrayList = new ArrayList<BasicUser>();
        }
        if(getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideLoadingDialog();
                if(getView() != null) {
                    getAddByFbAdapter().setArrayList(arrayList);
                }

            }
        });
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
        setOnlineText(R.id.addFromFb, "addbyfb_ttl_addfromfacebook");
        setOnlineText(R.id.btnCancelText, "pblc_btn_cancel");
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
        return (EditText)getView().findViewById(R.id.searchEditText);
    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_MORE_DATA;
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
}
