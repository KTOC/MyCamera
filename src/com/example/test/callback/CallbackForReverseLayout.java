package com.example.test.callback;

import android.graphics.SurfaceTexture;

public interface CallbackForReverseLayout {

	void setTexture(SurfaceTexture surface,int currentCameraId,boolean replace,int lastCameraId);
	void hideReverseWindow(int lastCameraId);
	
}
