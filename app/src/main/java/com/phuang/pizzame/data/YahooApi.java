package com.phuang.pizzame.data;

import com.phuang.pizzame.model.QueryResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.QueryMap;

public interface YahooApi {

    String BASE_URL = "https://query.yahooapis.com/";

    String QUERY_KEY_Q = "q";
    String QUERY_KEY_FORMAT = "format";
    String QUERY_VALUE_JSON = "json";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    // Example: https://query.yahooapis.com/v1/public/yql?q=select * from local.search where zip='95014' and query='pizza'&format=json

    @GET("v1/public/yql")
    Call<QueryResponse> query(@QueryMap Map<String, String> params);

}
