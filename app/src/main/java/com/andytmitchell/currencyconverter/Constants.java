package com.andytmitchell.currencyconverter;

public class Constants {
    public static final String API_URL_PREFIX = "https://api.exchangerate-api.com/v4/latest/";
    public static final String SHARED_PREF_FILENAME = "CurrencyConverterPreferences";
    public static final String HOME_CURRENCY_REFERENCE_KEY = "HomeCurrency";
    public static final String TARGET_CURRENCY_REFERENCE_KEY = "TargetCurrency";
    public static final String LAST_FETCH_TIME_REFERENCE_KEY = "LastFetchTime";
    public static final String RATES_REFERENCE_KEY = "Rates";
    public static final long CURRENCY_RATE_FRESHNESS_THRESHOLD = 24 * 60 * 60 * 1000; //24 hours
}
