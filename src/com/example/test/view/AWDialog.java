package com.example.test.view;

import com.example.test.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


/**
 * 自定义dialog
 * @author xiashaojun
 *
 */
public class AWDialog extends Dialog {
	private Button mNegativeButton;
	private Button mPositiveButton;
	private TextView mTitleView;
	private TextView mMessageView;

	public AWDialog(Context context, int theme) {
		super(context, R.style.bvDialog);
	}

	public AWDialog(Context context) {
		super(context, R.style.bvDialog);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dialog_custom);
		mNegativeButton = (Button) findViewById(R.id.negativeButton);
		mPositiveButton = (Button) findViewById(R.id.positiveButton);
		mPositiveButton.requestFocus();
		mTitleView = (TextView) findViewById(R.id.title);
		mMessageView = (TextView) findViewById(R.id.message);
	}
	
	public void setMessage(int message) {
		mMessageView.setText(message);
	}
	
	public void setTitle(int title) {
		mTitleView.setText(title);
		mTitleView.setVisibility(View.VISIBLE);
	}
	
	public void setNegativeListener(View.OnClickListener clickListener) {
		mNegativeButton.setOnClickListener(clickListener);
	}
	
	public void setPositiveListener(View.OnClickListener clickListener) {
		mPositiveButton.setOnClickListener(clickListener);
	}
}
