package com.example.test.view;

import com.example.test.R;
import com.example.test.R.drawable;
import com.example.test.R.id;
import com.example.test.Helper.CameraHolder;
import com.example.test.callback.CallbackForReverseLayout;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.TextureView;
import android.view.View;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class ReverseLayout extends RelativeLayout implements SurfaceTextureListener{
	
	private TextureView mReveserSurface;
	private MyRadarSurfaceView mRadarSurface;
	private ImageView mReverseImageViewAuxLineStandard;
	private ImageView mReverseImageViewAuxLineAngle;
	private SurfaceTexture mSurfaceTexture;
	private boolean mAttached = false;
	private final static String ACTION_START_SERVICE = "com.luyuan.recorder";
	private final static String PACKAGE_NAME = "com.example.test";
	private Context mContext;
	private Handler mHandler;
	private SetTextureRunnable mSetTextureRunnable;
	private SetTextureRunnable360 mSetTextureRunnable360;
	private HandleData mHandleData;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLayoutParams;
	private boolean mIsShow = false;
	private byte mRadarFlag;
	private byte mReverseAuxLineFlag;
	private byte mReverseAuxLineDirection;
	private byte mReverseAuxLineAngleValue;
	private static final String THREAD_NAME = "reverse_thread";
	private static final String TAG = "LY_RECORDER_reverseLayout";
	private int mCurrentCameraId = -1;
	private int mLastCameraId = -1;
	private final byte BIT_MASK_HIGH = 0x1f;  //去掉高位的掩码
	private int[] mDrawableArraysLeft = new int[]{
		R.drawable.eps00,R.drawable.epsl01,	R.drawable.epsl02,R.drawable.epsl03,R.drawable.epsl04,
		R.drawable.epsl05,R.drawable.epsl06,R.drawable.epsl07,R.drawable.epsl08,R.drawable.epsl09,
		R.drawable.epsl10,R.drawable.epsl11,R.drawable.epsl12,R.drawable.epsl13,R.drawable.epsl14,
		R.drawable.epsl15,R.drawable.epsl16,R.drawable.epsl17,R.drawable.epsl18,R.drawable.epsl19
	};
	
	private int[] mDrawableArraysRight = new int[]{
		R.drawable.eps00,R.drawable.epsr01,	R.drawable.epsr02,R.drawable.epsr03,R.drawable.epsr04,
		R.drawable.epsr05,R.drawable.epsr06,R.drawable.epsr07,R.drawable.epsr08,R.drawable.epsr09,
		R.drawable.epsr10,R.drawable.epsr11,R.drawable.epsr12,R.drawable.epsr13,R.drawable.epsr14,
		R.drawable.epsr15,R.drawable.epsr16,R.drawable.epsr17,R.drawable.epsr18,R.drawable.epsr19
	};
	private CallbackForReverseLayout mCallback;
	
	public ReverseLayout(Context context) {
		this(context,null);
	}

	public ReverseLayout(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public ReverseLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
		mLayoutParams = new WindowManager.LayoutParams();
		mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE
					/*|WindowManager.LayoutParams.TYPE_PHONE*/;
			
		mLayoutParams.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN
				|WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				|WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
				/*|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL*/
				|WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
		mLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
		mLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
		
		mHandler = new Handler();
		
	}
	
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		mReveserSurface = (TextureView)findViewById(R.id.reverse_texture);
		mRadarSurface = (MyRadarSurfaceView)findViewById(R.id.reverse_radar);
		mReverseImageViewAuxLineStandard = (ImageView)findViewById(R.id.reverse_aux_line_standard);
		mReverseImageViewAuxLineAngle = (ImageView)findViewById(R.id.reverse_aux_line_angle);
		mReveserSurface.setSurfaceTextureListener(this);
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		mAttached = true;
		//startAndBindService();
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.w(TAG, "event---"+event.getKeyCode());
		return true;
	}
	
	public void showReverseWindow(int cameraId){
		if(!mIsShow){
			mIsShow = true;
			mCurrentCameraId = cameraId;
			mWindowManager.addView(this, mLayoutParams);
		}else{
			if(mCurrentCameraId == cameraId){
				//do nothing
			}else{
				Log.w(TAG, "when show reverse window,cameraId changed!");
				changeSurfaceTexture(cameraId);
			}
		}
	}
	
	public void showReverseWindow360(int cameraId){
		Log.w(TAG, "show ReverseWindow360");
		if(!mIsShow){
			mIsShow = true;
			mCurrentCameraId = cameraId;
			mWindowManager.addView(this, mLayoutParams);
		}else{
			if(mCurrentCameraId == cameraId){
				//do nothing
			}else{
				changeSurfaceTexture360(cameraId,false);
			}
		}
	}
	
	private void changeSurfaceTexture(int cameraId){
		mLastCameraId = mCurrentCameraId;
		mCurrentCameraId = cameraId;
		if(mSurfaceTexture == null){
			Log.e(TAG, "something error when change surfaceTexture!");
		}else{
			mSetTextureRunnable = new SetTextureRunnable(mCurrentCameraId,mLastCameraId);
			mHandler.post(mSetTextureRunnable);
		}		
	}
	
	private void changeSurfaceTexture360(int cameraId,boolean reset){
		if(mSurfaceTexture == null){
			Log.e(TAG, "something error when change surfaceTexture!");
		}else{
			mSetTextureRunnable360 = new SetTextureRunnable360(cameraId, false);
			mHandler.post(mSetTextureRunnable360);
		}
	}
	
	public void hideReverseWindow(){
		if(mIsShow){
			if(mCallback != null)
				mCallback.hideReverseWindow(mCurrentCameraId); //给camera预览窗口
			mWindowManager.removeView(this);
			mIsShow = false;
			mCurrentCameraId = -1;
			mLastCameraId = -1;
		}
	}
	
	public void hideReverseWindow360(){
		if(mIsShow){
			if(mCallback != null)
				mCallback.hideReverseWindow360(mCurrentCameraId); //给camera预览窗口
			mWindowManager.removeView(this);
			mIsShow = false;
			mCurrentCameraId = -1;
		}
	}
	
	public void setDataFromMcu(byte[] data){
		Log.w(TAG, "set by broadcast!");
		mHandleData = new HandleData(data);
		mHandler.post(mHandleData);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mAttached = false;
		mHandler.removeCallbacksAndMessages(null);
		//mContext.unbindService(mServiceconnection);
	}
	
	public void registerCallback(CallbackForReverseLayout callback){
		mCallback = callback;
	}
	
	public void unRegisterCallback(){
		mCallback = null;
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
			int height) {
		mSurfaceTexture = surface;
		if(mCurrentCameraId != CameraHolder.getGlobalCameraId()){
			mSetTextureRunnable = new SetTextureRunnable(mCurrentCameraId,-1);
			mHandler.post(mSetTextureRunnable);
		}else{
			mSetTextureRunnable360 = new SetTextureRunnable360(mCurrentCameraId, true);
			mHandler.post(mSetTextureRunnable360);
		}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
			int height) {
		
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		mSurfaceTexture = null;
		return true;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		
	}
	
	
	private class SetTextureRunnable implements Runnable{
		private int tCurrentCameraId;
		private int tLastCameraId;
		
		public SetTextureRunnable(int currentCameraId,int lastCameraId){
			tCurrentCameraId = currentCameraId;
			tLastCameraId = lastCameraId;
		}
		
		@Override
		public void run() {
			if(mSurfaceTexture != null && mCallback != null){
				mCallback.setTexture(mSurfaceTexture, tCurrentCameraId , false, tLastCameraId);
			}else{
				mHandler.postDelayed(mSetTextureRunnable, 500);
			}
		}		
	}
	
	private class SetTextureRunnable360 implements Runnable{

		private int tCameraId;
		private boolean tReset;
		
		public SetTextureRunnable360(int cameraId,boolean reset){
			tCameraId = cameraId;
			tReset = reset;
		}
		
		@Override
		public void run() {
			if(mSurfaceTexture != null && mCallback != null){
				mCallback.setTexture360(mSurfaceTexture,tCameraId,false,tReset);
			}else{
				mHandler.postDelayed(mSetTextureRunnable360, 500);
			}
		}
		
	}
	
	private class HandleData implements Runnable{

		private byte[] tData;
		private byte[] tRadarData;
		
		public HandleData(byte[] data){
			tData = data;
		}
		
		@Override
		public void run() {
			if(tData.length < 2){ 
				Log.w(TAG, "data length less than 2,ignore it!length:"+tData.length);
				return;
			}
			tRadarData = new byte[tData.length-2];
			System.arraycopy(tData, 2, tRadarData, 0, tData.length-2);
			Log.w(TAG, "data0:data1 = "+tRadarData[0]+" : "+tRadarData[1]);
			mRadarFlag = (byte)((tRadarData[0]>>7)&0x1);			
			if(mRadarSurface != null){
				if(mRadarFlag == 0){
					mRadarSurface.setVisibility(View.GONE);
				}else{
					mRadarSurface.setVisibility(View.VISIBLE);
					mRadarSurface.updateData(tRadarData);
				}
			}
			mReverseAuxLineFlag = (byte)((tRadarData[0]>>6)&0x1);
			if(mReverseAuxLineFlag == 0){
				mReverseImageViewAuxLineStandard.setVisibility(View.GONE);
				mReverseImageViewAuxLineAngle.setVisibility(View.GONE);
				return;
			}else if(mReverseAuxLineFlag == 1){
				mReverseImageViewAuxLineStandard.setVisibility(View.VISIBLE);
				mReverseImageViewAuxLineAngle.setVisibility(View.VISIBLE);				
			}else{
				Log.e(TAG, "something error when handle reverse data!");
				return;
			}
			mReverseAuxLineDirection = (byte)(tRadarData[0]>>5&0x1);
			mReverseAuxLineAngleValue = (byte)(tRadarData[0]&BIT_MASK_HIGH);
			if(mReverseAuxLineAngleValue>=19){
				mReverseAuxLineAngleValue = 19;
			}
			if(mReverseAuxLineDirection == 0){
				mReverseImageViewAuxLineAngle.setImageResource(mDrawableArraysRight[mReverseAuxLineAngleValue]);
			}else{
				mReverseImageViewAuxLineAngle.setImageResource(mDrawableArraysLeft[mReverseAuxLineAngleValue]);
			}
		}
	}
	
	
	public void releaseAll(){
		mHandler.removeCallbacksAndMessages(null);
		mRadarSurface.releaseAll();
	}
	
}
