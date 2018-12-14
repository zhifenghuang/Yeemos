package com.yeemos.app.services;

import com.gigabud.core.upgrade.BaseUpgradeDialog;
import com.yeemos.app.utils.Preferences;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-10-13.
 */

public class UpgradeDialog extends BaseUpgradeDialog{

    public static final String UPGRADE_DIALOG_TO_BACKGROUND = "upgrade_dialog_to_background";

    @Override
    protected void downloadFile(String newPackageName) {
        super.downloadFile(newPackageName);
    }

    @Override
    protected int getForcedLayoutId() {
        return R.layout.layout_one_btn_upgrade_dialog;
    }

    @Override
    protected int getNormalLayoutId() {
        return R.layout.layout_two_btn_upgrade_dialog;
    }

    @Override
    protected int getCancelId() {
        return R.id.cancel;
    }

    @Override
    protected int getOkId() {
        return R.id.okay;
    }

    @Override
    protected int getTextViewId() {
        return R.id.dialogMessage;
    }

    @Override
    protected int getTitleId() {
        return R.id.dialogTitle;
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
        Preferences.getInstacne().setValues(UpgradeDialog.UPGRADE_DIALOG_TO_BACKGROUND, true);
    }
}
