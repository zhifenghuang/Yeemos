package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gbsocial.BeansBase.HashTagBean;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.adapter.HotTagAdapter;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-9-8.
 */
public class SearchTagFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    @Override
    protected int getLayoutId() {
        return R.layout.layout_search_person_fragment;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getSwipeRefreshLayout().setOnRefreshListener(this);
        getSwipeRefreshLayout().setSize(SwipeRefreshLayout.DEFAULT);
//        getData();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HotTagAdapter hotTagAdapter = (HotTagAdapter)getListView().getAdapter();
//                Bundle bundle = new Bundle();
//                bundle.putString(Constants.KEY_SEND_STRING_TO_HASHTAGSFRAGMENT, hotTagAdapter.getItem(position).getHasTag());
                DataManager.getInstance().setCurKeyWord(hotTagAdapter.getItem(position).getHasTag());
                gotoPager(HashTagsFragment.class, null);
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

    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_MORE_DATA;
    }

    @Override
    public void onRefresh() {
        getData();
    }

    private void getData() {
        if (getView() == null) {
            return;
        }
        if (!getSwipeRefreshLayout().isRefreshing()) {
            getSwipeRefreshLayout().post(new Runnable() {
                @Override
                public void run() {
                    getSwipeRefreshLayout().setRefreshing(true);
                }
            });
        }
        GBExecutionPool.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                getDataFromServer();
            }
        });
    }

    private synchronized void getDataFromServer() {
        final ArrayList<HashTagBean> result = DataManager.getInstance().searchHashTags(DataManager.getInstance().getCurKeyWord(),
                0, TextUtils.isEmpty(DataManager.getInstance().getCurKeyWord())
                        || DataManager.getInstance().getCurKeyWord().equals("") ? 20 : Constants.PAGE_NUMBER);
        if(getActivity() == null) {
            return;
        }
        if (result != null && !result.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getView() == null) {
                        return;
                    }
                    if (getSwipeRefreshLayout().isRefreshing()) {
                        getSwipeRefreshLayout().setRefreshing(false);
                    }
                    getHotTagAdapter().setArrayList(result);
                }
            });
        } else {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (getView() == null) {
                        return;
                    }
                    if (getSwipeRefreshLayout().isRefreshing()) {
                        getSwipeRefreshLayout().setRefreshing(false);
                    }
                    getHotTagAdapter().setArrayList(new ArrayList<HashTagBean>());
                }
            });
        }
    }

    private HotTagAdapter getHotTagAdapter() {
        if (getListView().getAdapter() == null) {
            HotTagAdapter hotTagAdapter = new HotTagAdapter();
            getListView().setAdapter(hotTagAdapter);
        }
        return (HotTagAdapter) getListView().getAdapter();
    }
    private ListView getListView() {
        return (ListView) getView().findViewById(R.id.listView);
    }

    private SwipeRefreshLayout getSwipeRefreshLayout() {
        return (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshLayout);
    }
}
