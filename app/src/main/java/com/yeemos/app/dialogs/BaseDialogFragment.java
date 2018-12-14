package com.yeemos.app.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import com.yeemos.app.interfaces.IDismissListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseDialogFragment extends DialogFragment {


    protected boolean  mIsShow = false;
    protected List<IDismissListener> onDismissListenerList = new ArrayList<IDismissListener>();

    public BaseDialogFragment()
    {
        super();
    }


    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();


        //Window window = this.getDialog().getWindow();

        //window.setLayout(mDlgWidth, mDlgHeight);
        //window.setGravity(Gravity.CENTER);


    }

    /**
     * 是否支持在外点击消失
     * @return
     */
    protected  boolean isSupportCancelOnTouchOutside()
    {
        return false;
    }

    /**
     * 是否支持点击Back键返回
     */
    protected  boolean isSupportBackKeyEvent()
    {
        return true;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dlg = super.onCreateDialog(savedInstanceState);
        dlg.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                return !isSupportBackKeyEvent();
            }
        });
        return dlg;
    }

    public void onStart() {
        super.onStart();
        if(getDialog()!=null)
        {
            getDialog().setCanceledOnTouchOutside( isSupportCancelOnTouchOutside() );
            if(!isSupportCancelOnTouchOutside()) {
                getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            return true;
                        }
                        return false;
                    }
                });
            }

        }

    }



    @Override
    public void onDismiss(DialogInterface dialog) {
        // TODO Auto-generated method stub
        super.onDismiss(dialog);

        if (onDismissListenerList != null && onDismissListenerList.size() > 0)
        {
            onDismissListenerList.get(0).onDismiss();
            onDismissListenerList.remove(0);
        }
        mIsShow =false;
    }


    public void setOnDismissListener(IDismissListener onDismissListener) {
        this.onDismissListenerList.add(onDismissListener);
    }


    public void setIsShow(boolean bIsShow )
    {
        mIsShow = bIsShow;
    }

    public boolean isShow()
    {
        return mIsShow;

    }

    @Override
    public void show(FragmentManager manager, String tag) {
        mIsShow = true;
        super.show(manager, tag);
    }


    @Override
    public int show(FragmentTransaction transaction, String tag) {
        // TODO Auto-generated method stub
        mIsShow = true;
        return super.show(transaction, tag);
    }

    @Override
    public void dismiss() {
        mIsShow = false;
        super.dismiss();
    }

}
