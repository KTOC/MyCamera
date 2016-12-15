package com.example.test;

import com.example.test.callback.TextureViewCallback;
import com.example.test.view.MyTouchTextureView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity3 extends Activity implements TextureViewCallback{

	private MyTouchTextureView mMyTouchTextureView;
	private final String TAG = "TestTouch";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main3);
		mMyTouchTextureView = (MyTouchTextureView)findViewById(R.id.myTextureView);
		mMyTouchTextureView.registerCallback(this);
	}

	@Override
	public void onShortClick() {
		// TODO Auto-generated method stub
		Log.w(TAG, "------onClick-------");
		
	}

	@Override
	public void onLongClick() {
		// TODO Auto-generated method stub
		Log.w(TAG, "------onLongClick-------");
	}

	@Override
	public void onLeftSlip() {
		// TODO Auto-generated method stub
		Log.w(TAG, "------onLeftSlip-------");
	}

	@Override
	public void onRightSlip() {
		// TODO Auto-generated method stub
		Log.w(TAG, "------onRightSlip-------");
	}

	@Override
	public void onUpSlip() {
		// TODO Auto-generated method stub
		Log.w(TAG, "------onUpSlip-------");
	}

	@Override
	public void onDownSlip() {
		// TODO Auto-generated method stub
		Log.w(TAG, "------onDownSlip-------");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mMyTouchTextureView.unRegisterCallback();
		mMyTouchTextureView.release();
	}
	
}
