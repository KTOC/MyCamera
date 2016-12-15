package com.example.test.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.allwinnertech.libaw360.api.AW360API;
import com.example.test.R;


/**
 * 自定义progressdialog。
 * 主要调节progressview和文件的位置
 *
 * @author xiashaojun
 */
public class DisplaySetDialog extends ProgressDialog {
    private TextView mBrightnessView;
    private TextView mSaturationView;
    private TextView mContrastView;

    private SeekBar mBrightnessBar;
    private SeekBar mSaturationBar;
    private SeekBar mContrastBar;

    public DisplaySetDialog(Context context) {
        this(context, R.style.selectDialog);
    }

    public DisplaySetDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_display);
        initView();
    }

    private void initView() {
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = 540; // 宽度
        lp.height = 230; // 高度
        lp.alpha = 0.7f; // 透明度
        dialogWindow.setAttributes(lp);
        int lum = AW360API.getInstance(getContext()).getLum();
        int saturatin = AW360API.getInstance(getContext()).getSaturation();
        int contrast = AW360API.getInstance(getContext()).getContrast();
        mBrightnessView = (TextView) findViewById(R.id.brightness_tv);
        mBrightnessView.setText(lum + "");
        mSaturationView = (TextView) findViewById(R.id.saturation_tv);
        mSaturationView.setText(saturatin + "");
        mContrastView = (TextView) findViewById(R.id.contrast_tv);
        mContrastView.setText(contrast + "");
        mBrightnessBar = (SeekBar) findViewById(R.id.brightness_sb);
        mSaturationBar = (SeekBar) findViewById(R.id.saturation_sb);
        mContrastBar = (SeekBar) findViewById(R.id.contrast_sb);
        mBrightnessBar.setProgress(lum);
        mSaturationBar.setProgress(saturatin);
        mContrastBar.setProgress(contrast);

        mBrightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBrightnessView.setText("" + progress);
                AW360API.getInstance(getContext()).setLum(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSaturationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSaturationView.setText("" + progress);
                AW360API.getInstance(getContext()).setSaturation(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mContrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mContrastView.setText("" + progress);
                AW360API.getInstance(getContext()).setContrast(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

}
