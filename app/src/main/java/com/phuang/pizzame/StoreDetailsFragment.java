package com.phuang.pizzame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.phuang.pizzame.model.Store;

public class StoreDetailsFragment extends Fragment implements View.OnClickListener {

    public static final String KEY_STORE = "store";

    public static final int PERMISSION_REQUEST_CODE_CALL_PHONE = 200;

    private Store mStore;

    private TextView mTextTitle;

    private TextView mTextAddress;

    private TextView mTextPhone;

    private TextView mTextDistance;

    private TextView mTextUrl;

    private View mPhoneLayout;

    private View mAddressLayout;

    private View mCallIcon;

    private View mMapIcon;

    public static StoreDetailsFragment newInstance(Store store) {
        StoreDetailsFragment fragment = new StoreDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_STORE, store);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_details, container, false);
        mTextTitle = (TextView) view.findViewById(R.id.store_details_title);
        mTextAddress = (TextView) view.findViewById(R.id.store_details_address);
        mTextPhone = (TextView) view.findViewById(R.id.store_details_phone);
        mTextDistance = (TextView) view.findViewById(R.id.store_details_distance);
        mTextUrl = (TextView) view.findViewById(R.id.store_details_url);
        mPhoneLayout = view.findViewById(R.id.store_details_phone_layout);
        mAddressLayout = view.findViewById(R.id.store_details_address_layout);
        mCallIcon = view.findViewById(R.id.store_details_call_icon);
        mMapIcon = view.findViewById(R.id.store_details_map_icon);
        mStore = (Store) getArguments().getSerializable(KEY_STORE);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showDetails();
        setupClickActions();
    }

    private void showDetails() {
        if (mStore != null) {
            mTextTitle.setText(mStore.Title);

            mTextAddress.setText(mStore.Address + " " + mStore.City + " " + mStore.State);
            if (TextUtils.isEmpty(mStore.Address)) {
                mMapIcon.setVisibility(View.GONE);
            } else {
                mMapIcon.setVisibility(View.VISIBLE);
            }

            mTextPhone.setText(mStore.Phone);
            if (TextUtils.isEmpty(mStore.Phone)) {
                mCallIcon.setVisibility(View.GONE);
            } else {
                mCallIcon.setVisibility(View.VISIBLE);
            }

            mTextDistance.setText(mStore.Distance + " "
                    + getContext().getResources().getString(R.string.distance_unit));

            mTextUrl.setText(mStore.BusinessUrl);
            mTextUrl.setPaintFlags(mTextUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        }
    }

    private void setupClickActions() {
        mPhoneLayout.setOnClickListener(this);
        mAddressLayout.setOnClickListener(this);
        mTextUrl.setOnClickListener(this);
    }

    private void call() {
        if (mStore == null || TextUtils.isEmpty(mStore.Phone)) {
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + mStore.Phone));
        startActivity(callIntent);
    }

    private void openInMap() {
        if (mStore == null || TextUtils.isEmpty(mStore.Address)) {
            return;
        }

        String address = mStore.Address + " " + mStore.City + " " + mStore.State;
        String uri = "geo:" + mStore.Latitude + "," + mStore.Longitude + "?q=" + address;
        Uri gmmIntentUri = Uri.parse(uri);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        //mapIntent.setPackage("com.google.android.apps.maps");

        // make sure there is at least one app that can handle the intent
        if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(mapIntent);
        }
    }

    private void openInBrowser() {
        if (mStore == null || TextUtils.isEmpty(mStore.BusinessUrl)) {
            return;
        }

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mStore.BusinessUrl));

        // make sure there is at least one app that can handle the intent
        if (browserIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(browserIntent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.store_details_phone_layout:
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{ Manifest.permission.CALL_PHONE },
                            PERMISSION_REQUEST_CODE_CALL_PHONE);
                } else {
                    call();
                }
                break;
            case R.id.store_details_address_layout:
                openInMap();
                break;
            case R.id.store_details_url:
                openInBrowser();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // Now make the phone call
                    call();

                } else {
                    // permission denied, boo!
                }
                return;
            }
        }
    }
}
