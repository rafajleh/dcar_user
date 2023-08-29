package com.elluminati.eber.driver.mapUtils;

import android.app.Activity;
import android.os.AsyncTask;
import androidx.core.content.res.ResourcesCompat;

import com.elluminati.eber.driver.R;
import com.elluminati.eber.driver.parse.ApiClient;
import com.elluminati.eber.driver.parse.ApiInterface;
import com.elluminati.eber.driver.parse.ParserPathJSON;
import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.elluminati.eber.driver.utils.PreferenceHelper;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PathDrawOnMap  {


    public static final String TAG = "PathDrawOnMap";
    private String url;
    private GetResultFromPathDraw getResultFromPathDraw;
    private Activity activity;
    private ParserPathJSON parserPathJSON;
    private PreferenceHelper preferenceHelper;
    private String response;

    public PathDrawOnMap(Activity activity, GetResultFromPathDraw getResultFromPathDraw) {
        // TODO Auto-generated constructor stub
        this.getResultFromPathDraw = getResultFromPathDraw;
        this.activity = activity;
        preferenceHelper = PreferenceHelper.getInstance(activity);

    }

    public void getPathDrawOnMap(LatLng pickUpLatLng, LatLng destinationLatLng, boolean
            isWantToDraw) {

        if (pickUpLatLng != null & destinationLatLng != null & isWantToDraw) {

            String origins = String.valueOf(pickUpLatLng.latitude + "," + pickUpLatLng.longitude);
            String destination = destinationLatLng.latitude + "," + destinationLatLng.longitude;
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Const.google.ORIGIN, origins);
            hashMap.put(Const.google.DESTINATION, destination);
            hashMap.put(Const.google.KEY, preferenceHelper.getGoogleServerKey());
            ApiInterface apiInterface = new ApiClient().changeApiBaseUrl(Const.GOOGLE_API_URL)
                    .create(ApiInterface.class);
            Call<ResponseBody> call = apiInterface.getGoogleDirection(hashMap);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            new ParserTask(Const.ServiceCode.PATH_DRAW).execute(response.body()
                                    .string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    AppLog.handleThrowable(PathDrawOnMap.class.getSimpleName(), t);
                }
            });
        }
    }



    public interface GetResultFromPathDraw {
        void getPathResult(PolylineOptions polylineOptions, String routes, int serviceCode);


    }

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,
            String>>>> {
        int serviceCode;

        public ParserTask(int serviceCode) {
            this.serviceCode = serviceCode;
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {
            // TODO Auto-generated method stub
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                parserPathJSON = new ParserPathJSON();
                routes = parserPathJSON.parse(jObject);
            } catch (Exception e) {
                AppLog.handleException(TAG, e);
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            // TODO Auto-generated method stub
            super.onPostExecute(routes);
            AppLog.Log("ROUTE", routes + "");
            ArrayList<LatLng> points;
            PolylineOptions polylineOptions = null;
            // traversing through routes
            int routeSize = routes.size();
            for (int i = 0; i < routeSize; i++) {
                points = new ArrayList<LatLng>();
                polylineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                int pathSize = path.size();
                for (int j = 0; j < pathSize; j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get(Const.google.LAT));
                    double lng = Double.parseDouble(point.get(Const.google.LNG));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }
                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(ResourcesCompat.getColor(activity.getResources(), R.color
                        .color_app_path, null));

            }
            getResultFromPathDraw.getPathResult(polylineOptions, response, serviceCode);
        }
    }

}
