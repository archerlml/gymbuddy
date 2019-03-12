package com.github.archerlml.gymbuddy.util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.github.archerlml.gymbuddy.application.GymBuddyApplication;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationUtil {

    public static final double MILE_TO_METER = 1609.344;
    public static final double METER_TO_FEET = 3.2808;

    /* reference: https://developer.android.com/reference/android/location/Location.html */
    public static float findDistanceBetween(double startLatitude,
                                            double startLongitude,
                                            double endLatitude,
                                            double endLongitude,
                                            float[] results) {
        Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude,
                results);

        // The computed distance is stored in results[0]
        // distance is in meters
        Log.i("distance = ", results[0]);
        return results[0];
    }

    /* reference: https://developer.android.com/reference/android/location/Address.html*/
    // recommended n, max number of addresses, is 1 to 5
    public static List<Address> findStreetAddress(Context context, double latitude, double longitude, int n /*max number of addresses to return*/) throws IOException {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, n);
        return addresses;
    }

    public static double convertMeterToFeet(double m) {
        return m * METER_TO_FEET;
    }

    public static boolean with30Feet(LatLng l1, LatLng l2) {
        return findDistanceBetween(l1.latitude, l1.longitude, l2.latitude, l2.longitude, new float[10]) < 100;
    }

    public static List<String> latlngToAddr(LatLng latLng) {
        List<String> result = new ArrayList<>();
        try {
            for (Address streetAddress : findStreetAddress(GymBuddyApplication.getApp(), latLng.latitude, latLng.longitude, 4)) {
                StringBuilder sb = new StringBuilder();
                for (int n = 0; n <= Math.min(streetAddress.getMaxAddressLineIndex(), 1); n++) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(streetAddress.getAddressLine(n));
                }
                String addr = com.github.archerlml.gymbuddy.util.Util.or(sb.toString(), streetAddress.getThoroughfare(), streetAddress.getSubThoroughfare());
                Log.i("addr = ", addr);
                if (addr != null) {
                    result.add(addr);
                }
            }
        } catch (IOException e) {
            Log.e(e.getMessage());
        }
        return result;
    }
}
