package com.elluminati.eber.driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.elluminati.eber.driver.adapter.PlaceAutocompleteAdapter;
import com.elluminati.eber.driver.components.CustomEventMapView;
import com.elluminati.eber.driver.components.MyFontAutocompleteView;
import com.elluminati.eber.driver.components.MyFontButton;
import com.elluminati.eber.driver.models.responsemodels.IsSuccessResponse;
import com.elluminati.eber.driver.models.singleton.AddressUtils;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.LocationHelper;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.elluminati.eber.driver.utils.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetDestination extends BaseAppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback, LocationHelper.OnLocationReceived {

    //    private CustomAddressChooseDialog dialogFavAddress;
    public AddressUtils addressUtils;

    private static boolean isMapTouched = true;
    private MyFontAutocompleteView acFavAddress;
    private CustomEventMapView favAddressMapView;

    private GoogleMap googleMap;
    private LocationHelper locationHelper;
    private FrameLayout frameMap;
    private CameraPosition cameraPosition;
    private PlaceAutocompleteAdapter autocompleteAdapter;
    private String trimedFavAddress, longFavAddress;
    private LatLng addressLatlng;
    private MyFontButton btnConfirmFavAddress;
    private ImageView ivClearAddress;
    private PreferenceHelper preferenceHelper;
    private FloatingActionButton ivDialogLocation;
    private String address;
    private int addressRequestCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_destination);


        initToolBar();
        setTitleOnToolbar(getResources().getString(R.string.setdestination));

        addressUtils = AddressUtils.getInstance();
        preferenceHelper = PreferenceHelper.getInstance(this);

        frameMap = (FrameLayout) findViewById(R.id.frameMap);
        favAddressMapView = (CustomEventMapView) findViewById(R.id.favAddressMapView);
        acFavAddress = (MyFontAutocompleteView) findViewById(R.id.acFavAddress);
        btnConfirmFavAddress = (MyFontButton) findViewById(R.id.btnConfirmFavAddress);
        ivClearAddress = (ImageView) findViewById(R.id.ivClearAddress);
        ivDialogLocation = (FloatingActionButton) findViewById(R.id.ivDialogLocation);

        btnConfirmFavAddress.setOnClickListener(this);
        ivClearAddress.setOnClickListener(this);
        ivDialogLocation.setOnClickListener(this);
        locationHelper = new LocationHelper(this);
        locationHelper.setLocationReceivedLister(this);
        favAddressMapView.onCreate(savedInstanceState);
        favAddressMapView.getMapAsync(this);
        getTouchEventOnMap();

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
    public void onAdminApproved() {

    }

    @Override
    public void onAdminDeclined() {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setMapTouched(true);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    public void onprofileApprove() {

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {

    }

    @Override
    public void onGpsConnectionChanged(boolean isConnected) {

    }


    @Override
    protected void onStart() {
        favAddressMapView.onResume();
        super.onStart();
        favAddressMapView.onStart();
        locationHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationHelper.onStop();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        favAddressMapView.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnConfirmFavAddress:
                saveFavouriteAddress(addressRequestCode, longFavAddress, addressLatlng);
                break;
            case R.id.ivClearAddress:
                acFavAddress.getText().clear();
                acFavAddress.requestFocus();
                showKeybord();
                break;
            case R.id.ivDialogLocation:
                locationHelper.getLastLocation(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            moveCameraToLocation(location);
                        } else {
                            Utils.showToast(getResources().getString(R.string
                                    .text_location_not_found), SetDestination.this);
                        }


                    }
                });

                break;
            default:
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        AppLog.Log(Const.Tag.MAP_FRAGMENT, "GoogleMapReady");
        setUpMap();
        this.googleMap.clear();
        locationHelper.getLastLocation(SetDestination.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    autocompleteAdapter = new PlaceAutocompleteAdapter(SetDestination.this);
                    initAutocomplete();
                    setPlaceFilter(AddressUtils.getInstance().getCountryCode(), location);
                    if (!TextUtils.isEmpty(address) && addressLatlng != null) {
                        setAddress(address);
                        //setMapTouched(true);
                        Location location1 = new Location("selectedAddress");
                        location1.setLongitude(addressLatlng.longitude);
                        location1.setLatitude(addressLatlng.latitude);
                        moveCameraToLocation(location1);
                    } else {
                        if(preferenceHelper.getLAtEndtrip()!="")
                        {
                            Location location2 = new Location("selectedAddress");
                            location2.setLatitude(Double.parseDouble(preferenceHelper.getLAtEndtrip()));
                            location2.setLongitude(Double.parseDouble(preferenceHelper.getLngEndtrip()));
                            moveCameraToLocation(location2);
                        }
                        else
                        {
                            moveCameraToLocation(location);
                        }

                    }


                } else {
                    Utils.showToast(getResources().getString(R.string
                            .text_location_not_found), SetDestination.this);
                }
            }
        });
    }

    public void getTouchEventOnMap() {
        frameMap.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN | MotionEvent.ACTION_MOVE:
                        setMapTouched(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        setMapTouched(true);
                        break;
                }
                return true;
            }
        });
    }

    private static void setMapTouched(boolean isTouched) {
        isMapTouched = isTouched;
    }

    private void setUpMap() {

        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.getUiSettings().setMapToolbarEnabled(false);
        this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        this.googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            boolean doNotMoveCameraToCenterMarker = true;

            public boolean onMarkerClick(Marker marker) {
                return doNotMoveCameraToCenterMarker;
            }
        });

        this.googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                if (isMapTouched) {

                    if(preferenceHelper.getLAtEndtrip()!="")
                    {
                        addressLatlng = new LatLng(Double.parseDouble(preferenceHelper.getLAtEndtrip()), Double.parseDouble(preferenceHelper.getLngEndtrip()));
                        new GetPickUpAddressFromLocation().executeOnExecutor(Executors
                                .newSingleThreadExecutor(), addressLatlng);
                        setMapTouched(false);
                    }
                    else
                    {
                        addressLatlng = googleMap.getCameraPosition().target;
                        new GetPickUpAddressFromLocation().executeOnExecutor(Executors
                                .newSingleThreadExecutor(), addressLatlng);

                        setMapTouched(false);
                    }


                } else {


                    addressLatlng = googleMap.getCameraPosition().target;
                    new GetPickUpAddressFromLocation().executeOnExecutor(Executors
                            .newSingleThreadExecutor(), addressLatlng);

                    setMapTouched(false);
                }

            }

        });
    }

    @Override
    public void onLocationChanged(Location location) {
        // do something
    }

    @Override
    public void onConnected(Bundle bundle) {
        // do something
    }

    private void setPlaceFilter(String countryCode, Location currentLocation) {
        if (autocompleteAdapter != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(),
                    currentLocation.getLongitude());
            RectangularBounds latLngBounds = RectangularBounds.newInstance(
                    latLng,
                    latLng);
            autocompleteAdapter.setBounds(latLngBounds);
            autocompleteAdapter.setPlaceFilter(countryCode);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void initAutocomplete() {
        acFavAddress.setAdapter(autocompleteAdapter);
        acFavAddress.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hideKeybord();
                longFavAddress = acFavAddress.getText().toString();
                trimedFavAddress = Utils.trimString(acFavAddress.getText().toString());
                getLocationFromPlaceId(autocompleteAdapter.getPlaceId(position));
            }
        });

        acFavAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    ivClearAddress.setVisibility(View.VISIBLE);
                } else {
                    ivClearAddress.setVisibility(View.INVISIBLE);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void hideKeybord() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(acFavAddress.getWindowToken(), 0);
    }

    private void showKeybord() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity
                .INPUT_METHOD_SERVICE);
        imm.showSoftInput(acFavAddress, 0);
    }

    private void moveCameraToLocation(Location currentLocation) {
        if (currentLocation != null) {
            LatLng latLngOfMyLocation = new LatLng(currentLocation.getLatitude(), currentLocation
                    .getLongitude());
            cameraPosition = new CameraPosition.Builder().target(latLngOfMyLocation).zoom(17)
                    .build();
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void setAddress(String address) {
        acFavAddress.setFocusable(false);
        longFavAddress = address;
        acFavAddress.setFocusableInTouchMode(false);
        trimedFavAddress = Utils.trimString(address);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            acFavAddress.setText(trimedFavAddress, false);
        } else {
            acFavAddress.setText(trimedFavAddress);
        }
        acFavAddress.setFocusable(true);
        acFavAddress.setFocusableInTouchMode(true);
    }

    private void getLocationFromPlaceId(String placeId) {

        if (!TextUtils.isEmpty(placeId) && autocompleteAdapter != null) {
            autocompleteAdapter.getFetchPlaceRequest(placeId,
                    new OnSuccessListener<FetchPlaceResponse>() {
                        @Override
                        public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
                            Place place = fetchPlaceResponse.getPlace();
                            addressLatlng = place.getLatLng();
                            Location location = new Location("");
                            location.setLatitude(addressLatlng.latitude);
                            location.setLongitude(addressLatlng.longitude);
                            moveCameraToLocation(location);
                        }
                    });
        } else {
            Utils.showToast(getResources().getString(R.string.error_place_id), SetDestination.this);
        }
    }


    private class GetPickUpAddressFromLocation extends AsyncTask<LatLng, Integer, Address> {

        @Override
        protected Address doInBackground(LatLng... latLngs) {
            Geocoder gCoder = new Geocoder(SetDestination.this, new Locale("en_US"));
            LatLng latLng = latLngs[0];
            try {
                final List<Address> list = gCoder.getFromLocation(latLng.latitude, latLng
                        .longitude, 1);
                if (list != null && !list.isEmpty()) {
                    return list.get(0);
                }
            } catch (IOException e) {
                AppLog.handleException(Const.Tag.MAP_FRAGMENT, e);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Address address) {
            super.onPostExecute(address);
            StringBuilder sb = new StringBuilder();
            if (address != null) {
                AppLog.Log("ADDRESS", address.toString());
                if (address.getMaxAddressLineIndex() > 0) {
                    int addressLineSize = address.getMaxAddressLineIndex();
                    for (int i = 0; i < addressLineSize; i++) {
                        sb.append(address.getAddressLine(i)).append(",").append("\n");
                    }
                    sb.append(address.getCountryName());
                } else {
                    sb.append(address.getAddressLine(0));
                }
                String strAddress = sb.toString();
                strAddress = strAddress.replace(",null", "");
                strAddress = strAddress.replace("null", "");
                strAddress = strAddress.replace("Unnamed", "");
                strAddress = strAddress.replace("\n", "");
                if (!TextUtils.isEmpty(strAddress)) {
                    setAddress(strAddress);
                }
            }
        }
    }



  /*  private void openFavAddressDialog(final int addressRequestCode, LatLng addressLatLng, String
            address) {



        if (dialogFavAddress != null && dialogFavAddress.isShowing()) {
            return;
        }


        dialogFavAddress = new CustomAddressChooseDialog(this, addressLatLng, address,
                addressRequestCode) {

            @Override
            public void setSavedData(String address, LatLng latLng, int addressRequestCode) {

                switch (addressRequestCode) {

                 *//*   case Const.DESTINATION_ADDRESS:
                        setDestinationAddress(address);
                        addressUtils.setDestinationLatLng(latLng);
                        goWithConfirmAddress();
                        break;*//*
                    default:
                        saveFavouriteAddress(addressRequestCode, address, latLng);
                        break;
                }


            }
        };

        dialogFavAddress.show();

    }*/


    private void saveFavouriteAddress(int addressRequestCode, String address, LatLng latLng) {


        preferenceHelper.putaddressEndtrip(address);
        addressUtils.setDestinationLatLng(latLng);

        Utils.showCustomProgressDialog(this, "", false, null);

        JSONObject jsonObject = new JSONObject();


        try {
            jsonObject.put(Const.Params.TOKEN, preferenceHelper.getSessionToken());
            jsonObject.put(Const.Params.PROVIDER_ID, preferenceHelper.getProviderId());
            jsonObject.put(Const.Params.ENDADDRESS, address);
            jsonObject.put(Const.Params.ENDLNG, addressUtils.getDestinationLatLng().longitude);
            jsonObject.put(Const.Params.ENDLAT, addressUtils.getDestinationLatLng().latitude);


            Call<IsSuccessResponse> call = ApiClient.getClient().create(ApiInterface.class).setdestination
                    (ApiClient.makeJSONRequestBody(jsonObject));

            call.enqueue(new Callback<IsSuccessResponse>() {
                @Override
                public void onResponse(Call<IsSuccessResponse> call, Response<IsSuccessResponse> response) {


                    if (parseContent.isSuccessful(response)) {
                        Utils.hideCustomProgressDialog();
                        preferenceHelper.putLatEndtrip(addressUtils.getDestinationLatLng().latitude);
                        preferenceHelper.putLngEndtrip(addressUtils.getDestinationLatLng().longitude);

                        Utils.showMessageToast(response.body().getMessage(),
                                SetDestination.this);
                        onBackPressed();
                    } else {
                        Utils.showErrorToast(response.body().getErrorCode(), SetDestination.this);
                    }

                }

                @Override
                public void onFailure(Call<IsSuccessResponse> call, Throwable t) {
                    Utils.hideCustomProgressDialog();
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
            Utils.hideCustomProgressDialog();
        }


    }
}