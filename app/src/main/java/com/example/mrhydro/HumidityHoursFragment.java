package com.example.mrhydro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class HumidityHoursFragment extends Fragment {

    String[] daysOfMonth = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    LineChart humidityChart;
    public HumidityHoursFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_humidity_hours, container, false);

        // Get the selected option from arguments
        Bundle args = getArguments();
        if (args != null) {
            String selectedOption = args.getString("selectedOption");
            // Now, based on the selectedOption, customize your line chart view
            customizeLineChart(view, selectedOption);
        }

        return view;
    }

    private void customizeLineChart(View view, String selectedOption) {
        // Customize your line chart based on the selected option
        // Example: Load different data or set different configurations

        // Sample customization based on the option
        switch (selectedOption) {
            case "Hourly Line Chart":
                // Customize for hourly chart
                break;
            case "Daily Line Chart":
                // Customize for daily chart
                break;
            case "Weekly Line Chart":
                // Customize for weekly chart
                break;
            case "Monthly Line Chart":
                humidityChart = view.findViewById(R.id.humidityChart);

                MyXAxisFormatter xAxisFormatter = new MyXAxisFormatter(daysOfMonth);
                XAxis xAxis = humidityChart.getXAxis();
                xAxis.setValueFormatter(xAxisFormatter);

                MyYAxisFormatter yAxisFormatter = new MyYAxisFormatter();
                YAxis leftYAxis = humidityChart.getAxisLeft();
                leftYAxis.setValueFormatter(yAxisFormatter);

                LineDataSet humidityDataSet = new LineDataSet(dataValues(), "Temperature Data");
                ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                dataSets.add(humidityDataSet);
                LineData data = new LineData(dataSets);

                Description description = new Description();
                description.setText("Humidity Chart");
                humidityChart.setDescription(description);

                // Set the data to the chart
                humidityChart.setData(data);
                humidityChart.invalidate();

                break;
            case "Yearly Line Chart":
                // Customize for weekly chart
                break;
            default:
                // Handle unknown option
        }
    }
    private List<Entry> dataValues() {
        ArrayList<Entry> dataValue = new ArrayList<>();
        dataValue.add(new Entry(1f, 28.9f));
        dataValue.add(new Entry(2f, 26.9f));
        dataValue.add(new Entry(3f, 27.9f));
        dataValue.add(new Entry(4f, 30.0f));
        dataValue.add(new Entry(5f, 21.0f));
        dataValue.add(new Entry(6f, 27.9f));
        dataValue.add(new Entry(7f, 27.9f));
        dataValue.add(new Entry(8f, 27.9f));
        dataValue.add(new Entry(9f, 27.9f));
        dataValue.add(new Entry(10f, 27.9f));
        dataValue.add(new Entry(11f, 27.9f));
        dataValue.add(new Entry(12f, 27.9f));
        dataValue.add(new Entry(13f, 27.9f));
        dataValue.add(new Entry(14f, 27.9f));
        dataValue.add(new Entry(15f, 27.9f));
        dataValue.add(new Entry(16f, 27.9f));
        dataValue.add(new Entry(17f, 27.9f));
        dataValue.add(new Entry(18f, 27.9f));
        dataValue.add(new Entry(19f, 27.9f));
        dataValue.add(new Entry(20f, 26.7f));
        dataValue.add(new Entry(21f, 27.9f));
        dataValue.add(new Entry(22f, 27.9f));
        dataValue.add(new Entry(23f, 27.9f));
        dataValue.add(new Entry(24f, 27.9f));
        dataValue.add(new Entry(25f, 27.9f));
        dataValue.add(new Entry(26f, 27.9f));
        dataValue.add(new Entry(27f, 27.9f));
        dataValue.add(new Entry(28f, 27.9f));


        return dataValue;
    }

}