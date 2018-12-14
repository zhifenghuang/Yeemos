package com.yeemos.app.interfaces;

import android.view.MotionEvent;

import com.gbsocial.BeansBase.PostBean;

import java.util.ArrayList;

/**
 * Created by gigabud on 16-5-25.
 */
public interface OnItemClickListener {
    public void onItemClick(int position);

    public void onItemTouch(MotionEvent event);
}
