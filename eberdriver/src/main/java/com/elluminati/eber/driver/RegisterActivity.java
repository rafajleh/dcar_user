package com.elluminati.eber.driver;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.elluminati.eber.driver.adapter.OpratorAdapter;
import com.elluminati.eber.driver.components.CustomAddressChooseDialog;
import com.elluminati.eber.driver.models.datamodels.Oprators;
import com.elluminati.eber.driver.models.datamodels.RegisterDataModel;
import com.elluminati.eber.driver.models.singleton.AddressUtils;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.textfield.TextInputLayout;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.elluminati.eber.driver.adapter.CityAdapter;
import com.elluminati.eber.driver.broadcast.OtpReader;
import com.elluminati.eber.driver.components.CustomCountryDialog;
import com.elluminati.eber.driver.components.CustomDialogBigLabel;
import com.elluminati.eber.driver.components.CustomDialogEnable;
import com.elluminati.eber.driver.components.CustomDialogVerifyDetail;
import com.elluminati.eber.driver.components.CustomPhotoDialog;
import com.elluminati.eber.driver.components.MyAppTitleFontTextView;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontEdittextView;
import com.elluminati.eber.driver.components.MyFontTextView;
import com.elluminati.eber.driver.components.MyFontTextViewMedium;
import com.elluminati.eber.driver.interfaces.ClickListener;
import com.elluminati.eber.driver.interfaces.OTPListener;
import com.elluminati.eber.driver.interfaces.RecyclerTouchListener;
import com.elluminati.eber.driver.models.datamodels.City;
import com.elluminati.eber.driver.models.datamodels.Country;
import com.elluminati.eber.driver.models.responsemodels.CountriesResponse;
import com.elluminati.eber.driver.models.responsemodels.ProviderDataResponse;
import com.elluminati.eber.driver.models.responsemodels.VerificationResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.GlideApp;
import com.elluminati.eber.driver.utils.GoogleClientHelper;
import com.elluminati.eber.driver.utils.ImageCompression;
import com.elluminati.eber.driver.utils.ImageHelper;
import com.elluminati.eber.driver.utils.LocationHelper;
import com.elluminati.eber.driver.utils.Utils;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends BaseAppCompatActivity implements TextView
        .OnEditorActionListener,
        OTPListener, GoogleApiClient.OnConnectionFailedListener {


    private Uri picUri;
    private ImageView ivProfilePicture;
    private String uploadImageFilePath = "";
    private MyFontEdittextView etFirstName, etLastName, etAddress, etZipCode,
            etContactNumber, etEmail, etPassword, etBio, tvRegisterCountryName, tvRegisterCityName, tvopratorName, etpcolicence;
    private MyFontTextViewMedium tvCountryCode;
    private MyFontButton btnRegisterDone;
    private String loginType = Const.MANUAL;
    private ArrayList<Country> countryList;
    private Location lastLocation;
    private MyAppTitleFontTextView tvGoSignIn;
    private LinearLayout llPassword;
    private LocationHelper locationHelper;
    private CustomCountryDialog customCountryDialog;
    private CustomPhotoDialog customPhotoDialog;
    private CustomDialogEnable customDialogEnable;
    private CustomDialogBigLabel customDialogBigLabel;
    private ArrayList<City> cityList;
    private ArrayList<Oprators> opratorlist;

    private CityAdapter cityAdapter;
    private OpratorAdapter opratorAdapter;

    private boolean isEmailVerify, isSMSVerify, isBackPressedOnce;
    private String otpForSMS, otpForEmail;
    private CustomDialogVerifyDetail customDialogVerifyDetail;
    private ImageHelper imageHelper;
    private int phoneNumberLength, phoneNumberMinLength;
    private String selectedCountryPhoneCode = "";
    private MyFontTextView tvTerms;
    private CallbackManager callbackManager;
    private String socialId, socialEmail, socialPhotoUrl, socialFirstName, socialLastName;
    private String msg, gender = "";
    private Spinner selectGender;
    private OtpReader otpReader;
    private Country country;
    private GoogleApiClient googleApiClient;
    private City city;
    private Oprators oprators;
    private LinearLayout llSocialLogin;
    private TextInputLayout tilPassword;
    private AccessTokenTracker accessTokenTracker;
    RegisterDataModel registerDataModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        CurrentTrip.getInstance().clear();

        registerDataModel = (RegisterDataModel) getIntent().getSerializableExtra("reg_model");


        // Print the JSON string
        Log.d("", "EditTextValue2: "+registerDataModel.toString());
        googleApiClient = new GoogleClientHelper(this).build();
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }
        imageHelper = new ImageHelper(this);
        isEmailVerify = preferenceHelper.getIsProviderEmailVerification();
        isSMSVerify = preferenceHelper.getIsProviderSMSVerification();
        locationHelper = new LocationHelper(this);
        locationHelper.checkLocationSetting(this);
        ivProfilePicture = (ImageView) findViewById(R.id.ivProfilePicture);
        ivProfilePicture.setOnClickListener(this);
        btnRegisterDone = (MyFontButton) findViewById(R.id.btnRegisterDone);
        btnRegisterDone.setOnClickListener(this);
        etFirstName = (MyFontEdittextView) findViewById(R.id.etFirstName);
        etLastName = (MyFontEdittextView) findViewById(R.id.etLastName);
        etAddress = (MyFontEdittextView) findViewById(R.id.etAddress);
        etZipCode = (MyFontEdittextView) findViewById(R.id.etZipCode);
        etPassword = (MyFontEdittextView) findViewById(R.id.etRegisterPassword);
        etEmail = (MyFontEdittextView) findViewById(R.id.etRegisterEmailId);
        etBio = (MyFontEdittextView) findViewById(R.id.etBio);
        etContactNumber = (MyFontEdittextView) findViewById(R.id.etContactNumber);
        tvCountryCode = (MyFontTextViewMedium) findViewById(R.id.tvCountryCode);
        tvRegisterCountryName = findViewById(R.id.tvRegisterCountryName);
        tvRegisterCountryName.setOnClickListener(this);
        tvRegisterCityName = findViewById(R.id.tvRegisterCityName);
        tvRegisterCityName.setOnClickListener(this);
        tvCountryCode.setOnClickListener(this);
        llPassword = (LinearLayout) findViewById(R.id.llPassword);
        tvGoSignIn = (MyAppTitleFontTextView) findViewById(R.id.tvGoSignIn);
        tilPassword = findViewById(R.id.tilPassword);
        tvGoSignIn.setOnClickListener(this);
        etAddress.setOnEditorActionListener(this);
        etBio.setOnEditorActionListener(this);
        etZipCode.setOnEditorActionListener(this);
        tvTerms = (MyFontTextView) findViewById(R.id.tvTerms);



        tvopratorName = findViewById(R.id.tvopratorName);
        etpcolicence = findViewById(R.id.etpcolicence);
        tvopratorName.setOnClickListener(this);


        String link = getResources().getString(R.string.text_trems_and_condition_main,
                preferenceHelper.getTermsANdConditions(), preferenceHelper.getPolicy());
        tvTerms.setText(Utils.fromHtml(link));
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        initFBLogin();
        initGoogleLogin();
        countryList = new ArrayList<>();
        cityList = new ArrayList<>();
        opratorlist = new ArrayList<>();
        enableRegisterButton();
        updateUi(false);
        checkPermission();
        initGenderSelection();
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                tilPassword.setPasswordVisibilityToggleTintMode(PorterDuff.Mode.MULTIPLY);
            }
        });

        llSocialLogin=findViewById(R.id.llSocialLogin);

        if (preferenceHelper.getIsSocial())
        {
            llSocialLogin.setVisibility(View.VISIBLE);
        }
        else
        {
            llSocialLogin.setVisibility(View.GONE);
        }
//        otpReader = new OtpReader(this, preferenceHelper.getTwilioNumber());
//        registerReceiver(otpReader, new IntentFilter(Const.ACTION_OTP_SMS));


    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
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


    /**
     * Facebook login part
     */
  /*  private void registerCallForFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager, new
                FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Utils.showCustomProgressDialog(RegisterActivity.this, "", false, null);
                        getFacebookProfileDetail(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        AppLog.Log("Facebook :", "" + error);
                        Utils.showToast(getString(R.string.message_can_not_register_facebook),
                                RegisterActivity.this);
                    }
                });
        LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList
                ("email"));
    }*/
    private void getFacebookProfileDetail(AccessToken accessToken) {

        GraphRequest graphRequest = GraphRequest.newMeRequest(accessToken, new GraphRequest
                .GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Utils.hideCustomProgressDialog();
                try {
                    socialId = object.getString("id");
                    socialPhotoUrl = new URL("https://graph.facebook.com/" + socialId +
                            "/picture?width=250&height=250").toString();
                    storeSocialImageFile(socialPhotoUrl);
                    if (object.has("email")) {
                        socialEmail = object.getString("email");
                    }
                    socialFirstName = object.getString("first_name");
                    socialLastName = object.getString("last_name");
                    loginType = Const.SOCIAL_FACEBOOK;
                    LoginManager.getInstance().logOut();
                    setSocialData();


                } catch (Exception e) {
                    socialPhotoUrl = "";
                    AppLog.handleException(Const.Tag.SIGN_IN_ACTIVITY, e);
                }
                AppLog.Log("fb response", object.toString());
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, first_name, last_name, email");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();

    }


    /**
     * Google signIn part...
     */
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent, Const.google.RC_SIGN_IN);
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            try {
                GoogleSignInAccount account = result.getSignInAccount();
                loginType = Const.SOCIAL_GOOGLE;
                socialId = account.getId();
                socialEmail = account.getEmail();
                String str = account.getDisplayName();
                if (str.trim().contains(" ")) {
                    String[] name = str.split("\\s+");
                    socialFirstName = name[0].trim();
                    socialLastName = name[1].trim();
                } else {
                    socialFirstName = str.trim();
                    socialLastName = "";
                }
                socialPhotoUrl = account.getPhotoUrl().toString();
                storeSocialImageFile(socialPhotoUrl);
                setSocialData();
            } catch (Exception e) {
                setSocialData();
                AppLog.handleException(Const.Tag.SIGN_IN_ACTIVITY, e);
            }
        } else {
            AppLog.Log("Error", result.getStatus().toString());
            Utils.showToast(getString(R.string.message_can_not_register_google), RegisterActivity
                    .this);
        }
    }

    private void signOutFromGoogle() {
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        AppLog.Log("Google SignOut", "" + status);
                    }
                });
    }


    /**
     * Use for download profile image from facebook and google profile picture url...
     *
     * @param url
     */
    private void storeSocialImageFile(String url) {
        GlideApp.with(getApplicationContext()).asBitmap()
                .load(url)

                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model,
                                                   Target<Bitmap> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        uploadImageFilePath = createProfilePhoto(resource).getPath();
                        ivProfilePicture.setImageBitmap(resource);
                        return true;
                    }
                }).into(ivProfilePicture);
    }


    private File createProfilePhoto(Bitmap bitmap) {
        File imgaeFile = new File(this.getFilesDir(), "image.jpg");
        FileOutputStream os;
        try {
            os = new FileOutputStream(imgaeFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgaeFile;
    }


    private void register(String loginType) {
        AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "Register valid");

        HashMap<String, RequestBody> map = new HashMap<>();


        if (etPassword.getVisibility() == View.VISIBLE) {
            map.put(Const.Params.PASSWORD, ApiClient.makeTextRequestBody(etPassword.getText()
                    .toString()));
            FirebaseAuth.getInstance().signOut();
        } else {
            map.put(Const.Params.PASSWORD, ApiClient.makeTextRequestBody(""));
            map.put(Const.Params.SOCIAL_UNIQUE_ID, ApiClient.makeTextRequestBody(socialId));
        }
        map.put(Const.Params.FIRST_NAME, ApiClient.makeTextRequestBody(etFirstName.getText()
                .toString()));
        map.put(Const.Params.LAST_NAME, ApiClient.makeTextRequestBody(etLastName.getText()
                .toString()));
        map.put(Const.Params.EMAIL, ApiClient.makeTextRequestBody(etEmail.getText().toString
                ()));
        map.put(Const.Params.DEVICE_TYPE, ApiClient.makeTextRequestBody(Const
                .DEVICE_TYPE_ANDROID));
        map.put(Const.Params.DEVICE_TOKEN, ApiClient.makeTextRequestBody(preferenceHelper
                .getDeviceToken()));

        map.put(Const.Params.PHONE, ApiClient.makeTextRequestBody(etContactNumber.getText()
                .toString()));
        map.put(Const.Params.BIO, ApiClient.makeTextRequestBody(etBio.getText().toString()));

        map.put(Const.Params.APP_NAME, ApiClient.makeTextRequestBody(getResources().getString(R.string.app_name)));

        map.put(Const.Params.SERVICE_TYPE, ApiClient.makeTextRequestBody(""));
        map.put(Const.Params.DEVICE_TIMEZONE, ApiClient.makeTextRequestBody(Utils
                .getTimeZoneName()));
        map.put(Const.Params.ADDRESS, ApiClient.makeTextRequestBody(etAddress.getText()
                .toString()));

        map.put(Const.Params.ZIPCODE, ApiClient.makeTextRequestBody(etZipCode.getText()
                .toString()));
        map.put(Const.Params.LOGIN_BY, ApiClient.makeTextRequestBody(loginType));
        map.put(Const.Params.APP_VERSION, ApiClient.makeTextRequestBody(getAppVersion()));
        map.put(Const.Params.PROVIDER_TYPE_ID, ApiClient.makeTextRequestBody(oprators.getId()));
        map.put(Const.Params.PCO_LICENCE, ApiClient.makeTextRequestBody(etpcolicence.getText().toString()));

        // map.put(Const.Params.ISENDOFTRIP, ApiClient.makeTextRequestBody(preferenceHelper.getCheckEnd()));
       /* map.put(Const.Params.ENDADDRESS, ApiClient.makeTextRequestBody(preferenceHelper.getaddressEndtrip()));

        map.put(Const.Params.ENDLAT, ApiClient.makeTextRequestBody(preferenceHelper.getLAtEndtrip()));
        map.put(Const.Params.ENDLNG, ApiClient.makeTextRequestBody(preferenceHelper.getLngEndtrip()));
*/
        if (country != null) {
            map.put(Const.Params.COUNTRY, ApiClient.makeTextRequestBody(country.getCountryname()));
            map.put(Const.Params.COUNTRY_ID, ApiClient.makeTextRequestBody(country.getId()));
            map.put(Const.Params.COUNTRY_PHONE_CODE,
                    ApiClient.makeTextRequestBody(country.getCountryphonecode()));
        }
        if (city != null) {
            map.put(Const.Params.CITY_ID, ApiClient.makeTextRequestBody(city.getId()));
            map.put(Const.Params.CITY, ApiClient.makeTextRequestBody(city.getFullCityName()));
        }


//        Utils.showCustomProgressDialog(this, getResources().getString(R.string
//                .msg_waiting_for_registering), false, null);
        Call<ProviderDataResponse> userDataResponseCall;
        if (!TextUtils.isEmpty(uploadImageFilePath)) {
            userDataResponseCall = ApiClient.getClient().create
                    (ApiInterface.class).register(ApiClient.makeMultipartRequestBody
                            (this, uploadImageFilePath, Const.Params.PICTURE_DATA)
                    , map);
        } else {
            userDataResponseCall = ApiClient.getClient().create
                    (ApiInterface.class).register(null, map);
        }
        userDataResponseCall.enqueue(new Callback<ProviderDataResponse>() {
            @Override
            public void onResponse(Call<ProviderDataResponse> call,
                                   Response<ProviderDataResponse> response) {
                if (parseContent.isSuccessful(response)) {
                    if (parseContent.saveProviderData(response.body(), true)) {
                        AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "Register Success");
                        CurrentTrip.getInstance().clear();
                        Utils.hideCustomProgressDialog();
                        moveWithUserSpecificPreference();
                    } else {
                        Utils.hideCustomProgressDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<ProviderDataResponse> call, Throwable t) {
                AppLog.handleThrowable(RegisterActivity.class.getSimpleName(), t);
            }
        });


    }


    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
        AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "GoogleApiClient is Disconnected");
    }

    @Override
    protected void onDestroy() {
        //locationHelper.onStop();
        //unregisterReceiver(otpReader);
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onDestroy();
        if (accessTokenTracker != null) {
            accessTokenTracker.stopTracking();
        }
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
            case Const.PERMISSION_FOR_LOCATION:
                checkPermission();
                break;
            case Const.PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE:
                openPhotoDialog();
                break;
            case Const.google.RC_SIGN_IN:
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleGoogleSignInResult(result);
                break;
            case Const.LOCATION_SETTING_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    getServicesCountry();
                }
                break;
            default:
                callbackManager.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }

    /**
     * This method is used for handel result after select image from gallery .
     */

    private void onSelectFromGalleryResult(Intent data) {
        if (data != null) {
            picUri = data.getData();
            beginCrop(picUri);
        }
    }

    /**
     * This method is used for handel result after captured image from camera .
     */
    private void onCaptureImageResult() {
        beginCrop(picUri);
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
                .drawable.ellipse).into(ivProfilePicture);
    }

    /**
     * This method is used for  handel crop result after crop the image.
     */
    private void handleCrop(int resultCode, Intent result) {
        final CropImage.ActivityResult activityResult = CropImage.getActivityResult(result);
        if (resultCode == RESULT_OK) {
            uploadImageFilePath = imageHelper.getRealPathFromURI(activityResult.getUri());
            new ImageCompression(this).setImageCompressionListener(new ImageCompression
                    .ImageCompressionListener() {
                @Override
                public void onImageCompression(String compressionImagePath) {
                    setProfileImage(activityResult.getUri());
                    uploadImageFilePath = compressionImagePath;

                }
            }).execute(uploadImageFilePath);
        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            Utils.showToast(activityResult.getError().getMessage(), this);
        }
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
            picUri = FileProvider.getUriForFile(RegisterActivity.this, this.getApplicationContext
                    ().getPackageName(), imageHelper.createImageFile());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } else {
            picUri = Uri.fromFile(imageHelper.createImageFile());
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        startActivityForResult(intent, Const.ServiceCode.TAKE_PHOTO);
    }

    protected void openPhotoDialog() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission
                .CAMERA) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission
                (RegisterActivity.this, Manifest.permission
                        .READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest
                    .permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, Const
                    .PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE);
        } else {
            customPhotoDialog = new CustomPhotoDialog(this) {
                @Override
                public void clickedOnCamera() {
                    customPhotoDialog.dismiss();
                    takePhotoFromCamera();
                }

                @Override
                public void clickedOnGallery() {
                    customPhotoDialog.dismiss();
                    choosePhotoFromGallery();
                }
            };
            customPhotoDialog.show();
        }


    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnRegisterDone:
                checkValidationForRegister();
                break;
            case R.id.ivProfilePicture:
                openPhotoDialog();
                break;
            case R.id.tvRegisterCountryName:
                openCountryCodeDialog();
                break;
            case R.id.tvRegisterCityName:
                if (cityList.size() == 0) {
                    openRequestRegisterTypeDialog(getResources().getString(R.string
                            .msg_type_not_available_current_city));
                } else {
                    openCityNameDialog();
                }
                break;
            case R.id.tvGoSignIn:
                goToSignInActivity();
                break;
            case R.id.tvopratorName:
                if (opratorlist.size() == 0) {
                    openRequestRegisterTypeDialog(getResources().getString(R.string
                            .msg_type_not_available_current_city));
                } else {
                    openpartenerNameDialog();
                }
                break;

            default:
                //Do something with default
                break;
        }

    }





    private void checkValidationForRegister() {
        if (isValidate()) {
            if (isEmailVerify || isSMSVerify) {
                OTPVerify();
            } else {
                register(loginType);
            }
        }
    }


    private void updateUi(boolean update) {
        if (update) {
            llPassword.setVisibility(View.GONE);
            etPassword.setVisibility(View.GONE);
        } else {
            llPassword.setVisibility(View.VISIBLE);
            etPassword.setVisibility(View.VISIBLE);
        }
    }

    private void setSocialData() {
        updateUi(true);
        if (!TextUtils.isEmpty(socialEmail))
            etEmail.setEnabled(false);
        etEmail.setText(socialEmail);
        etFirstName.setText(socialFirstName);
        etLastName.setText(socialLastName);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "GoogleApiClient is Failed to Connect ");
    }


    @Override
    protected boolean isValidate() {
        msg = null;
        if (TextUtils.isEmpty(etFirstName.getText().toString().trim())) {
            msg = getString(R.string.msg_enter_name);
            etFirstName.requestFocus();
            etFirstName.setError(msg);
        } else if (TextUtils.isEmpty(etLastName.getText().toString().trim())) {
            msg = getString(R.string.msg_enter_lname);
            etLastName.requestFocus();
            etLastName.setError(msg);
        } else if (!Utils.eMailValidation((etEmail.getText().toString().trim()))) {
            msg = getString(R.string.msg_enter_valid_email);
            etEmail.requestFocus();
            etEmail.setError(msg);
        } else if (etPassword.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(etPassword.getText().toString().trim())) {
                msg = getString(R.string.msg_enter_password);
                etPassword.requestFocus();
                etPassword.setError(msg);
                tilPassword.setPasswordVisibilityToggleTintMode(PorterDuff.Mode.CLEAR);
            } else if (etPassword.getText().toString().trim().length() < 6) {
                msg = getString(R.string.msg_enter_valid_password);
                etPassword.requestFocus();
                etPassword.setError(msg);
                tilPassword.setPasswordVisibilityToggleTintMode(PorterDuff.Mode.CLEAR);
            } else {
                validateOtherInformation();
            }
        } else if (etPassword.getVisibility() == View.GONE) {
            validateOtherInformation();
        }
        return TextUtils.isEmpty(msg);

    }


    private void validateOtherInformation() {
        int phoneLength = etContactNumber.getText().toString().length();
        if (TextUtils.equals(tvRegisterCountryName.getText().toString(), getResources()
                .getString(R.string.text_hint_select_country)
        ) || TextUtils.isEmpty(tvRegisterCountryName.getText().toString())) {
            msg = getString(R.string.msg_plz_select_country);
            tvRegisterCountryName.requestFocus();
            tvRegisterCountryName.setError(msg);
        } else if (TextUtils.equals(tvRegisterCityName.getText().toString(), getResources()
                .getString(R.string.text_hint_select_city)) || TextUtils.isEmpty
                (tvRegisterCityName.getText().toString())) {
            msg = getString(R.string.msg_plz_select_city);
            tvRegisterCityName.requestFocus();
            tvRegisterCityName.setError(msg);
        } else if (TextUtils.equals(tvopratorName.getText().toString(), getResources()
                .getString(R.string.selectoprator)) || TextUtils.isEmpty
                (tvopratorName.getText().toString())) {
            msg = getString(R.string.msg_plz_select_city);
            tvopratorName.requestFocus();
            tvopratorName.setError(msg);
        } else if (TextUtils.isEmpty(etContactNumber.getText().toString())) {
            msg = getString(R.string.msg_enter_number);
            etContactNumber.requestFocus();
            etContactNumber.setError(msg);
        } else if (phoneLength > phoneNumberLength || phoneLength < phoneNumberMinLength) {
            msg = validPhoneNumberMessage(phoneNumberMinLength, phoneNumberLength);
            etContactNumber.requestFocus();
            etContactNumber.setError(msg);
        }
    }


    @Override
    public void goWithBackArrow() {
        // Do with back arrow
    }


    private void getCountryCodeList(String country) {
        int countryListSize = countryList.size();
        for (int i = 0; i < countryListSize; i++) {
            Log.e("877",countryList.get(i).getCountryname());
            if (countryList.get(i).getCountryname().toUpperCase().startsWith(country.toUpperCase
                    ())) {
                setCountry(i);
                return;
            }
        }
        setCountry(0);
        Utils.hideCustomProgressDialog();
    }

    private void openCountryCodeDialog() {
        tvRegisterCountryName.setError(null);
        customCountryDialog = null;
        customCountryDialog = new CustomCountryDialog(this, countryList) {
            @Override
            public void onSelect(int position, ArrayList<Country> filterList) {
                if (!selectedCountryPhoneCode.equalsIgnoreCase(filterList.get(position)
                        .getCountryphonecode())) {
                    etContactNumber.getText().clear();
                    selectedCountryPhoneCode = filterList.get(position).getCountryphonecode();
                }
                phoneNumberLength = filterList.get(position).getPhoneNumberLength();
                phoneNumberMinLength = filterList.get(position).getPhoneNumberMinLength();
                tvCountryCode.setText(filterList.get(position).getCountryphonecode());
                country = filterList.get(position);
                setContactNoLength(phoneNumberLength);
                tvRegisterCountryName.setText(filterList.get(position).getCountryname());
                tvRegisterCountryName.setTextColor(ResourcesCompat.getColor(getResources(), R.color
                        .color_app_text, null));
                getCityListSelectedCountry(filterList.get(position).getCities(),filterList.get(position).getOprators());
                InputMethodManager inm = (InputMethodManager) getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                inm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                customCountryDialog.dismiss();
            }
        };
        customCountryDialog.show();

    }

    private void openCityNameDialog() {
        tvRegisterCityName.setError(null);
        RecyclerView rcvCountryCode;
        MyFontEdittextView etCountrySearch;
        TextView tvDialogTitle;
        MyFontButton btnAddCity;

        final Dialog cityDialog = new Dialog(this);

        cityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cityDialog.setContentView(R.layout.dialog_country_code);
        btnAddCity = (MyFontButton) cityDialog.findViewById(R.id.btnAddCity);
        btnAddCity.setVisibility(View.VISIBLE);
        tvDialogTitle = cityDialog.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText(getResources().getString(R.string.text_city_name));
        rcvCountryCode = (RecyclerView) cityDialog.findViewById(R.id.rcvCountryCode);
        etCountrySearch = (MyFontEdittextView) cityDialog.findViewById(R.id.etCountrySearch);
        etCountrySearch.setHint(getResources().getString(R.string.text_search_city_name));
        rcvCountryCode.setLayoutManager(new LinearLayoutManager(this));
        cityAdapter = new CityAdapter(cityList);
        rcvCountryCode.setAdapter(cityAdapter);
        WindowManager.LayoutParams params = cityDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        cityDialog.getWindow().setAttributes(params);
        cityDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityDialog.dismiss();
                openRequestRegisterTypeDialog(getResources().getString(R.string
                        .msg_type_not_available_current_city));

            }
        });
        etCountrySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (cityAdapter != null) {
                    cityAdapter.getFilter().filter(s);
                } else {
                    Log.d("filter", "no filter availible");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                // do with text
            }
        });

        rcvCountryCode.addOnItemTouchListener(new RecyclerTouchListener(RegisterActivity.this,
                rcvCountryCode,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        tvRegisterCityName.setText(cityAdapter.getFilterResult().get(position)
                                .getCityName());
                        city = cityAdapter.getFilterResult().get(position);
                        tvRegisterCityName.setTextColor(ResourcesCompat.getColor(getResources(), R
                                .color.color_app_text, null));
                        InputMethodManager inm = (InputMethodManager) getSystemService(Context
                                .INPUT_METHOD_SERVICE);
                        inm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        cityDialog.dismiss();
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

        cityDialog.show();
    }




    private void openpartenerNameDialog()
    {

        tvopratorName.setError(null);
        RecyclerView rcvCountryCode;
        MyFontEdittextView etCountrySearch;
        TextView tvDialogTitle;
        MyFontButton btnAddCity;

        final Dialog cityDialog = new Dialog(this);

        cityDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        cityDialog.setContentView(R.layout.dialog_country_code);
        btnAddCity = (MyFontButton) cityDialog.findViewById(R.id.btnAddCity);
        btnAddCity.setVisibility(View.VISIBLE);
        tvDialogTitle = cityDialog.findViewById(R.id.tvDialogTitle);
        tvDialogTitle.setText(getResources().getString(R.string.selectoprator));
        rcvCountryCode = (RecyclerView) cityDialog.findViewById(R.id.rcvCountryCode);
        etCountrySearch = (MyFontEdittextView) cityDialog.findViewById(R.id.etCountrySearch);
        etCountrySearch.setHint(getResources().getString(R.string.searchoprator));
        rcvCountryCode.setLayoutManager(new LinearLayoutManager(this));
        opratorAdapter = new OpratorAdapter(opratorlist);
        rcvCountryCode.setAdapter(opratorAdapter);
        WindowManager.LayoutParams params = cityDialog.getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        cityDialog.getWindow().setAttributes(params);
        cityDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        btnAddCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cityDialog.dismiss();
                openRequestRegisterTypeDialog(getResources().getString(R.string
                        .msg_type_not_available_current_city));

            }
        });
        etCountrySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (opratorAdapter != null) {
                    opratorAdapter.getFilter().filter(s);
                } else {
                    Log.d("filter", "no filter availible");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                // do with text
            }
        });

        rcvCountryCode.addOnItemTouchListener(new RecyclerTouchListener(RegisterActivity.this,
                rcvCountryCode,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        tvopratorName.setText(opratorAdapter.getFilterResult().get(position)
                                .getFirstname());
                        oprators = opratorAdapter.getFilterResult().get(position);
                        tvopratorName.setTextColor(ResourcesCompat.getColor(getResources(), R
                                .color.color_app_text, null));
                        InputMethodManager inm = (InputMethodManager) getSystemService(Context
                                .INPUT_METHOD_SERVICE);
                        inm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        cityDialog.dismiss();
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }
                }));

        cityDialog.show();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.etAddress:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    //checkValidationForRegister();
                    hideKeyBord();
                    return true;
                }
                break;
            default:
                //Do something
                break;
        }
        return false;
    }

    @Override
    public void otpReceived(String otp) {
        if (customDialogVerifyDetail != null && customDialogVerifyDetail.isShowing()) {
            customDialogVerifyDetail.notifyDataSetChange(otp);
        }
    }


    private void enableRegisterButton() {
        btnRegisterDone.setClickable(true);
        btnRegisterDone.setBackground(AppCompatResources.getDrawable(this, R.drawable
                .selector_round_rect_shape_blue));
    }

    private void disableRegisterButton() {
        btnRegisterDone.setClickable(false);
        btnRegisterDone.setBackground(AppCompatResources.getDrawable(this, R.drawable
                .selector_round_rect_shape_blue_disable));

    }

    @Override
    public void onBackPressed() {
        if (isBackPressedOnce) {
            super.onBackPressed();
            backToMainActivity();
            return;
        } else {
            openDetailNotSaveDialog();
        }


    }

    private void backToMainActivity() {
        Intent sigInIntent = new Intent(this, MainActivity.class);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sigInIntent);
        finishAffinity();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);

    }

    private void openCameraPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources().getString(R.string
                .msg_reason_for_camera_permission), getString(R.string.text_i_am_sure), getString
                (R.string.text_re_try)) {
            @Override
            public void doWithEnable() {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest
                        .permission
                        .CAMERA, Manifest
                        .permission
                        .READ_EXTERNAL_STORAGE}, Const
                        .PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE);
                closedPermissionDialog();
            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
            }
        };
        customDialogEnable.show();
    }

    private void openPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources().getString(R.string
                .msg_reason_for_permission_location), getString(R.string.text_i_am_sure), getString
                (R.string.text_re_try)) {
            @Override
            public void doWithEnable() {
                ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest
                        .permission
                        .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Const
                        .PERMISSION_FOR_LOCATION);
                closedPermissionDialog();
            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
                finishAffinity();
            }
        };
        customDialogEnable.show();
    }

    private void openPermissionNotifyDialog(final int code) {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            return;
        }
        customDialogEnable = new CustomDialogEnable(this, getResources()
                .getString(R.string
                        .msg_permission_notification), getResources()
                .getString(R
                        .string.text_exit_caps), getResources().getString(R
                .string
                .text_settings)) {
            @Override
            public void doWithEnable() {
                closedPermissionDialog();
                startActivityForResult(getIntentForPermission(), code);

            }

            @Override
            public void doWithDisable() {
                closedPermissionDialog();
                finishAffinity();
            }
        };
        customDialogEnable.show();

    }

    private void closedPermissionDialog() {
        if (customDialogEnable != null && customDialogEnable.isShowing()) {
            customDialogEnable.dismiss();
            customDialogEnable = null;

        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, Const
                    .PERMISSION_FOR_LOCATION);
        } else {
            getServicesCountry();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Const.PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        openPhotoDialog();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest
                                .permission.CAMERA)) {
                            openCameraPermissionDialog();
                        } else {
                            closedPermissionDialog();
                            openPermissionNotifyDialog(Const
                                    .PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE);
                        }
                    } else if (grantResults[1] == PackageManager.PERMISSION_DENIED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest
                                .permission.READ_EXTERNAL_STORAGE)) {
                            openCameraPermissionDialog();
                        } else {
                            closedPermissionDialog();
                            openPermissionNotifyDialog(Const
                                    .PERMISSION_FOR_CAMERA_AND_EXTERNAL_STORAGE);
                        }
                    }
                    break;
                }
            case Const.PERMISSION_FOR_LOCATION:
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        getServicesCountry();
                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest
                                .permission.ACCESS_COARSE_LOCATION) && ActivityCompat
                                .shouldShowRequestPermissionRationale(this, Manifest
                                        .permission.ACCESS_FINE_LOCATION)) {
                            openPermissionDialog();
                        } else {
                            openPermissionNotifyDialog(Const.PERMISSION_FOR_LOCATION);
                        }
                    }
                    break;
                }
            default:
                //Do something
                break;

        }
    }

    private void getCityListSelectedCountry(List<City> cities,List<Oprators> oprator) {
        tvRegisterCityName.setText(getResources().getString(R.string
                .text_hint_select_city));
        tvRegisterCityName.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.color_app_label, null));
        tvopratorName.setText(getResources().getString(R.string
                .selectoprator));
        tvopratorName.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.color_app_label, null));
        cityList.clear();
        opratorlist.clear();

        if (cities != null && !cities.isEmpty()) {
            cityList.addAll(cities);
        }
        if (oprator != null && !oprator.isEmpty()) {
            opratorlist.addAll(oprator);
        }
        Utils.hideCustomProgressDialog();
        if (cityList.isEmpty()) {
            if (Utils.isGpsEnable(RegisterActivity.this)) {
                openRequestRegisterTypeDialog(getResources().getString(R.string
                        .msg_type_not_available_current_country));
            }
        }

    }

    private void setCountry(int position) {
        tvCountryCode.setText((countryList.get(position).getCountryphonecode()));
        phoneNumberLength = countryList.get(position).getPhoneNumberLength();
        phoneNumberMinLength = countryList.get(position).getPhoneNumberMinLength();
        if (!selectedCountryPhoneCode.equalsIgnoreCase(countryList.get(position)
                .getCountryphonecode())) {
            etContactNumber.getText().clear();
            selectedCountryPhoneCode = countryList.get(position).getCountryphonecode();
        }
        setContactNoLength(phoneNumberLength);
        country = countryList.get(position);
        tvRegisterCountryName.setText(countryList.get(position).getCountryname());
        tvRegisterCountryName.setTextColor(ResourcesCompat.getColor(getResources(), R.color
                .color_app_text, null));
        getCityListSelectedCountry(countryList.get(position).getCities(),countryList.get(position).getOprators());
    }

    private void setContactNoLength(int length) {
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(length);
        etContactNumber.setFilters(FilterArray);
    }

    private void openRequestRegisterTypeDialog(String message) {
        customDialogBigLabel = new CustomDialogBigLabel(this, getResources().getString(R.string
                .text_register_type), message, getResources().getString(R.string
                .text_email), getResources().getString(R.string.text_cancel)) {
            @Override
            public void positiveButton() {
                dismiss();
                contactUsWithEmail(preferenceHelper.getContactUsEmail());

            }

            @Override
            public void negativeButton() {
                dismiss();
            }
        };
        customDialogBigLabel.show();
    }

    private void OTPVerify() {
//        Utils.showCustomProgressDialog(this, getResources().getString(R.string.msg_loading),
//                false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.EMAIL, etEmail.getText());
            jsonObject.put(Const.Params.PHONE, etContactNumber.getText());
            jsonObject.put(Const.Params.COUNTRY_PHONE_CODE, country.getCountryphonecode());
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER);
            Call<VerificationResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .verification(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<VerificationResponse>() {
                @Override
                public void onResponse(Call<VerificationResponse> call,
                                       Response<VerificationResponse> response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            Utils.hideCustomProgressDialog();
                            otpForEmail = response.body().getOtpForEmail();
                            otpForSMS = response.body().getOtpForSMS();
                            openOTPVerifyDialog();

                        } else {
                            Utils.hideCustomProgressDialog();
                            Utils.showErrorToast(response.body().getErrorCode(), RegisterActivity
                                    .this);
                        }
                    }


                }

                @Override
                public void onFailure(Call<VerificationResponse> call, Throwable t) {
                    AppLog.handleThrowable(ProfileActivity.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.REGISTER_ACTIVITY, e);
        }
    }

    private void openOTPVerifyDialog() {
        if (customDialogVerifyDetail != null && customDialogVerifyDetail.isShowing()) {
            return;
        }
        customDialogVerifyDetail = new CustomDialogVerifyDetail(this, isEmailVerify, isSMSVerify) {
            @Override
            public void doWithSubmit(EditText etEmailVerify, EditText etSMSVerify) {

                if (isEmailVerify && isSMSVerify) {
                    if (otpForEmail.equals(etEmailVerify.getText().toString()) && otpForSMS.equals
                            (etSMSVerify.getText().toString())) {
                        dismiss();
                        register(loginType);
                    } else {
                        Utils.showToast(getResources().getString(R.string.msg_otp_wrong),
                                RegisterActivity.this);
                    }


                } else if (isEmailVerify) {
                    if (otpForEmail.equals(etEmailVerify.getText().toString())) {
                        dismiss();
                        register(loginType);
                    } else {
                        Utils.showToast(getResources().getString(R.string.msg_otp_wrong),
                                RegisterActivity.this);
                    }


                } else {
                    if (otpForSMS.equals(etSMSVerify.getText().toString())) {
                        dismiss();
                        register(loginType);
                    } else {
                        Utils.showToast(getResources().getString(R.string.msg_otp_wrong),
                                RegisterActivity.this);
                    }
                }


            }

            @Override
            public void doCancel() {
                customDialogVerifyDetail.dismiss();
            }
        };
        customDialogVerifyDetail.show();
    }

    private void openDetailNotSaveDialog() {
        CustomDialogBigLabel detailNotSaveDialog = new CustomDialogBigLabel(this, getResources()
                .getString(R.string.msg_are_you_sure),
                getResources().getString(R.string.msg_not_save)
                , getResources().getString(R.string.text_yes), getResources().getString(R.string
                .text_no)) {
            @Override
            public void positiveButton() {
                this.dismiss();
                isBackPressedOnce = true;
                RegisterActivity.this.onBackPressed();

            }

            @Override
            public void negativeButton() {
                this.dismiss();
            }
        };
        detailNotSaveDialog.show();
    }

    private void getServicesCountry() {
        //Utils.showCustomProgressDialog(this, "", false, null);
        Call<CountriesResponse> call = ApiClient.getClient().create(ApiInterface.class)
                .getCountries();
        call.enqueue(new Callback<CountriesResponse>() {
            @Override
            public void onResponse(Call<CountriesResponse> call, Response<CountriesResponse>
                    response) {
                if (parseContent.isSuccessful(response)) {
                    Utils.hideCustomProgressDialog();
                    Log.e("countryr",response.body().toString());
                    countryList.addAll(response.body().getCountry());
                    Log.e("countryl", String.valueOf(countryList.size()));
                    locationHelper.getLastLocation(RegisterActivity.this, new
                            OnSuccessListener<Location>() {
                                @Override
                                public void onSuccess(Location location) {
                                    lastLocation = location;
                                    AppLog.Log("GET LOCATION IS :", "" + lastLocation);
                                    if (!countryList.isEmpty()) {
                                        if (lastLocation != null) {
                                            new GetCityAndCountryTask().execute();
                                        } else {
                                            setCountry(0);
                                        }
                                    }
                                }
                            });


                }
            }

            @Override
            public void onFailure(Call<CountriesResponse> call, Throwable t) {
                Log.e("countryf", String.valueOf(t));
                AppLog.handleThrowable(ProfileActivity.class.getSimpleName(), t);
            }
        });
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


    private void initGenderSelection() {
        selectGender = findViewById(R.id.selectGender);
        String[] typedArray = getResources().getStringArray(R.array.gender);
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.item_spinner,
                typedArray);
        selectGender.setAdapter(arrayAdapter);
        selectGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String[] typedArray = getResources().getStringArray(R.array.gender_english);
                gender = typedArray[i].toString();
                AppLog.Log("GENDER", gender);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    protected class GetCityAndCountryTask extends AsyncTask<String, Void, Address> {

        @Override
        protected Address doInBackground(String... params) {

            Geocoder geocoder = new Geocoder(RegisterActivity.this, new Locale("en_US"));
            try {
                List<Address> addressList = geocoder.getFromLocation(lastLocation.getLatitude(),
                        lastLocation.getLongitude(), 1);
                if (addressList != null && !addressList.isEmpty()) {

                    Address address = addressList.get(0);
                    return address;
                }

            } catch (IOException e) {
                e.printStackTrace();
                AppLog.handleException(Const.Tag.REGISTER_ACTIVITY, e);
                publishProgress();

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            getCountryCodeList("Bangladesh");
        }

        @Override
        protected void onPostExecute(Address address) {
            if (address != null) {
                String countryName = address.getCountryName();
                Log.e("1614",countryName);
                getCountryCodeList(countryName);
                String currentCityName;
                if (address.getLocality() != null) {
                    currentCityName = address.getLocality();
                } else if (address.getSubAdminArea() != null) {
                    currentCityName = address.getSubAdminArea();
                } else {
                    currentCityName = address.getAdminArea();
                }
                AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "countryName= " + countryName);
                AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "currentCityName= " + currentCityName);
            } else {
                getCountryCodeList("Bangladesh");
            }
        }
    }

    private void initFBLogin() {
        callbackManager = CallbackManager.Factory.create();
        LoginButton faceBookLogin = findViewById(R.id.btnFbLogin);
        faceBookLogin.setReadPermissions(Arrays.asList("email", "public_profile"));
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       final AccessToken currentAccessToken) {
                if (currentAccessToken != null && !currentAccessToken.isExpired()) {
                    getFacebookProfileDetail(AccessToken.getCurrentAccessToken());

                }


            }
        };

        accessTokenTracker.startTracking();


    }

    private void initGoogleLogin() {
        SignInButton btnGoogleSingIn;
        btnGoogleSingIn = findViewById(R.id.btnGoogleLogin);
        btnGoogleSingIn.setSize(SignInButton.SIZE_WIDE);
        btnGoogleSingIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOutFromGoogle();
                googleSignIn();
            }
        });
    }


}