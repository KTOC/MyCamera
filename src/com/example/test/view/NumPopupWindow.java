package com.example.test.view;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.PopupWindow;

import com.example.test.R;
import com.example.test.Helper.DensityUtil;
import com.example.test.Helper.StringUtils;

/**
 * 自定义数字输入键盘窗口
 *
 * @author xiashaojun
 */
public class NumPopupWindow extends PopupWindow implements OnClickListener, OnKeyListener {
    private static final String TAG = "LY_CAMERA_NumPopupWindow";
    private EditText mEditText;
    private String mNumber;
    private Context mContext;

    public NumPopupWindow(Context context) {
        mContext = context;
        setContentView(initView());
        int width = DensityUtil.dip2px(context, (float) mContext.getResources()
                .getDimensionPixelSize(R.dimen.keyboard_width));
        int height = DensityUtil.dip2px(context, (float) mContext.getResources()
                .getDimensionPixelSize(R.dimen.keyboard_height));
        this.setWidth(width);
        this.setHeight(height);
        this.setFocusable(true);
        // 刷新状态
        this.update();
    }

    private View initView() {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.keyboard_view, null);
        mEditText = (EditText) view.findViewById(R.id.value_edit);
        mEditText.setInputType(InputType.TYPE_NULL);
        mEditText.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        mEditText.requestFocus();
        view.findViewById(R.id.one_B).setOnClickListener(this);
        view.findViewById(R.id.two_B).setOnClickListener(this);
        view.findViewById(R.id.three_B).setOnClickListener(this);
        view.findViewById(R.id.four_B).setOnClickListener(this);
        view.findViewById(R.id.five_B).setOnClickListener(this);
        view.findViewById(R.id.six_B).setOnClickListener(this);
        view.findViewById(R.id.seven_B).setOnClickListener(this);
        view.findViewById(R.id.eight_B).setOnClickListener(this);
        view.findViewById(R.id.nine_B).setOnClickListener(this);
        view.findViewById(R.id.zero_B).setOnClickListener(this);
        view.findViewById(R.id.clear_b).setOnClickListener(this);
        view.findViewById(R.id.cancel_B).setOnClickListener(this);
        view.findViewById(R.id.ok_B).setOnClickListener(this);
        mEditText.setOnKeyListener(this);
        view.findViewById(R.id.one_B).setOnKeyListener(this);
        view.findViewById(R.id.two_B).setOnKeyListener(this);
        view.findViewById(R.id.three_B).setOnKeyListener(this);
        view.findViewById(R.id.four_B).setOnKeyListener(this);
        view.findViewById(R.id.five_B).setOnKeyListener(this);
        view.findViewById(R.id.six_B).setOnKeyListener(this);
        view.findViewById(R.id.seven_B).setOnKeyListener(this);
        view.findViewById(R.id.eight_B).setOnKeyListener(this);
        view.findViewById(R.id.nine_B).setOnKeyListener(this);
        view.findViewById(R.id.zero_B).setOnKeyListener(this);
        view.findViewById(R.id.clear_b).setOnKeyListener(this);
        view.findViewById(R.id.cancel_B).setOnKeyListener(this);
        view.findViewById(R.id.ok_B).setOnKeyListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.one_B:
                input(1);
                break;
            case R.id.two_B:
                input(2);
                break;
            case R.id.three_B:
                input(3);
                break;
            case R.id.four_B:
                input(4);
                break;
            case R.id.five_B:
                input(5);
                break;
            case R.id.six_B:
                input(6);
                break;
            case R.id.seven_B:
                input(7);
                break;
            case R.id.eight_B:
                input(8);
                break;
            case R.id.nine_B:
                input(9);
                break;
            case R.id.zero_B:
                input(0);
                break;
            case R.id.clear_b:
                deleteOneChar();
                break;
            case R.id.cancel_B:
                mEditText.setText("");
                mNumber = "cancel";
                dismiss();
                break;
            case R.id.ok_B:
                setNumber();
                dismiss();
                break;

            default:
                break;
        }
    }

    private void deleteOneChar() {
        if (mEditText.getText() != null) {
            String value = mEditText.getText().toString();
            if (StringUtils.isNotEmpty(value)) {
                value = value.substring(0, value.length() - 1);
            }
            mEditText.setText(value);
        }
    }

    private void input(int num) {
        mEditText.append("" + num);
    }

    private void setNumber() {
        Editable editable = mEditText.getText();
        if (editable == null) {
            mNumber = null;
        }
        mNumber = editable.toString();
    }

    public String getNumber() {
        return mNumber;
    }

    public void show(int parentLayout) {
        mEditText.setText("");
        showAtLocation(LayoutInflater.from(mContext)
                .inflate(parentLayout, null), Gravity.CENTER, 0, 0);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        Log.i(TAG, "---onKey---keyCode = " + keyCode + "----event=" + event.getAction());
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    dismiss();
                    return true;
                case KeyEvent.KEYCODE_0:
                    mEditText.append("0");
                    return true;
                case KeyEvent.KEYCODE_1:
                    mEditText.append("1");
                    return true;
                case KeyEvent.KEYCODE_2:
                    mEditText.append("2");
                    return true;
                case KeyEvent.KEYCODE_3:
                    mEditText.append("3");
                    return true;
                case KeyEvent.KEYCODE_4:
                    mEditText.append("4");
                    return true;
                case KeyEvent.KEYCODE_5:
                    mEditText.append("5");
                    return true;
                case KeyEvent.KEYCODE_6:
                    mEditText.append("6");
                    return true;
                case KeyEvent.KEYCODE_7:
                    mEditText.append("7");
                    return true;
                case KeyEvent.KEYCODE_8:
                    mEditText.append("8");
                    return true;
                case KeyEvent.KEYCODE_9:
                    mEditText.append("9");
                    return true;
                default:
                    break;
            }
        }

        return false;
    }

}
