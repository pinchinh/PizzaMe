package com.phuang.pizzame.mvp;

import android.content.Context;

import com.phuang.pizzame.model.Store;

import java.util.List;

public interface StoreListContract {

    interface View {
        void showProgress();

        void showItems(List<Store> stores);

        void showError(String error);
    }

    interface Presenter {

        /**
         * Start the presenter to retrieve data
         * @param context
         * @param zipCode
         * @param refreshData whether we should refresh data (from network) or try to use cache
         */
        void start(Context context, String zipCode, boolean refreshData);
    }

}
