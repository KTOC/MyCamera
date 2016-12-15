package com.example.test.view;

import java.text.DecimalFormat;

import com.example.test.R;
import com.example.test.R.id;
import com.example.test.R.string;
import com.example.test.callback.CallbackForUpdateSpace;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter.ViewBinder;

public class AlertViewByOther extends LinearLayout implements CallbackForUpdateSpace{
	
	private Button mButtonOk;
	private Button mButtonJump;
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLParams;
	private int w = 0;
	private int h = 0;
	private Context mContext;
	private ButtonClickEvents mButtonClickEvents;
	private CheckChangeEvents mCheckChangeEvents;
	private boolean isShow = false;
	private int screenHeight = 0;
	private int screenWidth = 0;
	private DisplayMetrics mDisplayMetrics;
    private View mAlertOtherUpdateSpaceLayout;
    private TextView mLockTextView;
    private TextView mCommonTextView;
    private TextView mOtherTextView;
    private TextView mSpaceTextView;
    private ProgressView mProgressView;
    private TextView mTextViewTips1;
    private TextView mTextViewTips2;
    private Bundle mUpdateBundle;
    private Message mUpdateMsg;
    private static final int UPDATE_TEXT = 3;
    private static final String KEY_LOCK = "lock";
    private static final String KEY_VID = "vid";
    private static final String KEY_OTHER = "other";
    private static final String KEY_SPACE = "space";
    private static final String KEY_PATH_NAME = "pathName";
    private static final String KEY_RATE = "rate";
    private DecimalFormat mDecimalFormat;
        
    private Handler mHandler = new Handler(){
            private Bundle temp;
            
            public void handleMessage(android.os.Message msg) {     
                    switch (msg.what) {
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

	public AlertViewByOther(Context context) {
			this(context,null);
	}

	public AlertViewByOther(Context context, AttributeSet attrs) {
			this(context, attrs,0);		
	}
	
	public AlertViewByOther(Context context, AttributeSet attrs,int defStyleAttr) {
			super(context, attrs, defStyleAttr);
			mContext = context;
			mWindowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
			w = 600;
			h = 390;
			mDisplayMetrics = new DisplayMetrics();
			mWindowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);
			screenHeight = mDisplayMetrics.heightPixels;
			screenWidth = mDisplayMetrics.widthPixels;
			mLParams = new WindowManager.LayoutParams(w, h, (int)((screenWidth-w)*0.5), (int)((screenHeight-h)*0.35), 
																	WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, 
																	WindowManager.LayoutParams.FLAG_FULLSCREEN| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM,
																	PixelFormat.TRANSLUCENT);
			mLParams.gravity = Gravity.START|Gravity.TOP;
			mButtonClickEvents = new ButtonClickEvents();
			mCheckChangeEvents = new CheckChangeEvents();
			mDecimalFormat = new DecimalFormat("#.##");
	}
	
	@Override
	protected void onFinishInflate() {
			super.onFinishInflate();
			mButtonOk = (Button)findViewById(R.id.btn_info_ok_other);
			mButtonJump = (Button)findViewById(R.id.btn_enter_folder_other);
			mAlertOtherUpdateSpaceLayout = (View)findViewById(R.id.alert_other_update_space_layout);
            mLockTextView = (TextView)mAlertOtherUpdateSpaceLayout.findViewById(R.id.tv_lockSize);
            mCommonTextView = (TextView)mAlertOtherUpdateSpaceLayout.findViewById(R.id.tv_vidSize);
            mOtherTextView = (TextView)mAlertOtherUpdateSpaceLayout.findViewById(R.id.tv_otherSize);
            mSpaceTextView = (TextView)mAlertOtherUpdateSpaceLayout.findViewById(R.id.tv_spaceSize);
            mProgressView = (ProgressView)mAlertOtherUpdateSpaceLayout.findViewById(R.id.progressView);
			mTextViewTips1 = (TextView)findViewById(R.id.tv_tips_other1);
			mTextViewTips2 = (TextView)findViewById(R.id.tv_tips_other2);
            
			mButtonOk.setOnClickListener(mButtonClickEvents);
			mButtonJump.setOnClickListener(mButtonClickEvents);
			mProgressView.setmCallbackForUpdateSpace(this);
	}
	
	public  void showWindowView(){
			if(mWindowManager != null && mLParams != null){										
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
	
	public boolean isWinowShow(){
			return isShow;
	}
	
	private class ButtonClickEvents implements View.OnClickListener{
			@Override
			public void onClick(View v) {
					switch (v.getId()) {
					case R.id.btn_info_ok_other:
						hideWindowView();
						break;
					case R.id.btn_enter_folder_other:
						hideWindowView();
						jumpToFolder();
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
				default:
					break;
				}
			
		}
		
	}
	
	private void jumpToFolder(){

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
