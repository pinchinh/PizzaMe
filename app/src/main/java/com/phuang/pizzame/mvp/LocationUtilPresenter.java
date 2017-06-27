package com.phuang.pizzame.mvp;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.phuang.pizzame.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for obtaining device location: <br>
 * 1) Obtain last known location. If not available, request a location update from {@link LocationManager} <br>
 * 2) Once we have a location and lat/lon, use {@link Geocoder} to obtain a postal code in a worker thread. <br>
 * <br>
 * This class also plays a presenter role to display progress bar and location error message.
 */

public class LocationUtilPresenter implements LocationListener {

    public static final String TAG = LocationUtilPresenter.class.getSimpleName();

    private static String sZipCode; // cached version of zip code

    private static String sErrorMessage;

    private WeakReference<LocationUtilListener> mLocationListenerRef;

    private WeakReference<StoreListContract.View> mViewRef;

    private Context mAppContext;

    public interface LocationUtilListener {
        void onZipCodeResult(String zipCode);
    }

    @SuppressWarnings({"MissingPermission"})
    public void getZipCode(Context appContext,
                           LocationUtilListener locationUtilListener,
                           StoreListContract.View view) {

        view.showProgress();

        mAppContext = appContext;
        mLocationListenerRef = new WeakReference<>(locationUtilListener);
        mViewRef = new WeakReference<>(view);
        sErrorMessage = appContext.getResources().getString(R.string.error_unable_to_obtain_location);

        if (!TextUtils.isEmpty(sZipCode)) {
            locationUtilListener.onZipCodeResult(sZipCode);
            return;
        }

        LocationManager locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            view.showError(sErrorMessage);
            return;
        }

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        android.util.Log.d(TAG, "best location provider=" + bestProvider);

        Location location = locationManager.getLastKnownLocation(bestProvider);

        if (location != null) {
            startZipCodeThread(mAppContext, location);
        } else {
            android.util.Log.d(TAG, "requesting location update..");
            locationManager.requestSingleUpdate(criteria, this, null);
        }
    }

    /**
     * Starts worker thread to obtain zip code.
     */
    private void startZipCodeThread(@NonNull Context appContext, @NonNull Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        new ZipCodeThread(appContext, this, lat, lon).start();
    }

    private void onZipCodeThreadResult(String zipCode) {
        sZipCode = zipCode;
        if (!TextUtils.isEmpty(zipCode)) {
            LocationUtilListener listener = mLocationListenerRef.get();
            if (listener != null) {
                listener.onZipCodeResult(zipCode);
            }
        } else {
            StoreListContract.View view = mViewRef.get();
            if (view != null) {
                view.showError(sErrorMessage);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        startZipCodeThread(mAppContext, location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    private static class ZipCodeThread extends Thread {

        LocationUtilPresenter mLocationUtil;
        Context mAppContext;
        double mLat;
        double mLon;

        public ZipCodeThread(Context appContext, LocationUtilPresenter locationUtil, double lat, double lon) {
            mAppContext = appContext;
            mLocationUtil = locationUtil;
            mLat = lat;
            mLon = lon;
        }

        @Override
        public void run() {

            Geocoder geocoder = new Geocoder(mAppContext, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(mLat, mLon, 1);
                if (addresses == null || addresses.size() < 1) {
                    notifyResultOnMainThread(null);
                    return;
                }

                String postalCode = addresses.get(0).getPostalCode();
                notifyResultOnMainThread(postalCode);

            } catch (IOException e) {
                notifyResultOnMainThread(null);
                e.printStackTrace();
            }
        }

        private void notifyResultOnMainThread(final String postalCode) {

            Handler handler = new Handler(mAppContext.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mLocationUtil.onZipCodeThreadResult(postalCode);
                }
            });
        }
    }
}
