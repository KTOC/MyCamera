package com.example.test.settings;

import com.example.test.Helper.CameraMode;

import android.content.SharedPreferences;
import android.os.SystemProperties;
import android.view.TextureView;

public class CameraSettings {
	
	public static final String KEY_CAMERA_MODE = "cameraMode"; 
	public static final String DIR_ROOT = "/LY_DVR";
	public static final String DIR_LOCK = DIR_ROOT+"/LOCK";
	public static final String DIR_USB = DIR_ROOT+"/USB";
	public static final String DIR_CVBS = DIR_ROOT+"/CVBS";
	public static final String DIR_360 = DIR_ROOT+"/360";
	public static final String DIR_PIC = DIR_ROOT+"/PIC";
	public static final String SUFFIX_TMP = ".tmp";
    public static final String RECORDING_HINT = "recording-hint";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String PROPERTY_SHOW_CAMERA_CONTROL_ICON = "persist.show.camera.mode"; 
    public static final String PROPERTY_TEST_LEFT_RIGHT_NODE = "persist.left.right.node";
	
	public static final String KEY_NEED_REVERSE = "isBackCar";
	public static final String KEY_NEED_RECORD = "record_flag";
	public static final int MAX_CAMERA_NUMBERS = 8;
	public static boolean LOG_SWITCH = true;
	public static long STORAGE_INVALID = 0;
	
	public static int getCurrentCameraMode(){
		int re = 0;
		String mode = SystemProperties.get(KEY_CAMERA_MODE,CameraMode.MODE_STR_360);
		if(mode.equals(CameraMode.MODE_STR_USB_CVBS)){
			re = CameraMode.MODE_INT_USB_CVBS;
		}else if(mode.equals(CameraMode.MODE_STR_USB_360)){
			re = CameraMode.MODE_INT_USB_360;
		}else{
			re = CameraMode.MODE_INT_360;
		}
		return re;
	}
	
	public static int getCurrentLeftRightNode(){
		return SystemProperties.getInt(PROPERTY_TEST_LEFT_RIGHT_NODE, 0);
	}
	
}
