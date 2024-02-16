package com.andytmitchell.currencyconverter.model;

import android.content.Context;
import android.content.SharedPreferences;

import com.andytmitchell.currencyconverter.Constants;
import com.andytmitchell.currencyconverter.controller.CurrencyRateService;
import com.andytmitchell.currencyconverter.controller.NetworkUtil;

import org.json.JSONObject;

public class SharedPrefsCurrencyDataRepository implements CurrencyDataRepository{
    private final SharedPreferences sharedPref;
    private final CurrencyRateService currencyRateService;

    public SharedPrefsCurrencyDataRepository(Context context) {
        this.sharedPref = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREF_FILENAME, Context.MODE_PRIVATE);
        this.currencyRateService = new CurrencyRateService();
    }
    @Override
    public void saveHomeCurrency(String homeCurrency) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.HOME_CURRENCY_REFERENCE_KEY, homeCurrency);

        editor.apply();
    }

    @Override
    public String getHomeCurrency() {
        return sharedPref.getString(Constants.HOME_CURRENCY_REFERENCE_KEY, "");
    }

    @Override
    public void saveTargetCurrency(String targetCurrency) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.TARGET_CURRENCY_REFERENCE_KEY, targetCurrency);

        editor.apply();
    }

    @Override
    public String getTargetCurrency() {
        return sharedPref.getString(Constants.TARGET_CURRENCY_REFERENCE_KEY, "");
    }

    @Override
    public void saveConversionRates(JSONObject ratesJson) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Constants.RATES_REFERENCE_KEY, ratesJson.toString());
        editor.apply();
    }

    @Override
    public void getConversionRates(final String homeCurrency, boolean apiCallRequired, Context context, final CurrencyRateService.CurrencyRateCallback callback) {
        String ratesJson = sharedPref.getString(Constants.RATES_REFERENCE_KEY, null);

        long lastFetchTime = getLastUpdated();
        long currentTime = System.currentTimeMillis();

        if  (NetworkUtil.isNetworkConnected(context) && ((currentTime - lastFetchTime > Constants.CURRENCY_RATE_FRESHNESS_THRESHOLD  || apiCallRequired))) {
            currencyRateService.fetchCurrencyRate(homeCurrency.substring(0, 3), new CurrencyRateService.CurrencyRateCallback() {
                @Override
                public void onRateFetched(JSONObject response) {
                    try {
                        JSONObject rates = response.getJSONObject("rates");
                        saveConversionRates(rates);
                        saveLastFetchTime(currentTime);
                        callback.onRateFetched(rates);
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            //If we fail, just try to get the ones we last stored.
                            callback.onRateFetched(new JSONObject(ratesJson));
                        } catch (Exception ex) {
                            callback.onError(ex);
                        }
                    }
                }

                @Override
                public void onError(Exception e) {
                    e.printStackTrace();
                    try {
                        //If we fail, just try to get the ones we last stored.
                        callback.onRateFetched(new JSONObject(ratesJson));
                    } catch (Exception ex) {
                        callback.onError(ex);
                    }
                }
            });
        } else {
            try {
                //If we updated recently, just try to get the ones we last stored.
                callback.onRateFetched(new JSONObject(ratesJson));
            } catch (Exception e) {
                callback.onError(e);
            }
        }
    }


    @Override
    public void saveLastFetchTime(long lastFetchTime) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(Constants.LAST_FETCH_TIME_REFERENCE_KEY, lastFetchTime);
        editor.apply();
    }

    @Override
    public long getLastUpdated() {
        return sharedPref.getLong(Constants.LAST_FETCH_TIME_REFERENCE_KEY, 0);
    }
}
