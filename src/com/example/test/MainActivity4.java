package com.example.test;

import com.example.test.view.MyRadarSurfaceView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity4 extends Activity implements OnClickListener{

	private MyRadarSurfaceView mMySurfaceView;
	private Button mButton;
	private byte[] mData;
	private Handler mHandle = new Handler();
	private int count = 0;
	private int mCurrentArrayIndex = 0;
	private boolean needChange = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main3);
		mMySurfaceView = (MyRadarSurfaceView)findViewById(R.id.testSurfaceView);
		Log.w("test","mSurfaceView is "+((mMySurfaceView==null)?"null":"not null"));
		mButton = (Button)findViewById(R.id.myBtn);
		mButton.setOnClickListener(this); 
		mData = new byte[]{-1,0x00,0x00,0x00,0x00};
		/*mHandle.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mSurfaceView.updateData(mData);	
			}
		}, 3000);*/
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMySurfaceView.releaseAll();
	}


	@Override
	public void onClick(View v) {
		if(mMySurfaceView == null){
			Log.w("test", "null");
		}else{
			int length = mData.length;
			if(needChange){
				if(mCurrentArrayIndex >= 0 && mCurrentArrayIndex < 4){
					mCurrentArrayIndex++;
				}else{
					mCurrentArrayIndex=1;
					mData = new byte[]{-1,0x00,0x00,0x00,0x00};
				}
				needChange = false;
			}
			if(mData[mCurrentArrayIndex]>=0 && mData[mCurrentArrayIndex]<5){
				mData[mCurrentArrayIndex]++;
				count = 0;
			}else if(mData[mCurrentArrayIndex] >= 5 && mData[mCurrentArrayIndex] < 85){
				count++;
				mData[mCurrentArrayIndex] = (byte)(5+16*count);
			}else{
				count = 0;				
				needChange = true;
			}
			
			
			mMySurfaceView.updateData(mData);	
		}
	}
}
