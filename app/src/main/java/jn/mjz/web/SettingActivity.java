package jn.mjz.web;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import jn.mjz.web.Util.GlobalUtil;


public class SettingActivity extends AppCompatActivity {

    private TextView mTvHomeUrl;
    private InputDialog inputDialog;
    private Button mBtnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mTvHomeUrl = findViewById(R.id.text_view_setting_home_url);
        mTvHomeUrl.setText(GlobalUtil.getInstance().sharedPreferences.getString("homeUrl", getString(R.string.defHomeUrl)));
        mTvHomeUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initInputDialog();
                inputDialog.setStringUrl(GlobalUtil.getInstance().sharedPreferences.getString("homeUrl", getString(R.string.defHomeUrl)))
                        .show();
            }
        });

        mBtnBack =findViewById(R.id.imageButton_back);
        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        // 使通知栏透明化
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    //初始化inputDialog
    private void initInputDialog() {
        inputDialog = new InputDialog(SettingActivity.this, new InputDialog.IOnCancelClickListener() {
            @Override
            public void onCancelClick() {
                inputDialog.dismiss();
            }
        }, new InputDialog.IOnConfirmClickListener() {
            @Override
            public void onConfirmClick() {
                if (!TextUtils.isEmpty(inputDialog.getStringUrl())) {
                    GlobalUtil.getInstance().editor.putString("homeUrl", inputDialog.getStringUrl());
                    GlobalUtil.getInstance().editor.apply();
                    mTvHomeUrl.setText(inputDialog.getStringUrl());
                    Toast.makeText(SettingActivity.this, "设置成功!", Toast.LENGTH_SHORT).show();
                    inputDialog.dismiss();
                } else {
                    Toast.makeText(SettingActivity.this, "请输入网址", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
