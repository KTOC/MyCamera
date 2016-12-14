package com.example.test;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.TextureView.SurfaceTextureListener;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity2 extends Activity{

	TextureView mTextureView0;
	TextureView mTextureView1;
	SurfaceTexture mSurfaceTexture;
	Camera mCamera;
	private String TAG = "testCamera";
	Parameters mParameters;
	private int mCameraNumbers;
	private LinearLayout mLinearLayout;
	private Context mContext;
	private static final int MSG_OPEN_CAMERA = 0;
	private static final int MSG_CLOSE_CAMERA = 1;
	private static final int MSG_START_RENDER = 2;
	private static final int MSG_STOP_RENDER = 3;
	private int mCameraId;
	private Camera[] mCameras = new Camera[2];
	private SurfaceTexture[] mSurfaceTextures = new SurfaceTexture[2];
	private boolean mSwitch = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main2);
		mContext = this;
		mTextureView0 = (TextureView)findViewById(R.id.textureView0);
		mTextureView0.setSurfaceTextureListener(new MySurfaceTextureListener(0));
		mTextureView1 = (TextureView)findViewById(R.id.textureView1);
		mTextureView1.setSurfaceTextureListener(new MySurfaceTextureListener(1));		
		mLinearLayout = (LinearLayout)findViewById(R.id.container);
		//getCameraIds();
		addSwitchButton();
	}


	
	private void openCamera(int cameraId,int surfaceTextureIndex){
		try{
			if(mCameras[cameraId] != null){
				mCameras[cameraId].setPreviewTexture(null);
				mCameras[cameraId].stopPreview();
				mCameras[cameraId].release();
			}
			mCameras[cameraId] = Camera.open(cameraId==0?0:5);
			mParameters = mCameras[cameraId].getParameters();
			Size previewSize = mParameters.getPreferredPreviewSizeForVideo();
			Log.i(TAG, "previewSize = "+previewSize.width+"x"+previewSize.height);
			mParameters.setPreviewSize(previewSize.width, previewSize.height);
			mCameras[cameraId].setParameters(mParameters);
			mCameras[cameraId].setPreviewTexture(mSurfaceTextures[surfaceTextureIndex]);
			mCameras[cameraId].startPreview();
		}catch(Exception e){
			mCameras[cameraId] = null;
			Log.e(TAG,"open camera failed!");
		}
	}
	
	private void closeCamera(int cameraId){
		if(mCameras[cameraId] != null){
			mCameras[cameraId].stopPreview();
			mCameras[cameraId].release();
		}
	}
	
	private void getCameraIds(){
		mCameraNumbers = Camera.getNumberOfCameras();
		Log.i(TAG, "camera total numbers = "+mCameraNumbers+"\n");
		CameraInfo info = new CameraInfo();
		for(int i =0;i< mCameraNumbers;i++){
			Camera.getCameraInfo(i, info);
			Log.i(TAG, "cameraId :"+info.facing+"\n");
			Button button= new Button(mContext);
			button.setText("C"+i);
			button.setTag(i);
			button.setOnClickListener(new MyButtonClick());
			mLinearLayout.addView(button);
		}
	}
	
	private void addSwitchButton(){
		Button button= new Button(mContext);
		button.setText("SWITCH");
		button.setTag("SWITCH");
		button.setOnClickListener(new MyButtonClick());
		mLinearLayout.addView(button);
	}
	
	private class  MyButtonClick implements View.OnClickListener{
		
		@Override
		public void onClick(View v) {
			/*mCameraId = (Integer)v.getTag();
			mHandler.obtainMessage(MSG_OPEN_CAMERA, mCameraId, 0).sendToTarget();*/
			String tag = (String)v.getTag();
			if("SWITCH".equals(tag)){
				if(mCameras[0]== null ||mCameras[1]==null||mSurfaceTextures[0]==null||mSurfaceTextures[1]==null){
					Log.e(TAG, "camera or surfaceTexture not prepare!");
					return;
				}
				mCameras[0].stopRender();
				mCameras[1].stopRender();
				try {
					if(mSwitch){
						mCameras[0].setPreviewTexture(mSurfaceTextures[0]);
						mCameras[1].setPreviewTexture(mSurfaceTextures[1]);
						mCameras[0].startRender();
						mCameras[1].startRender();
						mSwitch = false;
					}else{
						mCameras[1].setPreviewTexture(mSurfaceTextures[0]);
						mCameras[0].setPreviewTexture(mSurfaceTextures[1]);
						mCameras[1].startRender();
						mCameras[0].startRender();
						mSwitch = true;
					}
					
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_OPEN_CAMERA:
				if(mSwitch){
					openCamera(msg.arg1==0?1:0,msg.arg1);
				}else{
					openCamera(msg.arg1==0?0:1,msg.arg1);
				}
				
				break;
			case MSG_CLOSE_CAMERA:
				if(mSwitch){
					closeCamera(msg.arg1==0?1:0);
				}else{
					closeCamera(msg.arg1==0?0:1);
				}
				break;
			case MSG_START_RENDER:
				break;
			case MSG_STOP_RENDER:
				break;
			default:
				break;
			}
		};
	};
	
	private class MySurfaceTextureListener implements SurfaceTextureListener{
		
		private int tIndex;
		
		public MySurfaceTextureListener(int index){
			tIndex = index;
		}
		
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
				int height) {
			mSurfaceTextures[tIndex] = surface;
			mHandler.obtainMessage(MSG_OPEN_CAMERA, tIndex, 0).sendToTarget();		
			
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
				int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			mSurfaceTextures[tIndex] = null;
			mHandler.obtainMessage(MSG_CLOSE_CAMERA, tIndex, 0).sendToTarget();
			return false;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			
		}
	}
	
}
