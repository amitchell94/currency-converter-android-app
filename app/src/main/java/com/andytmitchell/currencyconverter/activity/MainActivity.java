package com.andytmitchell.currencyconverter.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
        EditText ammountEditText = findViewById(R.id.editTextAmount);

        ArrayAdapter<CharSequence> adapterTarget = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_dropdown_item_1line);

        autoCompleteTargetCurrency.setAdapter(adapterTarget);

        String homeCurr = currencyDataRepository.getHomeCurrency();
        String targCurr = currencyDataRepository.getTargetCurrency();

        boolean apiCallRequired = false;
        if (homeCurr.equals("")) {
            showDialog();
            apiCallRequired = true;
        }

        if (targCurr.equals("")) {
            //Set a default value
            targCurr = (String) adapterTarget.getItem(1);
            currencyDataRepository.saveTargetCurrency(targCurr);
            apiCallRequired = true;
        }

        autoCompleteTargetCurrency.setText(targCurr);
        updateTargetCurrencyLabel(targCurr);
        updateHomeCurrencyLabel(homeCurr);

        updateRate(apiCallRequired);

        autoCompleteTargetCurrency.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCurrency = parent.getItemAtPosition(position).toString();
            updateTargetCurrencyLabel(selectedCurrency);

            currencyDataRepository.saveTargetCurrency(selectedCurrency);
            updateRate(false);
        });

        autoCompleteTargetCurrency.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
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

        Button changeHomeCurrencyBtn = findViewById(R.id.changeCurrencyBtn);
        changeHomeCurrencyBtn.setOnClickListener(v -> showDialog());

        ammountEditText.requestFocus();
    }

    private void updateRate(boolean apiCallRequired) {
        String homeCurr = currencyDataRepository.getHomeCurrency();
        currencyDataRepository.getConversionRates(homeCurr, apiCallRequired, getApplicationContext(), new CurrencyRateService.CurrencyRateCallback() {
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
        lastUpdatedTextView.setText("Exchange rate last updated: " + sdf.format(date));
    }

    public void updateHomeCurrencyLabel(String homeCurrency) {
        TextView label = findViewById(R.id.labelConversion);
        label.setText(homeCurrency.substring(0,3));

        TextView label2 = findViewById(R.id.labelChangeHomeCurrency);
        label2.setText("Home currency is set to " + homeCurrency.substring(0,3));
    }

    public void updateTargetCurrencyLabel(String targetCurrency) {
        TextView label = findViewById(R.id.labelAmount);
        label.setText(targetCurrency.substring(0,3));
    }

    public void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomAlertDialogStyle);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.home_currency_dialog_form, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        final AutoCompleteTextView autoCompleteTextViewHomeCurr = dialogView.findViewById(R.id.autoCompleteHomeCurrencyDialog);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.currency_array, android.R.layout.simple_dropdown_item_1line);

        autoCompleteTextViewHomeCurr.setAdapter(adapter);

        autoCompleteTextViewHomeCurr.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                autoCompleteTextViewHomeCurr.showDropDown();
            }
        });

        autoCompleteTextViewHomeCurr.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String selectedCurrency = autoCompleteTextViewHomeCurr.getText().toString();
                if (adapter.getPosition(selectedCurrency) != -1) {
                    updateHomeCurrencyLabel(selectedCurrency);

                    currencyDataRepository.saveHomeCurrency(selectedCurrency);
                    dialog.dismiss();
                }
                return true;
            }
            return false;
        });

        String homeCurr = currencyDataRepository.getHomeCurrency();

        if (homeCurr.equals("")) {
            homeCurr = (String) adapter.getItem(0);
        }
        autoCompleteTextViewHomeCurr.setText(homeCurr);

        Button buttonDialogSubmit = dialogView.findViewById(R.id.buttonDialogSubmit);

        buttonDialogSubmit.setOnClickListener(view -> {
            String selectedCurrency = autoCompleteTextViewHomeCurr.getText().toString();
            if (adapter.getPosition(selectedCurrency) != -1) {
                updateHomeCurrencyLabel(selectedCurrency);

                currencyDataRepository.saveHomeCurrency(selectedCurrency);
                dialog.dismiss();
            }
        });

        dialog.setCancelable(false);
        dialog.show();

        setDialogDimensions(dialog.getWindow());
    }

    private void setDialogDimensions(Window window) {
        window.setBackgroundDrawableResource(R.drawable.dialog_background);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int displayWidth = displayMetrics.widthPixels;
        int marginSide = (int) (32 * getResources().getDisplayMetrics().density);
        int dialogWidth = displayWidth - 2 * marginSide; // Subtract margins from both sides
        window.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT);
    }
}