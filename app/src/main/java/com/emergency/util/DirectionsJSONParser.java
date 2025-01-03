package com.emergency.util;

import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DirectionsJSONParser {
    public List<LatLng> parse(JSONObject jObject) {
        List<LatLng> routes = new ArrayList<>();
        try {
            // First check the status
            String status = jObject.getString("status");
            if (!"OK".equals(status)) {
                Log.e("DirectionsParser", "Status not OK: " + status);
                return routes;
            }

            JSONArray routesArray = jObject.getJSONArray("routes");
            if (routesArray.length() == 0) {
                return routes;
            }

            // Get first route
            JSONObject route = routesArray.getJSONObject(0);

            // Get the overview polyline
            JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
            String encodedPath = overviewPolyline.getString("points");

            // Decode the polyline points
            routes = decodePoly(encodedPath);

        } catch (JSONException e) {
            Log.e("DirectionsParser", "Error parsing JSON", e);
        }
        return routes;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
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

            double latitude = lat * 1e-5;
            double longitude = lng * 1e-5;
            poly.add(new LatLng(latitude, longitude));
        }
        return poly;
    }
}