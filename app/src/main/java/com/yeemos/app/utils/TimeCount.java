package com.yeemos.app.utils;

import android.os.CountDownTimer;

/**
 * Created by gigabud on 16-7-12.
 */
public class TimeCount extends CountDownTimer {

    private OnTimeCountListener onTimeCountListener;

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public TimeCount(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if(onTimeCountListener != null){
            onTimeCountListener.timeCountOnTick(millisUntilFinished);
        }
    }

    @Override
    public void onFinish() {
        if(onTimeCountListener != null){
            onTimeCountListener.timeCountOnFinish();
        }
    }

    public void setOnTimeCountListener(OnTimeCountListener onTimeCountListener){
        this.onTimeCountListener = onTimeCountListener;
    }


    public interface OnTimeCountListener{
        void timeCountOnTick(long millisUntilFinished);
        void timeCountOnFinish();
    }
}
