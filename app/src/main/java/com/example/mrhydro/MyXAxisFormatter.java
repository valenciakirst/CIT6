package com.example.mrhydro;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MyXAxisFormatter extends ValueFormatter {
    private final String[] daysOfMonth;

    public MyXAxisFormatter(String[] daysOfMonth) {
        this.daysOfMonth = daysOfMonth;
    }

    @Override
    public String getAxisLabel(float value, AxisBase axis) {
        int index = (int) value;
        return index >= 0 && index < daysOfMonth.length ? daysOfMonth[index] : String.valueOf(value);
    }
}