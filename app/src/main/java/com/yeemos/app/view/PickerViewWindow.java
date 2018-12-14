package com.yeemos.app.view;

import android.app.Activity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import com.gigabud.core.util.Country;
import com.gigabud.core.util.LanguagePreferences;
import com.yeemos.app.R;
import com.yeemos.app.view.PhonePickerView.onSelectListener;

import java.util.ArrayList;


/**
 * Created by gigabud on 16-6-24.
 */
public class PickerViewWindow extends PopupWindow {

    public onSelectListener mOnSelectListener;

    public PickerViewWindow(Activity activity){
        super(activity);
        setContentView(LayoutInflater.from(activity).inflate(R.layout.layout_picker_view, null));
        setFocusable(true);
        this.setWidth(LayoutParams.MATCH_PARENT);
        this.setHeight(350);
        this.setAnimationStyle(R.style.PopupWindowAnimation);
        this.setBackgroundDrawable(null);
        this.setOutsideTouchable(true);
        getContentView().setFocusableInTouchMode(true); // 设置view能够接听事件 标注2
        ArrayList<Country> arrayList = LanguagePreferences.getInstanse(activity).getAllCountries(activity);
        getPhonePickerView().setData(arrayList);
        getPhonePickerView().setOnSelectListener(new onSelectListener() {

            @Override
            public void onSelect(String countryName,String countryCode) {
                if(mOnSelectListener!=null) {
                    mOnSelectListener.onSelect(countryName, countryCode);
                }
            }
        });
        getContentView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int height = getPhonePickerView().getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK) {
                    if (this != null) {
                        dismiss();
                    }
                }
                return false;
            }
        });

    }

    public void setmOnSelectListener(onSelectListener mOnSelectListener) {
        this.mOnSelectListener = mOnSelectListener;
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
    }

    private PhonePickerView getPhonePickerView(){
        return (PhonePickerView)getContentView().findViewById(R.id.phonePicker);
    }


}
