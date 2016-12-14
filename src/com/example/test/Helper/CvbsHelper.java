package com.example.test.Helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

public class CvbsHelper {

	private static final String TAG = "LY_CAMERA_CVBS_HELP";
	private static final int CVBS_INT_STATE_USED_BY_KERNEL_START = 1;
	private static final int CVBS_INT_STATE_USED_BY_KERNEL_STOP = 0;
	
	private static final String CVBS_STR_STATE_USED_BY_KERNEL_START = "start";  //表示内核倒车正在使用cvbs
	private static final String CVBS_STR_STATE_USED_BY_KERNEL_STOP = "stop";    //表示内核倒车停止使用了
	private static final String KERNEL_STATE_NODE = "/sys/class/car_reverse/status";
	private static final String KERNEL_EXIT_NODE = "/sys/class/car_reverse/needexit";
	
	public static int getCvbsStateUsedByKernel(){   
		String modeStr = null;
		BufferedReader modeReader = null;

		try {
			modeReader = new BufferedReader(new FileReader(KERNEL_STATE_NODE));
			while ((modeStr = modeReader.readLine()) != null) {
				break;
			}
		} catch (Throwable t) {
			Log.e(TAG, "getMode() failed", t);
		} finally {
			if (modeReader != null) {
				try {
					modeReader.close();
				} catch (Throwable tt) {
				}
			}
		}

		Log.e(TAG, "getMode() mode=" + modeStr);
		if(CVBS_STR_STATE_USED_BY_KERNEL_START.equals(modeReader)){
			return CVBS_INT_STATE_USED_BY_KERNEL_START;
		}else{
			return CVBS_INT_STATE_USED_BY_KERNEL_STOP;
		}
	}
	
	public static void controlKernelExitFlag(){   //让内核倒车不再响应倒车事件
		FileWriter mFileWriter = null;
		try{
			mFileWriter = new FileWriter(KERNEL_EXIT_NODE);
			mFileWriter.write("1");
			mFileWriter.flush();
			Thread.sleep(500);
		}catch(Exception e){
			Log.e(TAG, "set exit flag failed!");
		}finally{
			if(mFileWriter != null){
				try {
					mFileWriter.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
}
