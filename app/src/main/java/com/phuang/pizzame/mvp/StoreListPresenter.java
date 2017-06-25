package com.phuang.pizzame.mvp;

import android.content.Context;

import com.phuang.pizzame.data.DataController;
import com.phuang.pizzame.model.Store;

import java.util.List;

public class StoreListPresenter implements StoreListContract.Presenter,
        DataController.DataListener<List<Store>> {

    private StoreListContract.View mView;

    private DataController mDataController;

    public StoreListPresenter(StoreListContract.View view) {
        mView = view;
        mDataController = DataController.getInstance();
    }

    @Override
    public void start(Context context, String zipCode, boolean refreshData) {
        mView.showProgress();
        mDataController.getStores(zipCode, this, refreshData);
    }

    @Override
    public void onDataSuccess(List<Store> response) {
        mView.showItems(response);
    }

    @Override
    public void onDataFailure(String errorMsg) {
        mView.showError(errorMsg);
    }
}
