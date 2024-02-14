package com.andytmitchell.currencyconverter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Spinner spinnerHomeCurrency = findViewById(R.id.spinnerHomeCurrency);
        final Spinner spinnerTargetCurrency = findViewById(R.id.spinnerTargetCurrency);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_spinner_item);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerHomeCurrency.setAdapter(adapter);
        spinnerTargetCurrency.setAdapter(adapter);

        setSpinnerToValue(spinnerHomeCurrency, "GBP");
        setSpinnerToValue(spinnerTargetCurrency, "EUR");

        getCurrencyRate("GBP", "EUR");
        AdapterView.OnItemSelectedListener currencySelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String homeCurrency = spinnerHomeCurrency.getSelectedItem().toString();
                String targetCurrency = spinnerTargetCurrency.getSelectedItem().toString();
                getCurrencyRate(homeCurrency,targetCurrency);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };

        spinnerHomeCurrency.setOnItemSelectedListener(currencySelectedListener);
        spinnerTargetCurrency.setOnItemSelectedListener(currencySelectedListener);
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
                    final double rate = rates.getDouble(targetCurrency);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = findViewById(R.id.textView);
                            textView.setText(homeCurrency + " to " + targetCurrency + ": " + rate);
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
}