package com.example.test;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity5 extends Activity {

	public DisplayMetrics mDisplayMetrics;
	public final String TAG = "DisplayTest";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_usb_cvbs);
        mDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(mDisplayMetrics);
        Log.w(TAG, "width: "+mDisplayMetrics.widthPixels+"\n"+"height: "+mDisplayMetrics.heightPixels+"\n"
        					+"dpi: "+mDisplayMetrics.densityDpi+"\n"+"scale: "+mDisplayMetrics.density+"\n"+"fontScale: "+mDisplayMetrics.scaledDensity);
    
        int number = Camera.getNumberOfCameras();
        Log.w(TAG, "camera number = "+number);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
