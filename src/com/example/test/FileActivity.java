package com.example.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.example.test.Helper.Constant;
import com.example.test.Helper.FileBeanCompareInfos;
import com.example.test.Helper.FileNameUtil;
import com.example.test.Helper.PicNameFilter;
import com.example.test.Helper.VideoNameFilter;
import com.example.test.app.MyApplication;
import com.example.test.file.FileAdapter;
import com.example.test.settings.CameraSettings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MiniThumbFile;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class FileActivity extends Activity implements OnCheckedChangeListener{

	private RadioGroup mRadioGroup;
	private RadioButton mRadioButtonLock;
	private RadioButton mRadioButtonUsb;
	private RadioButton mRadioButtonCvbs;
	private RadioButton mRadioButton360;
	private RadioButton mRadioButtonPic;
	private ListView mListView;
	private ImageView mImageViewDelete;
	private Button mButtonSelectAll;
	private Button mButtonSelectReverse;
	private TextView mTextViewTotal;
	private TextView mTextViewFree;
	private List<FileBeanCompareInfos> mFileList;
	private String mTag = Constant.TAG_LOCK;
	private HandlerThread mHandlerThread;
	private Handler tHandler;
	private String THREAD_NAME = "listFile";
	private ListFileRunnable mListFileRunnable;
	private DeleteFileRunnable mDeleteFileRunnable;
	private String TAG = "LY_CAMERA_FileActivity";
	private MyItemClickListener mItemClickListener;
	private MyButtonClickListener mClickListener;
	private FileAdapter mFileAdapter;
	private Context mContext;
	private static final int MSG_UPDATE_SPACE = 1;
	private static final int MSG_UPDATE_LIST = 2;
	private static final int MSG_UPDATE_LIST_BY_TAG = 3;
	private static final int MSG_DISMISS_DIALOG = 4;
	private static final int MSG_UPDATE_DELETE_INFO = 5;
	private static final int MSG_SHOW_DIALOG = 6;
	private String mCurrentRootPath;
	private MyExternelStorageBroadcastReceiver mExternelStorageBroadcastReceiver;
	private View mProgressDeleteLayout;
	private TextView mTextViewDeleteInfo;
	private AlertDialog.Builder mBuilderDelete;
	private AlertDialog mDialogDelete;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_activity_layout);
		mContext = this;
		initView();
		
		mFileList = new ArrayList<FileBeanCompareInfos>();
		mFileAdapter = new FileAdapter(mFileList, mContext);
		mListView.setAdapter(mFileAdapter);
		
		mHandlerThread = new HandlerThread(THREAD_NAME);
		mHandlerThread.start();
		tHandler = new Handler(mHandlerThread.getLooper());
		
		registerExternelStorageListener();
		mHandler.sendEmptyMessage(MSG_UPDATE_SPACE);
		
	}
	
	private void initView(){
		mRadioGroup = (RadioGroup)findViewById(R.id.rg_total);
		mRadioButtonLock  = (RadioButton)mRadioGroup.findViewById(R.id.rb_lock);
		mRadioButtonLock.setTag(Constant.TAG_LOCK);
		mRadioButtonUsb  = (RadioButton)mRadioGroup.findViewById(R.id.rb_usb);
		mRadioButtonUsb.setTag(Constant.TAG_USB);
		mRadioButtonCvbs  = (RadioButton)mRadioGroup.findViewById(R.id.rb_cvbs);
		mRadioButtonCvbs.setTag(Constant.TAG_CVBS);
		mRadioButton360  = (RadioButton)mRadioGroup.findViewById(R.id.rb_360);
		mRadioButton360.setTag(Constant.TAG_360);
		mRadioButtonPic  = (RadioButton)mRadioGroup.findViewById(R.id.rb_pic);
		mRadioButtonPic.setTag(Constant.TAG_PIC);
		mListView = (ListView)findViewById(R.id.listview_file);
		mImageViewDelete = (ImageView)findViewById(R.id.iv_file_del);
		mButtonSelectAll = (Button)findViewById(R.id.btn_select_all);
		mButtonSelectReverse = (Button)findViewById(R.id.btn_select_reverse);
		mTextViewFree = (TextView)findViewById(R.id.tv_free_size);
		mTextViewTotal = (TextView)findViewById(R.id.tv_total_size);
		
		mRadioGroup.setOnCheckedChangeListener(this);

		mItemClickListener = new MyItemClickListener();
		mListView.setOnItemClickListener(mItemClickListener);

		mClickListener = new MyButtonClickListener();
		mImageViewDelete.setOnClickListener(mClickListener);
		mButtonSelectAll.setOnClickListener(mClickListener);
		mButtonSelectReverse.setOnClickListener(mClickListener);
		
		mProgressDeleteLayout = LayoutInflater.from(mContext).inflate(R.layout.progress_layout, null);
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		mCurrentRootPath = MyApplication.getCurrentStorageWithDefault();
		updateListViewByCurrentTag();
	}
	
	private void updateListViewByCurrentTag(){
		RadioButton button = (RadioButton)mRadioGroup.findViewById(mRadioGroup.getCheckedRadioButtonId());
		String tag = (String)button.getTag();
		if(!tag.equals(mTag)){
			Log.e(TAG, "tag: "+tag+"  ; mTag:"+mTag);
		}else{
			mListFileRunnable = new ListFileRunnable(Constant.tagToPath(mTag),mTag);
			tHandler.removeCallbacks(null);
			tHandler.post(mListFileRunnable);
		}
	}
	
	private void registerExternelStorageListener(){
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addDataScheme("file");
		mExternelStorageBroadcastReceiver = new MyExternelStorageBroadcastReceiver();
		mContext.registerReceiver(mExternelStorageBroadcastReceiver, intentFilter);
	}
	
	private void unRegisterExternelStorageListener(){
		mContext.unregisterReceiver(mExternelStorageBroadcastReceiver);
	}
	
    private class MyExternelStorageBroadcastReceiver extends BroadcastReceiver{
    	private String dataPath;

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent == null) return;
			dataPath = intent.getData()==null?"NULL":intent.getData().getPath();
			Log.w(TAG, "action:"+intent.getAction()+";path:"+dataPath);
			if(Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())){
				if(MyApplication.getCurrentStorageWithDefault().equals(dataPath)){
					mCurrentRootPath = MyApplication.getCurrentStorageWithDefault();
					mHandler.sendEmptyMessage(MSG_UPDATE_LIST_BY_TAG);
					mHandler.sendEmptyMessage(MSG_UPDATE_SPACE);
				}
			}else if(Intent.ACTION_MEDIA_EJECT.equals(intent.getAction())){
				if(mCurrentRootPath == null){
					
				}else{
					if(mCurrentRootPath.equals(dataPath)){
						mCurrentRootPath = null;
						synchronized (mFileList) {
							mFileList.clear();
						}
						mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
						mHandler.sendEmptyMessage(MSG_UPDATE_SPACE);
					}
				}
			}
		}
    	
    }

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if(checkedId == mRadioButtonLock.getId()){
			mTag = Constant.TAG_LOCK;
		}else if(checkedId == mRadioButtonUsb.getId()){
			mTag = Constant.TAG_USB;
		}else if(checkedId == mRadioButtonCvbs.getId()){
			mTag = Constant.TAG_CVBS;
		}else if(checkedId == mRadioButton360.getId()){
			mTag = Constant.TAG_360;
		}else if(checkedId == mRadioButtonPic.getId()){
			mTag = Constant.TAG_PIC;
		}
		mListFileRunnable = new ListFileRunnable(Constant.tagToPath(mTag),mTag);
		tHandler.removeCallbacks(null);
		tHandler.post(mListFileRunnable);
	}
	
	private class MyButtonClickListener implements View.OnClickListener{
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_select_all:
				selectAll();
				break;
			case R.id.btn_select_reverse:
				selectReverse();
				break;
			case R.id.iv_file_del:
				deleteSelect();
				break;
			default:
				break;
			}
		}
	}
	
	private void selectAll(){
		if(mFileList != null){
			for(FileBeanCompareInfos temp: mFileList){
				temp.setmChecked(true);
			}
		}
		mFileAdapter.setData(mFileList);
	}
	
	private void selectReverse(){
		if(mFileList != null){
			for(FileBeanCompareInfos temp: mFileList){
					temp.setmChecked(!temp.getmChecked());
			}
		}
		mFileAdapter.setData(mFileList);
	}
	
	private void deleteSelect(){
		if(mDeleteFileRunnable == null){
			mDeleteFileRunnable = new DeleteFileRunnable();
		}
		tHandler.post(mDeleteFileRunnable);
	}
	
	private void showDeleteDialog(){
		if(mDialogDelete == null){
			mBuilderDelete = new AlertDialog.Builder(mContext);
			mBuilderDelete.setView(mProgressDeleteLayout);
			mDialogDelete = mBuilderDelete.create();
			mDialogDelete.setCanceledOnTouchOutside(false);
			mDialogDelete.setCancelable(true);
		}
		mDialogDelete.show();
		mTextViewDeleteInfo = (TextView)mDialogDelete.findViewById(R.id.tv_delete_info);
	}
	
	private void dismissDeleteDialog(){
		if(mDialogDelete != null){
			mTextViewDeleteInfo = null;
			mDialogDelete.dismiss();
		}
	}
	
    public void startImageFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromFile(new File(param));
        try {
            intent.setDataAndType(uri, "lyimage/*");
            startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            intent.setDataAndType(uri, "image/*");
            startActivity(intent);
        }
    }

    public void startVideoFileIntent(String param) {

        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("oneshot", 0);
        intent.putExtra("configchange", 0);
        Uri uri = Uri.fromFile(new File(param));
        try {
            intent.setDataAndType(uri, "lyvideo/*");
            startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            intent.setDataAndType(uri, "video/*");
            startActivity(intent);
        }
    }
	
	private class MyItemClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			String path = mFileList.get(position).getmPath();
			Log.w(TAG, "path:"+path+"  ;position:"+position);
			if(path.endsWith(".mp4")){
				startVideoFileIntent(path);
			}else if(path.endsWith(".jpg")){
				startImageFileIntent(path);
			}
		}
		
	}
	
	public class DeleteFileRunnable implements Runnable{
		@Override
		public void run() {
			FileBeanCompareInfos temp;
			File tFile;
			int count = 0;
			if(mFileList == null) return;
			String info = mContext.getResources().getString(R.string.str_deleting);
			mHandler.sendEmptyMessageDelayed(MSG_SHOW_DIALOG, 500);  //大批量删除才会出对话框
			synchronized (mFileList) {
				int totalSize = mFileList.size();
				Iterator<FileBeanCompareInfos> iterator = mFileList.iterator();
				while(iterator.hasNext()){
					count++;
					temp = iterator.next();
					if(temp.getmChecked()){
						tFile = new File(temp.getmPath());
						if(tFile != null && tFile.exists()){
							tFile.delete();
							iterator.remove();
						}
					}
					mHandler.obtainMessage(MSG_UPDATE_DELETE_INFO, info+count+"/"+totalSize).sendToTarget();
				}
			}
			mHandler.removeMessages(MSG_SHOW_DIALOG);
			mHandler.sendEmptyMessage(MSG_DISMISS_DIALOG);
			mHandler.sendEmptyMessage(MSG_UPDATE_SPACE);
			mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
		}
	}
	
	public class ListFileRunnable implements Runnable{

		private String tPath;
		private String tTag; //防止高频率切换目录 会 刷新错 数据
		private File mFile;
		private FileBeanCompareInfos mFileBeanCompareInfos;
		private String tempPath;
		private int tempIndex;
		private File[] fileArray;
		
		public ListFileRunnable(String path,String tag){
			tPath = path;
			tTag = tag;
		}
		
		@Override
		public void run() {
			mFileList.clear();
			Log.w(TAG, "ListFileRunnable "+tPath);
			if(tPath == null || mCurrentRootPath == null || !MyApplication.isExternalStorageMounted(mCurrentRootPath)){
				listSuccess(0);
				return;
			}
			mFile = new File(mCurrentRootPath+tPath);
			if(!mFile.exists() || !mFile.isDirectory()){
				listSuccess(0);
				return;
			}
			if(Constant.TAG_LOCK.equals(tTag)){
				fileArray = mFile.listFiles(new VideoNameFilter());
				if(fileArray == null){
					listSuccess(0);
					return ;
				}
				for(File tmp : fileArray){
					if(tmp.exists() && tmp.isFile()){
						tempPath = tmp.getAbsolutePath();
						tempIndex = FileNameUtil.getIndexStringFromName_Lock(tempPath);
						mFileBeanCompareInfos = new FileBeanCompareInfos(tempPath, tempIndex, false);
						mFileList.add(mFileBeanCompareInfos);
					}
				}
			}else if(Constant.TAG_PIC.equals(tTag)){
				fileArray = mFile.listFiles(new PicNameFilter());
				if(fileArray == null){
					listSuccess(0);
					return ;
				}
				for(File tmp: fileArray){
					if(tmp.exists() && tmp.isFile()){
						tempPath = tmp.getAbsolutePath();
						mFileBeanCompareInfos = new FileBeanCompareInfos(tempPath, 0, false);
						mFileList.add(mFileBeanCompareInfos);
					}
				}
			}else{
				fileArray = mFile.listFiles(new VideoNameFilter());
				if(fileArray == null){
					listSuccess(0);
					return ;
				}
				for(File tmp : fileArray){
					if(tmp.exists() && tmp.isFile()){
						tempPath = tmp.getAbsolutePath();
						tempIndex = FileNameUtil.getIndexStringFromName_Common(tempPath);
						mFileBeanCompareInfos = new FileBeanCompareInfos(tempPath, tempIndex, false);
						mFileList.add(mFileBeanCompareInfos);
					}
				}
			}
			listSuccess(1);			
		}
		
		private void listSuccess(int flag){
			if(mFileList == null){
				return ;
			}
			Log.w(TAG, "list success, and size is "+mFileList.size()+"  ;flag = "+flag);
			Collections.sort(mFileList);
			if(tTag.equals(mTag)){
				mHandler.sendEmptyMessage(MSG_UPDATE_LIST);
			}else{
				
			}
		}
		
	}
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_UPDATE_SPACE:
				mTextViewFree.setText(MyApplication.getFreeSpace(MyApplication.getCurrentStorageWithDefault())+" M");
				mTextViewTotal.setText(MyApplication.getTotalSpace(MyApplication.getCurrentStorageWithDefault())+" M");
				break;
			case MSG_UPDATE_LIST:
				mFileAdapter.setData(mFileList);
				break;
			case MSG_UPDATE_LIST_BY_TAG:
				updateListViewByCurrentTag();
				break;
			case MSG_SHOW_DIALOG:
				showDeleteDialog();
				break;
			case MSG_DISMISS_DIALOG:
				dismissDeleteDialog();
				break;
			case MSG_UPDATE_DELETE_INFO:
				if(mDialogDelete != null && mDialogDelete.isShowing() && mTextViewDeleteInfo != null){
					mTextViewDeleteInfo.setText((String)msg.obj);
				}
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegisterExternelStorageListener();
		tHandler.removeCallbacksAndMessages(null);
		mHandler.removeCallbacksAndMessages(null);
		mHandlerThread.quitSafely();
	}
	
}
