package com.yeemos.app.fragment;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yeemos.app.utils.Constants;
import com.yeemos.app.R;

abstract public class HasBasicBtnsFragment extends BaseFragment {

    @Override
    public void onStop() {
        super.onStop();
        hideKeyBoard();
    }

    @Override
	public boolean refreshUIview(UI_SHOW_TYPE showType) {
		
		View view = getView();
		if(view != null) {
			if ( view.findViewById(R.id.btn_back) != null) {
				view.findViewById(R.id.btn_back).setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						goBack();
					}
				});
			}
			
			ViewGroup ll = (ViewGroup) view.findViewById(getBackgroudLayoutID());
			if (ll != null) {
				ll.setOnTouchListener(new OnTouchListener() {
				    @Override
				    public boolean onTouch(View view, MotionEvent ev) {
				        hideKeyBoard();
				        return false;
				    }
				});
			}
		
		}
		return false;
	}

	protected void setTopBarTitle(int titleResource) {
		View view = getView();
		if (view != null) {
			((TextView)view.findViewById(R.id.top_title)).setText(getResources().getString(titleResource));
		}
	}
	
	abstract public int getBackgroudLayoutID();
	
	@Override
	abstract public void updateUIText();

	@Override
	abstract public UI_SHOW_TYPE updateData(boolean bIsClearData);

	@Override
	abstract protected int getLayoutId();

	@Override
	public Constants.PHONE_FRAGMENT_UI_POSITION getFragmentPhoneUIPostion() {
		return Constants.PHONE_FRAGMENT_UI_POSITION.PHONE_FRAGMENT_UI_ALONE_POSITION;
	}

	@Override
	abstract protected void initFilterForBroadcast();
	
}
