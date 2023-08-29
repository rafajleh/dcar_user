package com.elluminati.eber.driver.components;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.elluminati.eber.driver.R;


/**
 * Created by elluminati on 07-10-2016.
 */
public abstract class CustomDialogVerifyDetail extends Dialog implements View.OnClickListener {
    private EditText etEmailVerify, etSMSVerify;
    private MyFontButton btnVerify, btnVerifyCancel;

    public CustomDialogVerifyDetail(Context context, boolean isEmailOTP,
                                    boolean isSmsOTP) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_verify_detail);
        etEmailVerify = (EditText) findViewById(R.id.etEmailVerify);
        etSMSVerify = (EditText) findViewById(R.id.etSMSVerify);
        btnVerify = (MyFontButton) findViewById(R.id.btnVerify);
        btnVerifyCancel = (MyFontButton) findViewById(R.id.btnVerifyCancel);
        btnVerify.setOnClickListener(this);
        btnVerifyCancel.setOnClickListener(this);
        if (isEmailOTP) {
            etEmailVerify.setVisibility(View.VISIBLE);
        }
        if (isSmsOTP) {
            etSMSVerify.setVisibility(View.VISIBLE);
        }


        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        getWindow().setDimAmount(0);
        setCancelable(false);
    }

    public void notifyDataSetChange(String data) {
        etSMSVerify.setText(data);
    }

    public abstract void doWithSubmit(EditText etEmailVerify, EditText etSMSVerify);

    public abstract void doCancel();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVerify:
                doWithSubmit(etEmailVerify, etSMSVerify);
                break;
            case R.id.btnVerifyCancel:
                doCancel();
                break;
        }
    }

}
