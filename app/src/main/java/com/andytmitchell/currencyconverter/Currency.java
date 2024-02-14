package com.andytmitchell.currencyconverter;

import androidx.annotation.NonNull;

public class Currency {
    private String code;
    private String name;

    public Currency(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return code + " - " + name;
    }
}
