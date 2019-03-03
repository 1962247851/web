package jn.mjz.web;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InputDialog extends Dialog {
    private EditText mEtUrl;
    private TextView mTvTitle;
    private IOnCancelClickListener mIOnCancelClickListener;
    private IOnConfirmClickListener mIOnConfirmClickListener;
    private Button mBtnCancel, mBtnConfirm;


    public String getStringUrl() {
            return mEtUrl.getText().toString();
    }

    public InputDialog setConfirmString(String confirmString) {
        this.confirmString = confirmString;
        return this;
    }

    public InputDialog setTitleString(String titleString) {
        this.titleString = titleString;
        return this;
    }

    private String stringUrl, confirmString, titleString;

    public InputDialog setIOnCancelClickListener(IOnCancelClickListener mIOnCancelClickListener) {
        this.mIOnCancelClickListener = mIOnCancelClickListener;
        return this;
    }

    public InputDialog setIOnConfirmClickListener(IOnConfirmClickListener mIOnConfirmClickListener) {
        this.mIOnConfirmClickListener = mIOnConfirmClickListener;
        return this;
    }


    public InputDialog(Context context, IOnCancelClickListener iOnCancelClickListener, IOnConfirmClickListener iOnConfirmClickListener) {
        super(context);
        this.mIOnCancelClickListener = iOnCancelClickListener;
        this.mIOnConfirmClickListener = iOnConfirmClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog_input);
        mBtnCancel = findViewById(R.id.btn_layout_cancel_input_dialog);
        mBtnConfirm = findViewById(R.id.btn_layout_confirm_input_dialog);
        mTvTitle = findViewById(R.id.text_view_input_dialog_title);
        mEtUrl = findViewById(R.id.et_layout_input_url);
        if (!TextUtils.isEmpty(stringUrl)) {
            mEtUrl.setText(stringUrl);
        }
        if (!TextUtils.isEmpty(titleString)) {
            mTvTitle.setText(titleString);
        }
        if (!TextUtils.isEmpty(confirmString)) {
            mBtnConfirm.setText(confirmString);
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



    public InputDialog setStringUrl(String stringUrl) {
        this.stringUrl = stringUrl;
        return this;
    }
}
