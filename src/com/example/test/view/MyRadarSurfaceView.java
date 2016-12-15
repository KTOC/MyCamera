package com.example.test.view;

import com.example.test.R;
import com.example.test.R.drawable;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewConfiguration;

public class MyRadarSurfaceView extends SurfaceView implements SurfaceHolder.Callback{
	
	private SurfaceHolder mSurfaceHolder;
	private Context mContext;
	private Paint mLinePaintLR;
	private Paint mLinePaintFB;
	private Bitmap mBitmapCar;
	private int mSrcBitmapWidth;
	private int mSrcBitmapHeight;
	private int mBitmapWidth;
	private int mBitmapHeight;
	
	private  static final int SPACE_LENGTH_FIRST = 5; //第一根线与车之间的距离
	private static final int SPACE_LENGTH_OTHER = 8;//线与线之间的间距
	private static final int SPACE_LENGTH_END = 10;//最后剩余的留白
	private static final int SPACE_ANGLE_FB = 2;  //竖直方向上每组线之间的角度留白
	private static final int SPACE_ANGLE_LR = 2;  //水平方向上每组线之间的角度留白
	private static final int SPACE_ANGLE_CORNER = 4;  //水平和竖直交界角落的角度留白
	private static final int LENGTH_INIT_LR = 10;   //水平或竖直方向上每组线的初始宽度
	private static final int LENGTH_INIT_FB = 20;
	private final byte BIT_MASK_HIGH = 0x0f;  //去掉高位的掩码（低4位直接&运算，高4位直接右移4位 >>）
	private static final int MSG_UPDATE_DATA = 1;
	private static final String KEY_DATA = "updateData";
	
	private float mDrawingStartAngle;
	private float mDrawingSweepAngle;
	private RectF mDrawingRectF;

	
	private float mLineWidthLR;   //水平方向上每组线的宽度
	private float mLineWidthFB;     //竖直方向上每组线的宽度	
	private float mAngleLR;       //水平方向上每组线的角度
	private float mAngleFB;         //竖直方向上每组线的角度
	private float mTotalAngleLR;       //水平方向上总角度
	private float mTotalAngleFB;         //竖直方向上总角度
	private float mWidth;                 //view的宽度
	private float mHeight;                //view的高度
	//private float mCenterX;
	//private float mCenterY;
	private float mFCenterX;
	private float mFCenterY;
	private float mBCenterX;
	private float mBCenterY;
	private float mBitmapStartX;          
	private float mBitmapStartY;
	private float mRadius;
	private Canvas mCanvas;

	private byte[] mNewData;  //有新数据过来就更新数据
	private MyDrawRunnable mDrawRunnable;
	
	private int mFrameCount = 0; //为了避免每次绘画都去加载图片，且由于是双缓存机制，因此仅在第一帧和第二帧才去加载图片
	private boolean mSurfaceCreate = false;
	private boolean mNeedDraw = false;
	
	private HandlerThread mThread;
	private Handler tHandler;
	private static final String THREAD_NAME = "DrawThread"; 
	private int mWindowHeight = 600;
	
	private Object mSyncLock = new Object();
	
	public MyRadarSurfaceView(Context context) {
		this(context,null);
	}

	public MyRadarSurfaceView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
	}

	public MyRadarSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		this.setZOrderOnTop(true);
		mSurfaceHolder = getHolder();
		mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
		mSurfaceHolder.addCallback(this);
		
		
		mThread = new HandlerThread(THREAD_NAME);
		mThread.start();
		
		tHandler = new Handler(mThread.getLooper());
		
		if(mBitmapCar == null){
			Bitmap src = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.car_back);
			Matrix matrix = new Matrix();
		
			matrix.postScale(0.33f, 0.33f);
			mBitmapCar = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
			mBitmapWidth = mBitmapCar.getWidth();
			mBitmapHeight = mBitmapCar.getHeight();
		}
	}

	private void initPaints(){
		mLinePaintLR = new Paint();
		mLinePaintLR.setAntiAlias(true);
		mLinePaintLR.setColor(Color.RED);
		mLinePaintLR.setStyle(Style.STROKE);
		mLinePaintLR.setStrokeCap(Cap.ROUND);
		mLinePaintLR.setStrokeJoin(Join.ROUND);
		mLinePaintLR.setStrokeWidth(mLineWidthLR);		
		
		mLinePaintFB = new Paint();
		mLinePaintFB.setAntiAlias(true);
		mLinePaintFB.setColor(Color.RED);
		mLinePaintFB.setStyle(Style.STROKE);
		mLinePaintLR.setStrokeCap(Cap.ROUND);
		mLinePaintLR.setStrokeJoin(Join.ROUND);
		mLinePaintFB.setStrokeWidth(mLineWidthFB);
		
		mDrawingRectF = new RectF();
	}
	
	private void initAngles(){
		mTotalAngleLR = 360*mBitmapHeight/(mBitmapWidth+mBitmapHeight)-2*SPACE_ANGLE_CORNER;//4个角落都有留白
		mTotalAngleFB = 360*mBitmapWidth/(mBitmapWidth+mBitmapHeight)-2*SPACE_ANGLE_CORNER;
		mAngleLR = (int)((mTotalAngleLR/2 - 3*SPACE_ANGLE_LR)/4);
		mAngleFB = (int)((mTotalAngleFB/2-3*SPACE_ANGLE_FB)/4);
	}
	
	private void initPoints(){
		//mCenterX = mWidth/2;
		//mCenterY = mHeight/2;
		mFCenterX = mWidth/2;
		mFCenterY = mHeight/2;		
		
		mBCenterX = mWidth/2;
		mBCenterY = mHeight/2;
		
		mBitmapStartX = (mWidth-mBitmapWidth)/2;
		mBitmapStartY = (mHeight-mBitmapHeight)/2;				
	}
	
	private void recalculateLineWidth(){   //防止画的线超出view的范围
		float remainSpaceLR = mBitmapStartX - SPACE_LENGTH_FIRST - SPACE_LENGTH_END - 4*SPACE_LENGTH_OTHER - 5*LENGTH_INIT_LR;
		if((remainSpaceLR) >= 0){
			mLineWidthLR = LENGTH_INIT_LR;
		}else{
			mLineWidthLR = (remainSpaceLR + 5*LENGTH_INIT_LR)/5;
		}
		
		float remainSpaceFB = mBitmapStartY - SPACE_LENGTH_FIRST - SPACE_LENGTH_END - 4*SPACE_LENGTH_OTHER - 5*LENGTH_INIT_FB; 
		if((remainSpaceFB) >= 0){
			mLineWidthFB = LENGTH_INIT_FB;
		}else{
			mLineWidthFB = (remainSpaceFB + 5*LENGTH_INIT_FB)/5;
		}
	
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		
		
		mFrameCount = 0;
	}
	
	private void caculateScale(){
		Options op = new Options();
		op.inJustDecodeBounds = true;
		Bitmap tmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.car_back, op);  //此处返回的是null
		mSrcBitmapWidth = op.outWidth;
		mSrcBitmapHeight = op.outHeight;
		
	}
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {				
		synchronized (mSyncLock) {
			mCanvas = holder.lockCanvas();
			drawCarBitmap(mCanvas);
			holder.unlockCanvasAndPost(mCanvas);
			
		}
		mSurfaceCreate = true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mSurfaceCreate = false;
		tHandler.removeCallbacksAndMessages(null);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		mWidth = getWidth();
		mHeight = getHeight();
		Log.w("LY_CAMERA", "bw:bh = "+mBitmapWidth+":"+mBitmapHeight+"; "+"w:h = "+mWidth+":"+mHeight);

		initPoints();
		initAngles();
		recalculateLineWidth();
		initPaints();
	}	
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mFrameCount = 0;
		
	}
	
	public void updateData(byte[] data){
		if(!mSurfaceCreate)
			return;
		synchronized (mSyncLock) {
			mDrawRunnable = new MyDrawRunnable(data);
			tHandler.post(mDrawRunnable);
		}
	}
	
	private class MyDrawRunnable implements Runnable{

		private byte[] mDrawingData;  //有可能为空的,传过来的数据是被截取过的
		private int length;
		private byte temp;
		
		
		public MyDrawRunnable(byte[] data){
			mDrawingData = data;
		}
		
		@Override
		public void run() {    //接收到的数据左右是反的
			mCanvas = mSurfaceHolder.lockCanvas();
			mCanvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG    
	                | Paint.FILTER_BITMAP_FLAG));
			/*mFrameCount++;
			if(mFrameCount<=2){
				drawCarBitmap(mCanvas);
			}*/
			mCanvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
			drawCarBitmap(mCanvas);
			if(mDrawingData == null){
				Log.w("test", "runable data is null");
				mSurfaceHolder.unlockCanvasAndPost(mCanvas);
				return;
			}
			length = mDrawingData.length;
			for(int i = 0;i<length;i++){
				switch (i) {
				case 0:
					temp = mDrawingData[i];
					if(temp < 0){
						mNeedDraw = true;
					}else{
						mNeedDraw = false;
						mSurfaceHolder.unlockCanvasAndPost(mCanvas);
						return;
					}
					break;
				case 1:
					Log.w("test", "data[1]");
					temp = (byte)(mDrawingData[i]>>4);
					drawLineFL(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineFLM(mCanvas,temp);
					break;
				case 2:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineFRM(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineFR(mCanvas,temp);
					break;
				case 3:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineBL(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineBLM(mCanvas,temp);
					break;
				case 4:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineBRM(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineBR(mCanvas,temp);
					break;
				case 5:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineLF(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineLFM(mCanvas,temp);
					break;
				case 6:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineLBM(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineLB(mCanvas,temp);
					break;
				case 7:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineRF(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineRFM(mCanvas,temp);
					break;
				case 8:
					temp = (byte)(mDrawingData[i]>>4);
					drawLineRBM(mCanvas,temp);
					temp = (byte)(mDrawingData[i]&BIT_MASK_HIGH);
					drawLineRB(mCanvas,temp);
					break;
				default:
					break;
				}
			}
			mSurfaceHolder.unlockCanvasAndPost(mCanvas);
		}	
	}
	
	private void drawCarBitmap(Canvas canvas){
		canvas.drawBitmap(mBitmapCar, mBitmapStartX, mBitmapStartY, null);
	}
	
	private void drawLineFL(Canvas canvas,byte value){		
		mDrawingStartAngle = 270-SPACE_ANGLE_FB*3/2-mAngleFB*2;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mFCenterX,mFCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineFLM(Canvas canvas,byte value){		
		mDrawingStartAngle = 270-SPACE_ANGLE_FB/2-mAngleFB;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mFCenterX,mFCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineFRM(Canvas canvas,byte value){
		mDrawingStartAngle = 270+SPACE_ANGLE_FB/2;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mFCenterX,mFCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineFR(Canvas canvas,byte value){
		mDrawingStartAngle = 270+SPACE_ANGLE_FB*3/2+mAngleFB;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mFCenterX,mFCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineBL(Canvas canvas,byte value){
		mDrawingStartAngle = 90+SPACE_ANGLE_FB*3/2+mAngleFB;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mBCenterX,mBCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineBLM(Canvas canvas,byte value){
		mDrawingStartAngle = 90+SPACE_ANGLE_FB/2;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mBCenterX,mBCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineBRM(Canvas canvas,byte value){
		mDrawingStartAngle = 90-SPACE_ANGLE_FB/2-mAngleFB;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mBCenterX,mBCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineBR(Canvas canvas,byte value){
		mDrawingStartAngle = 90-SPACE_ANGLE_FB*3/2-mAngleFB*2;
		mDrawingSweepAngle = mAngleFB;
		Log.w("test", "drawLineFL----value:"+value+"; mLineWidthFB:"+mLineWidthFB+"; startAngle:"+mDrawingStartAngle+";sweepAngle:"+mDrawingSweepAngle);
		drawOneGroupLine(canvas, value,mBCenterX,mBCenterY,mBitmapHeight/2,SPACE_LENGTH_FIRST,SPACE_LENGTH_OTHER,mLineWidthFB,mDrawingStartAngle, mDrawingSweepAngle,mLinePaintFB);
	}
	
	private void drawLineLF(Canvas canvas,byte value){
		
	}
	
	private void drawLineLFM(Canvas canvas,byte value){
		
	}
	
	private void drawLineLBM(Canvas canvas,byte value){
		
	}
	
	private void drawLineLB(Canvas canvas,byte value){
		
	}
	
	private void drawLineRF(Canvas canvas,byte value){
		
	}
	
	private void drawLineRFM(Canvas canvas,byte value){
		
	}
	
	private void drawLineRBM(Canvas canvas,byte value){
		
	}
	
	private void drawLineRB(Canvas canvas,byte value){
		
	}
	private void drawOneGroupLine(Canvas canvas,byte value,float centerX,float centerY,float initRadius,int firstSpace,int otherSpace,float lineWidth,float startAngle,float sweepAngle,Paint paint){
		if(value == 0)
			return;
		for(int i=1;i<=value;i++){
			switch (i) {
			case 0:
				break;
			case 1:
				paint.setColor(Color.RED);
				mRadius = initRadius+firstSpace;
				mDrawingRectF.set(centerX-mRadius, centerY-mRadius, centerX+mRadius, centerY+mRadius);
				canvas.drawArc(mDrawingRectF, startAngle, sweepAngle, false, paint);
				break;
			case 2:
				paint.setColor(Color.YELLOW);
				mRadius = initRadius+firstSpace+lineWidth*1+otherSpace*1;
				mDrawingRectF.set(centerX-mRadius, centerY-mRadius, centerX+mRadius, centerY+mRadius);
				canvas.drawArc(mDrawingRectF, startAngle, sweepAngle, false, paint);
				break;
			case 3:
				paint.setColor(Color.YELLOW);
				mRadius = initRadius+firstSpace+lineWidth*2+otherSpace*2;
				mDrawingRectF.set(centerX-mRadius, centerY-mRadius, centerX+mRadius, centerY+mRadius);
				canvas.drawArc(mDrawingRectF, startAngle, sweepAngle, false, paint);
				break;
			case 4:
				paint.setColor(Color.GREEN);
				mRadius = initRadius+firstSpace+lineWidth*3+otherSpace*3;
				mDrawingRectF.set(centerX-mRadius, centerY-mRadius, centerX+mRadius, centerY+mRadius);
				canvas.drawArc(mDrawingRectF, startAngle, sweepAngle, false, paint);
				break;
			case 5:
				paint.setColor(Color.GREEN);
				mRadius = initRadius+firstSpace+lineWidth*4+otherSpace*4;
				mDrawingRectF.set(centerX-mRadius, centerY-mRadius, centerX+mRadius, centerY+mRadius);
				canvas.drawArc(mDrawingRectF, startAngle, sweepAngle, false, paint);
				break;
			default:
				break;
			}
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		
		if(heightMode == MeasureSpec.EXACTLY && widthMode != MeasureSpec.EXACTLY){
			widthSize = mBitmapWidth*heightSize/mBitmapHeight;
			widthMeasureSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
		}else if(widthMode == MeasureSpec.EXACTLY && heightMode != MeasureSpec.EXACTLY){
			heightSize = mBitmapHeight*widthSize/mBitmapWidth;
			heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
		}else{
			Log.w("LY_CAMERA", "can not measure !");
		}
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void releaseAll(){
		tHandler.removeCallbacksAndMessages(null);
		if(mBitmapCar != null){
			mBitmapCar.recycle();
			mBitmapCar = null;
		}
		mThread.quitSafely();
	}
	
}
