package com.example.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HandshakeCompletedListener;

import com.example.test.Helper.CameraHolder;
import com.example.test.Helper.CameraMode;
import com.example.test.Helper.CameraUtils;
import com.example.test.Helper.CommonFileCompareInfos;
import com.example.test.Helper.CvbsHelper;
import com.example.test.Helper.FileNameUtil;
import com.example.test.Helper.FileSizeUtil;
import com.example.test.Helper.FileState;
import com.example.test.Helper.GlobalUtil;
import com.example.test.Helper.LockFileCompareInfos;
import com.example.test.Helper.ToastUtils;
import com.example.test.Helper.Utils;
import com.example.test.Helper.VideoNameFilter;
import com.example.test.app.MyApplication;
import com.example.test.callback.CallbackForAlertView;
import com.example.test.callback.CallbackForReverseLayout;
import com.example.test.callback.CameraActivityCallback;
import com.example.test.file.MyCleanInvalidFilesRunnable;
import com.example.test.settings.CameraSettings;
import com.luyuan.drivingrecorder.CameraDetect;
import com.luyuan.lydetect.lydetect;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.CameraProfile;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.widget.Toast;

public class CameraMainService extends Service{
	
	private int mCurrentCameraMode = 0; 
	private static final String THREAD_NAME_CLEAN = "cleanInvalidFiles";
	private static final String THREAD_NAME_DETECT_LEFT_RIGHT = "detectLeftRight";
	private HandlerThread mHandlerThread;
    private HandlerThread lrHandlerThread;
	private Handler tHandler;
	private Handler lrHandler; //检测左右转的引脚电平的
	private LeftRightDetectRunnable mLeftRightDetectRunnable;
	private boolean mLeftRightDetectOpen = false;
	private lydetect mLydetect;
	private MyCleanInvalidFilesRunnable mCleanInvalidFilesRunnable;
	private static String mCurrentRootPath;
	private CameraDetect mCameraDetect;
	private Camera[] mCameras;
	private Parameters[] mParameters;
	private boolean[] mCamerasPreviewing;
	private boolean[] mCamerasRecording;
	private SurfaceTexture[] mSurfaceTextures;
	private CamcorderProfile[] mProfiles;
	private int[] mDesiredPreviewWidth;
	private int[] mDesiredPreviewHeight;
	private int[] mDesiredVideoWidth;
	private int[] mDesiredVideoHeight;
	private MediaRecorder[] mMediaRecorders;
	private boolean[] mSnapShotInProgress;
	
	/*public  final static int mUsbCameraId = 0;
	public final static int mCvbsCameraId = 5;
	public final static int mGlobalCameraId = 360;*/
	
	private static final int MSG_FIND_USB = 1;
	private static final int MSG_OPEN_CAMERA_BY_REVERSE = 2;
	private static final int MSG_START_RECORDING = 3;
	private static final int MSG_NO_AVAILABLE_STORAGE = 4;
	private static final int MSG_TTS_UPDATE_TIME = 5;
	private static final int MSG_HIDE_ALERT_WINDOW = 6;
	private static final int MSG_TTS_SHOW_ALERT_VIEW_BY_LOCK = 7;
	private static final int MSG_TTS_SHOW_ALERT_VIEW_BY_OTHER = 8;
	private static final int MSG_SCAN_LOCK = 9;
	private static final int MSG_UPDATE_SPACE = 10;
	private static final int MSG_START_RECORDING_ASYNC = 11;
	//private static final int MSG_HANDLE_REVERSE_ON = 12;
	//private static final int MSG_HANDLE_REVERSE_OVER = 13;
	private static final int MSG_HANDLE_OPEN_CAMERA_SUCCESS = 14;
	private static final int MSG_OPEN_CAMERA_FAIL = 15;
	private static final int MSG_ACC_ON = 16;
	private static final int MSG_ACC_OFF = 17;
	private static final int MSG_PLUG_IN = 18;
	private static final int MSG_PLUG_OUT = 19;
	private static final int MSG_NO_AVAILABLE_SPACE = 20;
	private static final int MSG_DISPATCH_REVRSE_LEFT_RIGHT_STEER = 21;
	private static final int MSG_OPEN_CAMERA_BY_LEFT = 22;
	private static final int MSG_OPEN_CAMERA_BY_RIGHT = 23;
	private int mCheckUsbTimes = 0;
	private Context mContext;
	private final static String TAG = "LY_CAMERA";
	private CameraActivityCallback mCameraActivityCallback;
	private int mRotation;
	private int mDisplayOrientation;
	private long dateTime;
	private boolean mRecordingLock = false;
    private int minCommonIndexNum = -1;
    private int maxCommonIndexNum = -1;
    private int minLockIndexNum = -1;
    private int maxLockIndexNum = -1;
    private int mCurrentCommonIndex = -1;
    private int mCurrentLockIndex = -1;
    private FileState[] mCurrentPaths;
    private FileState[] mLastPaths;
    private boolean mLock;
    public static final int LIMIT_INDEX_NUM = 10000;
    private int mRecorderErrorTimes = 0;
    private long mRecorderErrorTime = 0;
    private boolean mMediaRecorderRecording = false;
    private int mInfoTimes = 0;
    private long mRecordingStartTime = 0;
    private long mPictureTakeTime = 0;
    private Object mStorageSpaceLock = new Object();
    private long mStorageSpaceBytes = 0;
    private long mUpdateStorageSpaceStartTime = 0;
    private List<CommonFileCompareInfos> mFilelist = new ArrayList<CommonFileCompareInfos>();
    private List<LockFileCompareInfos> mLockFileList = new ArrayList<LockFileCompareInfos>();
    private final static String TAG_FILE = "CAM_FILE";
    private VideoNameFilter mVideoNameFilter;
    private AlertViewByOther mAlertViewByOther;
    private AlertViewByLock mAlertViewByLock;
    private CallBackForAlertViewImpl mCallBackForAlertViewImpl;
    private int mMaxVideoDurationInMs;
    private int mRecordingCameraNumbers = 0;
    private Object mMathObject = new Object();
    private MyBind mBind;
    private final static String BROADCAST_BACK_CAR = "com.luyuan.mcu.back.car";
    private final static String KEY_REVERSE_STATE = "isBackCar";
    private final static String BROADCAST_BACK_CAR_RADAR_TRAJECTORY = "com.luyuan.mcu.back.car.radar.trajectory";
    private final static String KEY_MCU_DATA = "data";
    private final static String ACTION_START_SERVICE = "com.luyuan.recorder";
    private final static String ACTION_START_SERVICE_BG = "com.luyuan.recorder.bg";
    public final static String  ACTION_LY_MCUSERVICE_ACC_STATUS = "com.luyuan.mcuservice.accstatus";
    private static final String ACTION_USB_CAMERA_PLUG_IN_OUT = "android.hardware.usb.action.USB_CAMERA_PLUG_IN_OUT";
    public static final String  KEY_USB_CAMERA_TOTAL_NUMBER     = "UsbCameraTotalNumber";
    public static final String  KEY_USB_CAMERA_STATE            = "UsbCameraState";
    private static final String KEY_ACC_STATUS = "accstatus";
    public static final int     PLUG_IN                     = 1;
    public static final int     PLUG_OUT                    = 0;
    public static final String  KEY_USB_CAMERA_NAME             = "UsbCameraName";
    //public static final String  KEY_EXTRA_MNG                   = "extral_mng";
    private boolean mReverseState = false;
    private int mCurrentLeftRightStateValue = 0;
    private ReverseLayout mReverseLayout;
    private MyCarEventBroadcastReceiver mCarEventBroadcastReceiver;
    private CallbackForReverseLayoutImpl mCallbackForReverseLayoutImpl;
    private String mCurrentTimeText;
    private MyExternelStorageBroadcastReceiver mExternelStorageBroadcastReceiver;
    private Object mIndexLock = new Object();
    private Object[] mOpenCloseLock;

    
	@Override
	public IBinder onBind(Intent intent) {
		return mBind;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		MyApplication.SERVICE_RUNNING_FLAG = true;
		mCurrentCameraMode = CameraSettings.getCurrentCameraMode(); //获取当前的摄像头组合模式
		
		mHandlerThread = new HandlerThread(THREAD_NAME_CLEAN);
		mHandlerThread.start();		
		tHandler = new ThdHandler(mHandlerThread.getLooper());
		
		mLydetect = new lydetect();
		lrHandlerThread = new HandlerThread(THREAD_NAME_DETECT_LEFT_RIGHT);
		lrHandlerThread.start();
		lrHandler = new Handler(lrHandlerThread.getLooper());
		mLeftRightDetectRunnable = new LeftRightDetectRunnable();
		lrHandler.post(mLeftRightDetectRunnable);
		
		CvbsHelper.controlKernelExitFlag();  //让内核不再响应倒车
		
		mCurrentRootPath = MyApplication.getCurrentStorage();  //可能为null	
		Log.w(TAG, "onCreate  path:"+(mCurrentRootPath == null ? "null" : mCurrentRootPath));
		mCleanInvalidFilesRunnable = new MyCleanInvalidFilesRunnable(mCurrentRootPath);
		tHandler.post(mCleanInvalidFilesRunnable);
		new Thread(){
            public void run() {
                    initCurrentIndexNum();
            };
		}.start();
		
		mCameraDetect = new CameraDetect();
		mVideoNameFilter = new VideoNameFilter();
		mBind = new MyBind(this);
		
		initArrays();
		
		mReverseLayout = (ReverseLayout)LayoutInflater.from(mContext).inflate(R.layout.back_car_layout, null);
		mCallbackForReverseLayoutImpl = new CallbackForReverseLayoutImpl();
		mReverseLayout.registerCallback(mCallbackForReverseLayoutImpl);
		initWindowView();
	
		registerCarEventBroadcasts();
		registerExternelStorageListener();
		
		
	}
	
	private void registerCarEventBroadcasts(){
		IntentFilter mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(BROADCAST_BACK_CAR);
		mIntentFilter.addAction(BROADCAST_BACK_CAR_RADAR_TRAJECTORY);
		//mIntentFilter.addAction(Intent.ACTION_SCREEN_ON);  //休眠唤醒
		//mIntentFilter.addAction(Intent.ACTION_SCREEN_OFF); //休眠结束
		mIntentFilter.addAction(ACTION_USB_CAMERA_PLUG_IN_OUT);
		mIntentFilter.addAction(ACTION_LY_MCUSERVICE_ACC_STATUS);
		mCarEventBroadcastReceiver = new MyCarEventBroadcastReceiver();
		mContext.registerReceiver(mCarEventBroadcastReceiver, mIntentFilter);
	}
	
	private void unRegisterCarEventBroadcasts(){
		mContext.unregisterReceiver(mCarEventBroadcastReceiver);
	}
	
	private void initArrays(){
		if(mCameras == null){
			mCameras = new Camera[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mCamerasPreviewing == null){
			mCamerasPreviewing = new boolean[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mCamerasRecording == null){
			mCamerasRecording = new boolean[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mSurfaceTextures == null){
			mSurfaceTextures = new SurfaceTexture[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		for(int i=0;i<mSurfaceTextures.length;i++){
			mSurfaceTextures[i] = new SurfaceTexture(1000+i);
		}
		
		if(mProfiles == null){
			mProfiles = new CamcorderProfile[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mParameters == null){
			mParameters = new Parameters[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mDesiredPreviewHeight == null){
			mDesiredPreviewHeight = new int[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mDesiredPreviewWidth == null){
			mDesiredPreviewWidth = new int[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mDesiredVideoHeight == null){
			mDesiredVideoHeight = new int[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mDesiredVideoWidth == null){
			mDesiredVideoWidth = new int[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mMediaRecorders == null){
			mMediaRecorders = new MediaRecorder[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mSnapShotInProgress == null){
			mSnapShotInProgress = new boolean[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mCurrentPaths == null){
			mCurrentPaths = new FileState[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mLastPaths == null){
			mLastPaths = new FileState[CameraSettings.MAX_CAMERA_NUMBERS];
		}
		
		if(mOpenCloseLock == null){
			mOpenCloseLock = new Object[CameraSettings.MAX_CAMERA_NUMBERS];
			for(int i = 0;i< CameraSettings.MAX_CAMERA_NUMBERS;i++){
				mOpenCloseLock[i] = new Object();
			}
		}
	}
	
	private void initWindowView(){
        mAlertViewByLock = (AlertViewByLock)LayoutInflater.from(mContext).inflate(R.layout.alert_lock, null);
        mAlertViewByOther = (AlertViewByOther)LayoutInflater.from(mContext).inflate(R.layout.alert_other, null);
        mCallBackForAlertViewImpl = new CallBackForAlertViewImpl();
        mAlertViewByLock.registerCallback(mCallBackForAlertViewImpl);
}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent == null) return START_NOT_STICKY;
		if(ACTION_START_SERVICE_BG.equals(intent.getAction())){
			//考虑倒车的情况
			boolean needReverse = intent.getBooleanExtra(CameraSettings.KEY_NEED_REVERSE, false);
			boolean needRecord = MyApplication.getPowerOn();
			readPreferenceAndSettings();
			openAllCameraByMain(needRecord,needReverse);			
		}	

		return START_NOT_STICKY;
	}

	private void openAllCameraByMain(boolean needRecord,boolean needReverse){
		openAllCameras(needRecord,needReverse);
	}
	
	private void openAllCameraByThread(){
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			if(mCameraDetect.isUvcCameraConnected() != 1 ){
				mHandler.obtainMessage(MSG_FIND_USB).sendToTarget();
			}else{
				new CameraOpenThread(CameraHolder.getUsbCameraId()).start();
			}
			new CameraOpenThread(CameraHolder.getCvbsCameraId()).start();
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_USB_360){
			if(mCameraDetect.isUvcCameraConnected() != 1 ){
				mHandler.obtainMessage(MSG_FIND_USB).sendToTarget();
			}else{
				new CameraOpenThread(CameraHolder.getUsbCameraId()).start();
			}
		}else{
			
		}
	}
	
	private void openAllCameras(boolean needRecord,boolean needReverse){
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			if(mCameraDetect.isUvcCameraConnected() != 1 ){
				mHandler.obtainMessage(MSG_FIND_USB).sendToTarget();
			}else{
				openCamera(CameraHolder.getUsbCameraId(),needRecord,needReverse);
			}
			openCamera(CameraHolder.getCvbsCameraId(),needRecord,needReverse);
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_USB_360){
			if(mCameraDetect.isUvcCameraConnected() != 1 ){
				mHandler.obtainMessage(MSG_FIND_USB).sendToTarget();
			}else{
				openCamera(CameraHolder.getUsbCameraId(),needRecord,needReverse);
			}
		}else{
			
		}
	}
	
	private boolean openCamera(int cameraId,boolean needRecord,boolean needReverse){
		boolean re = false;
		Log.w(TAG, "open camera :"+cameraId);
		if(mCameras[cameraId] == null){
			Log.w(TAG, "sync lock camera: "+cameraId+" wait for lock!");
			synchronized (mOpenCloseLock[cameraId]) {
				Log.w(TAG, "sync lock camera: "+cameraId+" enter");
				Camera temp = mCameras[cameraId];
				if(temp != null){
					Log.w(TAG, "yet open !sync lock camera: "+cameraId+" eixt");
				}
				try{
					mCameras[cameraId] = Camera.open(cameraId);
					Log.w(TAG, "open camera "+cameraId+" success");
					re = true;
				}catch(Exception e){
					re = false;
					handleWhenCameraOpenFailed(cameraId,needRecord,needReverse);
					mCameras[cameraId] = null;
					return re;
				}
				Log.w(TAG, "sync lock camera: "+cameraId+" eixt");
			}
		}
		handleWhenCameraOpenSuccess(cameraId,needRecord,needReverse);
		return re;
	}
	
	private void closeAllCamera(){
		for(int i = 0;i<CameraSettings.MAX_CAMERA_NUMBERS;i++){
			closeCamera(i);
		}
	}
	
	private void closeCamera(int cameraId){
		if(mCameras==null || mCameras[cameraId]==null){
			return;
		}
		Log.w(TAG, "close camera "+cameraId);
		synchronized (mOpenCloseLock[cameraId]) {
			Log.w(TAG, "sync lock camera "+cameraId+" enter");
			Camera temp = mCameras[cameraId];
			if(temp == null){
				Log.w(TAG, "yet close, sync lock camera "+cameraId+" exit");
			}
			mCameras[cameraId].setErrorCallback(null);
			mCameras[cameraId].release();
			mCameras[cameraId] = null;	
			Log.w(TAG, "sync lock camera "+cameraId+" exit");
		}
	}
	
	private void startPreview(int cameraId,boolean needRecord,boolean needReverse){
		if(mCameras == null || mCameras[cameraId] == null){ 
			Log.e(TAG, "start preview before camera open!");
			return;
		}
		if(mCamerasPreviewing[cameraId]){
			handleWhenCameraStartPreviewSuccess(cameraId,needRecord,needReverse);
		}else{
			getDesiredPreviewSizeAndQuality(cameraId);
			setDisplayOrientation(cameraId);
			
			setCameraCommonParameters(cameraId);
			
			mCameras[cameraId].enableShutterSound(false);
			mCameras[cameraId].setErrorCallback(new CameraErrorCallback(cameraId));
			
			try {
				if(mSurfaceTextures[cameraId] == null){
					mSurfaceTextures[cameraId] = new SurfaceTexture(1000+cameraId);
				}
				mCameras[cameraId].setPreviewTexture(mSurfaceTextures[cameraId]);
			} catch (IOException e) {
				e.printStackTrace();
			}
			try{
				mCameras[cameraId].startPreview();
				mCamerasPreviewing[cameraId] = true;
				handleWhenCameraStartPreviewSuccess(cameraId,needRecord,needReverse);
			}catch(Exception e){
				Log.e(TAG, e.toString());
				handleWhenCameraStartPreviewFailed(cameraId);
			}
			/*if(isAllPreviewStarted()){
				onPreviewStarted();
			}else{
				Log.w(TAG, "not start preview all!");
			}*/
		}
	}
	
	private boolean isPreviewing(int cameraId){
		if(mCamerasPreviewing != null && mCamerasPreviewing[cameraId]){
			return true;
		}else{
			return false;
		}
	}
	
	private boolean isAllPreviewStarted(){
		boolean result = false;
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			result = mCamerasPreviewing[CameraHolder.getUsbCameraId()] && mCamerasPreviewing[CameraHolder.getCvbsCameraId()];
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_USB_360){
			result = mCamerasPreviewing[CameraHolder.getUsbCameraId()] && mCamerasPreviewing[CameraHolder.getGlobalCameraId()];
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_360){
			result = mCamerasPreviewing[CameraMode.MODE_INT_360];
		}else{
			result = false;
		}
		return result;
	}
	
	private boolean isSnapShotting(){
		boolean result = false;
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			result = mSnapShotInProgress[CameraHolder.getUsbCameraId()] && mSnapShotInProgress[CameraHolder.getCvbsCameraId()];
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_USB_360){
			result = mSnapShotInProgress[CameraHolder.getUsbCameraId()] && mSnapShotInProgress[CameraHolder.getGlobalCameraId()];
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_360){
			result = mSnapShotInProgress[CameraMode.MODE_INT_360];
		}else{
			result = false;
		}
		return result;
	}
	
	private void onPreviewStarted(){
		
	}
	
	private void stopAllPreview(){
		for(int i = 0;i < CameraSettings.MAX_CAMERA_NUMBERS;i++){
			stopPreview(i);
		}
	}
	
	private void stopPreview(int cameraId){
		if(mCamerasPreviewing == null || !mCamerasPreviewing[cameraId] || mCameras[cameraId]==null)
			return;
		Log.w(TAG,"stop camera: "+cameraId +" preview");
		mCameras[cameraId].addCallbackBuffer(null);
		mCameras[cameraId].setPreviewCallbackWithBuffer(null);
		mCameras[cameraId].stopPreview();
		mCamerasPreviewing[cameraId] = false;
	}
	
	private void changeAllVideoLockState(){
		Log.w(TAG_FILE, "change cureent state ["+mLock+"] to new state ["+!mLock+"]");
		mLock = !mLock;
		setAllVideoLockState(mLock);
	}
	
	private void setAllVideoLockState(boolean lock){
		if(!mMediaRecorderRecording) return;
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			setVideoLockState(lock, CameraHolder.getUsbCameraId());
			setVideoLockState(lock, CameraHolder.getCvbsCameraId());
		}else if(mCurrentCameraMode == CameraMode.MODE_INT_360){
			
		}else{
			
		}
		if(mCameraActivityCallback != null){
			mCameraActivityCallback.updateActivityLockState(lock);
		}
	}
	
	private void setVideoLockState(boolean lock,int cameraId){
		if(mCurrentPaths[cameraId] != null){
			mCurrentPaths[cameraId].setmLock(lock);
		}
	}
	
	private void setDisplayOrientation(int cameraId){
		if(mCameras == null || mCameras[cameraId]==null) return;
		if(mCameraActivityCallback != null){
			mRotation = mCameraActivityCallback.getDisplayRotation();
		}else{
			mRotation = 0;
		}
		mDisplayOrientation = CameraUtils.getDisplayOrientation(mRotation, cameraId);
		mCameras[cameraId].setDisplayOrientation(mDisplayOrientation);		
	}
	
	private void getDesiredPreviewSizeAndQuality(int cameraId){
		if(mCameras == null || mCameras[cameraId]==null) return;
		if(cameraId == CameraHolder.getUsbCameraId()){
			if(CamcorderProfile.hasProfile(cameraId, 4)){
				mProfiles[cameraId] = CamcorderProfile.get(0, 4);
			}else if(CamcorderProfile.hasProfile(cameraId, 5)){
				mProfiles[cameraId] = CamcorderProfile.get(0, 5);
			}else{
				Log.w(TAG, "usb not support quality 4&&5!!!");
				mProfiles[cameraId] = null;
			}
			mParameters[cameraId] = mCameras[cameraId].getParameters();
			
			List<Size> sizes = mParameters[cameraId].getSupportedVideoSizes();
			if(CameraUtils.isSupportVideoSizes(mProfiles[cameraId], sizes)){
				mDesiredVideoWidth[cameraId] = mProfiles[cameraId].videoFrameWidth;
				mDesiredVideoHeight[cameraId] = mProfiles[cameraId].videoFrameHeight;
			}else{
				mDesiredVideoHeight[cameraId] = 480;
				mDesiredVideoWidth[cameraId] = 640;
			}
			mDesiredPreviewWidth[cameraId] = 640;
			mDesiredPreviewHeight[cameraId] = 480;
		}else if(cameraId == CameraHolder.getCvbsCameraId()
				||cameraId == CameraHolder.getLeftCameraId()
				||cameraId == CameraHolder.getRightCameraId()){//720*480 or 720*576
			mProfiles[cameraId] = CamcorderProfile.get(1, 4);
			mParameters[cameraId] = mCameras[cameraId].getParameters();
			List<Size> sizes = mParameters[cameraId].getSupportedVideoSizes();
			if(sizes != null){
				Iterator<Size> iterator = sizes.iterator();
				if(iterator.hasNext()){
					Size tmp= iterator.next();
					Log.w(TAG, "w:h = "+tmp.width+" : "+tmp.height);
					mDesiredVideoHeight[cameraId] = tmp.height;
					mDesiredVideoWidth[cameraId]=tmp.width;
				}
			}/*else{	*/			
				mDesiredVideoWidth[cameraId] = 720;
				mDesiredVideoHeight[cameraId] = 480;
			/*}*/
			mDesiredPreviewHeight[cameraId] = mDesiredVideoHeight[cameraId];
			mDesiredPreviewWidth[cameraId] = mDesiredVideoWidth[cameraId];
		}else if(cameraId == CameraHolder.getGlobalCameraId()){
			
		}		
		Log.w(TAG, "cameraId:"+cameraId+"  videoSize w:h = "+mDesiredVideoWidth[cameraId]+":"+mDesiredVideoHeight[cameraId]
				+"previewSize w:h = "+mDesiredPreviewWidth[cameraId]+":"+mDesiredPreviewHeight[cameraId]);
	}
	

	
	private boolean initMediaRecord(int cameraId){
		Log.w(TAG, "cameraId "+cameraId+ "  init recorder!");
        if (mCameras == null || mCameras[cameraId] == null) return false;
        if(mProfiles[cameraId] != null){
	        mMediaRecorders[cameraId] = new MediaRecorder();
	        mCameras[cameraId].unlock();
	        mMediaRecorders[cameraId].setCamera(mCameras[cameraId]);
	        mMediaRecorders[cameraId].setVideoSource(MediaRecorder.VideoSource.CAMERA);
	        //if(cameraId == CameraHolder.getUsbCameraId()){
	        	mMediaRecorders[cameraId].setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	        //}
	        mProfiles[cameraId].fileFormat = MediaRecorder.OutputFormat.MPEG_4;
	        mProfiles[cameraId].videoFrameHeight = mDesiredVideoHeight[cameraId];
	        mProfiles[cameraId].videoFrameWidth = mDesiredVideoWidth[cameraId];
	        mProfiles[cameraId].videoBitRate = 8*mProfiles[cameraId].videoFrameHeight*mProfiles[cameraId].videoFrameWidth;
	        mMediaRecorders[cameraId].setProfile(mProfiles[cameraId]);	        
	        Log.d(TAG,"videoFrameWidth=" + mDesiredVideoWidth[cameraId] + " height=" + mDesiredVideoHeight[cameraId]);              
	       	        
        }else{
        	Log.e(TAG, "initMediaRecord failed "+cameraId);
        	return false;
        }
        
        mMediaRecorders[cameraId].setOutputFile(generateVideoFilename(mProfiles[cameraId].fileFormat,cameraId));
		mMediaRecorders[cameraId].setMaxDuration(getMediaRecorderDuration());
        mMediaRecorders[cameraId].setMaxFileSize(0);
        mMediaRecorders[cameraId].setOrientationHint(mDisplayOrientation);
		try {
        	mMediaRecorders[cameraId].prepare();
        } catch (IOException e) {
            Log.e(TAG, "prepare failed for camera " + cameraId, e);
            releaseRecorder(cameraId);
            throw new RuntimeException(e);
        }

        mMediaRecorders[cameraId].setOnErrorListener(new MediaRecorderErrorListener(cameraId));
        mMediaRecorders[cameraId].setOnInfoListener(new MediaRecorderInfoListener(cameraId));
        return true;
	}
	
	private void startAllVideoRecording(){
		if(CameraSettings.LOG_SWITCH){
			Log.i(TAG, "startAllRecorder");
		}
		if(!MyApplication.isExternalStorageMounted(mCurrentRootPath)){
			mCurrentRootPath = MyApplication.getCurrentStorage();
		}
		if(mCurrentRootPath == null){
			ttsHandler.obtainMessage(MSG_NO_AVAILABLE_STORAGE).sendToTarget();
			if(mCameraActivityCallback != null){
				mCameraActivityCallback.enableRecorder(true);
			}
			return;
		}
		initCurrentIndexNum();
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			startVideoRecording(CameraHolder.getUsbCameraId());
			startVideoRecording(CameraHolder.getCvbsCameraId());
		}else{
			startVideoRecording(CameraHolder.getUsbCameraId());
		}
		if(mCameraActivityCallback != null){
			mCameraActivityCallback.enableRecorder(true);
		}
	}
	
	private void startAllVideoRecordingByThread(){
		tHandler.post(new Runnable() {
			
			@Override
			public void run() {
				startAllVideoRecording();
			}
		});
	}
	
	private void stopAllVideoRecordingByThread(){
		tHandler.post(new Runnable() {
			@Override
			public void run() {
				stopAllVideoRecording();
			}
		});
	}
	
	private void startVideoRecording(final int cameraId){		
		updateStorageSpaceAndHint(new OnStorageUpdateDoneListener() {
			@Override
			public void onStorageUpdateDone(long bytes) {
				if(bytes < getLimitStorage()){
					ttsHandler.obtainMessage(MSG_NO_AVAILABLE_SPACE);
					if(mCameraActivityCallback != null){
						mCameraActivityCallback.enableRecorder(true);
					}
					return;
				}
				if(CameraSettings.LOG_SWITCH){
					Log.w(TAG_FILE, "current space is "+bytes);
				}
				startRecorder(cameraId);					
			}
		}, false);	
	}
	
	private boolean startRecorder(int cameraId){
		if(mCamerasRecording[cameraId]){
			Log.w(TAG, "camera "+cameraId+" start recorder yet!");
			return true;
		}
		
		if(!initMediaRecord(cameraId)){
			return false;
		}
		if(mMediaRecorders[cameraId]==null){
			return false;
		}
		if(CameraSettings.LOG_SWITCH){
			Log.w(TAG, "camera "+cameraId+" start recorder ");
		}
		try{
			mMediaRecorders[cameraId].start();
			if(!mMediaRecorderRecording){  //以第一个启动的录制 为起始录制时间
				mRecordingStartTime = SystemClock.uptimeMillis();
				mCurrentTimeText = "00:00";
			}
			mMediaRecorderRecording = true;
			mCamerasRecording[cameraId] = true;
			addRecordingCameraNumbers();
			if(mCameraActivityCallback != null){
				mCameraActivityCallback.updateActivityUi(mMediaRecorderRecording, cameraId, mCamerasRecording[cameraId], mLock,mCurrentTimeText);
			}	
			updateRecordingTime();
		}catch(Exception e){
			mCamerasRecording[cameraId] = false;
			releaseRecorder(cameraId);
			return false;
		}
		mHandler.removeMessages(MSG_UPDATE_SPACE);
		mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SPACE, 8000);
		mCameras[cameraId].startWaterMark();
		return true;
	}
	
	private void stopAllVideoRecording(){
		if(SystemClock.uptimeMillis() - mRecordingStartTime <= 2000){
			ToastUtils.showShortToast(mContext,R.string.shoule_not_stop_immediately);
			return;
		}
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			stopRecorder(CameraHolder.getUsbCameraId());
			
			stopRecorder(CameraHolder.getCvbsCameraId());
			
		}else{
			stopRecorder(CameraHolder.getUsbCameraId());
		}
		if(mCameraActivityCallback != null){
			mCameraActivityCallback.enableRecorder(true);
		}
		mLock = false;
		if(mCameraActivityCallback != null){
			mCameraActivityCallback.updateActivityLockState(mLock);
		}
	}
	
	private boolean stopRecorder(int cameraId){
		if(mMediaRecorders[cameraId] == null || !mCamerasRecording[cameraId]){
			return true;
		}
		if(CameraSettings.LOG_SWITCH){
			Log.w(TAG, "camera "+cameraId+" stop recorder ");
		}
		boolean re = false;
		mMediaRecorders[cameraId].setOnErrorListener(null);
		mMediaRecorders[cameraId].setOnInfoListener(null);
		if(CameraSettings.LOG_SWITCH){
			Log.w(TAG, "camera "+cameraId+" stop recorder step 1");
		}
		mCameras[cameraId].stopWaterMark();
		if(CameraSettings.LOG_SWITCH){
			Log.w(TAG, "camera "+cameraId+" stop recorder step 2");
		}
		try{
			mMediaRecorders[cameraId].stop();
			if(CameraSettings.LOG_SWITCH){
				Log.w(TAG, "camera "+cameraId+" stop recorder step 3");
			}
			mCamerasRecording[cameraId] = false;
			reduceRecordingCameraNumbers();
			if(getRecordingCameraNumbers() == 0){
				mMediaRecorderRecording = false;
			}
			if(mCameraActivityCallback != null){
				mCameraActivityCallback.updateActivityUi(mMediaRecorderRecording, cameraId, mCamerasRecording[cameraId], mLock,mCurrentTimeText);
			}
			re = true;
			if(mCurrentPaths[cameraId] != null){
				tHandler.postDelayed(new RenameRunnable(mCurrentPaths[cameraId]),500);
				mLastPaths[cameraId] = mCurrentPaths[cameraId];
				mCurrentPaths[cameraId] = null;
			}
			if(CameraSettings.LOG_SWITCH){
				Log.w(TAG, "camera "+cameraId+" stop recorder step 4");
			}
			releaseRecorder(cameraId);		
		}catch(Exception e){
			Log.e(TAG, "fail to stop camera "+cameraId);
		}
		if(CameraSettings.LOG_SWITCH){
			Log.w(TAG, "camera "+cameraId+" stop recorder exit!");
		}
		return re;
	}
	
	private boolean isAllRecorderStoped(){
		if(mCamerasRecording == null){
			return true;
		}
		boolean result = true;
		for(int i=0;i< mCamerasRecording.length;i++){
			if(mCamerasRecording[i]){
				result = false;
				break;
			}
		}
		return result;
	}
	
	private void releaseAllRecorder(){
		if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
			releaseRecorder(CameraHolder.getUsbCameraId());
			releaseRecorder(CameraHolder.getCvbsCameraId());
		}else{
			releaseRecorder(CameraHolder.getUsbCameraId());
		}
	}
	
	private void releaseRecorder(int cameraId){
        if(mMediaRecorders == null ||mMediaRecorders[cameraId]==null) return;
        mMediaRecorders[cameraId].reset();
        mMediaRecorders[cameraId].release();
        mMediaRecorders[cameraId] = null;  
        mCameras[cameraId].lock();
	}
	
	private void takeAllPicture(){
		if(!MyApplication.isExternalStorageMounted(mCurrentRootPath)){
			mCurrentRootPath = MyApplication.getCurrentStorage();
		}
		if(mCurrentRootPath == null){
			ttsHandler.obtainMessage(MSG_NO_AVAILABLE_STORAGE).sendToTarget();
			mCameraActivityCallback.enableShutter(true);
			return;
		}
		updateStorageSpaceAndHint(new OnStorageUpdateDoneListener() {
			
			@Override
			public void onStorageUpdateDone(long bytes) {
				if(bytes < getLimitStorage()){
					ttsHandler.obtainMessage(MSG_NO_AVAILABLE_SPACE);
					mCameraActivityCallback.enableShutter(true);
					return;
				}
				Log.w(TAG_FILE, "before take picure , space is "+bytes);
				if(mCameraActivityCallback != null){
					mCameraActivityCallback.animationFlash();
				}
				if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
					takePicture(CameraHolder.getUsbCameraId());
					takePicture(CameraHolder.getCvbsCameraId());
				}else{
					takePicture(CameraHolder.getUsbCameraId());
				}
				
			}
		}, false);		
		
	}
	
	private void takePicture(int cameraId){		
		if(mCameras[cameraId] == null) return;
		Log.w(TAG, "camera "+cameraId+" take picture!");
		if(!mSnapShotInProgress[cameraId] || System.currentTimeMillis()-mPictureTakeTime >= 7000){
			mPictureTakeTime = System.currentTimeMillis();
			mSnapShotInProgress[cameraId] = true;
			mCameras[cameraId].takePicture(null, null, new JpegPictureCallback(cameraId));
		}else{
			Log.w(TAG, "take picture has bugs.");
		}		
	}
	
    private void updateStorageSpaceAndHint(final OnStorageUpdateDoneListener callback, boolean async) {
        /*
         * We execute disk operations on a background thread in order to
         * free up the UI thread.  Synchronizing on the lock below ensures
         * that when getStorageSpaceBytes is called, the main thread waits
         * until this method has completed.
         *
         * However, .execute() does not ensure this execution block will be
         * run right away (.execute() schedules this AsyncTask for sometime
         * in the future. executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
         * tries to execute the task in parellel with other AsyncTasks, but
         * there's still no guarantee).
         * e.g. don't call this then immediately call getStorageSpaceBytes().
         * Instead, pass in an OnStorageUpdateDoneListener.
         */
        if(CameraSettings.LOG_SWITCH)
                Log.i(TAG,"updateStorageSpace Async "+async);
        if (!async) {
                synchronized (mStorageSpaceLock) {
                        mStorageSpaceBytes = getAvailableSpace();
                        if(CameraSettings.LOG_SWITCH)
                                Log.i(TAG, "mStorageSpaceBytes = " + mStorageSpaceBytes + "M");
                }
                if (getStorageSpaceBytes() < getLimitStorage()) {
                        if(CameraSettings.LOG_SWITCH)
                                Log.i(TAG, "LOW_STORAGE_THRESHOLD_BYTES deleteOldestVideofile");
                        deleteOldestVideofile(true);
                }
                if (callback != null)
                        callback.onStorageUpdateDone(getStorageSpaceBytes());
                return;
        }
        mUpdateStorageSpaceStartTime = SystemClock.uptimeMillis();
        if(!MyApplication.isExternalStorageMounted(mCurrentRootPath)) return;
        (new AsyncTask<Void, Void, Long>() {
                @Override
                protected Long doInBackground(Void...arg) {
                        if(CameraSettings.LOG_SWITCH)
                                Log.i(TAG, "updateStorageSpace doInBackground");
                        synchronized (mStorageSpaceLock) {
                                mStorageSpaceBytes = getAvailableSpace();
                                if(CameraSettings.LOG_SWITCH)
                                        Log.i(TAG, "mStorageSpaceBytes = " + mStorageSpaceBytes + "M");
                        }
                        if (mMediaRecorderRecording) {
                                if (getStorageSpaceBytes() < getLimitStorage()) {
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.i(TAG, "LOW_STORAGE_THRESHOLD_BYTES deleteOldestVideofile");
                                        deleteOldestVideofile(false);
                                }
                        }
                        return mStorageSpaceBytes;
                }

                @Override
                protected void onPostExecute(Long bytes) {
                        if(CameraSettings.LOG_SWITCH)
                                Log.i(TAG, "updateStorageSpace onPostExecute mStorageSpaceBytes = "+ bytes + "M");
                        if (mMediaRecorderRecording && bytes < getLimitStorage()) {
                                if(CameraSettings.LOG_SWITCH)
                                        Log.i(TAG, "LOW_STORAGE_THRESHOLD_BYTES stopRecording");
                                if(mMediaRecorderRecording) {
                                        ttsHandler.sendEmptyMessage(MSG_NO_AVAILABLE_SPACE);
                                        stopAllVideoRecording();
                                }
                                if(mHandler != null){
                                        mHandler.removeMessages(MSG_SCAN_LOCK);
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.w(TAG, "异步发了一条消息");
                                        mHandler.sendEmptyMessageDelayed(MSG_SCAN_LOCK, 300);
                                }
                                return;
                        }
                        if (callback != null)
                                callback.onStorageUpdateDone(bytes);
                                if (mMediaRecorderRecording) {
                                        long delta = SystemClock.uptimeMillis() - mUpdateStorageSpaceStartTime;
                                        long targetNextUpdateDelay = 8000;
                                        long actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.i(TAG, "actualNextUpdateDelay = " + actualNextUpdateDelay);
                                        mHandler.removeMessages(MSG_UPDATE_SPACE);
                                        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_SPACE, actualNextUpdateDelay > 0 ? actualNextUpdateDelay : 0);
                                }
                        } 
        }).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }
    
    private void deleteOldestVideofile(boolean displayWindow) {
        if(!MyApplication.isExternalStorageMounted(mCurrentRootPath)){
                ttsHandler.sendEmptyMessage(MSG_NO_AVAILABLE_STORAGE);
                return;
        }
        synchronized (mFilelist) {
                getCommonFileListByIndexNum(false);
                if(getStorageSpaceBytes() <= getLimitStorage() && mFilelist.size() <= 0){
                        if(CameraSettings.LOG_SWITCH)
                                Log.e(TAG, "no file to delete----less than limit");
                        goToDeleteLockFiles(displayWindow);                          
                        return;
                }else{
                        //do nothing
                }
               
                while (getStorageSpaceBytes() < getLimitStorage()) {
                        getCommonFileListByIndexNum(false);
                        if (mFilelist.size() <= 0) {
                                if(CameraSettings.LOG_SWITCH)
                                        Log.e(TAG, "no file to delete----while--------less than 2");
                                goToDeleteLockFiles(displayWindow);
                                return;
                        }
                        CommonFileCompareInfos file = mFilelist.remove(0);
                        while (file != null && file.getmFile() != null) {
                                if (!file.getmFile().exists()) {
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.e(TAG, "deleteOldestVideofile "+ file.getmFile().getName() + " not exists");
                                        //如果有文件不存在了，说明目录结构被改变了
                                        mFilelist.clear();
                                        getCommonFileListByIndexNum(false);
                                        if (mFilelist.size() <= 0) {
                                                goToDeleteLockFiles(displayWindow);                                                        
                                                return;
                                        }
                                        file = mFilelist.remove(0);
                                        continue;
                                }
                                long startDelete = SystemClock.uptimeMillis();
                                long emptySpaceBytes = file.getmFile().length()>>20;
                                if (!deleteDir(file.getmFile())) {
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.e(TAG, "deleteOldestVideofile "+ file.getmFile().getName() + " fail");
                                } else {
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.d(TAG, "delete spent time = " + (SystemClock.uptimeMillis() - startDelete));
                                        if(CameraSettings.LOG_SWITCH)
                                                Log.d(TAG, "delete file size = " + emptySpaceBytes);
                                        synchronized (mStorageSpaceLock) {
                                                mStorageSpaceBytes += emptySpaceBytes;
                                        }
                                        break;
                                }
                        }
                        if(CameraSettings.LOG_SWITCH)
                                Log.i(TAG, "after delete file mStorageSpaceBytes = " + getStorageSpaceBytes() + "M");
                }
        }
}

/*删除上锁区域视频*/
private void cycleDeleteLockFiles(){
        long start = System.currentTimeMillis();
        long emptyLockSpaceBytes=0;
        File temp;
        getLockFileListbyIndexNum(false);
        if(mLockFileList.size() <= 2){
                Log.e(TAG_FILE, "lockFileRate >= 0.3 but size <= 2 ,there must be error when getLockFileList!");
                ToastUtils.showShortToast(mContext, mContext.getResources().getString(R.string.str_lock_invaild));
                ttsHandler.sendEmptyMessage(MSG_TTS_SHOW_ALERT_VIEW_BY_LOCK);
                return;
        }else{
                synchronized (mLockFileList) {
                        while(getStorageSpaceBytes() <= getLimitStorage()){
                                if(mLockFileList.size() > 0){
                                        temp = mLockFileList.remove(0).getmFile();
                                        if(!temp.exists()){
                                                mLockFileList.clear();
                                                getLockFileListbyIndexNum(false);
                                                if(mLockFileList.size() > 0){
                                                        temp = mLockFileList.remove(0).getmFile();
                                                }else{
                                                        break;
                                                }
                                        }
                                        long emptySpaceBytes = temp.length()>>20;
                                        if(!deleteDir(temp)){
                                                
                                        }else{
                                                if(CameraSettings.LOG_SWITCH)
                                                        Log.w(TAG_FILE, "delete lock file-------"+temp.getName());
                                                synchronized (mStorageSpaceLock) {
                                                        mStorageSpaceBytes += emptySpaceBytes;
                                               }
                                        }
                                }else{
                                        break;
                                }
                        }                               
                }
        }
        if(CameraSettings.LOG_SWITCH)
                Log.w(TAG, "delete  lockfile  cost time --------------"+(System.currentTimeMillis()-start) + "   current  space  :"+mStorageSpaceBytes);
}


//进入删除上锁视频第一阶段
private void goToDeleteLockFiles(boolean displayWindow){
        boolean lockRate = getLockFileSizeRate();
        Log.w(TAG_FILE, "goToDeleteLockFiles()-------rate: "+lockRate);
        if(lockRate){
                if(MyApplication.getDeleteLockFiles()){
                        cycleDeleteLockFiles();
                }else{
                        if(displayWindow){
                                ttsHandler.sendEmptyMessage(MSG_TTS_SHOW_ALERT_VIEW_BY_LOCK);
                        }
                }
        }else{     
                if(displayWindow){
                        ttsHandler.sendEmptyMessage(MSG_TTS_SHOW_ALERT_VIEW_BY_OTHER);      
                }
        }
}

/**
 * @param dir 
 * @return boolean Returns "true" if all deletions were successful.
 *                 If a deletion fails, the method stops attempting to delete and returns "false".
 */
	private boolean deleteDir(File dir) {
		if(dir == null) return true;
        if (dir.isDirectory()) {
                String[] children = dir.list();
                if(children == null || children.length == 0)  dir.delete();
                for (int i=0; i<children.length; i++) {
                        boolean success = deleteDir(new File(dir, children[i]));
                        if (!success) {
                                return false;
                        }
                }
        }
        return cmdDeleteFile(dir.getPath());//dir.delete();
	}

	private boolean cmdDeleteFile(String path) {
        if (path != null && (new File(path)).exists()) {
                String cmd = "rm " + path;
                if(CameraSettings.LOG_SWITCH)
                        Log.i(TAG, "cmd=" + cmd);
                try {
                        mContext.getContentResolver().delete(Video.Media.EXTERNAL_CONTENT_URI, Video.Media.DATA + "=?", new String[] {path});
                        Runtime.getRuntime().exec(cmd);
                        return true;
                } catch (IOException e) {
                        e.printStackTrace();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }
        return false;
	}
    
	private boolean delete(String res) {
        if(res == null) {
                return true;
        }
        try {
                File resFile = new File(res);
                if(resFile.exists() && resFile.isFile()) {
                        resFile.delete();
                }
                return true;
        } catch(Exception e) {
                e.printStackTrace();
        }
        return false;
	}


	private synchronized void moveTo(String res, String dest) {
        if(TextUtils.isEmpty(res) || TextUtils.isEmpty(dest))
                return;
        try {
                File source = new File(res);
                if(source.exists()) {
                        source.renameTo(new File(dest));
                        if(CameraSettings.LOG_SWITCH)
                                Log.d(TAG, "rename file to lock");
                }else {
                        if(CameraSettings.LOG_SWITCH)
                                Log.d(TAG, "rename file faield because of source file is not exists");
                }
        }catch(Exception e) {
                e.printStackTrace();
        }
	}

	//获取VIDA,VIDB的列表，并获取当前索引值
	public void getCommonFileListByIndexNum(boolean isInit) {
        File dir_U = new File(mCurrentRootPath+CameraSettings.DIR_USB);
        File dir_C = new File(mCurrentRootPath+CameraSettings.DIR_CVBS);
        File dir_G = new File(mCurrentRootPath+CameraSettings.DIR_360);
        dir_U.mkdirs();
        dir_C.mkdirs();
        dir_G.mkdirs();
        if ((!dir_U.isDirectory() || !dir_U.canWrite())
        		|| (!dir_C.isDirectory() || !dir_C.canWrite())
        		|| (!dir_G.isDirectory()|| !dir_G.canWrite())) {
                if(CameraSettings.LOG_SWITCH)
                        Log.e(TAG, "dir not exits or not writable");
                Utils.writeSdcardAuthority(Utils.readProcModules());
                return;
        }
        long now = SystemClock.uptimeMillis();
        File[] fileArray;
        if (mFilelist.size() < 4) {
                mFilelist.clear();   
                fileArray = dir_U.listFiles(mVideoNameFilter);
                if(fileArray != null){
	                for (File file : fileArray) {
	                        if (!file.isDirectory()) {
	                                if(isInit){
	                                        mFilelist.add(new CommonFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Common(file.getName())));
	                                }else{
	                                        mFilelist.add(new CommonFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Common(file.getName())));
	                                }
	                        } else {
	                                if(CameraSettings.LOG_SWITCH)
	                                        Log.d(TAG, "DVR directory  VIDA  has other files:" + file.getName() + "; is directory:" + file.isDirectory());
	                        }
	                }   
                }
                
                fileArray = dir_C.listFiles(mVideoNameFilter);
                if(fileArray != null){
	                for(File file : fileArray) {
	                        
	                        if (!file.isDirectory()) {
	                                if(isInit){
	                                        mFilelist.add(new CommonFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Common(file.getName())));
	                                }else{
	                                        mFilelist.add(new CommonFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Common(file.getName())));
	                                }
	                        } else {
	                                if(CameraSettings.LOG_SWITCH)
	                                        Log.d(TAG, "DVR directory   VIDB  has other files:" + file.getName() + "; is directory:" + file.isDirectory());
	                        }
	                }
                }
                
                fileArray = dir_G.listFiles(mVideoNameFilter);
                if(fileArray != null){
	                for(File file : fileArray) {
	                    
	                    if (!file.isDirectory()) {
	                            if(isInit){
	                                    mFilelist.add(new CommonFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Common(file.getName())));
	                            }else{
	                                    mFilelist.add(new CommonFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Common(file.getName())));
	                            }
	                    } else {
	                            if(CameraSettings.LOG_SWITCH)
	                                    Log.d(TAG, "DVR directory   VIDB  has other files:" + file.getName() + "; is directory:" + file.isDirectory());
	                    }
	                }
                }
                
                updateCommonFileMinMaxNum();
                
                Collections.sort(mFilelist);
                
                //从排序过后的列表中取最后一项即为当前最大值
                if(isInit){
                        if(mFilelist.size() != 0)
                                mCurrentCommonIndex = mFilelist.get(mFilelist.size()-1).getmIndexNum();
                        else
                                mCurrentCommonIndex = 0;
                }else{
                        //doNothing
                }      
                
                if(CameraSettings.LOG_SWITCH){
                        for(CommonFileCompareInfos temp:mFilelist){
                                Log.i(TAG_FILE, temp.getmFile().getName());
                        }
                }
        }
        Log.i(TAG_FILE, "getFileList count = " + mFilelist.size() + " spent time = "+ (SystemClock.uptimeMillis() - now) + "   max:  "+maxCommonIndexNum + "     min: "+minCommonIndexNum+"    cur: "+mCurrentCommonIndex);
        
	}

	private void  getLockFileListbyIndexNum(boolean isInit){
        File dir_Lock = new File(mCurrentRootPath+CameraSettings.DIR_LOCK);
        dir_Lock.mkdirs();
        if (!dir_Lock.isDirectory() || !dir_Lock.canWrite()) {
                if(CameraSettings.LOG_SWITCH)
                        Log.e(TAG, "dir not exits or not writable");
                return;
        }
        
        File[] fileArray;
        long now = SystemClock.uptimeMillis();
        if(mLockFileList.size() < 4){
                mLockFileList.clear();
                
                fileArray = dir_Lock.listFiles(mVideoNameFilter);
                if(fileArray != null){
	                for (File file : fileArray) {
	                        if (!file.isDirectory()) {
	                                mLockFileList.add(new LockFileCompareInfos(file,FileNameUtil.getIndexStringFromName_Lock(file.getName())));
	                        } else {
	                                if(CameraSettings.LOG_SWITCH)
	                                        Log.d(TAG, "DVR directory has other files:" + file.getName() + "; is directory:" + file.isDirectory());
	                        }
	                }
                }
                updateLockFileMinMaxNum();
                
                Collections.sort(mLockFileList);
                //从排序过后的列表中取最后一项即为当前最大值
                if(isInit){
                        if(mLockFileList.size() != 0)
                                mCurrentLockIndex = mLockFileList.get(mLockFileList.size()-1).getmIndexNum();
                        else
                                mCurrentLockIndex = 0;
                }else{
                        //doNothing
                }
                
                if(CameraSettings.LOG_SWITCH){
                        //仅作为测试用
                        for(LockFileCompareInfos temp: mLockFileList){
                                Log.i(TAG_FILE, temp.getmFile().getName());
                        }
                }
        }                
        
        Log.i(TAG_FILE, "getLockFileList count = " + mLockFileList.size() + " spent time = "+ (SystemClock.uptimeMillis() - now) + "   max:  "+maxLockIndexNum + "     min: "+minLockIndexNum+"    cur: "+mCurrentLockIndex);
        
	}

	public boolean  getLockFileSizeRate() {
		String lockPath = mCurrentRootPath+CameraSettings.DIR_LOCK;
        File dir_lock = new File(lockPath);
        dir_lock.mkdirs();
        if ((!dir_lock.isDirectory() || !dir_lock.canWrite())) {
                if(CameraSettings.LOG_SWITCH)
                        Log.e(TAG, "dir not exits or not writable");
                return  false;
        }
        double lock_size = FileSizeUtil.getFileOrFilesSize(lockPath, FileSizeUtil.SIZETYPE_MB);
        double total_size = MyApplication.getTotalSpace(mCurrentRootPath);
        if(CameraSettings.LOG_SWITCH)
                Log.w(TAG, "lock_size----------------------"+lock_size+"---total_size---------------"+total_size);
        if(lock_size >= total_size * MyApplication.getCleanSizeSetting()/100)
                return true;
        else
                return false;
	}

	//选出CommonList中索引编号最小值和最大值, 如果最大值与最小值的差值大于一定值，说明循环到尾部了
	private void updateCommonFileMinMaxNum(){
        int temp;
        minCommonIndexNum = -1;
        maxCommonIndexNum = -1;
        for(int i = mFilelist.size()-1;i>=0;i--){
                temp = mFilelist.get(i).getmIndexNum();
                if(minCommonIndexNum == -1 || maxCommonIndexNum == -1){
                        minCommonIndexNum = temp;
                        maxCommonIndexNum = temp;
                }else{
                        if(minCommonIndexNum >= temp){
                                minCommonIndexNum = temp;
                        }
                        if(maxCommonIndexNum <= temp){
                                maxCommonIndexNum = temp;
                        }
                }
        }
        
        if(maxCommonIndexNum - minCommonIndexNum > LIMIT_INDEX_NUM/2){ 
                GlobalUtil.mAddSize_comon = true;
        }else{
                GlobalUtil.mAddSize_comon = false;
        }
	}

	//选出LockList中索引编号最小值和最大值, 如果最大值与最小值的差值大于一定值，说明循环到尾部了
	private void updateLockFileMinMaxNum(){
        int temp;
        minLockIndexNum = -1;
        maxLockIndexNum = -1;
        for(int i = mLockFileList.size()-1;i>=0;i--){
                temp = mLockFileList.get(i).getmIndexNum();
                if(minLockIndexNum == -1 || maxLockIndexNum == -1){
                        minLockIndexNum = temp;
                        maxLockIndexNum = temp;
                }else{
                        if(minLockIndexNum >= temp){
                                minLockIndexNum = temp;
                        }
                        if(maxLockIndexNum <= temp){
                                maxLockIndexNum = temp;
                        }
                }
        }
        
        if(maxLockIndexNum - minLockIndexNum > LIMIT_INDEX_NUM/2){ 
                GlobalUtil.mAddSize_lock = true;
        }else{
                GlobalUtil.mAddSize_lock = false;
        }
	}
	
	private void readPreferenceAndSettings(){
		
	}
	
	private void setCameraCommonParameters(int cameraId){
		if(mCameras==null || mCameras[cameraId] == null)return;
		mParameters[cameraId] = mCameras[cameraId].getParameters();
		//previewSize
		mParameters[cameraId].setPreviewSize(mDesiredPreviewWidth[cameraId],mDesiredPreviewHeight[cameraId]);
		Log.i(TAG, "camera "+cameraId+"  previewSize:"+mDesiredPreviewWidth[cameraId]+"x"+mDesiredPreviewHeight[cameraId]);
		
		//pictureSize
		List<Size> sizes = mParameters[cameraId].getSupportedPictureSizes();
		Log.i(TAG, "camera "+cameraId+"  pictureSize:"+sizes.get(0).width+"x"+sizes.get(0).height);
		
		//Set JPEG quality.
		//int jpeg = CameraProfile.getJpegEncodingQualityParameter(cameraId, CameraProfile.QUALITY_HIGH);
		mParameters[cameraId].setJpegQuality(90);
		Log.i(TAG, "camera "+cameraId+"  jpeg: "+90);
		
		//exposure compensation
		int max = mParameters[cameraId].getMaxExposureCompensation();
		int min = mParameters[cameraId].getMinExposureCompensation();
		if(max != 0 || min != 0)
			mParameters[cameraId].setExposureCompensation(0);
		Log.i(TAG, "camera "+cameraId+"  exposure max:"+max+"  min:"+min);
		
		//white balance parameter
		List<String> whiteBalance = mParameters[cameraId].getSupportedWhiteBalance();
		if(whiteBalance != null){
			StringBuilder sr = new StringBuilder("null ");
			for(String tmp:whiteBalance){
				sr.append(tmp).append(";");
			}
			Log.w(TAG, "camera "+cameraId+"  whiteBalance "+sr.toString());
		}
		
		//fps----range 取80%
		List<Integer> fpsLists = mParameters[cameraId].getSupportedPreviewFrameRates();
		if(fpsLists != null){
			int fps = getFrontPreviewframerate(80, fpsLists);
			mParameters[cameraId].setPreviewFrameRate(fps);
		}
		
		//continuous autofocus.
		List<String> supportedFocus = mParameters[cameraId].getSupportedFocusModes();
        if (isSupported(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO, supportedFocus)) {
                mParameters[cameraId].setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }   

        //record hint
        //mParameters[cameraId].set(CameraSettings.RECORDING_HINT, CameraSettings.TRUE);

	    // Enable video stabilization. Convenience methods not available in API
	    // level <= 14
	    String vstabSupported = mParameters[cameraId].get("video-stabilization-supported");
	    if ("true".equals(vstabSupported)) {
	         mParameters[cameraId].set("video-stabilization", "true");
	    }   
	    
	    mCameras[cameraId].setParameters(mParameters[cameraId]);
	}
	
	/**
     * 返回支持列表中与给定百分比最接近的那个
    * @Title: getFrontPreviewframerate
    * @param previewFpsPercent
    * @return 
    * @return int 
    */
    private int getFrontPreviewframerate(int previewFpsPercent,List<Integer> list) {
            int totalFramerate = 30;/*Integer.valueOf(mPreferences.getString(CameraSettings.KEY_PREVIEW_FPS,getResources().getString(R.string.entry_default_setting_preview_fps_30)));*/
            final int targetFramerate = totalFramerate * previewFpsPercent / 100;
            int fps = targetFramerate;
            if (list != null && list.size() > 0) {
                    int minDiff = Integer.MAX_VALUE;
                    for (int framerate : list) {
                            if (Math.abs(framerate - targetFramerate) < minDiff) {
                                    fps = framerate;
                                    minDiff = Math.abs(framerate - targetFramerate);
                            }
                    }
            }else{
                    fps = ((int)(targetFramerate / 5)) * 5;
            }
            Log.w(TAG, "front camera  fps--------"+fps);
            return fps;
           /* if(previewFpsPercent != 100)
                    return 20;
            return Integer.valueOf(getResources().getString(R.string.entry_default_setting_preview_fps_30));*/
     }
    
    private static boolean isSupported(String value, List<String> supported) {
        return supported == null ? false : supported.indexOf(value) >= 0;
    }
	
	private void handleAccOn(){
		mHandler.removeMessages(MSG_ACC_OFF);
	}
	
	private void handleAccOver(){
		mHandler.sendEmptyMessageDelayed(MSG_ACC_OFF, 2000);
	}
	
	private void dispatchReverseLeftRightSteer(){
		if(mReverseState){
			handleReverseOn(CameraHolder.getCvbsCameraId());
		}else{
			if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_LEFT){
				handleReverseOn(CameraHolder.getLeftCameraId());
			}else if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_RIGHT){
				handleReverseOn(CameraHolder.getRightCameraId());
			}else{
				handleReverseOver();
			}
		}
	}
	
	private void handleReverseOn(int cameraId){
		if(mReverseLayout != null){
			mReverseLayout.showReverseWindow(cameraId);
			setBackCarBacklight(true);
		}
	}
	
	private void handleReverseOver(){
		if(mReverseLayout!=null){
			mReverseLayout.hideReverseWindow();
			setScreenBacklight(false);
			setBackCarBacklight(false);
			setScreenBacklight(true);
		}
		//setSurfaceTexture(mSurfaceTextures[CameraHolder.getCvbsCameraId()], CameraHolder.getCvbsCameraId(), false, -1);
	}
	
	private void handleWhenCameraOpenSuccess(int cameraId,boolean needRecord,boolean needReverse){  //线程归一到主线程去做
		Log.w(TAG, "open camera "+cameraId+" success!");
		mHandler.obtainMessage(MSG_HANDLE_OPEN_CAMERA_SUCCESS, needRecord?1:0, needReverse?1:0,cameraId).sendToTarget();
	}
	
	private void handleWhenCameraOpenFailed(int cameraId,boolean needRecord,boolean needReverse){
		closeCamera(cameraId);
		Log.e(TAG, "open camera "+cameraId+"  failed!");		
		if(cameraId == CameraHolder.getCvbsCameraId()){
			int mode = CvbsHelper.getCvbsStateUsedByKernel();
			if(mode == 1){
				Log.e(TAG, "cvbs failed as kernel use it !");
			}else{
				Log.e(TAG, "cvbs failed as other reasons !");
			}
		}
		tHandler.removeMessages(MSG_OPEN_CAMERA_FAIL);
		Message msg = tHandler.obtainMessage(MSG_OPEN_CAMERA_FAIL, needRecord?1:0, needReverse?1:0,cameraId);
		tHandler.sendMessageDelayed(msg, 1000);
		
	}
	
	private void handleWhenCameraStartPreviewSuccess(int cameraId,boolean needRecord,boolean needReverse){
		Log.w(TAG, "camera "+cameraId+" start preview success, needRecord:"+needRecord+";needReverse:"+needReverse);
		if(needReverse){ 
			mHandler.sendEmptyMessage(MSG_DISPATCH_REVRSE_LEFT_RIGHT_STEER);
		}
		if(needRecord){
			startVideoRecording(cameraId);
		}else{
			if(mCameraActivityCallback != null){
				mCameraActivityCallback.updateActivityUi(mMediaRecorderRecording, cameraId, mCamerasRecording[cameraId], mLock, mCurrentTimeText);
			}
		}
	}
	
	private void handleWhenCameraStartPreviewFailed(int cameraId){
		closeCamera(cameraId);
		Log.e(TAG, "camera "+cameraId+"  start preview failed!");
	}
	
	private void registerExternelStorageListener(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addDataScheme("file");
		mExternelStorageBroadcastReceiver = new MyExternelStorageBroadcastReceiver();
		mContext.registerReceiver(mExternelStorageBroadcastReceiver, intentFilter);
	}
	
	private void unRegisterExternelStorageListener(){
		mContext.unregisterReceiver(mExternelStorageBroadcastReceiver);
	}
	
	private void handleWhenSpaceNotEnough(boolean takePicture,boolean startRecorder){
		
	}
	
	private void handleWhenReverseWindowHide(int cameraId){
		setSurfaceTexture(mSurfaceTextures[cameraId], cameraId, false, -1);
	}
	
	private void handleUsbCameraPlugIn(){
		boolean needRecord = (getRecordingCameraNumbers()> 0);
		openCamera(CameraHolder.getUsbCameraId(), needRecord, false);
	}
	
	private void handleUsbCameraPlugOut(){
		int cameraId = CameraHolder.getUsbCameraId();
		stopPreview(cameraId);
		Log.w(TAG, "out step 1");
		stopRecorder(cameraId);
		Log.w(TAG, "out step 2");
		closeCamera(cameraId);
	}
	
	private void lockFiles(){
		
	}
	
	private void unLockFiles(){
		
	}
	
	private int getMediaRecorderDuration(){
		mMaxVideoDurationInMs = MyApplication.getMaxDuration();
		return mMaxVideoDurationInMs;
	}
	
	private int getLimitStorage(){
		return MyApplication.DEFAULT_CLEAN_SPACE;
	}
	
	public static String getRootPath(){
		return mCurrentRootPath;
	}
	
	private long getAvailableSpace(){  //每次操作前都判断外部存储是否已挂载
		if(!MyApplication.isExternalStorageMounted(mCurrentRootPath)){
			mCurrentRootPath = MyApplication.getCurrentStorage();
		}
		if(mCurrentRootPath == null){
			Log.w(TAG, "getAvailableSpace() rootPath --- null");
			return CameraSettings.STORAGE_INVALID;
		}else{
			MyApplication.generateDirRoot(mCurrentRootPath);
			return MyApplication.getFreeSpace(mCurrentRootPath);
		}
	}
	
    protected long getStorageSpaceBytes() {
        synchronized (mStorageSpaceLock) {
                return mStorageSpaceBytes;
        }
    }
    
    private class RenameRunnable implements Runnable{
    	
    	private FileState rState;
    	private String path;
    	String newPath;
    	
    	public RenameRunnable(FileState fileState){
    		rState = fileState;
    	}

		@Override
		public void run() {
			renameFile(rState);
		}
		
		private void renameFile(FileState fileState){
			if(fileState == null) return;
			path = fileState.getmPath();
			if(!path.endsWith(".tmp"))
				return;
			int firstIndex = path.lastIndexOf("/");
			int secondIndex = path.lastIndexOf(".");
			if(firstIndex < 0 || secondIndex < 0) return;
			if(rState.ismLock()){
				mCurrentLockIndex += 1;
				newPath = mCurrentRootPath+CameraSettings.DIR_LOCK+FileNameUtil.commonToLock(path.substring(firstIndex, secondIndex),mCurrentLockIndex);
			}else{
				newPath = path.substring(0, secondIndex);
			}
			Log.w(TAG_FILE, "src:"+path+"  -->rename to--->"+"new:"+newPath);
			File srcFile = new File(path);
			if(srcFile.exists() && srcFile.isFile()){
				srcFile.renameTo(new File(newPath));
			}
		}
    }
	
    //在开始录像那里加锁获取两者的当前索引值
    private String generateVideoFilename(int outputFileFormat, int cameraId) {
            long dateTaken = System.currentTimeMillis();
            String title;
            dateTime = SystemClock.uptimeMillis();
            if(mRecordingLock){
                    mCurrentLockIndex = (mCurrentLockIndex +1)%LIMIT_INDEX_NUM;
                    title = createVideoName(dateTaken, cameraId, true, mCurrentLockIndex);
            }else{
                    mCurrentCommonIndex = (mCurrentCommonIndex +1)%LIMIT_INDEX_NUM;
                    title = createVideoName(dateTaken, cameraId, false, mCurrentCommonIndex);
            }
            String filename = title + convertOutputFormatToFileExt(outputFileFormat);
            String path = null;
            if(mRecordingLock) {
                    path = mCurrentRootPath + "/"+CameraSettings.DIR_LOCK+'/' + filename;
            } else {
                    if(filename.contains("VIDC_")) {
                            path = mCurrentRootPath + "/"+CameraSettings.DIR_CVBS+'/' + filename;
                    }else if(filename.contains("VIDU_")) {
                    		path = mCurrentRootPath + "/"+CameraSettings.DIR_USB+'/' + filename;
                    }else{
                            path = mCurrentRootPath + "/"+CameraSettings.DIR_360+'/' + filename;
                    }
            }
            if(mCurrentPaths[cameraId] != null){
            	mLastPaths[cameraId] = mCurrentPaths[cameraId];
            }
            mCurrentPaths[cameraId] = new FileState(path, false);
            if(CameraSettings.LOG_SWITCH)
                    Log.i(TAG, "path: "+path);
            //主要是为了防止录制过程中正好手动删除了此目录，导致无法开始下一段
            if(path != null){
                    File dirPath = new File(path).getParentFile();
                    if(!dirPath.exists() || !dirPath.isDirectory()){
                            dirPath.mkdirs();
                    }
            }
           
            return path;
    }

    private String createVideoName(long dateTaken, int cameraId,final boolean lock, final int indexNum) {
            Date date = new Date(dateTaken);
            String src;
            String formatString;
            if(lock){
            	if(cameraId == CameraHolder.getUsbCameraId()){
            		src = mContext.getString(R.string.video_lock_usb_file_name_format);
            	}else if(cameraId == CameraHolder.getGlobalCameraId()){
            		src = mContext.getString(R.string.video_lock_360_file_name_format);
            	}else{
            		src = mContext.getString(R.string.video_lock_cvbs_file_name_format);
            	}                    
            }else{
            	if(cameraId == CameraHolder.getUsbCameraId()){
            		src = mContext.getString(R.string.video_common_usb_file_name_format);
            	}else if(cameraId == CameraHolder.getGlobalCameraId()){
            		src = mContext.getString(R.string.video_common_360_file_name_format);
            	}else{
            		src = mContext.getString(R.string.video_common_cvbs_file_name_format);
            	}  
                    
            }
            formatString = String.format(src, indexNum);
            SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
            return dateFormat.format(date);
    }

    private String createJpegName(long dateTaken, int cameraId) {
            Date date = new Date(dateTaken);
            String fileName = null;
        	if(cameraId == CameraHolder.getUsbCameraId()){
        		fileName = mContext.getString(R.string.image_usb_file_name_format);
        	}else if(cameraId == CameraHolder.getGlobalCameraId()){
        		fileName = mContext.getString(R.string.image_360_file_name_format);
        	}else{
        		fileName = mContext.getString(R.string.image_cvbs_file_name_format);
        	}  
            SimpleDateFormat dateFormat = new SimpleDateFormat(fileName);
            String result = dateFormat.format(date);
            if((new File(result).exists())) {
                    if(CameraSettings.LOG_SWITCH)
                            Log.i(TAG, "The file name already existed, need delete it first!");
                    if(!deleteImg(mContext, mCurrentRootPath + "/" + CameraSettings.DIR_PIC+"/"+ result+".jpg")) {
                            deleteImg(mContext, mCurrentRootPath + "/" + CameraSettings.DIR_PIC+"/"+ result+".jpg");
                    }
            }
            return result;
    }
    
    public boolean deleteImg(Context context, String filePath) {
        if(context == null || filePath == null) {
                if(CameraSettings.LOG_SWITCH)
                        Log.e(TAG, "deleteImg() method has null parameters");
                return false;
        }
        File file = new File(filePath);
        if(file.exists()) {
                 try {
                         String params[] = new String[]{filePath};
                         context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media.DATA + " LIKE ?", params);
                         file.delete();
                         return true;
                 } catch (Exception e) {
                         if(CameraSettings.LOG_SWITCH)
                                 Log.w(TAG, "delete media data error");
                 }
        }
        return false;
}
    
    private String convertOutputFormatToFileExt(int outputFileFormat) {
        if (outputFileFormat == MediaRecorder.OutputFormat.MPEG_4) {
                return ".mp4.tmp";
        }
        return ".3gp.tmp";
    }
    
    public void scanLockFileRunnable(){
        
        boolean re = getLockFileSizeRate();
        if(!re){
                ttsHandler.sendEmptyMessage(MSG_TTS_SHOW_ALERT_VIEW_BY_OTHER);
        }else{
                ttsHandler.sendEmptyMessage(MSG_TTS_SHOW_ALERT_VIEW_BY_LOCK);   
        }       
        
    }
    
    private void updateRecordingTime() {
        if (!mMediaRecorderRecording) {
                return;
        }
        long delta = SystemClock.uptimeMillis() - mRecordingStartTime;

        // Starting a minute before reaching the max duration limit, we'll countdown the remaining time instead.
        boolean countdownRemainingTime = ( mMaxVideoDurationInMs != 0 && delta >= 0);

        if (countdownRemainingTime) {
            delta = Math.max(0, delta) + 999;
        }

        long targetNextUpdateDelay;

        mCurrentTimeText = millisecondToTimeString(delta, false);
        targetNextUpdateDelay = 1000;

        if (mCameraActivityCallback != null)
                mCameraActivityCallback.updateRecordingTime(mCurrentTimeText,mRecordingStartTime, mMaxVideoDurationInMs);

        long actualNextUpdateDelay = targetNextUpdateDelay - (delta % targetNextUpdateDelay);
        ttsHandler.removeMessages(MSG_TTS_UPDATE_TIME);
        ttsHandler.sendEmptyMessageDelayed(MSG_TTS_UPDATE_TIME,actualNextUpdateDelay);
    }

    private void releaseAllSurfaceTexture(){
    	for(int i = 0;i<CameraSettings.MAX_CAMERA_NUMBERS;i++){
    		if(mSurfaceTextures != null && mSurfaceTextures[i] != null){
    			mSurfaceTextures[i].release();
    			mSurfaceTextures[i]=null;
    		}
    	}
    }
    

    private static String millisecondToTimeString(long milliSeconds, boolean displayCentiSeconds) {
        long seconds = milliSeconds / 1000; // round down to compute seconds
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long remainderMinutes = minutes - (hours * 60);
        long remainderSeconds = seconds - (minutes * 60);

        StringBuilder timeStringBuilder = new StringBuilder();

        // Hours
        if (hours > 0) {
                if (hours < 10) {
                        timeStringBuilder.append('0');
                }
                timeStringBuilder.append(hours);

                timeStringBuilder.append(':');
        }

        // Minutes
        if (remainderMinutes < 10) {
                timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderMinutes);
        timeStringBuilder.append(':');

        // Seconds
        if (remainderSeconds < 10) {
                timeStringBuilder.append('0');
        }
        timeStringBuilder.append(remainderSeconds);

        // Centi seconds
        if (displayCentiSeconds) {
                timeStringBuilder.append('.');
                long remainderCentiSeconds = (milliSeconds - seconds * 1000) / 10;
                if (remainderCentiSeconds < 10) {
                        timeStringBuilder.append('0');
                }
                timeStringBuilder.append(remainderCentiSeconds);
        }

        return timeStringBuilder.toString();
    }
    
    private void addRecordingCameraNumbers(){
    	synchronized (mMathObject) {
			mRecordingCameraNumbers++;
		}
    }
    
    private int getRecordingCameraNumbers(){
    	synchronized (mMathObject) {
    		return mRecordingCameraNumbers;
		}
    }
    
    private void reduceRecordingCameraNumbers(){
    	synchronized (mMathObject) {
    		mRecordingCameraNumbers--;
		}
    }
		
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.w(TAG, "CameraMainService on Destroy!");
		if(mCameraActivityCallback != null){
			mCameraActivityCallback.finishActivity();
		}
		handleReverseOver();   //回收倒车窗口
		stopAllVideoRecording();
		stopAllPreview();
		closeAllCamera();
		releaseAllSurfaceTexture();
		unRegisterCarEventBroadcasts();
		unRegisterExternelStorageListener();
		mReverseLayout.releaseAll();
		mLydetect.Close_lydet();
		lrHandler.removeCallbacksAndMessages(null);
		mHandler.removeCallbacksAndMessages(null);
		ttsHandler.removeCallbacksAndMessages(null);
		tHandler.removeCallbacksAndMessages(null);
		mHandlerThread.quitSafely();
		lrHandlerThread.quitSafely();
		MyApplication.SERVICE_RUNNING_FLAG = false;
	}
	
	private class CameraOpenThread extends Thread{
		
		private int tCameraId;
		
		public CameraOpenThread(int cameraId){
			tCameraId = cameraId;
		}
		
		@Override
		public void run() {			
			openCamera(tCameraId, false, false);			
		}
	}
	
    public class CameraErrorCallback implements android.hardware.Camera.ErrorCallback {
        private int mCameraId = -1;
        public CameraErrorCallback(int cameraId) {
            mCameraId = cameraId;
        }
        @Override
        public void onError(int error, android.hardware.Camera camera) {
            if(CameraSettings.LOG_SWITCH)
                    Log.e(TAG, "Got camera " + mCameraId + " error callback. error=" + error);
            Utils.writeCamErrors("CameraRecorder " + mCameraId + " error  "+error+"\n");
            
            if (error == android.hardware.Camera.CAMERA_ERROR_SERVER_DIED) {
                    // We are not sure about the current state of the app (in preview or snapshot or recording). Closing the app is better than creating a new Camera object.                                                              
                                                
            		Utils.killAppByPackage(mContext, mContext.getPackageName());
                    	
                    ToastUtils.showShortToast(mContext, R.string.cannot_connect_camera);
            }
        }
    }
    
    private class MediaRecorderErrorListener implements MediaRecorder.OnErrorListener {
        private int mCameraId = -1;

        public MediaRecorderErrorListener(int cameraId) {
                mCameraId = cameraId;
        }    

        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
                // TODO Auto-generated method stub
                if(CameraSettings.LOG_SWITCH)
                        Log.e(TAG, "MediaRecorder " + mCameraId + " error. what=" + what + ". extra=" + extra);           
                Utils.writeCamErrors("MediaRecorder " + mCameraId + " error. what=" + what + ". extra=" + extra+"\n");                        
               /* switch (what) {
                        case -1004:
                                Log.d(TAG, "MEDIA_ERROR_IO");
                                break;
                        case -1007:
                                Log.d(TAG, "MEDIA_ERROR_MALFORMED");
                                break;
                        case 200:
                                Log.d(TAG, "MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
                                break;
                        case 100:
                                Log.d(TAG, "MEDIA_ERROR_SERVER_DIED");
                                break;
                        case -110:
                                Log.d(TAG, "MEDIA_ERROR_TIMED_OUT");
                                break;
                        case 1:
                                Log.d(TAG, "MEDIA_ERROR_UNKNOWN");
                                break;
                        case -1010:
                                Log.d(TAG, "MEDIA_ERROR_UNSUPPORTED");
                                break;
                }
                switch (extra) {
                        case 800:
                                Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING");
                                break;
                        case 702:
                                Log.d(TAG, "MEDIA_INFO_BUFFERING_END");
                                break;
                        case 701:
                                Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE");
                                break;
                        case 802:
                                Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE");
                                break;
                        case 801:
                                Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE");
                                break;
                        case 1:
                                Log.d(TAG, "MEDIA_INFO_UNKNOWN");
                                break;
                        case 3:
                                Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START");
                                break;
                        case 700:
                                Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING");
                                break;
                }*/                                     
                stopAllVideoRecording();
                if(what == MediaRecorder.MEDIA_ERROR_SERVER_DIED){     
                        releaseAllRecorder();
                }
                
                if (mRecorderErrorTimes < 100 && SystemClock.uptimeMillis() - mRecorderErrorTime > 20000) {
                        mRecorderErrorTimes++;
                        mRecorderErrorTime = SystemClock.uptimeMillis();
                        mHandler.removeMessages(MSG_START_RECORDING);
                        mHandler.sendEmptyMessageDelayed(MSG_START_RECORDING, 3000);
                } else {
                        mRecorderErrorTimes = 1;
                        mRecorderErrorTime = SystemClock.uptimeMillis();
                        Log.w(TAG, "too many errors ,so not try to start recorder!");
                        Utils.writeCamErrors("Too many errors ,so not try to start recorder!  \n");        
                }
          }
    }
    
    private void setSurfaceTexture(SurfaceTexture texture,int cameraId,boolean replace,int lastCameraId){ 
    	Log.w(TAG, "texture "+(texture == null ? "is null!":"not null")+"; currentCameraId: "+cameraId
    			+" ;lastCameraId:"+lastCameraId+"; replace: "+replace);
    	
    	if(replace){
    		if(mSurfaceTextures[cameraId] != null){
    			mSurfaceTextures[cameraId].release();
    		}
    		mSurfaceTextures[cameraId] = texture;
    	}else{
    		if(lastCameraId >= 0 && lastCameraId < CameraSettings.MAX_CAMERA_NUMBERS 
    				&& mCameras[lastCameraId] != null && mCamerasPreviewing[lastCameraId] 
    				&& mSurfaceTextures[lastCameraId] != null){
    			mCameras[lastCameraId].stopRender();
    			try{
        			mCameras[lastCameraId].setPreviewTexture(mSurfaceTextures[lastCameraId]);
        		}catch(Exception e){
        			Log.e(TAG, "setTexture failed!");
        		}
        		mCameras[lastCameraId].startRender();
    		}else{
    			Log.e(TAG, "something error when restore suface!");
    		}
    	}
    	
    	if(mCameras == null || mCameras[cameraId]==null || texture == null){
    		Log.e(TAG, "setSurfaceTexture before camera +"+cameraId+" open");  		
    	}else{
    		
    		mCameras[cameraId].stopRender();
    		try{
    			mCameras[cameraId].setPreviewTexture(texture);
    		}catch(Exception e){
    			Log.e(TAG, "setTexture failed!");
    		}
    		mCameras[cameraId].startRender();
    	}
    }
    
    private void registerCallback(CameraActivityCallback callback){
    	mCameraActivityCallback = callback;
    }
    
    private void unRegisterCallback(){
    	mCameraActivityCallback = null;
    }
    
    private void stopRenders(){
    	if(mCurrentCameraMode == CameraMode.MODE_INT_USB_CVBS){
    		stopRender(CameraHolder.getUsbCameraId());
    		stopRender(CameraHolder.getCvbsCameraId());
    	}
    }
    
    private void stopRender(int cameraId){
    	if(mCameras != null && mCameras[cameraId] != null){
    		mCameras[cameraId].stopRender();
    	}
    }
    
    private void setBackCarBacklight(boolean onOff) {
        Intent intent = new Intent("com.luyuan.backar.backlight");
        intent.putExtra("backcar", onOff);
        sendBroadcast(intent);
    }

    private void setScreenBacklight(boolean onOff) {
        Intent intent = new Intent("com.luyuan.backar.backlight");
        intent.putExtra("screen", onOff);
        sendBroadcast(intent);
    }
    
    private void initCurrentIndexNum(){
        if(mCurrentCommonIndex == -1 || mCurrentLockIndex == -1){
        		MyApplication.generateDirRoot(mCurrentRootPath);
                synchronized (mIndexLock) {
                        if(mCurrentCommonIndex != -1 && mCurrentLockIndex != -1)
                                return;
                        if(!MyApplication.isExternalStorageMounted(mCurrentRootPath)) return;
                        getCommonFileListByIndexNum(true);
                        getLockFileListbyIndexNum(true);
               }  
        }
    }

    private class MediaRecorderInfoListener implements MediaRecorder.OnInfoListener {
        private int mCameraId = -1;
        private int mMaxDurationReachedTimes;
        private boolean mRecordAudio;

        public MediaRecorderInfoListener(int cameraId) {
                mCameraId = cameraId;
        }

        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
                if(CameraSettings.LOG_SWITCH)
                        Log.e(TAG, "MediaRecorder " + mCameraId + " info. what=" + what + ". extra=" + extra);
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                	mInfoTimes++;
                	
                    try {
						mMediaRecorders[mCameraId].setNextSaveFile(generateVideoFilename(MediaRecorder.OutputFormat.MPEG_4, mCameraId));
						if(getRecordingCameraNumbers()==1){
	                		mRecordingStartTime = SystemClock.uptimeMillis();
	                		updateRecordingTime();
	                		mLock = false;
	                		if(mCameraActivityCallback != null){
	                			mCameraActivityCallback.updateActivityLockState(mLock);
	                		}
	                		mInfoTimes = 0;
	                	}else{
	                		if(mInfoTimes == 1){
	                			mRecordingStartTime = SystemClock.uptimeMillis();
	                    		updateRecordingTime();
	                    		mLock = false;
		                		if(mCameraActivityCallback != null){
		                			mCameraActivityCallback.updateActivityLockState(mLock);
		                		}
	                		}else{
	                			mInfoTimes = 0;
	                		}
	                	}	
						tHandler.postDelayed(new RenameRunnable(mLastPaths[mCameraId]), 500);
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						stopRecorder(mCameraId);
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						stopRecorder(mCameraId);
						e.printStackTrace();
					}
                        
                } else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
                        if (mMediaRecorderRecording) {
                                stopAllVideoRecording();
                        }
                        Toast.makeText(mContext, R.string.video_reach_size_limit, Toast.LENGTH_LONG).show();
                }
        }
    
    }
    
    private class JpegPictureCallback implements PictureCallback{
    	
    	private int sCameraId;
    	
    	public JpegPictureCallback(int cameraId){
    		sCameraId = cameraId;
    	}

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			mSnapShotInProgress[sCameraId] = false;
			SavePictureRunnable sPictureRunnable = new SavePictureRunnable(data, sCameraId, mPictureTakeTime);
			tHandler.post(sPictureRunnable);
			if(!isSnapShotting()&& mCameraActivityCallback != null){
				mCameraActivityCallback.enableShutter(true);
			}
			camera.startPreview();
		}
    	
    }
    
    private class CallBackForAlertViewImpl implements CallbackForAlertView{
        @Override
        public void nextStepAfterChecked() {
                if(mHandler != null){
                        mHandler.sendEmptyMessage(MSG_START_RECORDING_ASYNC);
                }
        }               
    }
    
    private class SavePictureRunnable implements Runnable{

    	private byte[] sData;
    	private int sCameraId;
    	private long sTakeTime;
    	
    	public SavePictureRunnable(byte[] data,int cameraId,long takeTime){
    		sData = data;
    		sCameraId = cameraId;
    		sTakeTime = takeTime;
    	}
    	
		@Override
		public void run() {
			String picturePath = mCurrentRootPath + CameraSettings.DIR_PIC+"/"+createJpegName(sTakeTime, sCameraId)+".jpg";
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(picturePath);
				fos.write(sData);
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				try {
					if(fos != null)
						fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
    	
    }
    
    public class MyBind extends Binder{
    	
    	private WeakReference<CameraMainService> mService;
    	
    	public MyBind(CameraMainService service){
    		mService = new WeakReference<CameraMainService>(service);
    	}

		public void openCameras() {
			mService.get().openAllCameraByThread();
		}

		public void startVideoRecording() {
			mService.get().startAllVideoRecordingByThread();
		}

		public void stopVideoRecording() {
			mService.get().stopAllVideoRecordingByThread();
		}

		public void takePictures() {
			mService.get().takeAllPicture();
		}

		public SurfaceTexture getSurfaceTextures(int cameraId) {
			// TODO Auto-generated method stub
			return null;
		}

		public void closeCameras() {
			// TODO Auto-generated method stub
			
		}
		
		public boolean  isPreviewing(int cameraId){
			return mService.get().isPreviewing(cameraId);
		}

		public void registerCallback(CameraActivityCallback callback) {
			mService.get().registerCallback(callback);
		}

		public void unRegisterCallback() {
			mService.get().unRegisterCallback();
		}

		public void postSurfaceTexture(SurfaceTexture surfaceTexture,int cameraId,boolean replace) {
			mService.get().setSurfaceTexture(surfaceTexture, cameraId, replace,-1);
			
		}

		public boolean isRecording(){
			return mService.get().mMediaRecorderRecording;
		}
		
		public void stopRenders(){
			mService.get().stopRenders();
		}
		
		public void changeLockState(){
			mService.get().changeAllVideoLockState();
		}
    }
    
    private class CallbackForReverseLayoutImpl implements CallbackForReverseLayout{

		@Override
		public void setTexture(SurfaceTexture surface,int currentCameraId,boolean replace,int lastCameraId) {
			setSurfaceTexture(surface, currentCameraId,replace,lastCameraId);
		}

		@Override
		public void hideReverseWindow(int lastCameraId) {
			handleWhenReverseWindowHide(lastCameraId);
		}
    	
    }
    
    private class MyCarEventBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null) return;
			String action = intent.getAction();
			Log.w(TAG, "MyCarEventBroadcastReceiver  action:"+action);
			if(BROADCAST_BACK_CAR.equals(action)){
				mReverseState = intent.getBooleanExtra(KEY_REVERSE_STATE, false);
				if(mReverseState){
					mHandler.sendEmptyMessage(MSG_OPEN_CAMERA_BY_REVERSE);
				}else{
					mHandler.sendEmptyMessage(MSG_DISPATCH_REVRSE_LEFT_RIGHT_STEER);
				}
			}else if(BROADCAST_BACK_CAR_RADAR_TRAJECTORY.equals(action)){
				if(CameraSettings.LOG_SWITCH){
					Log.w(TAG, "receiver msg from mcu to update radar!");
				}
				byte[] mcuData = intent.getByteArrayExtra(KEY_MCU_DATA);
				if(CameraSettings.LOG_SWITCH){
					Log.w(TAG, "receiver msg from mcu,data length is "+mcuData.length);
				}
				if(mReverseLayout!=null){
					mReverseLayout.setDataFromMcu(mcuData);
				}else{
					Log.w(TAG, "mReverseLayout is null");
				}
			}else if(ACTION_LY_MCUSERVICE_ACC_STATUS.equals(action)){
				int flag = intent.getIntExtra(KEY_ACC_STATUS, 0);
				if(flag == 0){
					handleAccOver();
				}else{
					handleAccOn();
				}
			}
			/*else if(Intent.ACTION_SCREEN_ON.equals(action)){
				handleAccOn();
			}else if(Intent.ACTION_SCREEN_OFF.equals(action)){
				handleAccOver();
			}*/else if(ACTION_USB_CAMERA_PLUG_IN_OUT.equals(action)){
				Bundle bundle = intent.getExtras();
				if(bundle != null){
					int flag = bundle.getInt(KEY_USB_CAMERA_STATE);
					Log.w(TAG, "plug flag:"+flag);
					if(flag == PLUG_IN){
						tHandler.removeMessages(MSG_PLUG_IN);
						tHandler.removeMessages(MSG_PLUG_OUT);
						tHandler.sendEmptyMessageDelayed(MSG_PLUG_IN, 1000);
					}else if(flag == PLUG_OUT){
						tHandler.removeMessages(MSG_PLUG_IN);
						tHandler.removeMessages(MSG_PLUG_OUT);
						tHandler.sendEmptyMessageDelayed(MSG_PLUG_OUT, 1000);
					}else{
						Log.e(TAG, "usb plug in or out ,error data!");
					}
				}
			}
		}
    }
    
    private class MyExternelStorageBroadcastReceiver extends BroadcastReceiver{
    	private String dataPath;

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null) return;
			dataPath = intent.getData()==null?"NULL":intent.getData().getPath();
			Log.w(TAG_FILE, "action:"+intent.getAction()+";path:"+dataPath);
			if(Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())){
				if(mCurrentRootPath == null){
					mCurrentRootPath = MyApplication.getCurrentStorage();
					if(mCurrentRootPath != null){
						new Thread(){
                            public void run() {
                                    initCurrentIndexNum();
                            }
						}.start();
					}
				}
			}else if(Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())){
				if(mCurrentRootPath == null){
					
				}else{
					if(mCurrentRootPath.equals(dataPath)){
						stopAllVideoRecording();
						ttsHandler.sendEmptyMessage(MSG_HIDE_ALERT_WINDOW);
						mCurrentCommonIndex = -1;
	                    mCurrentLockIndex = -1;
	                    mFilelist.clear();
	                    mLockFileList.clear();
	                    mCurrentRootPath = null;
					}
				}
			}
		}
    	
    }
    
    private class ThdHandler extends Handler{
    	public ThdHandler(Looper loop){
    		super(loop);
    	}
    	
    	@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_PLUG_IN:
				handleUsbCameraPlugIn();
				break;
			case MSG_PLUG_OUT:
				handleUsbCameraPlugOut();
				break;
			case MSG_OPEN_CAMERA_FAIL:
				int cameraId = (Integer)msg.obj;
				openCamera(cameraId, msg.arg1==1, msg.arg2==1);
				break;
			default:
				break;
			}
		}
    }
    
    private class LeftRightDetectRunnable implements Runnable{
		@Override
		public void run() {
			if(!mLeftRightDetectOpen){
				Log.w(TAG, "open leftRightNode");
				int openFlag = mLydetect.Open_lydet();
				if(openFlag < 0){
					Log.e(TAG, "open leftRightNode failed");
					lrHandler.removeCallbacks(mLeftRightDetectRunnable);
					lrHandler.postDelayed(mLeftRightDetectRunnable, 1000);
					return;
				}				
				mLeftRightDetectOpen = true;
			}
			Log.w(TAG, "select leftRightNode");
			int selectFlag = mLydetect.Select_lydetect();
			if(selectFlag<0){
				Log.e(TAG, "select leftRightNode failed");
				mLydetect.Close_lydet();
				lrHandler.removeCallbacks(mLeftRightDetectRunnable);
				lrHandler.postDelayed(mLeftRightDetectRunnable, 1000);
				return;
			}
			int value = mLydetect.GetDirection();
			if(CameraSettings.LOG_SWITCH)
				Log.w(TAG, "current left right direction is "+value);
			if(value != mCurrentLeftRightStateValue){
				Log.w(TAG, "left right node change！");
				mCurrentLeftRightStateValue = value;
				if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_LEFT){
					mHandler.sendEmptyMessage(MSG_OPEN_CAMERA_BY_LEFT);
				}else if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_RIGHT){
					mHandler.sendEmptyMessage(MSG_OPEN_CAMERA_BY_RIGHT);
				}else if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_CLOSE){
					mHandler.sendEmptyMessage(MSG_DISPATCH_REVRSE_LEFT_RIGHT_STEER);
				}else{
					
				}
			}
			lrHandler.postDelayed(mLeftRightDetectRunnable, 600);
		}
    }
    
    
    private class LeftRightDetectRunnable2 implements Runnable{
		@Override
		public void run() {
			//打开节点
			//打开select
			//读取节点信息
			int value = CameraSettings.getCurrentLeftRightNode();
			if(value != mCurrentLeftRightStateValue){
				Log.w(TAG, "left right node change！");
				mCurrentLeftRightStateValue = value;
				if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_LEFT){
					mHandler.sendEmptyMessage(MSG_OPEN_CAMERA_BY_LEFT);
				}else if(mCurrentLeftRightStateValue == CameraMode.MODE_DETECT_RIGHT){
					mHandler.sendEmptyMessage(MSG_OPEN_CAMERA_BY_RIGHT);
				}else{
					mHandler.sendEmptyMessage(MSG_DISPATCH_REVRSE_LEFT_RIGHT_STEER);
				}
			}
			lrHandler.postDelayed(mLeftRightDetectRunnable, 600);
		}
    }
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_FIND_USB:
				if(mCameraDetect.isUvcCameraConnected() == 1){
					mCheckUsbTimes = 0;
					mHandler.removeMessages(MSG_FIND_USB);
					new CameraOpenThread(CameraHolder.getUsbCameraId()).start();
				}else{
					mCheckUsbTimes++;
					mHandler.removeMessages(MSG_FIND_USB);
					if(mCheckUsbTimes < 10){						
						mHandler.sendEmptyMessageDelayed(MSG_FIND_USB, 800);
					}else{
						ToastUtils.showShortToast(mContext, R.string.not_find_usb);
					}
				}
				break;
			case MSG_HANDLE_OPEN_CAMERA_SUCCESS:
				startPreview((Integer)msg.obj,msg.arg1==1,msg.arg2==1);
				break;
			case MSG_OPEN_CAMERA_BY_REVERSE:
				openCamera(CameraHolder.getCvbsCameraId(),false,true);			
				break;
			case MSG_OPEN_CAMERA_BY_LEFT:
				openCamera(CameraHolder.getLeftCameraId(), false, true);
				break;
			case MSG_OPEN_CAMERA_BY_RIGHT:
				openCamera(CameraHolder.getRightCameraId(), false, true);
				break;
			case MSG_START_RECORDING:
				startAllVideoRecording();
				break;
			case MSG_START_RECORDING_ASYNC:
				startAllVideoRecordingByThread();
				break;
			case MSG_SCAN_LOCK:
                scanLockFileRunnable();
                break;
			case MSG_UPDATE_SPACE:
                if(CameraSettings.LOG_SWITCH)
                        Log.i(TAG, "MSG_UPDATE_SPACE");
                updateStorageSpaceAndHint(null, true);
                break;
			/*case MSG_HANDLE_REVERSE_ON:
				handleReverseOn();
				break;
			case MSG_HANDLE_REVERSE_OVER:
				handleReverseOver();
				break;*/			
			case MSG_ACC_OFF:
				stopSelf();
				break;
			/*case MSG_PLUG_IN:
				handleUsbCameraPlugIn();
				break;
			case MSG_PLUG_OUT:
				handleUsbCameraPlugOut();
				break;*/
			case MSG_DISPATCH_REVRSE_LEFT_RIGHT_STEER:
				dispatchReverseLeftRightSteer();
				break;
			default:
				break;
			}
		};
	};
	
	private Handler ttsHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NO_AVAILABLE_STORAGE:
				Log.w(TAG_FILE, "no available");
				ToastUtils.showShortToast(mContext, R.string.str_not_find_External_memory);
				break;
			case MSG_NO_AVAILABLE_SPACE:
				Log.w(TAG_FILE, "space is not enough");
				ToastUtils.showShortToast(mContext, R.string.str_space_not_enough);
				break;
			case MSG_TTS_SHOW_ALERT_VIEW_BY_LOCK:
                if(mAlertViewByLock != null  && !mAlertViewByLock.isWindowShow()){
                        mAlertViewByLock.showWindowView();
                }                                            
                break;
			case MSG_TTS_SHOW_ALERT_VIEW_BY_OTHER:
                if(mAlertViewByOther != null  && !mAlertViewByOther.isWinowShow()){
                        mAlertViewByOther.showWindowView();
                }    
                break;
			case MSG_HIDE_ALERT_WINDOW:
				if(mAlertViewByLock != null && mAlertViewByLock.isWindowShow()){
                    mAlertViewByLock.hideWindowView();
				}
				if(mAlertViewByOther != null && mAlertViewByOther.isWinowShow()){
                    mAlertViewByOther.hideWindowView();
				}
				break;
			case MSG_TTS_UPDATE_TIME:
				ttsHandler.removeMessages(MSG_TTS_UPDATE_TIME);
				updateRecordingTime();
				break;
			default:
				break;
			}
		};
	};
	
    protected interface OnStorageUpdateDoneListener {
        public void onStorageUpdateDone(long bytes);
    }
}
