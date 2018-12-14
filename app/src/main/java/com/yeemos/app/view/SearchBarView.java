package com.yeemos.app.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.gbsocial.server.ServerDataManager;
import com.yeemos.app.BaseApplication;
import com.yeemos.app.R;

/***
 * 搜索Bar
 * 
 * @author Damon
 * 
 */
public class SearchBarView extends LinearLayout {
	private SearchBarViewListener listener;

	public interface SearchBarViewListener {
		abstract void onTextChanged(String text);
		abstract void onKeyBoardSearchBtnClicked(String text);
	}

	public SearchBarView(Context context) {
		super(context);
	}

	public SearchBarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
	}

	public SearchBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.view_searchbar, this);
//		getSearchOffView().setClickable(true);
		getCancel().setText(ServerDataManager.getTextFromKey("pblc_btn_cancel"));
		getEditText().setImeOptions(EditorInfo.IME_ACTION_SEARCH);
		getEditText().setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {  
					clearFocus();
					getInputManager().hideSoftInputFromWindow(getEditText().getWindowToken(), 0);
					if (SearchBarView.this.listener != null) {
						SearchBarView.this.listener.onKeyBoardSearchBtnClicked(getEditText().getText().toString());
					}
                }    
				return false;
			}
		});
		
		getEditText().setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				//Log.v("damon", "hasFocus" + hasFocus + "getEditText().getText().toString()"+getEditText().getText().toString() +"   isempty:" + getEditText().getText().toString().isEmpty());
				if (!hasFocus) {
					clearFocus();
					getInputManager().hideSoftInputFromWindow(getEditText().getWindowToken(), 0);
					if (getEditText().getText().toString().isEmpty()) {
						setNotEditing();
					}
				}
			}
		});
		
//		getSearchOffView().setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//					setEditing();
//			}
//		});
		
		getEditText().addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void afterTextChanged(Editable s) {
				if (SearchBarView.this.listener != null) {
					listener.onTextChanged(s.toString());
				}
			}
		});
		getImageRemove().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getEditText().setText("");
			}
		});
		getCancel().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				getInputManager().hideSoftInputFromWindow(getEditText().getWindowToken(), 0);
				BaseApplication.getCurFragment().goBack();
			}
		});
	}
	
//	private RelativeLayout getSearchOffView() {
//		return (RelativeLayout) findViewById(R.id.llSearchOff);
//	}

//	private LinearLayout getSearchOnView() {
//		return (LinearLayout) findViewById(R.id.llSearchOn);
//	}

	public TextView getCancel(){
		return (TextView)findViewById(R.id.cancel);
	}

	public View getLine(){
		return findViewById(R.id.line);
	}

	private ImageView getImageRemove(){
		return (ImageView)findViewById(R.id.imageRemove);
	}

	public EditText getEditText() {
		return (EditText) findViewById(R.id.etSearch);
	}

	public void setListener(SearchBarViewListener listener) {
		this.listener = listener;
	}
	private InputMethodManager getInputManager() {
		return (InputMethodManager)getEditText().getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
	}
	public void setEditing() {
//		getSearchOffView().setVisibility(View.GONE);
//		getSearchOnView().setVisibility(View.VISIBLE);
		getEditText().requestFocus();
		getInputManager().showSoftInput(getEditText(), 0);    
	}
	
	public void setNotEditing(){
//		if (getEditText().getHint() == null || getEditText().getHint().toString().equals("") || getEditText().getHint().length() == 0) {
//			getSearchOffView().setVisibility(View.VISIBLE);
//			getSearchOnView().setVisibility(View.GONE);
//		}else {
//			getSearchOffView().setVisibility(View.GONE);
//			getSearchOnView().setVisibility(View.VISIBLE);
//		}
		clearFocus();
	}
	
	public void setHint(String hint) {
		getEditText().setHint(hint);
		setNotEditing();
	}
	
	public String getText() {
		return getEditText().getText().toString();
	}
}
