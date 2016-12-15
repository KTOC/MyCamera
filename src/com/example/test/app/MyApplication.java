package com.example.test.app;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import com.android.allwinnertech.libaw360.api.AW360API;
import com.example.test.BaseActivity;
import com.example.test.R;
import com.example.test.Helper.CameraMode;
import com.example.test.Helper.ToastUtils;
import com.example.test.settings.CameraSettings;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyApplication extends Application {

	public static final String TAG = "DrivingRecorder";
	private static Context mContext;
	private static SharedPreferences sharedPreferences;
	public final static String DEFAULT_VIDEO_DURATION = "10";
	public final static String DEFAULT_CLEAN_SIZE = "30";  //上锁视频的删除百分比设定
	public final static String REC_VIDEO_FILE_EXT = "mp4";
	public final static boolean DEFAULT_CLEAN_SIZE_STATUS = false; 
    public final static boolean DEFAULT_MIRROR_STATUS = false;  //是否开启镜像
	public final static boolean DEFAULT_POWER_ON_STATUS = true; //开机自启动
	public static boolean SERVICE_RUNNING_FLAG = false; //服务未起来时由静态广播监听倒车,起来后由服务接管
	public static boolean DEFAULT_CLEAN_LOCK_STATUS = false;
	public static boolean SHOW_PREVIEW_SIZE = false;
	public static boolean DEFAULT_CLEAN_TIME_STATUS = true;
	public static int DEFAULT_CLEAN_SPACE = 200;
	public static final String DEFAULT_PATH = "/mnt/usbhost/Storage02";
	public static final String KEY_LAST_PREVIEW_MODE = "preview_mode";
	
	public final static String CHANGE_STORAGE = "com.luyuan.drivingrecorder.changestorage"; //切换存储空间
	
	public static boolean showSizeSelect = false;
	
    public static int textureName = 1;
    
    private List<Activity> mActivitys = new ArrayList<Activity>();
    private MyActivityLifecycleCallback mMyActivityLifecycleCallback;
    
    
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		mContext = this;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		mMyActivityLifecycleCallback = new MyActivityLifecycleCallback();
		registerActivityLifecycleCallbacks(mMyActivityLifecycleCallback);
	}

	public static Context getContext() {
		return mContext;
	}

	public static int getSdkInt() {
		return android.os.Build.VERSION.SDK_INT;
	}

	public static String getProduct() {
		return android.os.Build.PRODUCT;
	}
	
	public static String getAvailableStorage() {  //may be null
		return getAvailableStorage0();
	}
	
	private static String getAvailableStorage0(){
		String result = null;
		StorageManager mStorageManager = (StorageManager) mContext
				.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] storageVolumes = mStorageManager.getVolumeList();

		for (int i = 0; i < storageVolumes.length; i++) {
			StorageVolume temp = storageVolumes[i];
			Log.i(TAG, "current is "+i+" ; path:"+temp.getPath()+" ; state: "+temp.getState()+" ; remove:" +temp.isRemovable());
			if (MyApplication.isSdcardPrepared(temp.getState())
					&& temp.isRemovable()) {
				result = temp.getPath();
				break;
			}
		}
		return result;
	}
	
	private static String getAvailableStorage1(){
		return "/mnt/usbhost/Storage02";
	}
	
	
	public static String getCurrentStorage() {   //1.可用路径  2. null---表示当前无可用磁盘
		String result = sharedPreferences.getString("storage_list",
				DEFAULT_PATH);
		if(!isExternalStorageMounted(result)){
			result = changeCurrentStorage();
			if(result != null){
				sharedPreferences.edit().putString("storage_list", result).commit();
			}
		}
		return result;
	}
	
	public static String getCurrentStorageWithDefault(){   //供设置用的
		return sharedPreferences.getString("storage_list",
				DEFAULT_PATH);
	}
	
	public static boolean isExternalStorageMounted(String path){
		return isExternalStorageMounted0(path);
	} 
	
	public static boolean isExternalStorageMounted0(String path){
		if(path!= null && Environment.MEDIA_MOUNTED.equals(Environment.getStorageState(new File(path)))){
			return true;
		}else{
			return false;
		}
	}
	
	public static boolean isExternalStorageMounted1(String path){
		return true;
	}
	
	public static String changeCurrentStorage(){
		String temp = getAvailableStorage();
		if(temp == null){
			ToastUtils.showShortToast(mContext, R.string.no_available_storage);
		}else{
			ToastUtils.showShortToast(mContext, R.string.change_storage);
		}
		return temp;
	}

	public static void generateDirRoot(String parentPath) {
		if (parentPath == null) {
			return ;
		}
		String path;
		File file;
		
		path = parentPath +CameraSettings.DIR_LOCK;
		file = new File(path);
		if (!file.exists()|| !file.isDirectory()) {
			file.mkdirs();
		}
		
		path = parentPath +CameraSettings.DIR_USB;
		file = new File(path);
		if (!file.exists()||!file.isDirectory()) {
			file.mkdirs();
		}
		
		path = parentPath +CameraSettings.DIR_CVBS;
		file = new File(path);
		if (!file.exists()||!file.isDirectory()) {
			file.mkdirs();
		}
		
		path = parentPath +CameraSettings.DIR_360;
		file = new File(path);
		if (!file.exists()||!file.isDirectory()) {
			file.mkdirs();
		}
		
		path = parentPath +CameraSettings.DIR_PIC;
		file = new File(path);
		if (!file.exists()||!file.isDirectory()) {
			file.mkdirs();
		}
	
		path = parentPath +"/"+ CameraSettings.DIR_ROOT+"/.nomedia";
		file = new File(path);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static boolean isSdcardPrepared(String state) {
		if(state != null && state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
		}
	}
	
	public static long getTotalSpace(String path) {
		String root = path;
		long result = 0;
		if (root == null) {
			return result;
		}
		try{
			StatFs sf = new StatFs(root);
			long blockSize = sf.getBlockSizeLong();
			long blockCount = sf.getBlockCountLong();
			result =  (blockSize * blockCount) >> 20;
		}catch(Exception e){
            Log.i(TAG, "Fail to access external storage", e);
		}
		Log.w(TAG, "getTotalSpace "+result);
		return result;
	}
	
	public static long getFreeSpace(String path) {
		String root = path;
		long result = 0;
		try{
			StatFs sf = new StatFs(root);
			long blockSize = sf.getBlockSizeLong();
			long availCount = sf.getAvailableBlocksLong();
	
			result = (blockSize * availCount)>>20;
		}catch(Exception e){
            Log.i(TAG, "Fail to access external storage", e);
		}
		Log.w(TAG, "getFreeSpace "+result);
		return result;
	}
	
	public static long getVideoSpace() {  // x/1024/1024 = x << 20

		return /*getMaxDuration() / 60000 **/ 100<<20;
	}

	public static int getMaxDuration() {
		String value = sharedPreferences.getString("video_duration",
				DEFAULT_VIDEO_DURATION);
		return Integer.parseInt(value) * 60 * 1000;
	}
	
	public static int getLastPreviewMode(){
		return sharedPreferences.getInt(KEY_LAST_PREVIEW_MODE, CameraMode.MODE_PREVIEW_CA);
	}
	
	public static void setCurrentPreviewMode(int mode){
		sharedPreferences.edit().putInt(KEY_LAST_PREVIEW_MODE, mode).commit();
	}

    public static boolean getMirror() {
        return sharedPreferences.getBoolean("mirror_image", DEFAULT_MIRROR_STATUS);
    }
	public static boolean getPowerOn() {
		return sharedPreferences.getBoolean("power_on", DEFAULT_POWER_ON_STATUS);
	}

	public static boolean getCleanSizeStatus() {
		return sharedPreferences.getBoolean("clean_size", DEFAULT_CLEAN_SIZE_STATUS);
	}

	public static int getCleanSizeSetting() {
		String string = sharedPreferences.getString("clean_size_setting",
				DEFAULT_CLEAN_SIZE);
		return Integer.parseInt(string);
	}

	public static boolean getDeleteLockFiles(){
		return sharedPreferences.getBoolean("clean_size", DEFAULT_CLEAN_SIZE_STATUS);
	}
	
	public static void setDeleteLockFiles(boolean delete){
		sharedPreferences.edit().putBoolean("clean_size", delete).commit();
	}
	
	public static void saveSurfaceSize(int width,int height) {
		Editor editor =sharedPreferences.edit();
		editor.putInt("surface_width", width);
		editor.putInt("surface_height", height);
		editor.commit();
	}
	
	public static long getFileCreateTime(File file) {
		String name = file.getName();
		int spt = name.indexOf("-");
		Long c_time;
		try {
            String create_time = name.substring(0, spt)
                    + name.substring(spt + 1, spt + 7);
			c_time = Long.parseLong(create_time);
		} catch (Exception e) {
			// TODO: handle exception
			c_time = file.lastModified();
		}

		return c_time;
	}
	
	public static String getStorageDescription(String path) {
		StorageManager mStorageManager = (StorageManager) mContext
				.getSystemService(Context.STORAGE_SERVICE);
		StorageVolume[] storageVolumes = mStorageManager.getVolumeList();

		for (int i = 0; i < storageVolumes.length; i++) {
			StorageVolume storageVolume = storageVolumes[i];
			Log.i(TAG, "current is "+i+" ; path:"+storageVolume.getPath()+" ; state: "+storageVolume.getState());
			if (storageVolume.getPath().equals(path)) {
				return storageVolume.getDescription(mContext);
			}
		}
		return "";
	}
	
    /**
     * 添加activity到运行activity列表
     * activity列表用于管理运行时activity。
     * 以及推出应用
     *
     * @param activity
     */
    public void addActivity(BaseActivity activity) {
        if (!mActivitys.contains(activity)) {
            mActivitys.add(activity);
        }
    }

    /**
     * 退出应用
     */
    public void finishAllActivities() {
        Log.i(TAG, "--------exitApp");
        for (int i = 0; i < mActivitys.size(); i++) {
            mActivitys.get(i).finish();
        }

        mActivitys.clear();
    }
    
    private void pushActivity(Activity activity){
    	if(activity instanceof BaseActivity){
			mActivitys.add(activity);
			Log.w(TAG, "add one activity,current size is "+mActivitys.size());
		}else{
			Log.w(TAG, "add,not child of baseActivity!");
		}
    }
    
    private void popActivity(Activity activity){
    	if(activity instanceof BaseActivity){
			mActivitys.remove(activity);
			Log.w(TAG, "remove one activity,current size is "+mActivitys.size());
		}else{
			Log.w(TAG, "remove,not child of baseActivity!");
		}
    }
    
    @Override
    public void onTerminate() {
    	super.onTerminate();
    	unregisterActivityLifecycleCallbacks(mMyActivityLifecycleCallback);
    }
    
    private class MyActivityLifecycleCallback implements ActivityLifecycleCallbacks{

		@Override
		public void onActivityCreated(Activity activity,
				Bundle savedInstanceState) {
			pushActivity(activity);
		}

		@Override
		public void onActivityStarted(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityResumed(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityPaused(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityStopped(Activity activity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivitySaveInstanceState(Activity activity,
				Bundle outState) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onActivityDestroyed(Activity activity) {
			popActivity(activity);
		}
    	
    }

	
}
