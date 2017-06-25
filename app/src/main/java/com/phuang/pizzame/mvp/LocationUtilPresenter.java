package com.phuang.pizzame.mvp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.text.TextUtils;

import com.phuang.pizzame.R;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for obtaining device location on a worker thread. Also plays a presenter role to
 * display progress bar and location error message.
 */

public class LocationUtilPresenter {

    private static String sZipCode; // cached version of zip code

    private static String sErrorMessage;

    private WeakReference<LocationListener> mLocationListenerRef;

    private WeakReference<StoreListContract.View> mViewRef;

    public interface LocationListener {
        void onZipCodeResult(String zipCode);
    }

    @SuppressWarnings({"MissingPermission"})
    public void getZipCode(Context context,
                           LocationListener locationListener,
                           StoreListContract.View view) {

        mLocationListenerRef = new WeakReference<>(locationListener);
        mViewRef = new WeakReference<>(view);
        sErrorMessage = context.getResources().getString(R.string.error_unable_to_obtain_location);

        if (!TextUtils.isEmpty(sZipCode)) {
            locationListener.onZipCodeResult(sZipCode);
            return;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            view.showError(sErrorMessage);
            return;
        }

        // Try GPS first.. but do we really need GPS accuracy?
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null) {
            view.showError(sErrorMessage);
            return;
        }

        // start worker thread to obtain zip code
        view.showProgress();
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        new ZipCodeThread(this, context, lat, lon).start();
    }

    private void onZipCodeThreadResult(String zipCode) {
        sZipCode = zipCode;
        if (!TextUtils.isEmpty(zipCode)) {
            LocationListener listener = mLocationListenerRef.get();
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

    private static class ZipCodeThread extends Thread {

        LocationUtilPresenter mLocationUtil;
        WeakReference<Context> mContextRef;
        double mLat;
        double mLon;

        public ZipCodeThread(LocationUtilPresenter locationUtil, Context context, double lat, double lon) {
            mLocationUtil = locationUtil;
            mContextRef = new WeakReference<>(context);
            mLat = lat;
            mLon = lon;
        }

        @Override
        public void run() {
            Context context = mContextRef.get();
            if (context == null) {
                return;
            }

            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
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

            Context context = mContextRef.get();
            if (context == null) {
                // original context is gone so there is no one to notify
                return;
            }

            Handler handler = new Handler(context.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    mLocationUtil.onZipCodeThreadResult(postalCode);
                }
            });
        }
    }
}
