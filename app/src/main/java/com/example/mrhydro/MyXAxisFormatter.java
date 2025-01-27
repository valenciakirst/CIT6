package com.example.mrhydro;

import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MyXAxisFormatter extends ValueFormatter {
    private String[] values;

    public MyXAxisFormatter(String[] values) {
        this.values = values;
    }

    @Override
    public String getFormattedValue(float value) {
        if (value >= 0 && value < values.length) {
            return values[(int) value];
        }
        return "";
    }
}
