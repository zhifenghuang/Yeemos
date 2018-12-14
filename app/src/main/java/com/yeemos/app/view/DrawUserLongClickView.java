package com.yeemos.app.view;

import android.app.Activity;
import android.view.View;

import com.gbsocial.BeansBase.BasicUser;
import com.gbsocial.BeansBase.PostBean;
import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.manager.MemberShipManager;

/**
 * Created by gigabud on 17-1-22.
 */

public class DrawUserLongClickView extends FiveBtnPopupWindow {

    private BasicUser basicUser;
    private PostBean postBean;
    public DrawUserLongClickView(Activity context, FiveBtnPopupWindowClickListener listener) {
        super(context, listener);
    }


    public void setBasicUser(BasicUser basicUser, PostBean postBean) {
        this.postBean = postBean;
        this.basicUser = basicUser;
    }

    @Override
    public void initView() {

        getTopLine().setVisibility(View.GONE);
        getSecondLine().setVisibility(View.GONE);
        getThirdLine().setVisibility(View.GONE);
        getFourthLine().setVisibility(View.GONE);

        getTitleText().setVisibility(View.GONE);
        getBtnThird().setVisibility(View.GONE);
        getBtnFourth().setVisibility(View.GONE);
        getBtnFifth().setVisibility(View.GONE);

        getBtnCancel().setText(ServerDataManager.getTextFromKey("pblc_btn_cancel"));

        if (basicUser.getUserId().equals(MemberShipManager.getInstance().getUserID())) {
            getBtnFirst().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));
            getBtnSecond().setVisibility(View.GONE);
        } else {
            if (postBean.getOwner().getUserId().equals(MemberShipManager.getInstance().getUserID())) {
                getBtnFirst().setText(ServerDataManager.getTextFromKey("pblc_btn_delete"));
                getBtnSecond().setText(ServerDataManager.getTextFromKey("cmmnt_btn_reply"));
                getBtnSecond().setVisibility(View.VISIBLE);
            }else {
                getBtnFirst().setVisibility(View.GONE);
                getBtnSecond().setText(ServerDataManager.getTextFromKey("cmmnt_btn_reply"));
            }
        }
    }
}
