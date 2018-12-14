package com.yeemos.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.yeemos.app.R;
import com.yeemos.app.view.FollowButton.FollowButtonStatus;

/**
 * Created by gigabud on 16-7-4.
 */
public class FollowImageView extends ImageView{

    private OnFollowImgClickListener listener;
    private FollowButtonStatus status;

    public FollowImageView(Context context) {
        super(context);
        init();
    }

    public FollowImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FollowImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        setClickable(true);
        setStatus(FollowButtonStatus.FollowButtonStatus_Follow);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                postListener();
            }
        });
    }
    public interface OnFollowImgClickListener {
        abstract void onClick();
    }

    public void setFollowImageClickListener(OnFollowImgClickListener listener) {
        this.listener = listener;
    }

    private void postListener() {
        if ( listener != null ) {
            listener.onClick();
        }
    }

    public void setStatus(FollowButtonStatus status) {
        this.status = status;
        switch (status) {
            case FollowButtonStatus_Follow:
                setImageDrawable(getResources().getDrawable(R.drawable.quick_follow_off));
                break;
            case FollowButtonStatus_Following:
                setImageDrawable(getResources().getDrawable(R.drawable.quick_follow_on));
                break;
            case FollowButtonStatus_Request:
                setImageDrawable(getResources().getDrawable(R.drawable.quick_follow_wait));
                break;
            default:
                break;
        }
    }
    public FollowButtonStatus getStatus() {
        return status;
    }
}
