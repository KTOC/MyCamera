package com.example.test.callback;

public interface CameraActivityCallback {
	int getDisplayRotation();
	void enableShutter(boolean enable);
	void enableRecorder(boolean enable);
	void animationFlash();
	void updateRecordingTime(String text,long startTime,long durationTime);
	void updateActivityUi(boolean recording,int cameraId,boolean cameraRecordingStatus,boolean lock,String time);
	void finishActivity();
	void updateActivityLockState(boolean lock);
	void updateActivityBirdMode(boolean bird);
	void updateActivityPreviewMode(int mode);
}
