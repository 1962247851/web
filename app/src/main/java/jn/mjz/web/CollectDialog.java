package jn.mjz.web;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CollectDialog extends Dialog {
    private EditText mEtName, mEtUrl;
    private TextView mTvTitle;
    private IOnCancelClickListener mIOnCancelClickListener;
    private IOnConfirmClickListener mIOnConfirmClickListener;
    private Button mBtnCancel, mBtnConfirm;

    public String getStringName() {
        return mEtName.getText().toString();
    }

    public String getStringUrl() {
        return mEtUrl.getText().toString();
    }

    public CollectDialog setConfirmString(String confirmString) {
        this.confirmString = confirmString;
        return this;
    }

    public CollectDialog setCancelString(String cancelString) {
        this.cancelString = cancelString;
        return this;
    }

    public CollectDialog setTitleString(String titleString) {
        this.titleString = titleString;
        return this;
    }

    private String stringName, stringUrl, confirmString, cancelString, titleString;

    public CollectDialog setIOnCancelClickListener(IOnCancelClickListener mIOnCancelClickListener) {
        this.mIOnCancelClickListener = mIOnCancelClickListener;
        return this;
    }

    public CollectDialog setIOnConfirmClickListener(IOnConfirmClickListener mIOnConfirmClickListener) {
        this.mIOnConfirmClickListener = mIOnConfirmClickListener;
        return this;
    }


    public CollectDialog(Context context, IOnCancelClickListener iOnCancelClickListener, IOnConfirmClickListener iOnConfirmClickListener) {
        super(context);
        this.mIOnCancelClickListener = iOnCancelClickListener;
        this.mIOnConfirmClickListener = iOnConfirmClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_collect);
        mBtnCancel = findViewById(R.id.btn_layout_cancel_collect_dialog);
        mBtnConfirm = findViewById(R.id.btn_layout_confirm_collect_dialog);
        mTvTitle = findViewById(R.id.text_view_collect_dialog_title);
        mEtName = findViewById(R.id.et_layout_collect_name);
        mEtUrl = findViewById(R.id.et_layout_collect_url);
        if (!TextUtils.isEmpty(stringUrl)) {
            mEtUrl.setText(stringUrl);
        }
        if (!TextUtils.isEmpty(stringName)) {
            mEtName.setText(stringName);
        }
        if (!TextUtils.isEmpty(titleString)) {
            mTvTitle.setText(titleString);
        }
        if (!TextUtils.isEmpty(confirmString)) {
            mBtnConfirm.setText(confirmString);
        }
        if (!TextUtils.isEmpty(cancelString)) {
            mBtnCancel.setText(cancelString);
        }
        mBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIOnConfirmClickListener != null) {
                    mIOnConfirmClickListener.onConfirmClick();
                }
            }
        });
        mBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIOnCancelClickListener != null) {
                    mIOnCancelClickListener.onCancelClick();
                }
            }
        });
    }

    public interface IOnCancelClickListener {
        void onCancelClick();
    }

    public interface IOnConfirmClickListener {
        void onConfirmClick();
    }


    public CollectDialog setStringName(String stringName) {
        this.stringName = stringName;
        return this;
    }

    public CollectDialog setStringUrl(String stringUrl) {
        this.stringUrl = stringUrl;
        return this;
    }

    public void hideUrl() {
        mEtUrl.setVisibility(View.GONE);
    }

}
