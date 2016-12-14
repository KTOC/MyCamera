package com.example.test.Helper;

public class FileState {
	public String mPath;
	public boolean mLock;
	
	public FileState(String mPath, boolean mLock) {
		this.mPath = mPath;
		this.mLock = mLock;
	}

	public String getmPath() {
		return mPath;
	}
	
	public void setmPath(String mPath) {
		this.mPath = mPath;
	}
	
	public boolean ismLock() {
		return mLock;
	}
	
	public void setmLock(boolean mLock) {
		this.mLock = mLock;
	}
	
	
}
