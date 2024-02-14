package com.andytmitchell.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private double rate = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AutoCompleteTextView autoCompleteTargetCurrency = findViewById(R.id.autoCompleteTargetCurrency);
        AutoCompleteTextView autoCompleteHomeCurrency = findViewById(R.id.autoCompleteHomeCurrency);
        EditText ammountEditText = findViewById(R.id.editTextAmount);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_dropdown_item_1line);

        autoCompleteHomeCurrency.setAdapter(adapter);
        autoCompleteTargetCurrency.setAdapter(adapter);

        getCurrencyRate("GBP", "EUR");
        autoCompleteHomeCurrency.setText(adapter.getItem(0));
        autoCompleteTargetCurrency.setText(adapter.getItem(1));
        autoCompleteHomeCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString().substring(0,3);
                String targetCurrency = autoCompleteTargetCurrency.getText().toString().substring(0,3);
                getCurrencyRate(selectedCurrency, targetCurrency);
            }
        });

        autoCompleteHomeCurrency.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoCompleteHomeCurrency.showDropDown();
                }
            }
        });

        autoCompleteTargetCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedCurrency = parent.getItemAtPosition(position).toString().substring(0,3);
                String homeCurrency = autoCompleteHomeCurrency.getText().toString().substring(0,3);
                getCurrencyRate(homeCurrency, selectedCurrency);
            }
        });

        autoCompleteTargetCurrency.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoCompleteTargetCurrency.showDropDown();
                }
            }
        });

        ammountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateConversion();
            }
        });
    }

    private void getCurrencyRate(final String homeCurrency, final String targetCurrency) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("https://api.exchangerate-api.com/v4/latest/" + homeCurrency);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();

                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();

                    String line;
                    while((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONObject rates = jsonResponse.getJSONObject("rates");
                    rate = rates.getDouble(targetCurrency);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            calculateConversion();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();;
                    }
                }
            }
        }).start();
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinner.getAdapter();
        int position = adapter.getPosition(value);
        spinner.setSelection(position);
    }

    private void calculateConversion() {
        EditText conversion = findViewById(R.id.editTextConversion);
        EditText amountEv = findViewById(R.id.editTextAmount);
        try {
            double amount = Double.parseDouble(amountEv.getText().toString());
            double result = amount / rate;
            // Format the result to 2 decimal places and pad with zeros if necessary
            String formattedResult = String.format(Locale.getDefault(), "%.2f", result);
            conversion.setText(formattedResult);
        } catch (NumberFormatException e) {

            conversion.setText("-");
        }
    }

    private void saveCurrencies(String homeCurrency, String targetCurrency) {
        SharedPreferences sharedPref = getSharedPreferences("CurrencyConverterPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("HomeCurrency", homeCurrency);
        editor.putString("TargetCurrency", targetCurrency);

        editor.apply();
    }

    private void saveRates(String ratesJson) {
        SharedPreferences sharedPref = getSharedPreferences("CurrencyConverterPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("Rates", ratesJson);
        editor.apply();
    }

    private JSONObject loadRates() {
        SharedPreferences sharedPref = getSharedPreferences("CurrencyConverterPreferences", Context.MODE_PRIVATE);
        String ratesJson = sharedPref.getString("Rates", "{}"); // Default to an empty JSON object
        try {
            return new JSONObject(ratesJson);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}