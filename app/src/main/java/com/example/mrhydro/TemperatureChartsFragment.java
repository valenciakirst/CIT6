package com.example.mrhydro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TemperatureChartsFragment extends Fragment {

    private static final String TAG = "TemperatureChartsFragment";
    private String[] hoursOfDay = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    private String[] daysOfMonth = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    private LineChart temperatureChart;
    private DatabaseReference databaseReference;
    private String selectedOption;
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public TemperatureChartsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_temperature_charts, container, false);
        temperatureChart = view.findViewById(R.id.temperatureChart);

        if (auth.getCurrentUser() != null) {
            Log.d(TAG, "User is authenticated");
            Bundle args = getArguments();
            if (args != null) {
                selectedOption = args.getString("selectedOption");
                Log.d(TAG, "Received option: " + selectedOption);
                customizeLineChart(view, selectedOption);
            } else {
                Log.e(TAG, "No arguments provided!");
            }
        } else {
            Log.e(TAG, "User is not authenticated!");
        }
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (auth.getCurrentUser() != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("temperatureHistory");
        } else {
            Log.e(TAG, "User is not authenticated!");
        }
    }

    private void fetchTemperatureData() {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "User is not authenticated!");
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Entry> dataValues = new ArrayList<>();

                if ("Hourly Line Chart".equals(selectedOption)) {
                    // Process hourly data
                    DataSnapshot hourlySnapshot = snapshot.child("hourly");
                    for (DataSnapshot hourSnapshot : hourlySnapshot.getChildren()) {
                        String hourKey = hourSnapshot.getKey();
                        if (hourSnapshot.hasChild("temperature")) {
                            Float temperatureValue = hourSnapshot.child("temperature").getValue(Float.class);

                            if (temperatureValue != null) {
                                try {
                                    int hourIndex = Integer.parseInt(hourKey.split(":")[0]); // Parse hour (e.g., "12:00")
                                    dataValues.add(new Entry(hourIndex, temperatureValue));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid hour format: " + hourKey);
                                }
                            }
                        }
                    }
                } else if ("Daily Line Chart".equals(selectedOption)) {
                    // Process daily data
                    DataSnapshot dailySnapshot = snapshot.child("daily");
                    for (DataSnapshot daySnapshot : dailySnapshot.getChildren()) {
                        String dayKey = daySnapshot.getKey();
                        if (daySnapshot.hasChild("temperature")) {
                            Float temperatureValue = daySnapshot.child("temperature").getValue(Float.class);

                            if (temperatureValue != null) {
                                try {
                                    int dayIndex = Integer.parseInt(dayKey); // Parse day index
                                    dataValues.add(new Entry(dayIndex, temperatureValue));
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Invalid day format: " + dayKey);
                                }
                            }
                        }
                    }
                }

                updateChartData(dataValues);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch temperature data: " + error.getMessage());
            }
        });
    }


    private void updateChartData(List<Entry> dataValues) {
        if (dataValues == null || dataValues.isEmpty()) {
            Log.w(TAG, "No data available to update the chart.");
            return;
        }

        LineDataSet dataSet = new LineDataSet(dataValues, "Temperature Data");
        setupDataSetAppearance(dataSet);

        temperatureChart.setData(new LineData(dataSet));
        temperatureChart.invalidate(); // Redraw the chart
    }

    private void setupDataSetAppearance(LineDataSet dataSet) {
        dataSet.setColor(getResources().getColor(R.color.teal));
        dataSet.setCircleColor(getResources().getColor(R.color.teal));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
    }

    private void customizeLineChart(View view, String selectedOption) {
        // Configure chart appearance
        temperatureChart.setPinchZoom(true);
        temperatureChart.getAxisRight().setEnabled(false);

        XAxis xAxis = temperatureChart.getXAxis();
        xAxis.setGranularity(1f);

        if ("Hourly Line Chart".equals(selectedOption)) {
            xAxis.setValueFormatter(new MyXAxisFormatter(hoursOfDay));
        } else if ("Daily Line Chart".equals(selectedOption)) {
            xAxis.setValueFormatter(new MyXAxisFormatter(daysOfMonth));
        }

        YAxis yAxis = temperatureChart.getAxisLeft();
        yAxis.setValueFormatter(new MyYAxisFormatter());

        Description description = new Description();
        description.setText(selectedOption);
        temperatureChart.setDescription(description);

        fetchTemperatureData();
    }

    private static class MyXAxisFormatter extends ValueFormatter {
        private final String[] labels;

        public MyXAxisFormatter(String[] labels) {
            this.labels = labels;
        }

        @Override
        public String getFormattedValue(float value) {
            int index = (int) value;
            if (index >= 0 && index < labels.length) {
                return labels[index];
            }
            return "";
        }
    }

    private static class MyYAxisFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            return String.format("%.1f°C", value);
        }
    }

}
