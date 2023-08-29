package com.elluminati.eber.driver;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.components.CustomDialogVerifyAccount;
import com.elluminati.eber.driver.components.CustomPhotoDialog;
import com.elluminati.eber.driver.components.MyFontEdittextView;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.models.datamodels.Bankdetails;
import com.elluminati.eber.driver.models.responsemodels.BankDetailResponse;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.GlideApp;
import com.elluminati.eber.driver.utils.ImageHelper;
import com.elluminati.eber.driver.utils.Utils;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BankDetailActivity extends BaseAppCompatActivity {
    private MyFontEdittextView etAccountNumber, etAccountHolderName, etRoutingNumber,
            etAccountHolderType, etPersonalIdNumber, etAddress, etStateCode, etPostalCode;
    private MyFontTextView tvDob;
    private String currentPassword;
    private CustomDialogVerifyAccount verifyDialog;
    private Uri picUri;
    private ImageView ivDocumentImage;
    private ImageHelper imageHelper;
    private String imageFilePath = null, uploadImage = "";
    private LinearLayout llPersonalId, llDob;
    private RadioButton rbMale;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_detail);
        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.text_bank_detail));

        imageHelper = new ImageHelper(this);
        etAccountNumber = (MyFontEdittextView) findViewById(R.id.etAccountNumber);
        etAccountHolderName = (MyFontEdittextView) findViewById(R.id.etAccountHolderName);
        etRoutingNumber = (MyFontEdittextView) findViewById(R.id.etRoutingNumber);
        etAccountHolderType = (MyFontEdittextView) findViewById(R.id.etAccountHolderType);
        ivDocumentImage = findViewById(R.id.ivDocumentImage);
        etPersonalIdNumber = findViewById(R.id.etPersonalIdNumber);
        etAddress = findViewById(R.id.etAddress);
        etStateCode = findViewById(R.id.etStateCode);
        etPostalCode = findViewById(R.id.etPostalCode);
        llPersonalId = findViewById(R.id.llPersonalId);
        rbMale = findViewById(R.id.rbMale);
        llDob = findViewById(R.id.llDob);
        tvDob = findViewById(R.id.tvDob);
        tvDob.setOnClickListener(this);
        ivDocumentImage.setOnClickListener(this);
        updateUIPartnerProvider(preferenceHelper.getProviderType() == Const.ProviderStatus
                .PROVIDER_TYPE_PARTNER);
        getBankDetail();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Const.ServiceCode.TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    onCaptureImageResult();
                }
                break;
            case Const.ServiceCode.CHOOSE_PHOTO:
                onSelectFromGalleryResult(data);
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                handleCrop(resultCode, data);
                break;
        }
    }

    /**
     * This method is used for handel result after select image from gallery .
     */

    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            picUri = data.getData();
            imageFilePath = imageHelper.getRealPathFromURI(picUri);
            // picUri = imageHelper.getPhotosUri(imageFilePath);
            beginCrop(picUri);
        }
    }

    /**
     * This method is used for handel result after captured image from camera .
     */
    private void onCaptureImageResult() {
        if (Utils.isNougat()) {
            beginCrop(picUri);
        } else {
            imageFilePath = picUri.getPath();
            picUri = imageHelper.getPhotosUri(imageFilePath);
            beginCrop(picUri);
        }
    }

    /**
     * This method is used for crop the image which selected or captured by currentTrip.
     */
    private void beginCrop(Uri sourceUri) {
        CropImage.activity(sourceUri).setGuidelines(com.theartofdev.edmodo.cropper.CropImageView
                .Guidelines.ON).start(this);
    }

    private void setProfileImage(Uri imageUri) {
        GlideApp.with(this).load(imageUri).fallback(R
                .drawable.ellipse).into(ivDocumentImage);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                    imageUri);
            uploadImage = Utils.convertImageToBase64(bitmap);

        } catch (IOException e) {
            AppLog.handleException(Const.Tag.REGISTER_ACTIVITY, e);
        }
    }

    /**
     * This method is used for  handel crop result after crop the image.
     */
    private void handleCrop(int resultCode, Intent result) {
        CropImage.ActivityResult activityResult = CropImage.getActivityResult(result);
        if (resultCode == RESULT_OK) {
            setProfileImage(activityResult.getUri());
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Toast.makeText(this, activityResult.getError().getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(Const.PIC_URI, picUri);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        picUri = savedInstanceState.getParcelable(Const.PIC_URI);

    }

    private void choosePhotoFromGallery() {

        /*Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Const.ServiceCode.CHOOSE_PHOTO);*/

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, Const.ServiceCode.CHOOSE_PHOTO);
    }

    private void takePhotoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (Utils.isNougat()) {
            picUri = FileProvider.getUriForFile(this, this.getApplicationContext
                    ().getPackageName(), imageHelper.createImageFile());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            picUri = Uri.fromFile(imageHelper.createImageFile());
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        startActivityForResult(intent, Const.ServiceCode.TAKE_PHOTO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
    }

    @Override
    protected boolean isValidate() {
        String msg = null;
        if (TextUtils.isEmpty(etAccountHolderName.getText().toString())) {
            msg = getString(R.string.msg_plz_account_name);
            etAccountHolderName.requestFocus();
        } else if (etAccountNumber.getText().toString().length() != 12) {
            msg = getString(R.string.msg_plz_valid_account_number);
            etAccountNumber.requestFocus();
        } else if (etRoutingNumber.getText().toString().length() != 9) {
            msg = getString(R.string.msg_plz_valid_routing_number);
            etRoutingNumber.requestFocus();
        } else if (etPersonalIdNumber.getText().toString().length() != 4) {
            msg = getString(R.string.msg_plz_valid_personal_id_number);
            etPersonalIdNumber.requestFocus();
        } else if (TextUtils.equals(tvDob.getText().toString(), getResources().getString(R.string
                .text_dob))) {
            msg = getString(R.string.msg_add_dob);
        } else if (TextUtils.isEmpty(uploadImage)) {
            msg = getString(R.string.msg_add_document_image);
        } else if (TextUtils.isEmpty(etAddress.getText().toString().trim())) {
            msg = getString(R.string.msg_enert_valid_data);
            etAddress.requestFocus();
        } else if (TextUtils.isEmpty(etStateCode.getText().toString().trim())) {
            msg = getString(R.string.msg_enert_valid_data);
            etStateCode.requestFocus();
        } else if (TextUtils.isEmpty(etPostalCode.getText().toString().trim())) {
            msg = getString(R.string.msg_enert_valid_data);
            etPostalCode.requestFocus();
        }
        if (msg != null) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;


    }


    private void openVerifyAccountDialog() {
        if (TextUtils.isEmpty(preferenceHelper.getSocialId())) {


            verifyDialog = new CustomDialogVerifyAccount(this, getResources().getString(R.string
                    .text_verify_account), getResources().getString(R.string.text_yes),
                    getResources()
                            .getString(R.string.text_no), getResources()
                    .getString(R.string.text_pass_current_hint), false) {
                @Override
                public void doWithEnable(EditText editText) {
                    currentPassword = editText.getText().toString();
                    if (!currentPassword.isEmpty()) {
                        verifyDialog.dismiss();
                        if (llDob.getVisibility() == View.VISIBLE) {
                            addBankDetail();
                        } else {
                            deleteBankDetail();
                        }
                    } else {
                        Utils.showToast(getString(R.string.msg_enter_password), BankDetailActivity
                                .this);
                    }
                }

                @Override
                public void doWithDisable() {
                    dismiss();
                }

                @Override
                public void clickOnText() {
                    dismiss();
                }
            };
            verifyDialog.show();
        } else {
            if (llDob.getVisibility() == View.VISIBLE) {
                addBankDetail();
            } else {
                deleteBankDetail();
            }
        }
    }

    private void addBankDetail() {
        Utils.showCustomProgressDialog(this, "", false, null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.BANK_ACCOUNT_HOLDER_NAME, etAccountHolderName
                    .getText().toString());
            jsonObject.put(Const.Params.BANK_ACCOUNT_NUMBER, etAccountNumber
                    .getText().toString());
            jsonObject.put(Const.Params.BANK_PERSONAL_ID_NUMBER, etPersonalIdNumber
                    .getText().toString());
            jsonObject.put(Const.Params.BANK_ACCOUNT_HOLDER_TYPE, Const.Bank
                    .BANK_ACCOUNT_HOLDER_TYPE);
            jsonObject.put(Const.Params.BANK_ROUTING_NUMBER, etRoutingNumber
                    .getText().toString());
            jsonObject.put(Const.Params.DOB, tvDob.getText().toString());
            jsonObject.put(Const.Params.ADDRESS, etAddress.getText().toString());
            jsonObject.put(Const.Params.STATE, etStateCode.getText().toString());
            jsonObject.put(Const.Params.GENDER, rbMale.isChecked() ? "male" : "female");
            jsonObject.put(Const.Params.POSTAL_CODE, etPostalCode.getText().toString());


            jsonObject.put(Const.Params.DOCUMENT, uploadImage);
            jsonObject.put(Const.Params.SOCIAL_UNIQUE_ID, preferenceHelper.getSocialId());
            jsonObject.put(Const.Params.PASSWORD, currentPassword);
            jsonObject.put(Const.Params.TOKEN, preferenceHelper
                    .getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper
                    .getProviderId());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .addBankDetail(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            Utils.hideCustomProgressDialog();
                            Utils.showMessageToast(response.body().getMessage(),
                                    BankDetailActivity.this);
                            onBackPressed();
                        } else {
                            Utils.hideCustomProgressDialog();
                            if (TextUtils.isEmpty(response.body().getStripeError())) {
                                Utils.showErrorToast(response.body().getErrorCode(),
                                        BankDetailActivity.this);
                            } else {
                                openBankDetailErrorDialog(response.body().getStripeError());
                            }


                        }
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(BankDetailActivity.class.getSimpleName(), t);

                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.BANK_DETAIL_ACTIVITY, e);
        }
    }

    private void getBankDetail() {

        Utils.showCustomProgressDialog(this, "", false, null);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());

            Call<BankDetailResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .getBankDetail(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<BankDetailResponse>() {
                @Override
                public void onResponse(Call<BankDetailResponse> call,
                                       Response<BankDetailResponse> response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            Bankdetails bankdetails = response.body().getBankdetails();
                            etAccountHolderName.setText(bankdetails.getAccountHolderName());
                            etAccountNumber.setText("********" + bankdetails.getAccountNumber());
                            etRoutingNumber.setText(bankdetails.getRoutingNumber());
                            etAccountHolderType.setText(bankdetails.getAccountHolderType());
                            updateUiOfBankDetail(true);
                            Utils.hideCustomProgressDialog();
                        } else {
                            Utils.hideCustomProgressDialog();
                        }

                    }


                }

                @Override
                public void onFailure(Call<BankDetailResponse> call, Throwable t) {
                    AppLog.handleThrowable(BankDetailActivity.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.BANK_DETAIL_ACTIVITY, e);
        }
    }

    /**
     * method update UI or make interaction enable or disable according provider type
     *
     * @param isPartnerProvider
     */
    private void updateUIPartnerProvider(boolean isPartnerProvider) {
        etAccountNumber.setEnabled(!isPartnerProvider);
        etAccountHolderName.setEnabled(!isPartnerProvider);
        etAccountHolderType.setEnabled(!isPartnerProvider);
        etRoutingNumber.setEnabled(!isPartnerProvider);
        etPersonalIdNumber.setEnabled(!isPartnerProvider);
        ivDocumentImage.setEnabled(!isPartnerProvider);
        tvDob.setEnabled(!isPartnerProvider);
        if (!isPartnerProvider) {
            setToolbarIcon(AppCompatResources.getDrawable(this, R.drawable.ic_done_black_24dp),
                    this);
        }

    }

    @Override
    public void goWithBackArrow() {
        onBackPressed();
    }


    private void deleteBankDetail() {
        Utils.showCustomProgressDialog(this, "", false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.PASSWORD, currentPassword);
            jsonObject.put(Const.Params.SOCIAL_UNIQUE_ID, preferenceHelper.getSocialId());

            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .deleteBankDetail(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {

                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            updateUiOfBankDetail(false);
                        } else {
                            Utils.showErrorToast(response.body().getErrorCode(),
                                    BankDetailActivity.this);
                        }

                        Utils.hideCustomProgressDialog();
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(BankDetailActivity.class.getSimpleName(), t);
                }
            });
        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.BANK_DETAIL_ACTIVITY, e);
        }
    }

    @Override
    public void onClick(View v) {
        // do with click
        switch (v.getId()) {
            case R.id.tvDob:
                openDatePickerDialog();
                break;
            case R.id.ivDocumentImage:
                openPhotoDialog();
                break;

            case R.id.ivToolbarIcon:
                if (llDob.getVisibility() == View.VISIBLE) {
                    if (isValidate()) {
                        openVerifyAccountDialog();
                    }
                } else {
                    openVerifyAccountDialog();
                }
                break;
            default:
                // do with default
                break;
        }

    }

    protected void openPhotoDialog() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission
                .CAMERA) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission
                (this, android.Manifest.permission
                        .READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest
                    .permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, Const
                    .PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE);
        } else {
            CustomPhotoDialog customPhotoDialog = new CustomPhotoDialog(this) {
                @Override
                public void clickedOnCamera() {
                    dismiss();
                    takePhotoFromCamera();
                }

                @Override
                public void clickedOnGallery() {
                    dismiss();
                    choosePhotoFromGallery();
                }
            };
            customPhotoDialog.show();
        }


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private void openDatePickerDialog() {

        final Calendar calendar = Calendar.getInstance();
        final int currentYear = calendar.get(Calendar.YEAR);
        final int currentMonth = calendar.get(Calendar.MONTH);
        final int currentDate = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog
                .OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                tvDob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
            }
        };
        final DatePickerDialog datePickerDialog = new DatePickerDialog(this, onDateSetListener,
                currentYear,
                currentMonth,
                currentDate);
        // DOB for stripe we only allow 13 year ago date
        Calendar calendar1 = Calendar.getInstance();
        calendar1.set(Calendar.YEAR, calendar1.get(Calendar.YEAR) - 13);
        datePickerDialog.getDatePicker().setMaxDate(calendar1.getTimeInMillis());
        datePickerDialog.show();

    }

    private void openBankDetailErrorDialog(String message) {

        final CustomDialogBigLabel customDialogBigLabel = new CustomDialogBigLabel(this,
                getResources()
                        .getString(R.string.text_error), message, getResources()
                .getString(R.string.text_retry),
                getResources()
                        .getString(R.string.text_ok)) {
            @Override
            public void positiveButton() {
                dismiss();
            }

            @Override
            public void negativeButton() {
                dismiss();

            }
        };
        customDialogBigLabel.btnYes.setVisibility(View.GONE);
        customDialogBigLabel.show();
    }

    private void updateUiOfBankDetail(boolean isHaveDetail) {
        if (isHaveDetail) {
            llDob.setVisibility(View.GONE);
            llPersonalId.setVisibility(View.GONE);
            if (preferenceHelper.getProviderType() != Const.ProviderStatus
                    .PROVIDER_TYPE_PARTNER) {
                setToolbarIcon(AppCompatResources.getDrawable(this, R.drawable.ic_close_black_24dp),
                        this);
            }
            ivDocumentImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable
                    .bank_icon));
            ivDocumentImage.setOnClickListener(null);
            updateUIPartnerProvider(true);


        } else {
            llDob.setVisibility(View.VISIBLE);
            llPersonalId.setVisibility(View.VISIBLE);
            etPersonalIdNumber.getText().clear();
            etRoutingNumber.getText().clear();
            etAccountHolderType.getText().clear();
            etAccountHolderName.getText().clear();
            etAccountNumber.getText().clear();
            ivDocumentImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable
                    .ic_add_image));
            ivDocumentImage.setOnClickListener(this);
            updateUIPartnerProvider(preferenceHelper.getProviderType() == Const.ProviderStatus
                    .PROVIDER_TYPE_PARTNER);
        }


    }
}
