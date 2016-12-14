package com.luyuan.drivingrecorder;

public class CameraDetect {
	
	private static final String TAG = "CameraDetect";
	
    static {
        try {
            System.loadLibrary("cameradetect");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }
    
	public native int isUvcCameraConnected(); //-1: not connect, 1: connected.
}
