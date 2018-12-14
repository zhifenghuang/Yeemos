package com.yeemos.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.gbsocial.preferences.GBSPreferences;
import com.gigabud.core.util.GBExecutionPool;
import com.yeemos.app.manager.DataManager;
import com.yeemos.app.R;

/**
 * Created by gigabud on 16-6-14.
 */
public class CustomToggleImageButton extends ImageButton {
    private String mKey;
    private int value;

    public CustomToggleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (value == 0) {
                    value = 1;
                } else {
                    value = 0;
                }
                setImageResource(value == 1 ? R.drawable.turn_on_btn : R.drawable.turn_off_btn);
                GBSPreferences.getInstacne().setValues(mKey, value);
                GBExecutionPool.getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        DataManager.getInstance().updateUserSetting();
                    }
                });
            }
        });
    }

    public void setKey(String key, int value) {
        mKey = key;
        this.value = value;
        setImageResource(value == 1 ? R.drawable.turn_on_btn : R.drawable.turn_off_btn);
    }
}
