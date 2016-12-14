package com.example.test.file;

import java.io.File;
import java.util.List;

import com.example.test.R;
import com.example.test.Helper.FileBeanCompareInfos;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

public class FileAdapter extends BaseAdapter{

	private List<FileBeanCompareInfos> mData;
	private Context mContext;
	private ViewHolder mViewHolder;
	private FileBeanCompareInfos mFileBeanCompareInfo;
	private File mFile;
	private MyCheckChangeListener mCheckChangeListener;
	private String TAG = "LY_CAMERA_FileAdapter";
	
	public FileAdapter(List<FileBeanCompareInfos> data,Context context){
		mData = data;
		mContext = context;
		mCheckChangeListener = new MyCheckChangeListener();
	}
	
	public void setData(List<FileBeanCompareInfos> newData){
		mData = newData;
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		if(mData == null)
			return 0;
		else 
			return mData.size();
	}

	@Override
	public Object getItem(int position) {
		if(mData == null || mData.size() <= position)
			return null;
		else
			return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent,false);
			mViewHolder = new ViewHolder();
			mViewHolder.mImageViewIcon = (ImageView)convertView.findViewById(R.id.iv_lis_icon);
			mViewHolder.mTextViewFileName = (TextView)convertView.findViewById(R.id.tv_list_filename);
			mViewHolder.mCheckBoxSelect = (CheckBox)convertView.findViewById(R.id.cb_list_select);
			convertView.setTag(mViewHolder);
		}else{
			mViewHolder = (ViewHolder)convertView.getTag();
		}
		mFileBeanCompareInfo = mData.get(position);
		mFile = new File(mFileBeanCompareInfo.getmPath());
		if(mFile != null && mFile.exists()&& mFile.isFile()){
			mViewHolder.mTextViewFileName.setText(mFile.getName());
		}
		mViewHolder.mCheckBoxSelect.setChecked(mFileBeanCompareInfo.getmChecked());
		mViewHolder.mCheckBoxSelect.setTag(position);
		mViewHolder.mCheckBoxSelect.setOnCheckedChangeListener(mCheckChangeListener);
		
		return convertView;
	}
	
	private class ViewHolder{
		public ImageView mImageViewIcon;
		public TextView mTextViewFileName;
		public CheckBox mCheckBoxSelect;
	}
	
	private class MyCheckChangeListener implements CompoundButton.OnCheckedChangeListener{

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			int positon = (Integer)buttonView.getTag();
			Log.w(TAG, "new value is "+isChecked+" ; position is "+positon);
			mData.get(positon).setmChecked(isChecked);			
		}
		
	}

}
