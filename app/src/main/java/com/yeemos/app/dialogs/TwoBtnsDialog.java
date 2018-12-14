package com.yeemos.app.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

/**
 * 有两个按钮的dialog
 *
 * @author gigabud
 */
public class TwoBtnsDialog extends BaseDialogFragment implements OnClickListener {
    private String strDialogTitle;
    private String strDialogBody;
    private String strDialogLeftBtn;
    private String strDialogRightBtn;
    private Handler callBackHandler;
    private boolean isDialogBackgroundClickable = true;
    private boolean isDialogBackKeyValid = true;
    private boolean isNeedExclamation = true;

    public TwoBtnsDialog() {
        super();
    }


    public void setNeedExclamation(boolean needExclamation) {
        isNeedExclamation = needExclamation;
    }

    /**
     * 是否支持在外点击消失
     *
     * @return
     */
    @Override
    protected boolean isSupportCancelOnTouchOutside() {
        return isDialogBackgroundClickable;
    }

    @Override
    protected boolean isSupportBackKeyEvent() {
        return isDialogBackKeyValid;
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshUI();
    }

    public void setTextInfo(String strTitle, String strBody, String strLeftBtn, String strRightBtn, boolean isBackgroundClick, boolean isBackKeyValid, boolean needExclamation, Handler callback) {
        strDialogBody = strBody;
        strDialogLeftBtn = strLeftBtn;
        strDialogRightBtn = strRightBtn;
        callBackHandler = callback;
        isDialogBackgroundClickable = isBackgroundClick;
        strDialogTitle = strTitle;
        isDialogBackKeyValid = isBackKeyValid;
        isNeedExclamation = needExclamation;
    }


    public void setTextInfo(String strTitle, String strBody, String strLeftBtn, String strRightBtn, boolean isBackgroundClick, boolean needExclamation, Handler callback) {
        setTextInfo(strTitle, strBody, strLeftBtn, strRightBtn, isBackgroundClick, true, needExclamation, callback);
    }

    public void setTextInfo(String strBody, String strLeftBtn, String strRightBtn, boolean isBackgroundClick, Handler callback) {
        setTextInfo(null, strBody, strLeftBtn, strRightBtn, isBackgroundClick, true, true, callback);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.dialog_fragment_style);

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_error, null);
        //View view = inflater.inflate(R.layout.dialog_one_btn, null);
        return view;
    }


    private void refreshUI() {
        View view = getView();
    //    view.setBackgroundDrawable(getResources().getDrawable(R.drawable.round_shape_popupwin));
        TextView textViewBody = (TextView) view.findViewById(R.id.tvContent);

        TextView leftTextView = (TextView) view.findViewById(R.id.leftTextView);
        TextView rightTextView = (TextView) view.findViewById(R.id.rightTextView);
        View viewMid = view.findViewById(R.id.viewMid);
        View viewLeft = view.findViewById(R.id.viewLeft);
        View viewRight = view.findViewById(R.id.viewRight);
        ImageView ivExclamation = (ImageView) view.findViewById(R.id.ivExclamation);

        ivExclamation.setVisibility(isNeedExclamation ? View.VISIBLE : View.GONE);

        viewMid.setVisibility(View.VISIBLE);

        textViewBody.setText(strDialogBody);
        if (strDialogLeftBtn == null) {
            leftTextView.setVisibility(View.GONE);
            viewMid.setVisibility(View.GONE);
        } else
            leftTextView.setText(strDialogLeftBtn);

        if (strDialogRightBtn == null) {
            rightTextView.setVisibility(View.GONE);
            viewMid.setVisibility(View.GONE);
        } else
            rightTextView.setText(strDialogRightBtn);


        if (strDialogLeftBtn == null || strDialogRightBtn == null) {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewLeft.getLayoutParams();
            lp.weight = 8;
            viewLeft.setLayoutParams(lp);
            lp = (LinearLayout.LayoutParams) viewRight.getLayoutParams();
            lp.weight = 8;
            viewRight.setLayoutParams(lp);
        } else {
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) viewLeft.getLayoutParams();
            lp.weight = 3;
            viewLeft.setLayoutParams(lp);
            lp = (LinearLayout.LayoutParams) viewRight.getLayoutParams();
            lp.weight = 3;
            viewRight.setLayoutParams(lp);
        }

        if (strDialogLeftBtn == null && strDialogRightBtn != null) {
            rightTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_roundrect_redbg_whitetext));
            rightTextView.setTextColor(getResources().getColor(android.R.color.white));
        } else if (strDialogRightBtn == null && strDialogLeftBtn != null) {
            leftTextView.setBackgroundDrawable(getResources().getDrawable(R.drawable.btn_roundrect_redbg_whitetext));
            leftTextView.setTextColor(getResources().getColor(android.R.color.white));
        }

        leftTextView.setOnClickListener(this);
        rightTextView.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        Message msg = null;
        switch (v.getId()) {
            case R.id.leftTextView:
                dismiss();
                if (callBackHandler != null) {
                    msg = callBackHandler.obtainMessage(Constants.DIALOG_LEFT_BTN);
                    callBackHandler.sendMessage(msg);
                }
                break;
            case R.id.rightTextView:
                dismiss();
                if (callBackHandler != null) {
                    msg = callBackHandler.obtainMessage(Constants.DIALOG_RIGHY_BTN);
                    callBackHandler.sendMessage(msg);
                }
                break;

            default:
                break;
        }

    }

}

