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

public class TemperatureChartsFragment extends Fragment {

    String[] daysOfMonth = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    String[] hoursOfDay = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    String[] monthsOfYear = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    LineChart temperatureChart;

    public TemperatureChartsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the generated binding class
        View view = inflater.inflate(R.layout.fragment_temperature_charts, container, false);

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
        switch (selectedOption) {
            case "Daily Line Chart":
                // Customize for daily chart
                temperatureChart = view.findViewById(R.id.temperatureChart);

                MyXAxisFormatter dailyXAxisFormatter = new MyXAxisFormatter(hoursOfDay);
                XAxis dailyXAxis = temperatureChart.getXAxis();
                dailyXAxis.setValueFormatter(dailyXAxisFormatter);

                MyYAxisFormatter dailyYAxisFormatter = new MyYAxisFormatter();
                YAxis dailyLeftYAxis = temperatureChart.getAxisLeft();
                dailyLeftYAxis.setValueFormatter(dailyYAxisFormatter);

                LineDataSet dailyTemperatureDataSet = new LineDataSet(dataValues(selectedOption), "Temperature Data");
                ArrayList<ILineDataSet> dailyDataSets = new ArrayList<>();
                dailyDataSets.add(dailyTemperatureDataSet);
                LineData dailyData = new LineData(dailyDataSets);

                Description dailyDescription = new Description();
                dailyDescription.setText("Hourly Temperature Chart");
                temperatureChart.setDescription(dailyDescription);

                temperatureChart.setPinchZoom(true);

                temperatureChart.setData(dailyData);
                temperatureChart.invalidate();
                break;

            case "Monthly Line Chart":
                // Customize for monthly chart
                temperatureChart = view.findViewById(R.id.temperatureChart);

                MyXAxisFormatter monthlyXAxisFormatter = new MyXAxisFormatter(daysOfMonth);
                XAxis monthlyXAxis = temperatureChart.getXAxis();
                monthlyXAxis.setValueFormatter(monthlyXAxisFormatter);

                MyYAxisFormatter monthlyYAxisFormatter = new MyYAxisFormatter();
                YAxis monthlyLeftYAxis = temperatureChart.getAxisLeft();
                monthlyLeftYAxis.setValueFormatter(monthlyYAxisFormatter);

                LineDataSet monthlyTemperatureDataSet = new LineDataSet(dataValues(selectedOption), "Temperature Data");
                ArrayList<ILineDataSet> monthlyDataSets = new ArrayList<>();
                monthlyDataSets.add(monthlyTemperatureDataSet);
                LineData monthlyData = new LineData(monthlyDataSets);

                Description monthlyDescription = new Description();
                monthlyDescription.setText("Monthly Temperature Chart");
                temperatureChart.setDescription(monthlyDescription);

                temperatureChart.setData(monthlyData);
                temperatureChart.invalidate();
                break;

            case "Yearly Line Chart":
                // Customize for yearly chart
                temperatureChart = view.findViewById(R.id.temperatureChart);

                MyXAxisFormatter yearlyXAxisFormatter = new MyXAxisFormatter(monthsOfYear);
                XAxis yearlyXAxis = temperatureChart.getXAxis();
                yearlyXAxis.setValueFormatter(yearlyXAxisFormatter);

                MyYAxisFormatter yearlyYAxisFormatter = new MyYAxisFormatter();
                YAxis yearlyLeftYAxis = temperatureChart.getAxisLeft();
                yearlyLeftYAxis.setValueFormatter(yearlyYAxisFormatter);

                LineDataSet yearlyTemperatureDataSet = new LineDataSet(dataValues(selectedOption), "Temperature Data");
                ArrayList<ILineDataSet> yearlyDataSets = new ArrayList<>();
                yearlyDataSets.add(yearlyTemperatureDataSet);
                LineData yearlyData = new LineData(yearlyDataSets);

                Description yearlyDescription = new Description();
                yearlyDescription.setText("Yearly Temperature Chart");
                temperatureChart.setDescription(yearlyDescription);

                temperatureChart.setPinchZoom(true);

                temperatureChart.setData(yearlyData);
                temperatureChart.invalidate();
                break;

            default:
                // Handle unknown option
                break;
        }
    }

    private List<Entry> dataValues(String selectedOption) {
        ArrayList<Entry> dataValue = new ArrayList<>();

        switch (selectedOption) {
            case "Daily Line Chart":
                // Example data for daily chart
                for (int i = 0; i < 24; i++) {
                    // Replace these values with actual hourly temperature data
                    float temperatureValue = (float) (Math.random() * 10 + 20);
                    dataValue.add(new Entry(i, temperatureValue));
                }
                break;

            case "Monthly Line Chart":
                // Example data for monthly chart
                for (int i = 0; i < daysOfMonth.length; i++) {
                    // Replace these values with actual daily temperature data
                    float temperatureValue = (float) (Math.random() * 10 + 20);
                    dataValue.add(new Entry(i + 1, temperatureValue));
                }
                break;

            case "Yearly Line Chart":
                // Example data for yearly chart
                for (int i = 0; i < monthsOfYear.length; i++) {
                    // Replace these values with actual monthly temperature data
                    float temperatureValue = (float) (Math.random() * 10 + 20);
                    dataValue.add(new Entry(i + 1, temperatureValue));
                }
                break;

            default:
                // Handle unknown option
                break;
        }
        return dataValue;
    }
}
