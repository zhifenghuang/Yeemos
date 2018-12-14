package com.yeemos.app.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.utils.Utils;
import com.yeemos.app.R;

public class FollowButton extends RelativeLayout {
    int followImgID, unfollowImgID, followStrID, unfollowStrID;
    onFollowBtnClickListener listener;
    FollowButtonStatus status;
    private Path path;
    private Paint paint;
    private TextView tvState;
    private TextView tvPlus;
    private int mStrokeWidth = 2;
    private int paintColor = R.color.color_88_89_91;

    public enum FollowButtonStatus {
        FollowButtonStatus_Following(1),
        FollowButtonStatus_Follow(0),
        FollowButtonStatus_Request(2);
//        FollowButtonStatus_Friend(3);
        int nValues;

        private FollowButtonStatus(int i) {
            nValues = i;
        }

        public int GetValues() {
            return nValues;
        }

        public boolean Compare(int nNum) {
            return nValues == nNum;
        }

        public static FollowButtonStatus GetObject(int nNum) {
            FollowButtonStatus[] As = FollowButtonStatus.values();
            for (int i = 0; i < As.length; i++) {
                if ( As[i].Compare(nNum) )
                    return As[i];
            }
            return FollowButtonStatus_Following;
        }
    }

    public interface onFollowBtnClickListener {
        abstract void onClick();
    }

    public void setFollowButtonClickListener(onFollowBtnClickListener listener) {
        this.listener = listener;
    }

    private void postListener() {
        if ( listener != null ) {
            listener.onClick();
        }
    }

    public FollowButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.btn_follow, this);
        setClickable(true);
        setStatus(FollowButtonStatus.FollowButtonStatus_Follow);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                postListener();
            }
        });
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        getPath().reset();
        getPath().addRoundRect(new RectF(0 + mStrokeWidth, 0 + mStrokeWidth, w - mStrokeWidth, h - mStrokeWidth), Utils.dip2px(getContext(), 3), Utils.dip2px(getContext(), 3), Path.Direction.CW);
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.translate(0, 0);
        canvas.drawPath(getPath(), getPaint());
        super.dispatchDraw(canvas);
    }

    private Paint getPaint() {
        if ( paint == null ) {
            paint = new Paint();
            paint.setStrokeWidth(mStrokeWidth);
            paint.setAntiAlias(true);
            paint.setStyle(Style.FILL);
        }
        paint.setColor(status == FollowButtonStatus.FollowButtonStatus_Following ?
                getResources().getColor(R.color.color_45_223_227) : getResources().getColor(paintColor));
        return paint;
    }

    private Path getPath() {
        if ( path == null ) {
            path = new Path();
        }
        return path;
    }

    public void setStatus(FollowButtonStatus status) {
        this.status = status;
        switch (status) {
            case FollowButtonStatus_Follow:
                getStateTextView().setText(ServerDataManager.getTextFromKey("pblc_btn_follow"));
                getStateTextView().setTextColor(getResources().getColor(R.color.color_88_89_91));
                break;
            case FollowButtonStatus_Following:
                getStateTextView().setText(ServerDataManager.getTextFromKey("pblc_btn_following"));
                getStateTextView().setTextColor(getResources().getColor(R.color.color_88_89_91));
                break;
            case FollowButtonStatus_Request:
                getStateTextView().setText(ServerDataManager.getTextFromKey("pblc_btn_requested"));
                getStateTextView().setTextColor(Color.WHITE);
                break;
//            case FollowButtonStatus_Friend:
//                getStateTextView().setText("Friends");//ServerDataManager.getTextFromKey("pblc_btn_Friends"));
//                getStateTextView().setTextColor(Color.WHITE);
//                break;
            default:
                break;
        }
        setPaintColor();
    }

    public FollowButtonStatus getStatus() {
        return status;
    }

    private TextView getStateTextView() {
        if ( tvState == null ) {
            tvState = (TextView) findViewById(R.id.tvState);
        }
        return tvState;
    }

    public void setPaintColor(){
        if(status == FollowButtonStatus.FollowButtonStatus_Following) {
            getPaint().setStyle(Style.FILL);
            getStateTextView().setTextColor(getResources().getColor(R.color.color_255_255_255));
        }else if(status== FollowButtonStatus.FollowButtonStatus_Request){
            this.paintColor = R.color.color_187_187_187;
            getPaint().setStyle(Style.FILL);
            getStateTextView().setTextColor(Color.WHITE);
        }
        else{
            this.paintColor = R.color.color_88_89_91;
            getPaint().setStyle(Style.STROKE);
            getStateTextView().setTextColor(getResources().getColor(R.color.color_88_89_91));
        }
        postInvalidate();
    }

}
