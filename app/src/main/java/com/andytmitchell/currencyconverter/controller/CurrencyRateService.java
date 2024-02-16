package com.andytmitchell.currencyconverter.controller;

import com.andytmitchell.currencyconverter.Constants;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CurrencyRateService {
    public interface CurrencyRateCallback {
        void onRateFetched(JSONObject rates);
        void onError(Exception e);
    }


    private CurrencyApiService apiService;

    public CurrencyRateService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.API_URL_PREFIX)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        System.out.println(retrofit.baseUrl());
        apiService = retrofit.create(CurrencyApiService.class);
    }

    public void fetchCurrencyRate(final String homeCurrency, final CurrencyRateCallback callback) {
        apiService.getCurrencyRate(homeCurrency).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject rates = new JSONObject(response.body().string());
                        callback.onRateFetched(rates);
                    } catch (Exception e) {
                        callback.onError(e);
                    }
                } else {
                    callback.onError(new Exception("Response not successful"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onError(new Exception(t));
            }
        });
    }
}
