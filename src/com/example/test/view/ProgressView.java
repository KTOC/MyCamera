package com.example.test.view;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.example.test.CameraMainService;
import com.example.test.R;
import com.example.test.Helper.FileSizeUtil;
import com.example.test.R.styleable;
import com.example.test.app.MyApplication;
import com.example.test.callback.CallbackForUpdateSpace;
import com.example.test.settings.CameraSettings;

import android.R.color;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class ProgressView extends View{
        private Context mContext;
        private float mDensity; 
        private int mLineWidth = 0;
        private int mLineHeight = 0;
        private int mSpaceColor;
        private int mLockColor;
        private int mVIDColor;
        private int mOtherColor;
        private double totalSize = 0.00; //GB,保留两位小数
        private double lockSize =0.00;
        private double vidSize =0.00;
        private double otherSize =0.00;
        private double spaceSize= 0.00;
        private double lockSizeRate;
        private double vidSizeRate;
        private double otherSizeRate;
        private double spaceSizeRate;
        private int mPadTop;
        private int mPadStart;
        private Paint lockPaint;
        private Paint vidPaint;
        private Paint otherPaint;
        private Paint spacePaint;
        private float lockStartX;
        private float vidStartX;
        private float otherStartX;
        private float spaceStartX;
        private Object drawLock = new Object();
        private StatFs mStatFs ;
        private BigDecimal mBigDecimal;
        private HandlerThread mHandlerThread;
        private Handler tHandler;
        private String THREAD_NAME = "OP_FILE";
        private FileRunnable mFileRunnable;
        private boolean canDraw = false;
        private CallbackForUpdateSpace mCallbackForUpdateSpace;
        private String mRootPath;

        public ProgressView(Context context) {
                this(context,null);
        }

        public ProgressView(Context context, AttributeSet attrs) {
                this(context, attrs,0);
        }

        public ProgressView(Context context, AttributeSet attrs,int defStyleAttr) {
                super(context, attrs, defStyleAttr);
                mContext = context;
                mDensity = context.getResources().getDisplayMetrics().density ;
                TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.ProgressView);
                int length = ta.length();
                int index = 0;
                for(int i = 0; i < length;i++){
                        index = ta.getIndex(i);
                        switch (index) {
                        case R.styleable.ProgressView_LineWidth:
                                mLineWidth = (int)(ta.getInt(index, 500) * mDensity);
                                break;
                        case R.styleable.ProgressView_LineHeight:
                                mLineHeight = (int)(ta.getInt(index, 20) * mDensity);
                                break;
                        case R.styleable.ProgressView_LockColor:
                                mLockColor = ta.getColor(index, mContext.getResources().getColor(R.color.myRed));
                                break;
                        case R.styleable.ProgressView_VIDColor:
                                mVIDColor = ta.getColor(index, mContext.getResources().getColor(R.color.myBlue));
                                break;
                        case R.styleable.ProgressView_OtherColor:
                                mOtherColor = ta.getColor(index, mContext.getResources().getColor(R.color.myYellow));
                                break;
                        case R.styleable.ProgressView_SpcaeColor:
                                mSpaceColor = ta.getColor(index, mContext.getResources().getColor(R.color.myWhite));
                                break;
                        default:
                                break;
                        }
                }
                
                ta.recycle();
                
                initPaint();
                initSizeRate(0f,0f,0f,1f);                
                mHandlerThread = new HandlerThread(THREAD_NAME);
                mHandlerThread.start();
                tHandler = new Handler(mHandlerThread.getLooper());
                mFileRunnable = new FileRunnable();
        }       
              
        public CallbackForUpdateSpace getmCallbackForUpdateSpace() {
                return mCallbackForUpdateSpace;
        }

        public void setmCallbackForUpdateSpace(CallbackForUpdateSpace mCallbackForUpdateSpace) {   
        	this.mCallbackForUpdateSpace = mCallbackForUpdateSpace;
        }

        private void initPaint() {
                lockPaint = new Paint();
                lockPaint.setStyle(Style.FILL);
                lockPaint.setColor(mLockColor);
                
                vidPaint = new Paint();
                vidPaint.setStyle(Style.FILL);
                vidPaint.setColor(mVIDColor);
                
                otherPaint = new Paint();
                otherPaint.setStyle(Style.FILL);
                otherPaint.setColor(mOtherColor);
                
                spacePaint = new Paint();
                spacePaint.setStyle(Style.FILL);
                spacePaint.setColor(mSpaceColor);
        }

        private void initSizeRate(double lockRate,double vidRate,double otherRate,double spaceRate){
                Log.w("drawRate", "lock:vid:other:space = "+lockRate+" : "+vidRate+" : "+otherRate+" : "+spaceRate);
                synchronized (drawLock) {
                        lockSizeRate = lockRate;
                        vidSizeRate = vidRate;
                        otherSizeRate = otherRate;
                        spaceSizeRate = spaceRate;
                }              
        }
        
        private void drawColor(Canvas canvas){                
                lockStartX = mPadStart;
                vidStartX = mPadStart+(float)(lockSizeRate * mLineWidth);
                otherStartX = mPadStart+(float)((lockSizeRate+vidSizeRate) * mLineWidth);
                spaceStartX = mPadStart+(float)((lockSizeRate+vidSizeRate+otherSizeRate) * mLineWidth);
                canvas.save();
                canvas.drawRect(lockStartX, mPadTop, vidStartX, mPadTop+mLineHeight,lockPaint);
                //canvas.restore();
                
                //canvas.save();
                canvas.drawRect(vidStartX, mPadTop, otherStartX, mPadTop+mLineHeight,vidPaint);
               // canvas.restore();
                
                //canvas.save();
                canvas.drawRect(otherStartX, mPadTop, spaceStartX, mPadTop+mLineHeight,otherPaint);
                //canvas.restore();
                
                //canvas.save();
                canvas.drawRect(spaceStartX, mPadTop, mPadStart+mLineWidth, mPadTop+mLineHeight,spacePaint);
                canvas.restore();
        }
        
        @Override
        protected void onLayout(boolean changed, int left, int top, int right,int bottom) {
                mPadTop = getPaddingTop();
                mPadStart = getPaddingStart();
                if(mLineHeight > getHeight() - mPadTop) mLineHeight = getHeight() - mPadTop;
                if(mLineWidth > getWidth() - mPadStart) mLineWidth = getWidth() - mPadStart;
                super.onLayout(changed, left, top, right, bottom);
        }
        
        @Override
        protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                canDraw = true;
                tHandler.removeCallbacksAndMessages(null);
                tHandler.postDelayed(mFileRunnable,100);
        }

        @Override
        protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                canDraw = false;
                tHandler.removeCallbacksAndMessages(null);
        }      

        @Override
        protected  void onDraw(Canvas canvas) {
                synchronized (drawLock) {
                        drawColor(canvas);  
                }                            
        }
        
        private class  FileRunnable implements Runnable{
                
                @Override
                public void run() {
                	 	mRootPath = CameraMainService.getRootPath();
                        if(!MyApplication.isExternalStorageMounted(mRootPath))
                                return;
                        totalSize = long2Double(getTotalSize(mRootPath));
                        spaceSize = long2Double(getSpaceSize(mRootPath));
                        lockSize = FileSizeUtil.getFileOrFilesSize(mRootPath+CameraSettings.DIR_LOCK, FileSizeUtil.SIZETYPE_GB);
                        vidSize = FileSizeUtil.getFileOrFilesSize(mRootPath+CameraSettings.DIR_USB, FileSizeUtil.SIZETYPE_GB)+
                                	FileSizeUtil.getFileOrFilesSize(mRootPath+CameraSettings.DIR_CVBS, FileSizeUtil.SIZETYPE_GB)+
                                	FileSizeUtil.getFileOrFilesSize(mRootPath+CameraSettings.DIR_360, FileSizeUtil.SIZETYPE_GB);
                        otherSize = totalSize - spaceSize -lockSize - vidSize;
                        if(otherSize < 0) otherSize = 0;
                        initSizeRate(lockSize/totalSize, vidSize/totalSize, otherSize/totalSize, spaceSize/totalSize);
                        if(canDraw){
                                Log.w("drawRate", "----"+canDraw+"   total:lock:vid:other:space = "+totalSize+":"+lockSize+":"+vidSize+":"+otherSize+":"+spaceSize);
                                postInvalidate();
                                if(mCallbackForUpdateSpace != null){
                                        mCallbackForUpdateSpace.updateSpace(lockSize, vidSize, otherSize, spaceSize,MyApplication.getStorageDescription(mRootPath),MyApplication.getCleanSizeSetting());
                                }else{
                                        
                                }
                        }
                }            
        }

        public long getSpaceSize(String path) {
                if(mStatFs == null)
                        mStatFs = new StatFs(path);
                return  mStatFs.getAvailableBlocksLong() * mStatFs.getBlockSizeLong();
        }

        public long getTotalSize(String path) {
                if(mStatFs == null)
                        mStatFs = new StatFs(path);
                return  mStatFs.getBlockSizeLong() * mStatFs.getBlockCountLong();
        }
        
        private double long2Double(long size){
                mBigDecimal = new BigDecimal((size/(1024.00*1024.00*1024.00)));               
                return mBigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
        }

}
