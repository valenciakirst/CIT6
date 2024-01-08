package com.example.mrhydro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import com.example.mrhydro.databinding.FragmentTemperatureBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class TemperatureFragment extends Fragment implements View.OnClickListener{
    long lastTimestamp = 0;
    int UPDATE_INTERVAL = 2000;
    FragmentTemperatureBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    private Spinner dropdownMenu;
    private FrameLayout tempChartContainer;
    private LineChart celsiusChart;


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


        dropdownMenu = view.findViewById(R.id.dropdownMenu);
        tempChartContainer = view.findViewById(R.id.lineChartContainer);


        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.hideToolbar();

        setupDropdownMenu();

        // Read data from Firebase
        readTemperatureData();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
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
                    logTemperatureData(temperatureCelsius, temperatureFahrenheit);

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

    private void logTemperatureData(double temperatureCelsius, double temperatureFahrenheit) {
        long currentTimestamp = System.currentTimeMillis();

        // Check if the time interval is greater than 30 minutes
        if (currentTimestamp - lastTimestamp >= TimeUnit.MINUTES.toMillis(30)) {
            // Format the timestamp
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String formattedTime = dateFormat.format(new Date(currentTimestamp));

            // Log the temperature with timestamp
            Log.d("TemperatureFragment", "Timestamp: " + formattedTime +
                    " - Temperature: " + temperatureCelsius + "°C / " + temperatureFahrenheit + "°F");

            // Update the last timestamp
            lastTimestamp = currentTimestamp;

            // Update the chart with the new data point
            updateChart(currentTimestamp, temperatureCelsius);
        }
    }

    private void updateChart(long timestamp, double temperatureCelsius) {
        // Add the new data point to the chart
        LineData data = celsiusChart.getData();
        ILineDataSet dataSet = data.getDataSetByIndex(0);

        float xValue = getXValueForTimestamp(timestamp);
        float yValue = (float) temperatureCelsius;

        Entry entry = new Entry(xValue, yValue);
        dataSet.addEntry(entry);

        // Notify the chart that the data has changed
        celsiusChart.notifyDataSetChanged();
        celsiusChart.invalidate();
    }

    private float getXValueForTimestamp(long timestamp) {
        // Calculate the day of the month for the timestamp
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        int dayOfMonth = Integer.parseInt(dayFormat.format(new Date(timestamp)));

        // Return the corresponding x-axis value
        return (float) dayOfMonth;
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
    private void setupDropdownMenu() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.line_chart_options,  // Add a string array resource for options
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        dropdownMenu.setAdapter(adapter);

        dropdownMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();

                showLineChartFragment(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
    }

    private void showLineChartFragment(String selectedOption) {
        // Create an instance of the TemperatureChartsFragment
        TemperatureChartsFragment lineChartFragment = new TemperatureChartsFragment();

        // Pass the selected option to the TemperatureChartsFragment
        Bundle bundle = new Bundle();
        bundle.putString("selectedOption", selectedOption);
        lineChartFragment.setArguments(bundle);

        // Replace the existing fragment with the TemperatureChartsFragment
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.tempChartContainer, lineChartFragment);  // Use the correct container ID
        transaction.commit();
    }


    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

