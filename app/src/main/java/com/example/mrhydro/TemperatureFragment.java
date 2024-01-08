package com.example.mrhydro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.example.mrhydro.databinding.FragmentTemperatureBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TemperatureFragment extends Fragment implements View.OnClickListener{
    private static final int UPDATE_INTERVAL = 2000;
    FragmentTemperatureBinding binding;
    DatabaseReference reference;
    LineChart celsiusChart;
    Handler handler = new Handler(Looper.getMainLooper());
    String[] daysOfMonth = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    public TemperatureFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the generated binding class
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        celsiusChart = view.findViewById(R.id.celsiusChart);

        MyXAxisFormatter xAxisFormatter = new MyXAxisFormatter(daysOfMonth);
        XAxis xAxis = celsiusChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);

        MyYAxisFormatter yAxisFormatter = new MyYAxisFormatter();
        YAxis leftYAxis = celsiusChart.getAxisLeft();
        leftYAxis.setValueFormatter(yAxisFormatter);

        LineDataSet celsiusDataSet = new LineDataSet(dataValues(), "Temperature Data");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(celsiusDataSet);
        LineData data = new LineData(dataSets);

        Description description = new Description();
        description.setText("Temperature Chart");
        celsiusChart.setDescription(description);

        // Set the data to the chart
        celsiusChart.setData(data);
        celsiusChart.invalidate();

        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.hideToolbar();

        // Read data from Firebase
        readTemperatureData();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
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

    private void readTemperatureData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");

        reference.child("Temperature in C").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof Double) {
                    double temperatureCelsius = (double) dataSnapshot.getValue();
                    double temperatureFahrenheit = celsiusToFahrenheit(temperatureCelsius);

                    Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureCelsius + "°C");

                    if (binding.celsiusValue != null && binding.fahrenheitValue != null) {
                        binding.celsiusValue.setText(String.format("%.2f", temperatureCelsius));
                        binding.fahrenheitValue.setText(String.format("%.2f", temperatureFahrenheit));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
            }
        });
    }
    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Fetch new data periodically
            readTemperatureData();
            // Schedule the next update
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    public void onDestroyView() {
        // Remove the callbacks to prevent memory leaks
        handler.removeCallbacks(updateRunnable);
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            openFragment(new HomeFragment());
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

