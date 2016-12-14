package com.example.test.callback;

import android.graphics.SurfaceTexture;

public interface CameraServiceCallback {
	void openCameras();
	void startVideoRecording();
	void stopVideoRecording();
	void takePictures();
	SurfaceTexture getSurfaceTextures(int cameraId);
	void closeCameras();
	void registerCallback(CameraActivityCallback callback);
	void unRegisterCallback();
	void postSurfaceTexture0(SurfaceTexture surfaceTexture);
	void postSurfaceTexture1(SurfaceTexture surfaceTexture);
}
