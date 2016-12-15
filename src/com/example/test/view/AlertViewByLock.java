package com.example.test.view;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.android.internal.R.style;
import com.example.test.CameraMainService;
import com.example.test.R;
import com.example.test.R.id;
import com.example.test.R.string;
import com.example.test.app.MyApplication;
import com.example.test.callback.CallbackForAlertView;
import com.example.test.callback.CallbackForUpdateSpace;
import com.example.test.settings.CameraSettings;

import android.R.anim;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AlertViewByLock extends FrameLayout implements CallbackForUpdateSpace{

	private CheckBox mCheckBoxAutoDelete;
	private Button mButtonOk;
	private Button mButtonJump;
	private Button mButtonDelete;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLParams;
	private int w = 0;
	private int h = 0;
	private Context mContext;
	private ButtonClickEvents mButtonClickEvents;
	private CheckChangeEvents mCheckChangeEvents;
	private AlertDialog.Builder builder;
	private AlertDialog  mAlertDialog;
	private AlertDialog.Builder builderJump;
	private AlertDialog  mAlertDialogJump;
	private AlertDialog.Builder builderDelete;
	private AlertDialog mAlertDialogDelete;
	private TextView mTextView;
	private ProgressBar mProgressBar;
	private RelativeLayout mRelativeLayout;
	private LinearLayout mLinearLayout;
	private CallbackForAlertView mCallbackForAlertView;
	private View mAlertLockUpdateSpaceLayout;
	private TextView mLockTextView;
	private TextView mCommonTextView;
	private TextView mOtherTextView;
	private TextView mSpaceTextView;
	private ProgressView mProgressView;
	private Bundle mUpdateBundle;
	private Message mUpdateMsg;
	private DecimalFormat mDecimalFormat;
	private TextView mTextViewTips1;
	private TextView mTextViewTips2;
	
	private boolean isShow = false;
	private int screenHeight = 0;
	private int screenWidth = 0;
	private DisplayMetrics mDisplayMetrics;
	private static final String KEY_EXTRA = "cmd_num";
	private static final String KEY_MAX_TIME = "max_time";
	private static final String KEY_LOCK = "lock";
    private static final String KEY_VID = "vid";
    private static final String KEY_OTHER = "other";
    private static final String KEY_SPACE = "space";
    private static final String KEY_PATH_NAME = "pathName";
    private static final String KEY_RATE = "rate";
	private String maxTime = "60000";
	private static final String ACTION_FLOAT = "android.intent.action.FROMFLOAT";
	private List<File> mFileList;
	private static final int DELETE_COMPLETE = 1;
	private static final int DELETE_START = 2;
	private static final int UPDATE_TEXT = 3;
	
	private Handler mHandler = new Handler(){
	        private Bundle temp;
	        
	        public void handleMessage(android.os.Message msg) {
	                switch (msg.what) {
                        case DELETE_COMPLETE:
                                if(mTextView != null){
                                        mTextView.setText("删除成功!");
                                }
                                changeLlAlpha(1, View.GONE);
                                hideWindowView();
                                if(mCallbackForAlertView != null)
                                        mCallbackForAlertView.nextStepAfterChecked();
                                break;
                        case DELETE_START:
                                if(mTextView != null){
                                        mTextView.setText("扫描结束,即将开始删除操作!");
                                }
                                break;
                         
                        case UPDATE_TEXT:
                                temp = msg.getData();
                                if(temp != null)
                                        updateText(temp.getDouble(KEY_LOCK, 0),temp.getDouble(KEY_VID, 0),temp.getDouble(KEY_OTHER, 0),temp.getDouble(KEY_SPACE, 0),temp.getString(KEY_PATH_NAME, "---"),temp.getInt(KEY_RATE, 20));
                                break;
                        default:
                                break;
                        }
	        };
	};
 //   private View mView;
	
	public AlertViewByLock(Context context) {
			this(context,null);
	}

        public AlertViewByLock(Context context, AttributeSet attrs) {
			this(context, attrs,0);		
	}
	
	public AlertViewByLock(Context context, AttributeSet attrs,int defStyleAttr) {
			super(context, attrs, defStyleAttr);	
			mContext = context;
			mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			w = 600;
			h = 440;
			mDisplayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
			screenHeight = mDisplayMetrics.heightPixels;
			screenWidth = mDisplayMetrics.widthPixels;
			mLParams = new WindowManager.LayoutParams(w, h, (int)((screenWidth-w)*0.5), (int)((screenHeight-h)*0.35), 
																	WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 
																	WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM ,
																	PixelFormat.TRANSLUCENT);
			mLParams.gravity = Gravity.START|Gravity.TOP;
			mButtonClickEvents = new ButtonClickEvents();
			mCheckChangeEvents = new CheckChangeEvents();
			mDecimalFormat = new DecimalFormat("#.##");
	}
	
	@Override
	protected void onFinishInflate() {
			super.onFinishInflate();
			mCheckBoxAutoDelete = (CheckBox)findViewById(R.id.cb_ifDelete_lock);
			mButtonOk = (Button)findViewById(R.id.btn_info_ok_lock);
			mButtonJump = (Button)findViewById(R.id.btn_enter_folder_lock);
			mButtonDelete = (Button)findViewById(R.id.btn_info_delete_all_lock);
			mTextView = (TextView)findViewById(R.id.tv_deleteVideo_info);
			mProgressBar = (ProgressBar)findViewById(R.id.pb_deleteVideo);
			mRelativeLayout = (RelativeLayout)findViewById(R.id.rl_pb_tv_layout);
			mLinearLayout = (LinearLayout)findViewById(R.id.ll_delete_layout);
			mAlertLockUpdateSpaceLayout = (View)findViewById(R.id.alert_lock_update_space_layout);
			mLockTextView = (TextView)mAlertLockUpdateSpaceLayout.findViewById(R.id.tv_lockSize);
			mCommonTextView = (TextView)mAlertLockUpdateSpaceLayout.findViewById(R.id.tv_vidSize);
			mOtherTextView = (TextView)mAlertLockUpdateSpaceLayout.findViewById(R.id.tv_otherSize);
			mSpaceTextView = (TextView)mAlertLockUpdateSpaceLayout.findViewById(R.id.tv_spaceSize);
			mProgressView = (ProgressView)mAlertLockUpdateSpaceLayout.findViewById(R.id.progressView);
			mTextViewTips1 = (TextView)findViewById(R.id.tv_tips1);
			mTextViewTips2 = (TextView)findViewById(R.id.tv_tips2);
			
			mCheckBoxAutoDelete.setOnCheckedChangeListener(mCheckChangeEvents);
			mButtonOk.setOnClickListener(mButtonClickEvents);
			mButtonJump.setOnClickListener(mButtonClickEvents);
			mButtonDelete.setOnClickListener(mButtonClickEvents);
			mProgressView.setmCallbackForUpdateSpace(this);
	}
	
	public  void showWindowView(){
			if(mWindowManager != null && mLParams != null){					
					mCheckBoxAutoDelete.setChecked(MyApplication.getDeleteLockFiles());
					mWindowManager.addView(this, mLParams);
					isShow = true;
			}
	}

	public void  hideWindowView(){
			if(mWindowManager != null && mLParams != null){
					mWindowManager.removeView(this);
					isShow = false;
			}
	}
	
	/*public void setView(View view){
			mView = view;
	}*/
	
	public void registerCallback(CallbackForAlertView callbackForAlertView){
	         this.mCallbackForAlertView = callbackForAlertView;
	}
	
	public void unRegisterCallback(){
	        this.mCallbackForAlertView = null;
	}
	
	public boolean isWindowShow(){
	        return isShow;
	}
	
	private class ButtonClickEvents implements View.OnClickListener{
			@Override
			public void onClick(View v) {
					switch (v.getId()) {
					case R.id.btn_info_ok_lock:
						if(MyApplication.getDeleteLockFiles()){
								showDialogFromOk();
						}else{
								hideWindowView();
						}
						break;
					case R.id.btn_enter_folder_lock:
						if(MyApplication.getDeleteLockFiles()){
								showDialogFromJump();
						}else{
								hideWindowView();
								jumpToFolder();
						}
						break;
					case R.id.btn_info_delete_all_lock:
					        showDialogFromDelete();
					        break;
					default:
						break;
					}
				
			}		
	}
	
	private class CheckChangeEvents implements OnCheckedChangeListener{
	
		@Override
		public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				switch (buttonView.getId()) {
				case R.id.cb_ifDelete_lock:
					MyApplication.setDeleteLockFiles(isChecked);
					break;
				default:
					break;
				}
			
		}
		
	}
	
	private void jumpToFolder(){
		
	}

	public void showDialogFromOk(){
		if(mAlertDialog == null){
				builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.dialog_alert_title);
				builder.setMessage(getResources().getString(R.string.delete_sure));
				builder.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {								
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							hideWindowView();
							if(mCallbackForAlertView != null)
							        mCallbackForAlertView.nextStepAfterChecked();
					}
				});
				builder.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
					}
				});
				mAlertDialog = builder.create();
				mAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				
		}			
		mAlertDialog.show();
	}
	
	public void showDialogFromJump(){
		if(mAlertDialogJump == null){
				builderJump = new AlertDialog.Builder(mContext);
				builderJump.setTitle(R.string.dialog_alert_title);
				builderJump.setMessage(getResources().getString(R.string.delete_sure));
				builderJump.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {								
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
							hideWindowView();
							if(mCallbackForAlertView != null)
							        mCallbackForAlertView.nextStepAfterChecked();
							jumpToFolder();
					}
				});
				builderJump.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
							arg0.dismiss();
					}
				});
				mAlertDialogJump = builderJump.create();
				mAlertDialogJump.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				
		}			
		mAlertDialogJump.show();
	}
	
	public void showDialogFromDelete(){
	        if(mAlertDialogDelete == null){
                        builderDelete = new AlertDialog.Builder(mContext);
                        builderDelete.setTitle(R.string.dialog_alert_title);
                        builderDelete.setMessage(getResources().getString(R.string.delete_all_sure));
                        builderDelete.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {                                                             
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                                arg0.dismiss();                                               
                                                startDeleteDirsRunnable();
                                }
                        });
                        builderDelete.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
                                
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                                arg0.dismiss();
                                }
                        });
                        mAlertDialogDelete = builderDelete.create();
                        mAlertDialogDelete.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        
	        }                       
	        mAlertDialogDelete.show();
	}
	
	private void startDeleteDirsRunnable(){
	        if(!MyApplication.isExternalStorageMounted(CameraMainService.getRootPath())) {
	                return;
	         };
	         
	        changeLlAlpha(0.4f,View.VISIBLE);
                          
	        new Thread(){
	                @Override
	                public void run() {
	                        deleteAllVideos();
	                }
	        }.start();
	}
	
	private void scanAllVideos(){
	        File dirU = new File(CameraMainService.getRootPath()+CameraSettings.DIR_USB);
	        dirU.mkdirs();
	        File dirC = new File(CameraMainService.getRootPath()+CameraSettings.DIR_CVBS);
	        dirC.mkdirs();
	        File dirG = new File(CameraMainService.getRootPath()+CameraSettings.DIR_360);
	        dirG.mkdirs();
	        File dirL = new File(CameraMainService.getRootPath()+CameraSettings.DIR_LOCK);
	        dirL.mkdirs();
	        
	        mFileList = new ArrayList<File>();
	        scanVideoByArg(dirU, mFileList);
	        scanVideoByArg(dirC, mFileList);
	        scanVideoByArg(dirG, mFileList);
	        scanVideoByArg(dirL, mFileList);
	}
	
	private void deleteAllVideos(){
	        File temp;
	        int size;	        
	        scanAllVideos();
	        size = mFileList.size();
	        //mProgressDialog.setMessage(new StringBuilder().append("检测到  ").append(size).append("  个文件,即将执行删除操作!"));
	        mHandler.sendEmptyMessage(DELETE_START);
	        
	        for(int i = 0;i < size;i++){
	                temp = mFileList.get(i);
	                if(temp != null && temp.exists() && temp.isFile()){
	                       // Log.w("CAM_delete", "删除了  "+temp.getAbsolutePath());
	                        temp.delete();
	                }
	                /*if((i*10) % size == 0){
	                        mProgressDialog.setMessage("已删除  "+i+"  个文件");
	                }else if(i == size-1){
	                        mProgressDialog.setMessage("删除完成!");
	                }*/
	        }
	        try {
                        Thread.sleep(500);
                } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
	       mHandler.sendEmptyMessage(DELETE_COMPLETE);
	}
	
	
	private void scanVideoByArg(File src,List<File> list){
	        if(src == null || !src.exists() || !src.isDirectory()) return;
	        File[] childList = src.listFiles();
	        if(childList == null) return;
	        for(File temp : src.listFiles()){
	                if(temp.isDirectory()){
	                        scanVideoByArg(temp, list);
	                }else if(temp.isFile()){
	                        list.add(temp);
	                }
	        }
	}
	
	private void changeLlAlpha(float des,int visble){
	        if(mLinearLayout != null){
	                mLinearLayout.setAlpha(des);
	        }
	        if(mRelativeLayout != null){
	                mRelativeLayout.setVisibility(visble);
	        }
	}

        @Override
        public void updateSpace(double lockSize, double vidSize,double otherSize, double spaceSize,String pathName,int rate) {
                mUpdateBundle = new Bundle();
                mUpdateBundle.putDouble(KEY_LOCK, lockSize);
                mUpdateBundle.putDouble(KEY_VID, vidSize);
                mUpdateBundle.putDouble(KEY_OTHER, otherSize);
                mUpdateBundle.putDouble(KEY_SPACE, spaceSize);
                mUpdateBundle.putString(KEY_PATH_NAME, pathName);
                mUpdateBundle.putInt(KEY_RATE, rate);
                mUpdateMsg = Message.obtain();
                mUpdateMsg.what = UPDATE_TEXT;
                mUpdateMsg.setData(mUpdateBundle);
                mHandler.sendMessage(mUpdateMsg);
        }	
        
        protected void updateText(double lockSize, double vidSize,double otherSize, double spaceSize,String pathName,int rate) {
                mLockTextView.setText(mDecimalFormat.format(lockSize)+"G");
                mCommonTextView.setText(mDecimalFormat.format(vidSize)+"G");
                mOtherTextView.setText(mDecimalFormat.format(otherSize)+"G");
                mSpaceTextView.setText(mDecimalFormat.format(spaceSize)+"G");
                String tip1 = String.format(mContext.getResources().getString(R.string.dir_first_str),rate);
                String tip2 = String.format(mContext.getResources().getString(R.string.dir_second_str), pathName);
                mTextViewTips1.setText(tip1);
                mTextViewTips2.setText(tip2);
        }
	
}
