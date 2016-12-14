package com.example.test.Helper;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.view.Surface;

public class CameraUtils {
    public static int getDisplayRotation(Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
        case Surface.ROTATION_0: return 0;
        case Surface.ROTATION_90: return 90;
        case Surface.ROTATION_180: return 180;
        case Surface.ROTATION_270: return 270;
        }
        return 0;
    }
    
    public static int getDisplayOrientation(int degrees, int cameraId) throws RuntimeException {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }
    
	public static boolean isSupportVideoSizes(CamcorderProfile profile,List<Size> sizes){
		if(sizes == null || profile == null) return false;
		if(sizes != null){			
			Iterator<Size> iterator = sizes.iterator();
			Size tmp;
			while(iterator.hasNext()){
				tmp = iterator.next();
				if(tmp.width == profile.videoFrameWidth && tmp.height == profile.videoFrameHeight){					
					return true;
				}
			}
		}
		return false;
	}
}
