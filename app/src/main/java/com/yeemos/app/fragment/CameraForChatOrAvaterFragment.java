package com.yeemos.app.fragment;

import android.Manifest;
import android.os.Bundle;

import com.gigabud.core.util.BaseUtils;
import com.yeemos.app.activity.BaseActivity;
import com.yeemos.app.utils.Constants;

/**
 * Created by gigabud on 16-6-3.
 */
public class CameraForChatOrAvaterFragment extends CameraFragment {

    public static final String USE_CAMERA_TYPE = "use_camera_type";

    @Override
    public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
        return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION_TWO;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle == null) {
            goBack();
            return;
        }
        mCameraUseType = bundle.getInt(USE_CAMERA_TYPE);
        ((BaseActivity) getActivity()).setScreenFull(true);
    }

    public void onResume() {
        super.onResume();
        if (!BaseUtils.isGrantPermission(getActivity(), Manifest.permission.CAMERA)) {
            ((BaseActivity) getActivity()).requestPermission(BaseActivity.PERMISSION_CAMERA_CODE, Manifest.permission.CAMERA);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);

        ((BaseActivity) getActivity()).setScreenFull(!hidden);
        if (!hidden) {
            mTakePhotoOrRecord.resetCircleButton();
        }
    }

}
