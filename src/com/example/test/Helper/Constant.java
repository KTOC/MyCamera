package com.example.test.Helper;

import com.example.test.app.MyApplication;
import com.example.test.settings.CameraSettings;

public class Constant {
	public static final String TAG_LOCK = "lock";
	public static final String TAG_USB = "usb";
	public static final String TAG_CVBS = "cvbs";
	public static final String TAG_360 = "360";
	public static final String TAG_PIC = "pic";
	
	public static String tagToPath(String tag){
		String path = null;
		if(tag.equals(TAG_USB)){
			path = CameraSettings.DIR_USB;
		}else if(tag.equals(TAG_CVBS)){
			path = CameraSettings.DIR_CVBS;
		}else if(tag.equals(TAG_360)){
			path = CameraSettings.DIR_360;
		}else if(tag.equals(TAG_PIC)){
			path = CameraSettings.DIR_PIC;
		}else{
			path = CameraSettings.DIR_LOCK;
		} 
		return path;
	}
}
