package com.example.test;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.widget.Toast;

import com.android.allwinnertech.libaw360.api.AW360API;

/**
 * Created by xiashaojun on 16-11-11.
 */

public class BaseActivity extends FragmentActivity {
    protected boolean isSleep = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isSleep) {
            return super.onKeyDown(keyCode, event);
        }
        AW360API.getInstance(this).onKeyDown(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    protected void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void showMessage(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
