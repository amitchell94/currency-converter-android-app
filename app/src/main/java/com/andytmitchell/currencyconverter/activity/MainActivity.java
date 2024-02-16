package com.andytmitchell.currencyconverter.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.andytmitchell.currencyconverter.R;
import com.andytmitchell.currencyconverter.controller.CurrencyRateService;
import com.andytmitchell.currencyconverter.model.CurrencyDataRepository;
import com.andytmitchell.currencyconverter.model.SharedPrefsCurrencyDataRepository;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private CurrencyDataRepository currencyDataRepository;
    private double rate = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currencyDataRepository = new SharedPrefsCurrencyDataRepository(getApplicationContext());

        AutoCompleteTextView autoCompleteTargetCurrency = findViewById(R.id.autoCompleteTargetCurrency);
        AutoCompleteTextView autoCompleteHomeCurrency = findViewById(R.id.autoCompleteHomeCurrency);
        EditText ammountEditText = findViewById(R.id.editTextAmount);

        ArrayAdapter<CharSequence> adapterHome = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_dropdown_item_1line);
        ArrayAdapter<CharSequence> adapterTarget = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_dropdown_item_1line);

        autoCompleteHomeCurrency.setAdapter(adapterHome);
        autoCompleteTargetCurrency.setAdapter(adapterTarget);


        String homeCurr = currencyDataRepository.getHomeCurrency();
        String targCurr = currencyDataRepository.getTargetCurrency();

        boolean apiCallRequired = false;
        if (homeCurr.equals("") || targCurr.equals("")) {
            //Set some default values
            homeCurr = (String) adapterHome.getItem(0);
            targCurr = (String) adapterTarget.getItem(1);
            currencyDataRepository.saveHomeCurrency(homeCurr);
            currencyDataRepository.saveHomeCurrency(targCurr);
            apiCallRequired = true;
        }

        autoCompleteHomeCurrency.setText(homeCurr);
        autoCompleteTargetCurrency.setText(targCurr);
        updateTargetCurrencyLabel(targCurr);
        updateHomeCurrencyLabel(homeCurr);

        updateRate(apiCallRequired);

        autoCompleteHomeCurrency.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCurrency = parent.getItemAtPosition(position).toString();
            updateHomeCurrencyLabel(selectedCurrency);

            currencyDataRepository.saveHomeCurrency(selectedCurrency);
            updateRate(true);
        });

        autoCompleteHomeCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTargetCurrency.dismissDropDown(); // Dismiss the dropdown of the target currency when home currency gains focus
                autoCompleteHomeCurrency.showDropDown();
            }
        });

        autoCompleteTargetCurrency.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCurrency = parent.getItemAtPosition(position).toString();
            updateTargetCurrencyLabel(selectedCurrency);

            currencyDataRepository.saveTargetCurrency(selectedCurrency);
            updateRate(false);
        });

        autoCompleteTargetCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteHomeCurrency.dismissDropDown(); // Dismiss the dropdown of the home currency when target currency gains focus
                autoCompleteTargetCurrency.showDropDown();
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

        ammountEditText.requestFocus();
    }

    private void updateRate(boolean apiCallRequired) {
        String homeCurr = currencyDataRepository.getHomeCurrency();
        currencyDataRepository.getConversionRates(homeCurr, apiCallRequired, new CurrencyRateService.CurrencyRateCallback() {
            @Override
            public void onRateFetched(JSONObject rates) {
                String targetCurrency = currencyDataRepository.getTargetCurrency();
                setLastUpdatedText();
                try {
                    rate = rates.getDouble(targetCurrency.substring(0, 3));
                    calculateConversion();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void calculateConversion() {
        EditText conversion = findViewById(R.id.editTextConversion);
        EditText amountEv = findViewById(R.id.editTextAmount);
        try {
            double amount = Double.parseDouble(amountEv.getText().toString());
            double result = amount / rate;

            String formattedResult = String.format(Locale.getDefault(), "%.2f", result);
            conversion.setText(formattedResult);
        } catch (NumberFormatException e) {
            conversion.setText("-");
        }
    }

    private void setLastUpdatedText () {
        long lastUpdated = currencyDataRepository.getLastUpdated();

        Date date = new Date(lastUpdated);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy HH:mm:ss");

        TextView lastUpdatedTextView = findViewById(R.id.lastUpdated);
        lastUpdatedTextView.setText("Last updated: " + sdf.format(date));
    }

    public void updateHomeCurrencyLabel(String homeCurrency) {
        TextView label = findViewById(R.id.labelConversion);
        label.setText(homeCurrency.substring(0,3));
    }

    public void updateTargetCurrencyLabel(String targetCurrency) {
        TextView label = findViewById(R.id.labelAmount);
        label.setText(targetCurrency.substring(0,3));
    }

}