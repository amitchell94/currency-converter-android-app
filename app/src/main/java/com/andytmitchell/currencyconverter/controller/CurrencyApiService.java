package com.andytmitchell.currencyconverter.controller;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CurrencyApiService {
    @GET("{currency}")
    Call<ResponseBody> getCurrencyRate(@Path("currency") String currency);
}
