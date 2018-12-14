package com.yeemos.app.viewholder;

import android.view.View;

import com.gbsocial.BeansBase.BasicUser;

/**
 * Created by gigabud on 16-5-31.
 */
public class TagUserHolder extends UserFollowSearchHolder {
    @Override
    public void fill(BasicUser bBean, int position) {
        super.fill(bBean, position);
        btnFollow.setVisibility(View.INVISIBLE);
        imgIcon.setClickable(false);
        tvTitle.setClickable(false);
        tvContent.setClickable(false);
    }

    @Override
    public void clickBConvertView(BasicUser bBean) {
//        DataManager.getInstance().setTagUser(bBean);
//        BaseApplication.getCurFragment().goBack();
    }
}
