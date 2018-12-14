package com.yeemos.app.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.inputmethod.InputMethodManager;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.R;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.view.MenuViewPagerTextIndicator;
import com.yeemos.app.view.SearchBarView;

import java.util.ArrayList;

public class SearchFragment extends HasBasicBtnsFragment {

    private static ArrayList<String> titlesArr;
    private ArrayList<BaseFragment> mPagerFragmentList;
    private int curentPagerPosition = 0;
    private SearchViewPagerIndicatorAdapter adapter;

    private Runnable runnable;


    private Runnable getRunnable() {

        if (runnable == null) {

            runnable = new Runnable() {
                @Override
                public void run() {
                    if (curentPagerPosition == 0) {
                        ((SearchPersonFragment) adapter.getItem(0)).setSearchStrAndGetData();
                    } else {
                        ((SearchTagFragment) adapter.getItem(1)).onRefresh();
                    }

                }
            };
        }
        return runnable;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        if (titlesArr == null) {
            titlesArr = new ArrayList<String>();
        }
        titlesArr.clear();
        titlesArr.add(ServerDataManager.getTextFromKey("srchid_btn_people"));
        titlesArr.add(ServerDataManager.getTextFromKey("srchtag_btn_tags"));
        if (mPagerFragmentList == null) {
            mPagerFragmentList = new ArrayList<>();
        }
        mPagerFragmentList.clear();
        mPagerFragmentList.add(new SearchPersonFragment());
        mPagerFragmentList.add(new SearchTagFragment());

        final SearchBarView searchBar = (SearchBarView) getView().findViewById(R.id.searchBar);
        adapter = new SearchViewPagerIndicatorAdapter(getActivity().getSupportFragmentManager()); // getChildFragmentManager()
        // getFragmentManager()
        final ViewPager pager = (ViewPager) getView().findViewById(R.id.pagerId);
        pager.setAdapter(adapter);
        MenuViewPagerTextIndicator indicator = (MenuViewPagerTextIndicator) getView().findViewById(R.id.pageIndicator);
        indicator.setViewPager(pager, 0);
        indicator.setDataSource(titlesArr);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
//                searchBar.getEditText().setText("");

                DataManager.getInstance().setCurKeyWord(searchBar.getEditText().getText().toString());
                curentPagerPosition = position;
                if (position == 0) {
                    ((SearchPersonFragment) adapter.getItem(0)).onRefresh();
//                    if(!TextUtils.isEmpty(searchBar.getEditText().getText().toString())
//                            && !searchBar.getEditText().getText().toString().equals("")) {
//                        ((SearchPersonFragment) adapter.getItem(0)).onRefresh();
//                    }
//                    ((SearchPersonFragment) adapter.getItem(0)).showSearchResult(DataManager.getInstance().getCurKeyWord());
                } else {
//                    if(!TextUtils.isEmpty(searchBar.getEditText().getText().toString())
//                            && !searchBar.getEditText().getText().toString().equals("")) {
//                        ((SearchTagFragment) adapter.getItem(1)).onRefresh();
//                    }else {
//                        ((SearchTagFragment) adapter.getItem(1)).clearSearchResult();
//                    }
                    ((SearchTagFragment) adapter.getItem(1)).onRefresh();

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        searchBar.setHint(ServerDataManager.getTextFromKey("pblc_txt_search"));
        searchBar.setListener(new SearchBarView.SearchBarViewListener() {
            @Override
            public void onTextChanged(String text) {
                while (text.startsWith("#")) {
                    text = text.substring(1);
                }
                DataManager.getInstance().setCurKeyWord(text);
                new Handler().postDelayed(getRunnable(), 1 * 1000);

            }

            @Override
            public void onKeyBoardSearchBtnClicked(String text) {

            }
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && getView() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                inputMethodManager.hideSoftInputFromWindow(((SearchBarView) getView().findViewById(R.id.searchBar)).getEditText().getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onStop() {
        DataManager.getInstance().resetCurKeyWord();
        super.onStop();
    }

    @Override
    public boolean refreshUIview(UI_SHOW_TYPE showType) {
        super.refreshUIview(showType);
        return false;
    }

    @Override
    public int getBackgroudLayoutID() {
        return R.id.Bgll;
    }

    @Override
    public void updateUIText() {

    }

    @Override
    public UI_SHOW_TYPE updateData(boolean bIsClearData) {
        return UI_SHOW_TYPE.UI_SHOW_NO_MORE_DATA;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_search;
    }

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
    }

    @Override
    protected void initFilterForBroadcast() {

    }

    private class SearchViewPagerIndicatorAdapter extends FragmentPagerAdapter {

        public SearchViewPagerIndicatorAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mPagerFragmentList.get(position);
        }


        @Override
        public int getCount() {
            return mPagerFragmentList.size();
        }
    }

}


