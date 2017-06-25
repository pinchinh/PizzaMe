package com.phuang.pizzame.data;

import android.support.v4.util.ArrayMap;

import com.phuang.pizzame.model.QueryResponse;
import com.phuang.pizzame.model.Store;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class provides data to the {@link com.phuang.pizzame.mvp.StoreListPresenter}.
 * Data either comes from the network or from the cache.
 */
public class DataController {

    private static DataController sInstance;

    private YahooApi mYahooApi;

    // simple in-memory cache..
    private List<Store> mDataCache;

    public interface DataListener<T> {
        void onDataSuccess(T response);

        void onDataFailure(String errorMsg);
    }

    private DataController() {
        mYahooApi = YahooApi.retrofit.create(YahooApi.class);
    }

    public static DataController getInstance() {
        if (sInstance == null) {
            sInstance = new DataController();
        }
        return sInstance;
    }

    /**
     * Gets a list of stores.
     * @param zipCode
     * @param dataListener
     * @param refreshData if true, will always try to retrieve data from network. Otherwise, will
     *                    try to use cache if available.
     */
    public void getStores(String zipCode, final DataListener<List<Store>> dataListener, boolean refreshData) {
        if (!refreshData && mDataCache != null && mDataCache.size() > 0) {
            dataListener.onDataSuccess(mDataCache);
            return;
        }

        // cache miss, let's retrieve from network
        Map<String, String> map = new ArrayMap<>();
        String query = "select * from local.search where zip='" + zipCode + "' and query='pizza'";
        map.put(YahooApi.QUERY_KEY_Q, query);
        map.put(YahooApi.QUERY_KEY_FORMAT, YahooApi.QUERY_VALUE_JSON);
        Call<QueryResponse> call = mYahooApi.query(map);
        call.enqueue(new Callback<QueryResponse>() {

            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                mDataCache = response.body().query.results.Result;
                Collections.sort(mDataCache);
                dataListener.onDataSuccess(mDataCache);
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {
                dataListener.onDataFailure(t.getMessage());
            }
        });
    }
}
