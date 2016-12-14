package com.example.test.receiver;

import com.example.test.app.MyApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver{

    private final static String BROADCAST_BACK_CAR = "com.luyuan.mcu.back.car";
    private final static String KEY_REVERSE_STATE = "isBackCar";
    private final static String ACTION_START_SERVICE = "com.luyuan.recorder.bg";
    private final static String PACKAGE_NAME = "com.example.test";
    private static final String TAG = "LY_CAMERA";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    public final static String  ACTION_LY_MCUSERVICE_ACC_STATUS = "com.luyuan.mcuservice.accstatus";
    private static final String KEY_ACC_STATUS = "accstatus";
    
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent == null) return;
		Log.w(TAG, "bootreceiver action:"+intent.getAction());
		boolean reverse = intent.getBooleanExtra(KEY_REVERSE_STATE, false);
		if(BROADCAST_BACK_CAR.equals(intent.getAction()) || ACTION_BOOT.equals(intent.getAction())){
			if(!MyApplication.SERVICE_RUNNING_FLAG){
				startService(context,reverse);
			}else{
				Log.i(TAG, "receiver reverse or boot broadcast,service is running yet");
			}
		}else if(ACTION_LY_MCUSERVICE_ACC_STATUS.equals(intent.getAction())){
			int flag = intent.getIntExtra(KEY_ACC_STATUS, 0);
			if(flag == 1 && !MyApplication.SERVICE_RUNNING_FLAG){
				startService(context,reverse);
			}
		}
	}
	
	private void startService(Context context,boolean reverse){
		Intent intent = new Intent(ACTION_START_SERVICE);
		intent.setPackage(PACKAGE_NAME);
		intent.putExtra(KEY_REVERSE_STATE, reverse);
		context.startService(intent);
	}

}
