package com.elluminati.eber.driver.parse;

import com.elluminati.eber.driver.utils.AppLog;
import com.elluminati.eber.driver.utils.Const;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ParserPathJSON {

    public static final String TAG = "ParserPathJSON";

    public List<List<HashMap<String, String>>> parse(JSONObject jObject) {
        List<List<HashMap<String, String>>> routes = new ArrayList<List<HashMap<String,
                String>>>();
        JSONArray jRoutes = null;
        JSONArray jLegs = null;
        JSONArray jSteps = null;
        try {
            jRoutes = jObject.getJSONArray(Const.google.ROUTES);
            /** Traversing all routes */
            int jRoutesSize = jRoutes.length();
            for (int i = 0; i < jRoutesSize; i++) {
                jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray(Const.google.LEGS);
                List<HashMap<String, String>> path = new ArrayList<HashMap<String, String>>();

                /** Traversing all legs */
                int jLegsSize = jLegs.length();
                for (int j = 0; j < jLegsSize; j++) {
                    jSteps = ((JSONObject) jLegs.get(j)).getJSONArray(Const.google.STEPS);

                    /** Traversing all steps */
                    int jStepsSize = jSteps.length();
                    for (int k = 0; k < jStepsSize; k++) {
                        String polyline = "";
                        polyline = (String) ((JSONObject) ((JSONObject) jSteps
                                .get(k)).get(Const.google.POLYLINE)).get(Const.google.POINTS);
                        List<LatLng> list = decodePoly(polyline);

                        /** Traversing all points */
                        int listSize = list.size();
                        for (int l = 0; l < listSize; l++) {
                            HashMap<String, String> hm = new HashMap<String, String>();
                            hm.put(Const.google.LAT,
                                    Double.toString(list.get(l).latitude));
                            hm.put(Const.google.LNG,
                                    Double.toString(list.get(l).longitude));
                            path.add(hm);
                        }
                    }
                    routes.add(path);
                }
            }

        } catch (JSONException e) {
            AppLog.handleException(TAG, e);
        } catch (Exception e) {
        }
        return routes;
    }

    /**
     * decoding-polylines-from-google-maps-direction-api-with-java
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}