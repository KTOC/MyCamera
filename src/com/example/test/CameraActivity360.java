package com.example.test;

import com.example.test.CameraMainService.MyBind;
import com.example.test.Helper.CameraHolder;
import com.example.test.Helper.CameraMode;
import com.example.test.Helper.CameraUtils;
import com.example.test.Helper.ToastUtils;
import com.example.test.callback.CameraActivityCallback;
import com.example.test.callback.TextureViewCallback;
import com.example.test.view.DisplaySetDialog;
import com.example.test.view.MyTouchTextureView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class CameraActivity360 extends Activity implements OnClickListener,TextureViewCallback{

	private MyTouchTextureView mMyTouchTextureView;
	private ImageView mImageViewRecorder;
	private ImageView mImageViewSettings;
	private ImageView mImageViewFolder;
	private ImageView mImageViewLock;
	private TextView mTextViewTime;
	private ImageView mImageViewTimeIcon0;
	private ImageView mImageViewTimeIcon1;
	private ImageView mImageViewBird;
	private ImageView mImageViewDisplay;
	private ImageView mImageViewStatus;
	private View mLayoutControl;
	private MyBind mService;
	private MyCameraActivityCallbackImpl mCameraActivityCallbackImpl;
	private SurfaceTexture mSurfaceTexture0;
	private static final int MSG_SET_TEXTURE0 = 0;
	private static final int MSG_UPDATE_TIME = 3;
	private static final int MSG_UPDATE_UI = 4;
	private static final int MSG_ENABLE_RECORDER = 6;
	private static final int MSG_CONTROL_ITEM_VISIBLE = 8;
	private static final int MSG_FINISH_ACTIVITY = 9;
	private static final int MSG_CLICK_SETTINGS = 10;
	private static final int MSG_CLICK_FOLDER = 11;
	private static final int MSG_CLICK_LOCK = 12;
	private static final int MSG_UPDATE_LOCK_STATE = 13;
	private static final int MSG_CLICK_LONG = 15;
	private static final int MSG_CLICK_UP = 16;
	private static final int MSG_CLICK_DOWN = 17;
	private static final int MSG_CLICK_LEFT = 18;
	private static final int MSG_CLICK_RIGHT = 19;
	private static final int MSG_CLICK_FIVE = 21;
	private static final int MSG_UPDATE_BIRD_MODE = 22;
	private static final int MSG_CLICK_BIRD = 23;
	private static final int MSG_CLICK_DISPLAY = 24;
	private static final int MSG_UPDATE_PREVIEW_MODE = 25;
	
	private static final String KEY_RECORD_TIME = "recordingTime";
	private static final String KEY_RECORD_TOTAL = "recordingStatusTotal";
	private static final String KEY_RECORD_SINGLE = "recordingStatusSingle";
	private static final String KEY_RECORD_CAMERA_ID = "recordingId";
	private static final String KEY_LOCK = "lockStatus";
	private static final String TAG = "CameraActivity";
	private Intent mIntent;
	private final static String ACTION_START_SERVICE = "com.luyuan.recorder";
	private final static String PACKAGE_NAME = "com.example.test";
	private MyServiceconnection mServiceconnection;
	private TextureListener mTextureListener0;
	private Context mContext;
	private TranslateAnimation mTranslateAnimationOpen;
	private TranslateAnimation mTranslateAnimationClose;
	private int mAnimationOffset = 0;
	private AlertDialog mAlertDialog;
	private AlertDialog.Builder mBuilder;
	private long firstClickTime;
	private long nextClickTime;
	private int mClickCount;
	private DisplaySetDialog mDisplaySetDialog;
	private int[] mPreviewModeDrawables = new int[]{
		R.drawable.ic_all_select,R.drawable.ic_front_select,R.drawable.ic_behind_select,
		R.drawable.ic_left_select,R.drawable.ic_right_select
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_360);
		mContext = this;
		mMyTouchTextureView = (MyTouchTextureView)findViewById(R.id.mMyTouchTextureView);
		mImageViewStatus = (ImageView)findViewById(R.id.iv_status);
		mImageViewRecorder = (ImageView)findViewById(R.id.iv_record);
		mImageViewSettings = (ImageView)findViewById(R.id.iv_settings);
		mImageViewFolder = (ImageView)findViewById(R.id.iv_fold);
		mImageViewLock = (ImageView)findViewById(R.id.iv_lock);
		mImageViewBird = (ImageView)findViewById(R.id.iv_bird);
		mImageViewDisplay = (ImageView)findViewById(R.id.iv_display);
		mTextViewTime = (TextView)findViewById(R.id.tv_time);
		mImageViewTimeIcon0 = (ImageView)findViewById(R.id.iv_time_icon0);
		mImageViewTimeIcon1 = (ImageView)findViewById(R.id.iv_time_icon1);
		mLayoutControl = (View)findViewById(R.id.layout_control_360);
		
		mCameraActivityCallbackImpl = new MyCameraActivityCallbackImpl();
		
		mTextureListener0 = new TextureListener();
		
		mMyTouchTextureView.setSurfaceTextureListener(mTextureListener0);
		mImageViewRecorder.setOnClickListener(this);
		mImageViewSettings.setOnClickListener(this);
		mImageViewFolder.setOnClickListener(this);
		mImageViewLock.setOnClickListener(this);
		mImageViewBird.setOnClickListener(this);
		mImageViewDisplay.setOnClickListener(this);
		mMyTouchTextureView.registerCallback(this);
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.w(TAG, "onStart-------");
		startAndBindService();
	}
	
	
	private void startAndBindService(){
		if(mIntent == null){
			mIntent = new Intent(ACTION_START_SERVICE);
			mIntent.setPackage(PACKAGE_NAME);
		}
		if(mServiceconnection == null){
			mServiceconnection = new MyServiceconnection();
		}
		
		startService(mIntent);
		bindService(mIntent, mServiceconnection, BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.w(TAG, "onResume------");
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.w(TAG, "onPause------");
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.w(TAG, "onStop-------");
		mLayoutControl.clearAnimation();
		if(mService != null){
			mService.activityExits();
		}
		unbindService(mServiceconnection);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMyTouchTextureView.unRegisterCallback();
		if(mService != null){
			mService.unRegisterCallback();
		}
		Log.w(TAG, "onDestroy-------");
		mHandler.removeCallbacksAndMessages(null);
	}
	
	private class TextureListener implements SurfaceTextureListener{
		
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height) {
			Log.w(TAG, "SurfaceTexture Available");
			mSurfaceTexture0 = surface;
			mHandler.sendEmptyMessageDelayed(MSG_SET_TEXTURE0,0);	
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height) {
			
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			Log.w(TAG, "SurfaceTexture destroy");
			
			return false;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
	private class MyServiceconnection implements ServiceConnection{

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = (MyBind)service;
			mService.registerCallback(mCameraActivityCallbackImpl);
			mService.openCameras();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			mService.unRegisterCallback();
		}
		
	}
	
	private class MyCameraActivityCallbackImpl  implements CameraActivityCallback{
		private Bundle sBundle;
		private Bundle tBundle;

		@Override
		public int getDisplayRotation() {			
			return CameraUtils.getDisplayRotation(CameraActivity360.this);
		}

		@Override
		public void enableShutter(boolean enable) {
			
		}

		@Override
		public void enableRecorder(boolean enable) {
			mHandler.obtainMessage(MSG_ENABLE_RECORDER, enable?1:0, 0).sendToTarget();
		}
		
		@Override
		public void animationFlash() {
			
		}

		@Override
		public void updateRecordingTime(String text, long startTime,
				long durationTime) {
			Log.w(TAG, "updateRecordingTime: "+text);
			tBundle = new Bundle();
			tBundle.putString(KEY_RECORD_TIME, text);
			Message msg = Message.obtain(mHandler, MSG_UPDATE_TIME);
			msg.setData(tBundle);
			mHandler.sendMessage(msg);
		}

		@Override
		public void updateActivityUi(boolean recording, int cameraId,
				boolean cameraRecordingStatus, boolean lock,String time) {
			sBundle = new Bundle();
			sBundle.putBoolean(KEY_RECORD_TOTAL, recording);
			sBundle.putBoolean(KEY_RECORD_SINGLE, cameraRecordingStatus);
			sBundle.putInt(KEY_RECORD_CAMERA_ID, cameraId);
			sBundle.putBoolean(KEY_LOCK, lock);
			sBundle.putString(KEY_RECORD_TIME, time);
			Message msg = Message.obtain();
			msg.what = MSG_UPDATE_UI;
			msg.setData(sBundle);
			mHandler.sendMessage(msg);
		}

		@Override
		public void finishActivity() {
			mHandler.sendEmptyMessage(MSG_FINISH_ACTIVITY);
		}

		@Override
		public void updateActivityLockState(boolean lock) {
			mHandler.obtainMessage(MSG_UPDATE_LOCK_STATE, lock?1:0, 0).sendToTarget();
		}

		@Override
		public void updateActivityBirdMode(boolean bird) {
			mHandler.obtainMessage(MSG_UPDATE_BIRD_MODE, bird?1:0, 0).sendToTarget();
		}

		@Override
		public void updateActivityPreviewMode(int mode) {
			mHandler.obtainMessage(MSG_UPDATE_PREVIEW_MODE, mode, 0).sendToTarget();
		}

	}
	
	private Handler mHandler = new Handler(){
		
		private Bundle hBundle;
		private Bundle tBundle;
		private boolean hTotalRecordingStatus;
		private boolean hSingleRecordingStatus;
		private int hCameraId;
		private boolean hLock;
		private String hTime;
		private int hPreviewMode;
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SET_TEXTURE0:
				if(mSurfaceTexture0 == null || mService == null || !mService.isPreviewing(CameraHolder.getGlobalCameraId())){
					mHandler.sendEmptyMessageDelayed(MSG_SET_TEXTURE0, 1000);
				}else{
					mService.postSurfaceTexture360(mSurfaceTexture0,CameraHolder.getGlobalCameraId(),true,true);
				}
				break;		
			case MSG_ENABLE_RECORDER:
				mHandler.removeMessages(MSG_ENABLE_RECORDER);
				mImageViewRecorder.setEnabled(msg.arg1==1?true:false);
				break;
			case MSG_UPDATE_TIME:				
				tBundle = msg.getData();
				hTime = tBundle.getString(KEY_RECORD_TIME);
				Log.w(TAG, "msg--->time:"+hTime);
				mTextViewTime.setText(hTime);
				break;
			case MSG_UPDATE_UI:
				hBundle = msg.getData();
				hTotalRecordingStatus = hBundle.getBoolean(KEY_RECORD_TOTAL);
				hSingleRecordingStatus = hBundle.getBoolean(KEY_RECORD_SINGLE);
				hCameraId = hBundle.getInt(KEY_RECORD_CAMERA_ID);
				hLock = hBundle.getBoolean(KEY_LOCK);
				hTime = hBundle.getString(KEY_RECORD_TIME);
				Log.w(TAG, "total:single:id = "+hTotalRecordingStatus+":"+hSingleRecordingStatus+":"+hCameraId);
				if(hTotalRecordingStatus){
					mImageViewRecorder.setImageResource(R.drawable.camera_stop_record);
					mTextViewTime.setVisibility(View.VISIBLE);
					mTextViewTime.setText(hTime);
				}else{
					mImageViewRecorder.setImageResource(R.drawable.camera_start_record);
					mTextViewTime.setVisibility(View.GONE);
				}
				
				if(hCameraId == CameraHolder.getGlobalCameraId()){
					mImageViewTimeIcon0.setVisibility(hSingleRecordingStatus?View.VISIBLE:View.GONE);
				}else{
					mImageViewTimeIcon1.setVisibility(hSingleRecordingStatus?View.VISIBLE:View.GONE);
				}
				
				mImageViewLock.setImageResource(hLock?R.drawable.lock_red:R.drawable.lock_grey);
				break;		
			case MSG_CONTROL_ITEM_VISIBLE:
				mClickCount = 0;
				mAnimationOffset = mLayoutControl.getHeight();
				mLayoutControl.clearAnimation();
				if(mLayoutControl.getVisibility() == View.GONE){
					startOpenAnimation();
				}else{
					startCloseAnimation();
				}
				break;
			case MSG_FINISH_ACTIVITY:
				CameraActivity360.this.finish();
				break;
			case MSG_CLICK_SETTINGS:
				if(mService != null && mService.isRecording()){
					showAlertDialog();
				}else{
					goToSettings();
				}
				break;
			case MSG_CLICK_FOLDER:
				goToFileActivity();
				break;
			case MSG_CLICK_LOCK:
				if(mService == null || !mService.isRecording()){
					ToastUtils.showShortToast(mContext, R.string.str_cannot_lock_as_not_recording);
				}else{
					if(mService != null){
						mService.changeLockState();
					}
				}
				break;
			case MSG_UPDATE_LOCK_STATE:
				if(msg.arg1 == 1){
					mImageViewLock.setImageResource(R.drawable.lock_red);
				}else{
					mImageViewLock.setImageResource(R.drawable.lock_grey);
				}
				break;
			case MSG_UPDATE_BIRD_MODE:
				if(msg.arg1==1){
					mImageViewBird.setImageResource(R.drawable.bird_y);
				}else{
					mImageViewBird.setImageResource(R.drawable.bird_n);
				}
				break;
			case MSG_CLICK_FIVE:
				mClickCount = 0;
				if(mImageViewBird.getVisibility() == View.GONE)
					mImageViewBird.setVisibility(View.VISIBLE);
				else
					mImageViewBird.setVisibility(View.GONE);
				break;
			case MSG_CLICK_LONG:
				if(mService != null && !mService.isBirdMode()){
					mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_CA,true);
				}
				break;
			case MSG_CLICK_UP:
				if(mService != null){
					if(mService.isBirdMode()){
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_BB,true);
					}else{
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_CB,true);
					}
				}
				break;
			case MSG_CLICK_DOWN:
				if(mService != null){
					if(mService.isBirdMode()){
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_BF,true);
					}else{
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_CF,true);
					}
				}
				break;
			case MSG_CLICK_LEFT:
				if(mService != null){
					if(mService.isBirdMode()){
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_BR,true);
					}else{
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_CR,true);
					}
				}
				break;
			case MSG_CLICK_RIGHT:
				if(mService != null){
					if(mService.isBirdMode()){
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_BL,true);
					}else{
						mService.changePreviewModeByUser(CameraMode.MODE_PREVIEW_CL,true);
					}
				}
				break;
			case MSG_CLICK_BIRD:
				if(mService != null){
					mService.changeBirdModeByUser(true);
				}
				break;
			case MSG_CLICK_DISPLAY:
				showDisplaySettingDialog();
				break;
			case MSG_UPDATE_PREVIEW_MODE:
				hPreviewMode = msg.arg1%10;
				mImageViewStatus.setImageResource(mPreviewModeDrawables[hPreviewMode]);
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_record:
			mImageViewRecorder.setEnabled(false);
			mHandler.removeMessages(MSG_ENABLE_RECORDER);
			Message msg1 = Message.obtain();
			msg1.what = MSG_ENABLE_RECORDER;
			msg1.arg1 = 1;
			mHandler.sendMessageDelayed(msg1, 5000);
			if(mService != null){
				if(!mService.isRecording()){
					Log.w(TAG, "onClick start record");
					mService.startVideoRecording();
				}else{
					Log.w(TAG, "onClick stop record");
					mService.stopVideoRecording();
				}
			}else{
				printErrorLog();
			}
			break;
		case R.id.iv_fold:
			mHandler.removeMessages(MSG_CLICK_FOLDER);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_FOLDER, 200);
			break;
		case R.id.iv_settings:
			mHandler.removeMessages(MSG_CLICK_SETTINGS);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_SETTINGS, 200);
			break;
		case R.id.iv_lock:
			mHandler.removeMessages(MSG_CLICK_LOCK);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_LOCK, 200);
			break;
		case R.id.iv_bird:
			mHandler.removeMessages(MSG_CLICK_BIRD);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_BIRD, 200);
			break;
		case R.id.iv_display:
			mHandler.removeMessages(MSG_CLICK_DISPLAY);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_DISPLAY, 200);
			break;
		default:
			break;
		}
		
	}
	
	private void goToSettings(){
		Intent intent = new Intent(this, SettingActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
	
	private void goToFileActivity(){
		Intent intent = new Intent(this, FileActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}
	
	private void showDisplaySettingDialog() {
        mDisplaySetDialog = new DisplaySetDialog(this);
        mDisplaySetDialog.setCanceledOnTouchOutside(true);
        mDisplaySetDialog.show();
    }
	
	private void showAlertDialog(){
		if(mAlertDialog == null){
			mBuilder = new AlertDialog.Builder(mContext);
			mBuilder.setTitle(mContext.getResources().getString(R.string.str_title));
			mBuilder.setMessage(mContext.getResources().getString(R.string.str_jump_to_settings_message));
			mBuilder.setPositiveButton(mContext.getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(mService != null && mService.isRecording()){
						mService.stopVideoRecording();
					}
					goToSettings();
					dialog.dismiss();
				}
			});
			mBuilder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			mAlertDialog = mBuilder.create();
		}
		mAlertDialog.show();
	}
	
	private void startOpenAnimation(){
		if(mTranslateAnimationOpen == null){
			mTranslateAnimationOpen = new TranslateAnimation(0,0,mAnimationOffset,0);
			mTranslateAnimationOpen.setDuration(500);
			mTranslateAnimationOpen.setInterpolator(new LinearInterpolator());
			mTranslateAnimationOpen.setAnimationListener(new MyAnimationListener(1));
		}
		mLayoutControl.startAnimation(mTranslateAnimationOpen);
	}
	
	private void startCloseAnimation(){
		if(mTranslateAnimationClose == null){
			mTranslateAnimationClose = new TranslateAnimation(0,0,0,mAnimationOffset);
			mTranslateAnimationClose.setDuration(500);
			mTranslateAnimationClose.setInterpolator(new LinearInterpolator());
			mTranslateAnimationClose.setAnimationListener(new MyAnimationListener(0));
		}
		mLayoutControl.startAnimation(mTranslateAnimationClose);
	}
	
	private class MyAnimationListener implements AnimationListener{

		private int mFlag = 0;
		
		public MyAnimationListener(int flag){
			mFlag = flag;
		}
		
		@Override
		public void onAnimationStart(Animation animation) {
			if(mFlag == 1){
				mLayoutControl.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onAnimationEnd(Animation animation) {
			if(mFlag == 0){
				mLayoutControl.setVisibility(View.GONE);
			}
		}

		@Override
		public void onAnimationRepeat(Animation animation) {
			
		}
		
	}
	
	private void printErrorLog(){
		Log.e(TAG, "service is null!");
	}


	@Override
	public void onShortClick() {
		mHandler.removeMessages(MSG_CONTROL_ITEM_VISIBLE);
		mHandler.removeMessages(MSG_CLICK_FIVE);
		if(mClickCount == 0){
			firstClickTime = SystemClock.uptimeMillis();
			mClickCount++;
		}else{
			nextClickTime = SystemClock.uptimeMillis();
			if(nextClickTime - firstClickTime <= 360){
				mClickCount ++;
			}
			firstClickTime = nextClickTime;
		}
		
		mHandler.sendEmptyMessageDelayed(MSG_CONTROL_ITEM_VISIBLE, 400);
		if(mClickCount >= 5){
			mHandler.removeMessages(MSG_CONTROL_ITEM_VISIBLE);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_FIVE, 400);
		}else{
			Log.w(TAG, "click count is "+mClickCount);
		}
	}


	@Override
	public void onLongClick() {
		mHandler.removeMessages(MSG_CLICK_LONG);
		mHandler.sendEmptyMessageDelayed(MSG_CLICK_LONG, 200);
	}


	@Override
	public void onLeftSlip() {
		mHandler.removeMessages(MSG_CLICK_LEFT);
		mHandler.sendEmptyMessageDelayed(MSG_CLICK_LEFT, 200);
	}


	@Override
	public void onRightSlip() {
		mHandler.removeMessages(MSG_CLICK_RIGHT);
		mHandler.sendEmptyMessageDelayed(MSG_CLICK_RIGHT, 200);
	}


	@Override
	public void onUpSlip() {
		mHandler.removeMessages(MSG_CLICK_UP);
		mHandler.sendEmptyMessageDelayed(MSG_CLICK_UP, 200);
	}


	@Override
	public void onDownSlip() {
		mHandler.removeMessages(MSG_CLICK_DOWN);
		mHandler.sendEmptyMessageDelayed(MSG_CLICK_DOWN, 200);
	}
	
}
