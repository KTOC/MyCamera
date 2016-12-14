package com.example.test;

import com.example.test.CameraMainService.MyBind;
import com.example.test.Helper.CameraHolder;
import com.example.test.Helper.CameraUtils;
import com.example.test.Helper.ToastUtils;
import com.example.test.callback.CameraActivityCallback;
import com.example.test.callback.CameraServiceCallback;

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
import android.os.UserHandle;
import android.util.Log;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CameraActivity extends Activity implements OnClickListener{

	private TextureView mTextureView0;
	private TextureView mTextureView1;
	private ImageButton mImageButtonRecorder;
	private ImageButton mImageButtonPhoto;
	private ImageButton mImageButtonSettings;
	private ImageButton mImageButtonFolder;
	private ImageButton mImageButtonLock;
	private TextView mTextViewTime;
	private ImageView mImageViewTimeIcon0;
	private ImageView mImageViewTimeIcon1;
	private CaptureAnimationOverlay mCaptureAnimationOverlay;
	private View mLayoutControl;
	private MyBind mService;
	private MyCameraActivityCallbackImpl mCameraActivityCallbackImpl;
	private SurfaceTexture mSurfaceTexture0;
	private SurfaceTexture mSurfaceTexture1;
	private static final int MSG_SET_TEXTURE0 = 0;
	private static final int MSG_SET_TEXTURE1 = 1;
	private static final int MSG_ENABLE_SHUTTER = 2;
	private static final int MSG_UPDATE_TIME = 3;
	private static final int MSG_UPDATE_UI = 4;
	private static final int MSG_ANIMATION_FLASH = 5;
	private static final int MSG_ENABLE_RECORDER = 6;
	private static final int MSG_SCALE_SURFACE = 7;
	private static final int MSG_CONTROL_ITEM_VISIBLE = 8;
	private static final int MSG_FINISH_ACTIVITY = 9;
	private static final int MSG_CLICK_SETTINGS = 10;
	private static final int MSG_CLICK_FOLDER = 11;
	private static final int MSG_CLICK_LOCK = 12;
	private static final int MSG_UPDATE_LOCK_STATE = 13;
	
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
	private TextureListener mTextureListener1;
	private boolean mMediaRecording = false;
	private Context mContext;
	private TranslateAnimation mTranslateAnimationOpen;
	private TranslateAnimation mTranslateAnimationClose;
	private int mAnimationOffset = 0;
	private RelativeLayout.LayoutParams mLayoutParams;
	private AlertDialog mAlertDialog;
	private AlertDialog.Builder mBuilder;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mContext = this;
		mTextureView0 = (TextureView)findViewById(R.id.mTextureView0);
		mTextureView1 = (TextureView)findViewById(R.id.mTextureView1);
		mImageButtonRecorder = (ImageButton)findViewById(R.id.ib_record);
		mImageButtonPhoto = (ImageButton)findViewById(R.id.ib_photo);
		mImageButtonSettings = (ImageButton)findViewById(R.id.ib_settings);
		mImageButtonFolder = (ImageButton)findViewById(R.id.ib_fold);
		mImageButtonLock = (ImageButton)findViewById(R.id.ib_lock);
		mTextViewTime = (TextView)findViewById(R.id.tv_time);
		mImageViewTimeIcon0 = (ImageView)findViewById(R.id.iv_time_icon0);
		mImageViewTimeIcon1 = (ImageView)findViewById(R.id.iv_time_icon1);
		mCaptureAnimationOverlay = (CaptureAnimationOverlay)findViewById(R.id.capture_overlay);
		mLayoutControl = (View)findViewById(R.id.layout_control);
		
		mCameraActivityCallbackImpl = new MyCameraActivityCallbackImpl();
		
		mTextureListener0 = new TextureListener(0);
		mTextureListener1 = new TextureListener(1);
		
		mTextureView0.setSurfaceTextureListener(mTextureListener0);
		mTextureView1.setSurfaceTextureListener(mTextureListener1);
		mImageButtonPhoto.setOnClickListener(this);
		mImageButtonRecorder.setOnClickListener(this);
		mImageButtonSettings.setOnClickListener(this);
		mImageButtonFolder.setOnClickListener(this);
		mImageButtonLock.setOnClickListener(this);
		mTextureView0.setOnClickListener(this);
		mTextureView1.setOnClickListener(this);
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
		unbindService(mServiceconnection);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mService != null){
			mService.unRegisterCallback();
		}
		Log.w(TAG, "onDestroy-------");
		mHandler.removeCallbacksAndMessages(null);
	}
	
	private class TextureListener implements SurfaceTextureListener{
		
		private int mId;
		
		public TextureListener(int id){
			mId = id;
		}

		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface,
				int width, int height) {
			Log.w(TAG, "SurfaceTexture "+mId+" Available");
			if(mId == 0){
				mSurfaceTexture0 = surface;
				mHandler.sendEmptyMessageDelayed(MSG_SET_TEXTURE0,0);
			}else{
				mSurfaceTexture1 = surface;
				mHandler.sendEmptyMessageDelayed(MSG_SET_TEXTURE1,0);
			}
			
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
				int width, int height) {
			
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			Log.w(TAG, "SurfaceTexture "+mId+" destroy");
			
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
			return CameraUtils.getDisplayRotation(CameraActivity.this);
		}

		@Override
		public void enableShutter(boolean enable) {
			mHandler.obtainMessage(MSG_ENABLE_SHUTTER, enable?1:0, 0).sendToTarget();
		}

		@Override
		public void enableRecorder(boolean enable) {
			mHandler.obtainMessage(MSG_ENABLE_RECORDER, enable?1:0, 0).sendToTarget();
		}
		
		@Override
		public void animationFlash() {
			mHandler.obtainMessage(MSG_ANIMATION_FLASH).sendToTarget();
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

	}
	
	private Handler mHandler = new Handler(){
		private boolean enableShutter = false;
		private Bundle hBundle;
		private Bundle tBundle;
		private boolean hTotalRecordingStatus;
		private boolean hSingleRecordingStatus;
		private int hCameraId;
		private boolean hLock;
		private String hTime;
		
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_SET_TEXTURE0:
				if(mSurfaceTexture0 == null || mService == null || !mService.isPreviewing(CameraHolder.getUsbCameraId())){
					mHandler.sendEmptyMessageDelayed(MSG_SET_TEXTURE0, 1000);
				}else{
					mService.postSurfaceTexture(mSurfaceTexture0,CameraHolder.getUsbCameraId(),true);
				}
				break;
			case MSG_SET_TEXTURE1:
				if(mSurfaceTexture1 == null || mService == null|| !mService.isPreviewing(CameraHolder.getCvbsCameraId())){
					mHandler.sendEmptyMessageDelayed(MSG_SET_TEXTURE1, 1000);
				}else{
					mService.postSurfaceTexture(mSurfaceTexture1,CameraHolder.getCvbsCameraId(),true);
				}
				break;
			case MSG_ENABLE_SHUTTER:
				mHandler.removeMessages(MSG_ENABLE_SHUTTER);
				mImageButtonPhoto.setEnabled(msg.arg1==1?true:false);
				break;
			case MSG_ENABLE_RECORDER:
				mHandler.removeMessages(MSG_ENABLE_RECORDER);
				mImageButtonRecorder.setEnabled(msg.arg1==1?true:false);
				break;
			case MSG_UPDATE_TIME:				
				tBundle = msg.getData();
				hTime = tBundle.getString(KEY_RECORD_TIME);
				Log.w(TAG, "msg--->time:"+hTime);
				mTextViewTime.setText(hTime);
				break;
			case MSG_ANIMATION_FLASH:
				mCaptureAnimationOverlay.startFlashAnimation(true);
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
					mImageButtonRecorder.setImageResource(R.drawable.camera_stop_record);
					mTextViewTime.setVisibility(View.VISIBLE);
					mTextViewTime.setText(hTime);
				}else{
					mImageButtonRecorder.setImageResource(R.drawable.camera_start_record);
					mTextViewTime.setVisibility(View.GONE);
				}
				
				if(hCameraId == CameraHolder.getUsbCameraId()){
					mImageViewTimeIcon0.setVisibility(hSingleRecordingStatus?View.VISIBLE:View.GONE);
				}else{
					mImageViewTimeIcon1.setVisibility(hSingleRecordingStatus?View.VISIBLE:View.GONE);
				}
				
				mImageButtonLock.setImageResource(hLock?R.drawable.lock_red:R.drawable.lock_grey);
				break;
			case MSG_SCALE_SURFACE:
				if(mLayoutParams == null){
					mLayoutParams = (RelativeLayout.LayoutParams)mTextureView1.getLayoutParams();
				}
				if(mTextureView1.getLayoutParams().height == mContext.getResources().getDimension(R.dimen.pic_in_pic_height)){
					mLayoutParams.width = android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
					mLayoutParams.height = android.widget.RelativeLayout.LayoutParams.MATCH_PARENT;
					mTextureView1.setLayoutParams(mLayoutParams);
				}else{
					mLayoutParams.width = (int)mContext.getResources().getDimension(R.dimen.pic_in_pic_width);
					mLayoutParams.height = (int)mContext.getResources().getDimension(R.dimen.pic_in_pic_height);
					mTextureView1.setLayoutParams(mLayoutParams);
				}
				break;
			case MSG_CONTROL_ITEM_VISIBLE:
				mLayoutControl.clearAnimation();
				if(mLayoutControl.getVisibility() == View.GONE){
					startOpenAnimation();
				}else{
					startCloseAnimation();
				}
				break;
			case MSG_FINISH_ACTIVITY:
				CameraActivity.this.finish();
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
					mImageButtonLock.setImageResource(R.drawable.lock_red);
				}else{
					mImageButtonLock.setImageResource(R.drawable.lock_grey);
				}
				break;
			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_record:
			mImageButtonRecorder.setEnabled(false);
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
		case R.id.ib_photo:
			mImageButtonPhoto.setEnabled(false);
			mHandler.removeMessages(MSG_ENABLE_SHUTTER);
			Message msg = Message.obtain();
			msg.what = MSG_ENABLE_SHUTTER;
			msg.arg1 = 1;
			mHandler.sendMessageDelayed(msg, 5000);
			if(mService != null){
				mService.takePictures();
			}else{
				printErrorLog();
			}
			break;
		case R.id.ib_fold:
			mHandler.removeMessages(MSG_CLICK_FOLDER);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_FOLDER, 200);
			break;
		case R.id.ib_settings:
			mHandler.removeMessages(MSG_CLICK_SETTINGS);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_SETTINGS, 200);
			break;
		case R.id.mTextureView0:
			mAnimationOffset = mLayoutControl.getWidth();
			Log.w(TAG, "control_item_width:"+mAnimationOffset);
			mHandler.removeMessages(MSG_CONTROL_ITEM_VISIBLE);
			mHandler.sendEmptyMessageDelayed(MSG_CONTROL_ITEM_VISIBLE, 100);
			break;
		case R.id.mTextureView1:
			mHandler.removeMessages(MSG_SCALE_SURFACE);
			mHandler.sendEmptyMessageDelayed(MSG_SCALE_SURFACE, 240);
			break;
		case R.id.ib_lock:
			mHandler.removeMessages(MSG_CLICK_LOCK);
			mHandler.sendEmptyMessageDelayed(MSG_CLICK_LOCK, 200);
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
			mTranslateAnimationOpen = new TranslateAnimation(-mAnimationOffset,0,0,0);
			mTranslateAnimationOpen.setDuration(500);
			mTranslateAnimationOpen.setInterpolator(new LinearInterpolator());
			mTranslateAnimationOpen.setAnimationListener(new MyAnimationListener(1));
		}
		mLayoutControl.startAnimation(mTranslateAnimationOpen);
	}
	
	private void startCloseAnimation(){
		if(mTranslateAnimationClose == null){
			mTranslateAnimationClose = new TranslateAnimation(0,-mAnimationOffset,0,0);
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
	
}
