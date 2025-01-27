// MyYAxisFormatter.java
package com.example.mrhydro;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

public class MyYAxisFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        return String.format("%.1f°C", value);
    }

    }
