package com.elluminati.eber.driver;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.elluminati.eber.driver.components.CustomDialogVerifyAccount;
import com.elluminati.eber.driver.components.MyAppTitleFontTextView;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.components.MyFontEdittextView;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.responsemodels.ProviderDataResponse;
import com.elluminati.eber.driver.models.singleton.CurrentTrip;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.GoogleClientHelper;
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
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends BaseAppCompatActivity implements TextView
        .OnEditorActionListener, GoogleApiClient.OnConnectionFailedListener {
    private MyFontEdittextView etSignInEmail, etSignInPassword;
    private TextView tvForgotPassword;
    private MyFontButton btnSignIn;
    private MyAppTitleFontTextView tvGotoRegister;
    private CustomDialogVerifyAccount dialogForgotPassword;
    private CallbackManager callbackManager;
    private String socialId, socialEmail;
    private GoogleApiClient googleApiClient;
    private AccessTokenTracker accessTokenTracker;
    private LinearLayout llSocialLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        CurrentTrip.getInstance().clear();
        googleApiClient = new GoogleClientHelper(this).build();
        btnSignIn = (MyFontButton) findViewById(R.id.btnSignIn);
        btnSignIn.setOnClickListener(this);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(this);

        etSignInEmail = (MyFontEdittextView) findViewById(R.id.etSignInEmail);
        etSignInPassword = (MyFontEdittextView) findViewById(R.id.etSignInPassword);
        etSignInPassword.setOnEditorActionListener(this);

        tvGotoRegister = (MyAppTitleFontTextView) findViewById(R.id.tvGoRegister);
        tvGotoRegister.setOnClickListener(this);

        llSocialLogin=findViewById(R.id.llSocialLogin);

        if (preferenceHelper.getIsSocial())
        {
            llSocialLogin.setVisibility(View.VISIBLE);
        }
        else
        {
            llSocialLogin.setVisibility(View.GONE);
        }

        initFBLogin();
        initGoogleLogin();

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null && !googleApiClient.isConnected()) {
            googleApiClient.connect(GoogleApiClient.SIGN_IN_MODE_OPTIONAL);
        }

    }

    @Override
    protected void onStop() {
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setConnectivityListener(this);
        setAdminApprovedListener(this);
    }


    /**
     * Facebook login part
     */
    /*private void registerCallForFacebook() {
        LoginManager.getInstance().registerCallback(callbackManager, new
                FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Utils.showCustomProgressDialog(SignInActivity.this, "", false, null);
                        getFacebookProfileDetail(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        AppLog.Log("Facebook :", "" + error);
                        Utils.showToast(getString(R.string.message_can_not_signin_facebook),
                                SignInActivity.this);
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
                    if (object.has("email")) {
                        socialEmail = object.getString("email");
                    }
                    socialId = object.getString("id");
                    LoginManager.getInstance().logOut();
                    signIn(Const.SOCIAL_FACEBOOK);

                } catch (JSONException e) {
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
                socialId = account.getId();
                socialEmail = account.getEmail();
                signIn(Const.SOCIAL_GOOGLE);
            } catch (NullPointerException e) {
                AppLog.handleException(Const.Tag.SIGN_IN_ACTIVITY, e);
            }
        } else {
            AppLog.Log("Error", result.getStatus().toString());
            Utils.showToast(getString(R.string.message_can_not_signin_google), SignInActivity.this);
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


    private void signIn(String loginType) {

        AppLog.Log(Const.Tag.SIGN_IN_ACTIVITY, "SingIn valid");
        Utils.showCustomProgressDialog(this, getResources().getString(R.string
                .msg_waiting_for_sing_in), false, null);

        JSONObject jsonObject = new JSONObject();


        try {
            if (loginType.equalsIgnoreCase(Const.MANUAL)) {
                jsonObject.put(Const.Params.PASSWORD, etSignInPassword.getText()
                        .toString());
                jsonObject.put(Const.Params.EMAIL, etSignInEmail.getText().toString());
                FirebaseAuth.getInstance().signOut();
            } else {
                jsonObject.put(Const.Params.PASSWORD, "");
                jsonObject.put(Const.Params.SOCIAL_UNIQUE_ID, socialId);
                jsonObject.put(Const.Params.EMAIL, socialEmail);
            }
            jsonObject.put(Const.Params.DEVICE_TYPE, Const.DEVICE_TYPE_ANDROID);

            jsonObject.put(Const.Params.DEVICE_TOKEN, preferenceHelper.getDeviceToken());
            jsonObject.put(Const.Params.LOGIN_BY, loginType);
            jsonObject.put(Const.Params.APP_VERSION, getAppVersion());
            jsonObject.put(Const.Params.APP_NAME, getResources().getString(R.string.app_name));

            Call<ProviderDataResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .login(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<ProviderDataResponse>() {
                @Override
                public void onResponse(Call<ProviderDataResponse> call,
                                       Response<ProviderDataResponse> response) {
                    if (parseContent.isSuccessful(response)) {
                        Log.e("255",response.body().toString());
                        if (parseContent.saveProviderData(response.body(), true)) {
                            AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "LogIn Success");
                            CurrentTrip.getInstance().clear();
                            Utils.hideCustomProgressDialog();
                            Log.e(TAG, "onResponse: "+response.body().getIspaidforregister() );
                            moveWithUserSpecificPreference();
                        } else {
                            Utils.hideCustomProgressDialog();
                            AppLog.Log(Const.Tag.REGISTER_ACTIVITY, "LogIn Failed");
                        }
                    }

                }

                @Override
                public void onFailure(Call<ProviderDataResponse> call, Throwable t) {
                    AppLog.handleThrowable(SignInActivity.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.SIGN_IN_ACTIVITY, e);
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:
                if (isValidate()) {
                    signIn(Const.MANUAL);
                }
                break;
            case R.id.tvGoRegister:
                goToRegisterActivity();
                break;
            case R.id.tvForgotPassword:
                openForgotPasswordDialog();
                break;
            default:
                // do wti default
                break;
        }

    }


    @Override
    protected boolean isValidate() {
        String msg = null;
        if (TextUtils.isEmpty(etSignInEmail.getText().toString().trim())) {
            msg = getString(R.string.msg_enter_email);
            etSignInEmail.requestFocus();
        } else if (!Utils.eMailValidation((etSignInEmail.getText().toString().trim()))) {
            msg = getString(R.string.msg_enter_valid_email);
            etSignInEmail.requestFocus();
        } else if (TextUtils.isEmpty(etSignInPassword.getText().toString().trim())) {
            msg = getString(R.string.msg_enter_password);
            etSignInPassword.requestFocus();
        } else if (etSignInPassword.getText().toString().trim().length() < 6) {
            msg = getString(R.string.msg_enter_valid_password);
            etSignInPassword.requestFocus();
        }

        if (msg != null) {
            Toast.makeText(SignInActivity.this, msg, Toast.LENGTH_SHORT).show();
            return false;

        }
        return true;
    }

    @Override
    public void goWithBackArrow() {
        // do with back arrow
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backToMainActivity();
    }

    private void backToMainActivity() {
        Intent sigInIntent = new Intent(this, MainActivity.class);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        sigInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sigInIntent);
        finishAffinity();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void openForgotPasswordDialog() {
        dialogForgotPassword = new CustomDialogVerifyAccount(this, getResources().getString(R.string
                .text_forgot_password), getResources().getString(R.string
                .text_send), getResources().getString(R.string
                .text_cancel), getResources().getString(R.string
                .text_hint_enter_email), true) {
            @Override
            public void doWithEnable(EditText editText) {
                if (Utils.eMailValidation(editText
                        .getText().toString())) {
                    forgotPassword((editText
                            .getText().toString()));
                } else {
                    Utils.showToast(getResources().getString(R.string.msg_enter_valid_email),
                            SignInActivity
                                    .this);
                }
            }

            @Override
            public void doWithDisable() {
                dismiss();
            }

            @Override
            public void clickOnText() {
                // do with click
            }
        };
        dialogForgotPassword.show();
    }

    private void forgotPassword(String email) {
        Utils.showCustomProgressDialog(this, getResources().getString(R.string.msg_loading),
                false, null);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Const.Params.EMAIL, email);
            jsonObject.put(Const.Params.TYPE, Const.PROVIDER);
            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class)
                    .forgotPassword(ApiClient.makeJSONRequestBody(jsonObject));
            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse>
                        response) {
                    if (parseContent.isSuccessful(response)) {
                        if (response.body().isSuccess()) {
                            if (dialogForgotPassword != null && dialogForgotPassword.isShowing()) {
                                dialogForgotPassword.dismiss();
                            }
                            Utils.showMessageToast(response.body().getMessage(), SignInActivity
                                    .this);
                            Utils.hideCustomProgressDialog();
                        } else {
                            Utils.hideCustomProgressDialog();
                            Utils.showErrorToast(response.body().getErrorCode(), SignInActivity
                                    .this);
                        }
                    }
                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    AppLog.handleThrowable(SignInActivity.class.getSimpleName(), t);
                }
            });

        } catch (JSONException e) {
            AppLog.handleException(Const.Tag.SIGN_IN_ACTIVITY, e);
        }


    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.etSignInPassword:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (isValidate()) {
                        signIn(Const.MANUAL);
                    }
                    return true;
                }
                break;
            default:
                // do with default
                break;

        }
        return false;
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.google.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (accessTokenTracker != null) {
            accessTokenTracker.stopTracking();
        }

    }
}
