package com.example.test.file;

import java.io.File;

import com.example.test.settings.CameraSettings;

import android.os.Environment;

public class MyCleanInvalidFilesRunnable implements Runnable{  //去掉非法的文件

	private String mParentPath;
	private File mDirFile;
	private InvalidFileFilter mInvalidFileFilter;
	
	public MyCleanInvalidFilesRunnable(String parentPath){
		mParentPath = parentPath;
	}
	
	@Override
	public void run() {
		if(mParentPath == null||(!Environment.MEDIA_MOUNTED.equals(Environment.getStorageState(new File(mParentPath)))))
			return;
		mParentPath = mParentPath+"/"+CameraSettings.DIR_ROOT;
		mDirFile = new File(mParentPath);
		mDirFile.mkdirs();
		for(File tmp: mDirFile.listFiles(mInvalidFileFilter)){
			if(tmp.exists() && (!isCurrentVideo(tmp))){
				tmp.delete();
			}
		}
		
	}
	
	
	private boolean isCurrentVideo(File file){
		long space =  System.currentTimeMillis()-file.lastModified();
		if(space >= 0 && space <= 30*1000){
			return true;
		}
		return false;
	}
}
