package com.example.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

//import org.apache.http.util.EncodingUtils;

import com.example.test.app.MyApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class SettingActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {

	private PreferenceScreen preferenceScreen;
	private ListPreference storage_list;
	private EditTextPreference video_duration;
	private EditTextPreference clean_size_setting;
	private CheckBoxPreference clean_size;
    private CheckBoxPreference mirror_image;
	private CheckBoxPreference power_on;
	private SharedPreferences sharedPreferences;
	private Handler handler =new Handler();
	private String mCurrentPath;
	private String mCurrentLockPercent;
	
	Context context;

	StorageManager mStorageManager;
	StorageVolume[] storageVolumes;
	
	private ListPreference preview_size;
	private ListPreference video_size;
	private EditTextPreference preview_frame_rate;
	private EditTextPreference video_frame_rate;
	Resources res = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.preference_list_content);

		Context mContextSkin = null;
		try {
			mContextSkin = this.createPackageContext("com.android.launcher",
					Context.CONTEXT_IGNORE_SECURITY);
			res = mContextSkin.getResources();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		Drawable drawable = res.getDrawable(getResourceId(this, "wallpaper_01",
				"drawable", "com.android.launcher"));
		LinearLayout relativeLayout = (LinearLayout) findViewById(R.id.ly_preference_list_layout);
        relativeLayout.setBackground(drawable);

		context = this;
		
		addPreferencesFromResource(R.xml.setting);

		Intent intent = getIntent();
		intent.getStringArrayListExtra("photoSize");

		preferenceScreen = getPreferenceScreen();

		storage_list = (ListPreference) preferenceScreen
				.findPreference("storage_list");
		video_duration = (EditTextPreference) preferenceScreen
				.findPreference("video_duration");
		clean_size_setting = (EditTextPreference) preferenceScreen
				.findPreference("clean_size_setting");
		clean_size = (CheckBoxPreference) preferenceScreen
				.findPreference("clean_size");
		
        mirror_image = (CheckBoxPreference) preferenceScreen
                .findPreference("mirror_image");
        
		power_on = (CheckBoxPreference) preferenceScreen
				.findPreference("power_on");

		video_duration.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		clean_size_setting.getEditText().setInputType(
				InputType.TYPE_CLASS_NUMBER);

		storage_list.setOnPreferenceChangeListener(this);
		video_duration.setOnPreferenceChangeListener(this);
		clean_size.setOnPreferenceChangeListener(this);
		clean_size_setting.setOnPreferenceChangeListener(this);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		initStorageList();
		
		initVideoDuration();
		initCleanSize();
		initCleanSizeSetting();
        initMirrorImage();
		initPowerOn();


		if (!clean_size.isChecked()) {
			preferenceScreen.removePreference(clean_size_setting);
		}

		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);
		intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		intentFilter.addDataScheme("file");
		registerReceiver(broadcastReceiver, intentFilter);
		
		preview_size = (ListPreference) preferenceScreen
				.findPreference("preview_size");
		video_size = (ListPreference) preferenceScreen
				.findPreference("video_size");
		preview_frame_rate = (EditTextPreference) preferenceScreen
				.findPreference("preview_frame_rate");
		video_frame_rate = (EditTextPreference) preferenceScreen
				.findPreference("video_frame_rate");
		
		preferenceScreen.removePreference(preview_size);
		preferenceScreen.removePreference(video_size);
		preferenceScreen.removePreference(preview_frame_rate);
		preferenceScreen.removePreference(video_frame_rate);
		
		preview_size.setOnPreferenceChangeListener(this);
		video_size.setOnPreferenceChangeListener(this);
		preview_frame_rate.setOnPreferenceChangeListener(this);
		video_frame_rate.setOnPreferenceChangeListener(this);
		
		/*initPreviewSize();
		initVideoSize();
		initPreviewFrameRate();
		initVideoFrameRate();*/
		
		if (MyApplication.SHOW_PREVIEW_SIZE) {
			preferenceScreen.removePreference(preview_size);
			preferenceScreen.removePreference(video_size);
			preferenceScreen.removePreference(preview_frame_rate);
			preferenceScreen.removePreference(video_frame_rate);
		}
	}

    static int getResourceId(Context context, String name, String type,
			String packageName) {
		Resources themeResources = null;
		PackageManager pm = context.getPackageManager();
		try {
			themeResources = pm.getResourcesForApplication(packageName);
			return themeResources.getIdentifier(name, type, packageName);
		} catch (NameNotFoundException e) {

			e.printStackTrace();
		}
		return 0;
	}

	BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			initStorageList();
		}
	};
		
	private void initStorageList() {
		mCurrentPath = MyApplication.getCurrentStorageWithDefault();

		ArrayList<CharSequence> arrayEntryList = new ArrayList<CharSequence>();
		ArrayList<CharSequence> arrayValueList = new ArrayList<CharSequence>();
		boolean found = false;

		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
			storageVolumes = mStorageManager.getVolumeList();
		}
		
		for (int i = 0; i < storageVolumes.length; i++) {
			StorageVolume storageVolume = storageVolumes[i];
			if (MyApplication.isSdcardPrepared(storageVolume.getState())
					&& storageVolume.isRemovable()) {
				arrayEntryList.add(storageVolume.getDescription(this));
				arrayValueList.add(storageVolume.getPath());
				found = true;
			}
		}

		if (!found) {
			/*preferenceScreen.removePreference(storage_list);
			return;*/
			arrayEntryList.add(context.getResources().getString(R.string.str_null));
			arrayValueList.add(mCurrentPath);
		}

		CharSequence[] arrayEntry = (CharSequence[]) arrayEntryList
				.toArray(new CharSequence[0]);
		CharSequence[] arrayValue = (CharSequence[]) arrayValueList
				.toArray(new CharSequence[0]);

		storage_list.setEntries(arrayEntry);
		storage_list.setEntryValues(arrayValue);

		boolean exist = false;
		int i = 0;
		for (i = 0; i < arrayValue.length; i++) {
			if (mCurrentPath.equals(arrayValue[i])) {
				exist = true;
				break;
			}
		}

		storage_list.setValue(mCurrentPath);
		if(!exist){
			storage_list.setSummary(arrayEntry[i]+context.getResources().getString(R.string.str_unmounted));
		}else
			storage_list.setSummary(arrayEntry[i]);
	}
	
	private void initVideoDuration() {
		video_duration.setText(sharedPreferences.getString("video_duration",
				MyApplication.DEFAULT_VIDEO_DURATION));
		video_duration
				.setSummary(String.format(
						(String) getResources().getText(
								R.string.video_duration_dec),
						video_duration.getText()));
	}

	private void initCleanSize() {
		clean_size.setChecked(sharedPreferences.getBoolean("clean_size",
				MyApplication.DEFAULT_CLEAN_SIZE_STATUS));
		String sizeString = sharedPreferences.getString("clean_size_setting",
				MyApplication.DEFAULT_CLEAN_SIZE);
		clean_size.setSummary(String.format(
				context.getResources().getString(R.string.clean_size_dec_default),
				MyApplication.DEFAULT_CLEAN_SPACE,sizeString));
	}


	private void initCleanSizeSetting() {
		String sizeString = sharedPreferences.getString("clean_size_setting",
				MyApplication.DEFAULT_CLEAN_SIZE);
		
		clean_size_setting.setText(sizeString);
		clean_size_setting.setSummary(String.format((String) getResources()
				.getText(R.string.clean_size_setting_dec), sizeString));
	}

    private void initMirrorImage() {
        preferenceScreen.removePreference(mirror_image);
        mirror_image.setChecked(sharedPreferences.getBoolean("mirror_image",
                MyApplication.DEFAULT_MIRROR_STATUS));
    }
	   
	private void initPowerOn() {
		power_on.setChecked(sharedPreferences.getBoolean("power_on",
				MyApplication.DEFAULT_POWER_ON_STATUS));
	}

/*	public String readFile(String fileName) throws IOException {
		String res = "";
		try {
			FileInputStream fin = new FileInputStream(fileName);

			int length = fin.available();

			byte[] buffer = new byte[length];
			fin.read(buffer);

			res = EncodingUtils.getString(buffer, "UTF-8");

			fin.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}*/
	
/*	public ArrayList<String> loadSupportList(String FileName) {
		File file = new File(getFilesDir().getAbsolutePath() + "/" + FileName);
		if (!file.exists()) {
			return null;
		}
		
		try {
			String sizeString = readFile(file.getAbsolutePath());
			ArrayList<String> arrayList = new ArrayList<String>();
			while (sizeString.length() > 0) {
				arrayList.add(sizeString.substring(0, sizeString.indexOf(',')));
				sizeString = sizeString.substring(sizeString.indexOf(',') + 1);
			}
			return arrayList;
		} catch (IOException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		return null;

	}*/
	
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub

		if (preference == video_duration
				|| preference == clean_size_setting) {

			int min = 1;
			int max = 0;

			if (preference == video_duration) {
				max = 20;
			} else if (preference == clean_size_setting) {
				max = 100;
			}
			
			int value;
			boolean error = false;
			
			try {
				value = Integer.parseInt((String) newValue);
				if (value < min || value > max) {
					error = true;
				}
				
				if (newValue.toString().charAt(0) == '0') {
					error = true;
				}
			} catch (Exception e) {
				// TODO: handle exception
				error = true;
			} finally {
				if (error) {
					Toast.makeText(
							this,
							String.format(
									getResources()
											.getString(R.string.input_tip), max),
							Toast.LENGTH_SHORT).show();
					return false;
				}
			}
		}

		if (preference == storage_list) {
			String string = MyApplication
					.getStorageDescription((String) newValue);
			storage_list.setSummary(string);
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					initCleanSizeSetting();
				}
			}, 50);

		} else if (preference == video_duration) {
			video_duration.setSummary(String.format((String) getResources()
					.getText(R.string.video_duration_dec), (String) newValue));
		}else if (preference == clean_size) {
			if (clean_size.isChecked()) {
				preferenceScreen.removePreference(clean_size_setting);
			} else {
				preferenceScreen.addPreference(clean_size_setting);
			}

		}else if (preference == clean_size_setting) {
			clean_size.setSummary(String.format(
					context.getResources().getString(R.string.clean_size_dec_default),
					MyApplication.DEFAULT_CLEAN_SPACE,Integer
					.parseInt((String) newValue)));
			clean_size_setting.setSummary(String.format((String) getResources()
					.getText(R.string.clean_size_setting_dec), Integer
					.parseInt((String) newValue)));
		} else if (preference == preview_size) {
			preview_size.setSummary((String) newValue);
		} else if (preference == video_size) {
			video_size.setSummary((String) newValue);
		} else if (preference == preview_frame_rate) {
			preview_frame_rate.setSummary((String) newValue);
		} else if (preference == video_frame_rate) {
			video_frame_rate.setSummary((String) newValue);
		}

		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
}
