package com.elluminati.eber.driver;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontEdittextView;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.utils.Utils;


public class ContactUsActivity extends BaseAppCompatActivity {

    private MyFontEdittextView etAdminEmail, etAdminPhoneNo;
    private MyFontTextView tvThankYouFor;
    private MyFontButton btnemailsend;
    private EditText emailtext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.text_contact_us));
        etAdminEmail = (MyFontEdittextView) findViewById(R.id.etAdminEmail);
        tvThankYouFor = (MyFontTextView) findViewById(R.id.tvThankYouFor);
        etAdminPhoneNo = (MyFontEdittextView) findViewById(R.id.etAdminPhoneNo);
        btnemailsend = findViewById(R.id.btnemailsend);
        emailtext = findViewById(R.id.emailtext);
        btnemailsend.setOnClickListener(this);
        tvThankYouFor.setText(getString(R.string.text_thank_you_for_choosing, getString(R.string.app_name)));
        etAdminEmail.setOnClickListener(this);
        etAdminPhoneNo.setOnClickListener(this);
        setProfileData();
    }

    @Override
    protected boolean isValidate() {
        return false;
    }

    @Override
    public void goWithBackArrow() {
        onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.etAdminEmail:
                sendEmail(preferenceHelper.getContactUsEmail());
                break;
            case R.id.etAdminPhoneNo:
                makePhoneCall(preferenceHelper.getAdminPhone());
                break;
            case R.id.btnemailsend:
                if (validation()) {
                    sendEmail1(preferenceHelper.getContactUsEmail());
                }
                break;
            default:
                // do with default
                break;
        }
    }

    private boolean validation() {
        if (TextUtils.isEmpty(emailtext.getText().toString())) {
            Utils.showToast(getResources().getString(R.string.text_enter_message_rq), ContactUsActivity.this);
            return false;
        } else {
            return true;
        }
    }

    private void sendEmail1(String mailTo) {
        if (!TextUtils.isEmpty(mailTo)) {
            String uriText =
                    "mailto:" + mailTo +
                            "?subject=" + Uri.encode("") +
                            "&body=" + Uri.encode(emailtext.getText().toString());
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(uriText));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Utils.showToast(getString(R.string.text_no_email_app), this);
            }
        }
    }

    @Override
    public void onAdminApproved() {
        goWithAdminApproved();
    }

    @Override
    public void onAdminDeclined() {
        goWithAdminDecline();
    }

    @Override
    public void onprofileApprove() {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogInternet();
        } else {
            openInternetDialog();
        }
    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {
        if (isConnected) {
            closedEnableDialogGps();
        } else {
            openGpsDialog();
        }
    }

    private void setProfileData() {
        etAdminEmail.setText(preferenceHelper.getContactUsEmail());
        etAdminPhoneNo.setText(preferenceHelper.getAdminPhone());
    }

    private void sendEmail(String mailTo) {
        if (!TextUtils.isEmpty(mailTo)) {
            mailTo = "mailto:" + mailTo;
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse(mailTo));
            try {
                startActivity(emailIntent);
            } catch (ActivityNotFoundException e) {
                Utils.showToast(getString(R.string.text_no_email_app), this);
            }
        }
    }

    private void makePhoneCall(String number) {
        if (!TextUtils.isEmpty(number)) {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        }
    }
}
