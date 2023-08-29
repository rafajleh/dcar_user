package com.elluminati.eber.driver.components;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.adapter.PlaceAutocompleteAdapter;
import com.elluminati.eber.driver.models.singleton.AddressUtils;
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

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public abstract class CustomAddressChooseDialog extends Dialog implements View.OnClickListener,
        OnMapReadyCallback, LocationHelper.OnLocationReceived{

    private static boolean isMapTouched = false;
    private MyFontAutocompleteView acFavAddress;
    private CustomEventMapView favAddressMapView;
    private ImageView ivDialogBack;
    private GoogleMap googleMap;
    private LocationHelper locationHelper;
    private Context context;
    private FrameLayout frameMap;
    private CameraPosition cameraPosition;
    private PlaceAutocompleteAdapter autocompleteAdapter;
    private AddressUtils addressUtils;
    private String trimedFavAddress, longFavAddress;
    private LatLng addressLatlng;
    private MyFontButton btnConfirmFavAddress;
    private ImageView ivClearAddress;
    private PreferenceHelper preferenceHelper;
    private FloatingActionButton ivDialogLocation;
    private String address;
    private int addressRequestCode;



    public CustomAddressChooseDialog(@NonNull Context context, LatLng addressLatlng, String
            address, int
                                             addressRequestCode) {
        super(context, android.R.style.Theme_Light_NoTitleBar);
        setContentView(R.layout.dialog_fav_address);
        this.context = context;
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        this.address = address;
        this.addressLatlng = addressLatlng;
        this.addressRequestCode = addressRequestCode;
        getWindow().setAttributes(params);
    }

    private static void setMapTouched(boolean isTouched) {
        isMapTouched = isTouched;
    }

    public abstract void setSavedData(String address, LatLng latLng, int addressRequestCode);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addressUtils = AddressUtils.getInstance();
        preferenceHelper = PreferenceHelper.getInstance(context);
        ivDialogBack = (ImageView) findViewById(R.id.ivDialogBack);
        frameMap = (FrameLayout) findViewById(R.id.frameMap);
        favAddressMapView = (CustomEventMapView) findViewById(R.id.favAddressMapView);
        acFavAddress = (MyFontAutocompleteView) findViewById(R.id.acFavAddress);
        btnConfirmFavAddress = (MyFontButton) findViewById(R.id.btnConfirmFavAddress);
        ivClearAddress = (ImageView) findViewById(R.id.ivClearAddress);
        ivDialogLocation = (FloatingActionButton) findViewById(R.id.ivDialogLocation);

        ivDialogBack.setOnClickListener(this);
        btnConfirmFavAddress.setOnClickListener(this);
        ivClearAddress.setOnClickListener(this);
        ivDialogLocation.setOnClickListener(this);
        locationHelper = new LocationHelper(context);
        locationHelper.setLocationReceivedLister(CustomAddressChooseDialog.this);
        favAddressMapView.onCreate(savedInstanceState);
        favAddressMapView.getMapAsync(CustomAddressChooseDialog.this);
        getTouchEventOnMap();

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
            case R.id.ivDialogBack:
                hideKeybord();
                dismiss();
                break;
            case R.id.btnConfirmFavAddress:
                setSavedData(longFavAddress, addressLatlng, addressRequestCode);
                dismiss();
                break;
            case R.id.ivClearAddress:
                acFavAddress.getText().clear();
                acFavAddress.requestFocus();
                showKeybord();
                break;
            case R.id.ivDialogLocation:
                locationHelper.getLastLocation(context, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            moveCameraToLocation(location);
                        } else {
                            Utils.showToast(context.getResources().getString(R.string
                                    .text_location_not_found), context);
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
        locationHelper.getLastLocation(getContext(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    autocompleteAdapter = new PlaceAutocompleteAdapter(context);
                    initAutocomplete();
                    setPlaceFilter(AddressUtils.getInstance().getCountryCode(), location);
                    if (!TextUtils.isEmpty(address) && addressLatlng != null) {
                        setAddress(address);
                        setMapTouched(true);
                        Location location1 = new Location("selectedAddress");
                        location1.setLongitude(addressLatlng.longitude);
                        location1.setLatitude(addressLatlng.latitude);
                        moveCameraToLocation(location1);
                    } else {
                        moveCameraToLocation(location);
                    }


                } else {
                    Utils.showToast(context.getResources().getString(R.string
                            .text_location_not_found), context);
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
                        setMapTouched(false);
                        break;
                }
                return true;
            }
        });
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

                if (!isMapTouched) {
                    addressLatlng = googleMap.getCameraPosition().target;
                    new GetPickUpAddressFromLocation().executeOnExecutor(Executors
                            .newSingleThreadExecutor(), addressLatlng);
                }
                setMapTouched(false);
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
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity
                .INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(acFavAddress.getWindowToken(), 0);
    }

    private void showKeybord() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity
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
            Utils.showToast(context.getResources().getString(R.string.error_place_id), context);
        }
    }


    private class GetPickUpAddressFromLocation extends AsyncTask<LatLng, Integer, Address> {

        @Override
        protected Address doInBackground(LatLng... latLngs) {
            Geocoder gCoder = new Geocoder(context, new Locale("en_US"));
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



}
