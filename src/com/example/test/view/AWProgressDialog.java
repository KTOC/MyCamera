package com.example.test.view;

import com.example.test.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

/**
 * 自定义progressdialog。
 * 主要调节progressview和文件的位置
 *
 * @author xiashaojun
 */
public class AWProgressDialog extends ProgressDialog {
    private TextView mMessageView;

    public AWProgressDialog(Context context) {
        super(context);
    }

    public AWProgressDialog(Context context, int theme) {
        super(context, R.style.bvDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_rl);
        initView();
    }

    private void initView() {
        mMessageView = (TextView) findViewById(R.id.progress_tv);
    }

    public void setMessage(String message) {
        mMessageView.setText(message);
    }

}
