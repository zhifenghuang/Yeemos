package com.yeemos.app.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.yeemos.app.activity.HomeActivity;
import com.yeemos.app.utils.Constants;
import com.yeemos.app.view.DirectionalViewPager;
import com.yeemos.app.R;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-6-15.
 */
public class DirectionViewPagerFragment extends BaseFragment {

    private ArrayList<BaseFragment> mFragmentList;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_direction_viewpager;
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
        return UI_SHOW_TYPE.UI_SHOW_EMPTY;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final DirectionalViewPager viewPager = (DirectionalViewPager) view.findViewById(R.id.pager);
        viewPager.setOrientation(DirectionalViewPager.VERTICAL);
        viewPager.setCanScroll(true);
        mFragmentList = new ArrayList<>();
        MyInfoFragment myInfoFragment = new MyInfoFragment();
        myInfoFragment.setParentDirectionalViewPager(viewPager);
        myInfoFragment.setInHomeActivity(true);
        mFragmentList.add(myInfoFragment);

        CameraFragment cameraFragment = new CameraFragment();
        cameraFragment.setParentDirectionalViewPager(viewPager);
        cameraFragment.setParentFragment(this);
        mFragmentList.add(cameraFragment);
        viewPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getView() == null) {
                    return;
                }
                viewPager.setAdapter(new FragmentPagerAdapter(getActivity().getSupportFragmentManager()) {

                    @Override
                    public int getCount() {
                        return mFragmentList.size();
                    }

                    @Override
                    public Fragment getItem(int position) {
                        return mFragmentList.get(position);
                    }

                    @Override
                    public void destroyItem(ViewGroup container, int position, Object object) {
                        try {
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                            transaction.remove((Fragment) object);
                            transaction.commit();
                            super.destroyItem(container, position, object);
                        } catch (Exception e) {

                        }
                    }
                });
                viewPager.setCurrentItem(1);
                ((HomeActivity) getActivity()).setFullOrNotScreen();
            }
        }, 100);

    }

    public void onStart() {
        super.onStart();
        setCanScroll(true);
    }

    public void setCanScroll(boolean isCanScroll) {
        if (getView() == null) {
            return;
        }
        DirectionalViewPager viewPager = (DirectionalViewPager) getView().findViewById(R.id.pager);
        viewPager.setCanScroll(isCanScroll);
    }


    public void resetMyUserInfo(BasicUser basicUser, ArrayList<PostBean> myPostList) {
        if (mFragmentList == null || mFragmentList.isEmpty()) {
            return;
        }
        ((MyInfoFragment) mFragmentList.get(0)).initUserInfo(basicUser);
//        ((MyInfoFragment) mFragmentList.get(0)).showUserPostViewPage(myPostList);
    }

    public boolean isCurrentCameraFragment() {
        if (getView() == null) {
            return false;
        }
        DirectionalViewPager viewPager = (DirectionalViewPager) getView().findViewById(R.id.pager);
        return viewPager.getCurrentItem() == 1;
    }

    public void toCameraFragment() {
        if (getView() == null) {
            return;
        }
        DirectionalViewPager viewPager = getView().findViewById(R.id.pager);
        viewPager.setCurrentItem(1);
    }



    public void resetCameraFragment() {
        if (mFragmentList == null || mFragmentList.isEmpty() || getView() == null) {
            return;
        }
        ((MyInfoFragment) mFragmentList.get(0)).showOrHideRedPoint();
        ((CameraFragment) mFragmentList.get(1)).resetCameraUI();
    }

    public void resetUnReadPostNum(int unReadPostNum) {
        if (mFragmentList == null || mFragmentList.isEmpty() || getView() == null) {
            return;
        }
        ((CameraFragment) mFragmentList.get(1)).resetUnReadPostNum(unReadPostNum);
    }

    public void onDestroyView() {
        super.onDestroyView();
        DirectionalViewPager viewPager = (DirectionalViewPager) getView().findViewById(R.id.pager);
        if (mFragmentList != null && !mFragmentList.isEmpty()) {
            FragmentPagerAdapter adapter = (FragmentPagerAdapter) viewPager.getAdapter();
            if (adapter == null) {
                return;
            }
            for (int i = 0; i < mFragmentList.size(); ++i) {
                adapter.destroyItem(viewPager, i, mFragmentList.get(i));
            }
            mFragmentList.clear();
            viewPager.setAdapter(null);
        }
    }

    @Override
    public void onBackKeyClick() {
        DirectionalViewPager viewPager = (DirectionalViewPager) getView().findViewById(R.id.pager);
        if (viewPager.getCurrentItem() == 0) {
            viewPager.setCurrentItem(1);
            return;
        }
        getActivity().finish();
    }
}
