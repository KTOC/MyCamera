package com.example.test;



import com.example.test.callback.TextureViewCallback;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;

public class MyTouchTextureView extends TextureView implements View.OnTouchListener{

	public TextureViewCallback mTextureViewCallback;
	private float mCurrentX;
	private float mCurrentY;
	private float mLastX;
	private float mLastY;
	private float mDownX;
	private float mDownY;
	private final long LONG_CLICK_CHECK_TIME = 600;
	private final int MOVE_MIN_LIMIT_LENGTH = 20;
	private final int MOVE_MIN_LIMIT_SPEED = 100;
	private int mMoveLimit;
	private boolean mTouchResultFlag = false;
	private long mDownTime;
	private boolean mMoveFlag = false;
	private static final int MOVE_FLAG_CLICK = 1;
	private static final int MOVE_FLAG_LONGCLICK = 2;	
	private static final int MOVE_FLAG_UP = 3;
	private static final int MOVE_FLAG_DOWN = 4;
	private static final int MOVE_FLAG_LEFT = 5;
	private static final int MOVE_FLAG_RIGHT = 6;
	private VelocityTracker mVelocityTracker;
	private float mMoveX;
	private float mMoveY;
	private float mMoveSpeedX;
	private float mMoveSpeedY;
	private int mFinalEventNum = 0;
	private final String TAG = "TestTouch";
	
	public MyTouchTextureView(Context context) {
		this(context,null);
	}
	
	public MyTouchTextureView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}


	public MyTouchTextureView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mMoveLimit = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		this.setOnTouchListener(this);
	}

	public void registerCallback(TextureViewCallback callback){
		mTextureViewCallback = callback;
	}
	
	public void unRegisterCallback(){
		mTextureViewCallback = null;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mCurrentX = event.getX();
		mCurrentY = event.getY();
		if(mTextureViewCallback == null){
			return false;
		}
		
		if(mVelocityTracker == null){
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mDownTime = SystemClock.uptimeMillis();
			mMoveFlag = false;
			mTouchResultFlag = true;
			mDownX = mCurrentX;
			mDownY = mCurrentY;
			break;
			
		case MotionEvent.ACTION_MOVE:
			//mMoveFlag = true;
			mTouchResultFlag = true;
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			mVelocityTracker.computeCurrentVelocity(1000);  //���ǻ�ȡ���� ˮƽ������ٶȡ�����ָ���һ�����ʱ���ٶ�����������֮Ϊ����
			mMoveX = mCurrentX-mDownX;
			mMoveY = mCurrentY-mDownY;
			mMoveSpeedX = mVelocityTracker.getXVelocity();
			mMoveSpeedY = mVelocityTracker.getYVelocity();
			if(Math.abs(mMoveX) >= MOVE_MIN_LIMIT_LENGTH || Math.abs(mMoveY)>= MOVE_MIN_LIMIT_LENGTH){ //���ȸ���λ��
				mMoveFlag = true;
				if(Math.abs(mMoveX) >= Math.abs(mMoveY)){
					if(mMoveX >= 0){
						mFinalEventNum = MOVE_FLAG_RIGHT;
					}else{
						mFinalEventNum = MOVE_FLAG_LEFT;
					}
				}else if(Math.abs(mMoveX) < Math.abs(mMoveY)){
					if(mMoveY >= 0){
						mFinalEventNum = MOVE_FLAG_DOWN;
					}else{
						mFinalEventNum = MOVE_FLAG_UP;
					}
				}
			}else{  //�����ٶ��ж�
				if(Math.abs(mMoveSpeedX) >= MOVE_MIN_LIMIT_SPEED || Math.abs(mMoveSpeedY) >= MOVE_MIN_LIMIT_SPEED){
					mMoveFlag = true;
					if(Math.abs(mMoveSpeedX)>= Math.abs(mMoveSpeedY)){
						if(mMoveSpeedX >= 0){
							mFinalEventNum = MOVE_FLAG_RIGHT;
						}else{
							mFinalEventNum = MOVE_FLAG_LEFT;
						}
					}else{
						if(mMoveSpeedY >= 0){
							mFinalEventNum = MOVE_FLAG_DOWN;
						}else{
							mFinalEventNum = MOVE_FLAG_UP;
						}
					}
				}else{
					if(SystemClock.uptimeMillis() - mDownTime >= LONG_CLICK_CHECK_TIME){
						mFinalEventNum = MOVE_FLAG_LONGCLICK;
					}else{
						mFinalEventNum = MOVE_FLAG_CLICK;
					}
				}
				
			}
			
			dispatchMyTouchEvent(mFinalEventNum);
			
			break;
			
		default:
			break;
		}		
		
		mLastX = mCurrentX;
		mLastY = mCurrentY;
		return true;
	}

	private void dispatchMyTouchEvent(int finalEventNum){
		switch (finalEventNum) {
		case MOVE_FLAG_UP:
			if(mTextureViewCallback != null){
				mTextureViewCallback.onUpSlip();
			}else{
				Log.e(TAG, "callback is null");
			}
			break;
		case MOVE_FLAG_DOWN:
			if(mTextureViewCallback != null){
				mTextureViewCallback.onDownSlip();
			}else{
				Log.e(TAG, "callback is null");
			}
			break;
		case MOVE_FLAG_LEFT:
			if(mTextureViewCallback != null){
				mTextureViewCallback.onLeftSlip();
			}else{
				Log.e(TAG, "callback is null");
			}
			break;
		case MOVE_FLAG_RIGHT:
			if(mTextureViewCallback != null){
				mTextureViewCallback.onRightSlip();
			}else{
				Log.e(TAG, "callback is null");
			}
			break;
		case MOVE_FLAG_CLICK:
			if(mTextureViewCallback != null){
				mTextureViewCallback.onClick();
			}else{
				Log.e(TAG, "callback is null");
			}
			break;
		case MOVE_FLAG_LONGCLICK:
			if(mTextureViewCallback != null){
				mTextureViewCallback.onLongClick();
			}else{
				Log.e(TAG, "callback is null");
			}
			break;
		default:
			break;
		}
	}

	public void release(){
		if(mVelocityTracker != null){
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}
	
}
