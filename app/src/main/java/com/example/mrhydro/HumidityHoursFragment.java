package com.example.mrhydro;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class HumidityHoursFragment extends Fragment {

    private String[] hoursOfDay = {"00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00"};
    private String[] daysOfMonth = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    private LineChart humidityChart;

    public HumidityHoursFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_humidity_hours, container, false);

        // Get the selected option from arguments
        Bundle args = getArguments();
        if (args != null) {
            String selectedOption = args.getString("selectedOption");
            humidityChart = view.findViewById(R.id.humidityChart);
            fetchDataFromFirebase(selectedOption);
        }

        return view;
    }

    private void fetchDataFromFirebase(String selectedOption) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference humidityRef = database.getReference("humidityHistory");

        // Choose the correct Firebase reference based on the selected option
        DatabaseReference selectedRef = (selectedOption.equals("Hourly Line Chart")) ? humidityRef.child("hourly") : humidityRef.child("daily");

        selectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Entry> dataValues = new ArrayList<>();

                // Iterate through the data and add it to the chart
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String key = snapshot.getKey();
                        float humidityValue;

                        // Handle both simple numeric values and complex objects
                        Object value = snapshot.getValue();
                        if (value instanceof Number) {
                            // Direct numeric value
                            humidityValue = ((Number) value).floatValue();
                        } else if (value instanceof Map) {
                            // Complex object with "humidity" field
                            Map<String, Object> map = (Map<String, Object>) value;
                            if (map.containsKey("humidity") && map.get("humidity") instanceof Number) {
                                humidityValue = ((Number) map.get("humidity")).floatValue();
                            } else {
                                Log.w("FirebaseData", "Skipping entry due to missing or invalid 'humidity' field: " + snapshot);
                                continue; // Skip invalid data
                            }
                        } else {
                            Log.w("FirebaseData", "Skipping unrecognized data format: " + snapshot);
                            continue; // Skip unrecognized data formats
                        }

                        // Only include valid data
                        if (humidityValue >= 30) { // Example: filter for humidity >= 30
                            int xValue = (selectedOption.equals("Hourly Line Chart")) ? getXValueFromTime(key) : getXValueFromDay(key);
                            dataValues.add(new Entry(xValue, humidityValue));
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseData", "Error processing entry: " + snapshot, e);
                    }
                }

                updateChart(dataValues, selectedOption);
            }



            @Override
            public void onCancelled(DatabaseError error) {
                // Handle error appropriately
            }
        });
    }
    private String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }


    private int getXValueFromTime(String time) {
        // For hourly data, use the hour to map it to the X-axis
        for (int i = 0; i < hoursOfDay.length; i++) {
            if (hoursOfDay[i].equals(time)) {
                return i; // Return the index of the hour
            }
        }
        return 0; // Default if not found
    }

    private int getXValueFromDay(String day) {
        // For daily data, use the day of the month to map it to the X-axis
        for (int i = 0; i < daysOfMonth.length; i++) {
            if (daysOfMonth[i].equals(day)) {
                return i; // Return the index of the day
            }
        }
        return 0; // Default if not found
    }

    private void updateChart(ArrayList<Entry> dataValues, String selectedOption) {
        if (dataValues.isEmpty()) {
            Log.d("HumidityChart", "No data to display");
        } else {
            LineDataSet dataSet = new LineDataSet(dataValues, "Humidity Data");
            dataSet.setColor(Color.BLUE); // Line color
            dataSet.setLineWidth(2f); // Line width

            // Disable the circle markers (red dots)
            dataSet.setDrawCircles(false);

            // Create the LineData object and set it to the chart
            LineData lineData = new LineData(dataSet);
            humidityChart.setData(lineData);

            // Customize chart based on selected option
            Description description = new Description();
            description.setText(selectedOption);
            humidityChart.setDescription(description);

            // Configure X-Axis labels based on the selected option
            XAxis xAxis = humidityChart.getXAxis();
            if (selectedOption.equals("Hourly Line Chart")) {
                xAxis.setValueFormatter(new MyXAxisFormatter(hoursOfDay)); // Use hourly data
            } else if (selectedOption.equals("Daily Line Chart")) {
                xAxis.setValueFormatter(new MyXAxisFormatter(daysOfMonth)); // Use daily data
            }

            // Configure Y-Axis for the humidity values
            YAxis leftAxis = humidityChart.getAxisLeft();
            leftAxis.setValueFormatter(new MyYAxisFormatter()); // Format Y-axis for humidity
            humidityChart.invalidate(); // Refresh the chart
        }
    }


}
