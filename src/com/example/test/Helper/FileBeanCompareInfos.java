package com.example.test.Helper;

import java.io.File;

public class FileBeanCompareInfos implements Comparable<FileBeanCompareInfos>{

	private String mPath;
	private int mIndex;
	private boolean mChecked;
	
	public FileBeanCompareInfos(String mPath, int mIndex, boolean mChecked) {
		super();
		this.mPath = mPath;
		this.mIndex = mIndex;
		this.mChecked = mChecked;
	}

	public String getmPath() {
		return mPath;
	}

	public void setmPath(String mPath) {
		this.mPath = mPath;
	}

	public int getmIndex() {
		return mIndex;
	}

	public void setmIndex(int mIndex) {
		this.mIndex = mIndex;
	}

	public boolean getmChecked() {
		return mChecked;
	}

	public void setmChecked(boolean mChecked) {
		this.mChecked = mChecked;
	}

	@Override
	public int compareTo(FileBeanCompareInfos another) {
		if(this.mIndex > another.mIndex){
			return 2;
		}else if(this.mIndex < another.mIndex){
			return -2;
		}else{
			File thisFile = new File(this.getmPath());
			File anotherFile = new File(another.getmPath());
			if(thisFile.lastModified() > anotherFile.lastModified()){
				return 1;
			}else if(thisFile.lastModified() < anotherFile.lastModified()){
				return -1;
			}else{
				return 0;
			}
		}
	}

}
