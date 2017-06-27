package com.phuang.pizzame;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.phuang.pizzame.model.Store;
import com.phuang.pizzame.mvp.StoreListContract;
import com.phuang.pizzame.mvp.StoreListPresenter;
import com.phuang.pizzame.mvp.LocationUtilPresenter;

import java.util.List;

public class StoreListFragment extends Fragment implements StoreListContract.View,
        LocationUtilPresenter.LocationUtilListener {

    public static final int PERMISSION_REQUEST_CODE_FINE_LOCATION = 100;

    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;

    private TextView mErrorText;

    private StoreListPresenter mPresenter;

    private OnItemSelectedListener mItemSelectedListener;

    private boolean mOrientationChange = false;

    private boolean mReceivedData = false;

    // Container Activity must implement this interface
    public interface OnItemSelectedListener {
        void onItemSelected(Store store);
    }

    public static StoreListFragment newInstance() {
        return new StoreListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new StoreListPresenter(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_store_list, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        mErrorText = (TextView) view.findViewById(R.id.error_text);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mOrientationChange = savedInstanceState != null;

        // obtain device location if we have permission
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_REQUEST_CODE_FINE_LOCATION);
        } else {
            new LocationUtilPresenter().getZipCode(PizzaMe.get(), this, this);
        }
    }

    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    public void onAttachToContext(Context context) {
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mItemSelectedListener = (OnItemSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mItemSelectedListener = null;
    }

    @Override
    public void showProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorText.setVisibility(View.GONE);
    }

    @Override
    public void showError(String error) {
        mErrorText.setVisibility(View.VISIBLE);
        mErrorText.setText(error);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void showItems(List<Store> stores) {
        mReceivedData = true;
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mErrorText.setVisibility(View.GONE);
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            mRecyclerView.setAdapter(new StoreListAdapter(stores, mItemSelectedListener));
        } else {
            ((StoreListAdapter)adapter).setItems(stores);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    // Now try to get device location
                    new LocationUtilPresenter().getZipCode(PizzaMe.get(), this, this);

                } else {
                    // permission denied, boo!
                    showError(getContext().getResources().getString(R.string.error_pizzame_needs_location_access));
                }
                return;
            }
        }
    }

    @Override
    public void onZipCodeResult(String zipCode) {
        // Finally, we can start the presenter!

        // There are two cases where we don't want to refresh data:
        // 1) orientation change, 2) from the back stack
        boolean refreshData = !(mOrientationChange || mReceivedData);
        mPresenter.start(getContext(), zipCode, refreshData);
    }
}
