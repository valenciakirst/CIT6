package com.example.mrhydro;

import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentTemperatureBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TemperatureFragment extends Fragment implements View.OnClickListener {
    private static final int UPDATE_INTERVAL = 2000;
    FragmentTemperatureBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    boolean isCelsius = false;
    String PREFS_NAME = "MyPrefsFile";
    String IS_CELSIUS_KEY = "isCelsius";
    Switch temperatureSwitch;
    private Spinner dropdownMenu;
    private FrameLayout tempChartContainer;
    private LineChart celsiusChart;

    public TemperatureFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        temperatureSwitch = view.findViewById(R.id.switch1);

        dropdownMenu = view.findViewById(R.id.dropdownMenu);
        tempChartContainer = view.findViewById(R.id.lineChartContainer);
//        celsiusChart = view.findViewById(R.id.celsiusChart);

        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.hideToolbar();
        setupDropdownMenu();
        temperatureSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update temperature unit based on the switch state
                isCelsius = isChecked;
                saveTemperatureUnitState(isCelsius);
                readTemperatureData(); // Update displayed temperature values

                String toastMessage = isChecked ? "Switched to Celsius" : "Switched to Fahrenheit";
                Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
            }
        });

        isCelsius = getTemperatureUnitState();
        temperatureSwitch.setChecked(isCelsius);
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
                    double temperatureValue = isCelsius ? temperatureCelsius : celsiusToFahrenheit(temperatureCelsius);

                    Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureValue +
                            (isCelsius ? "°C" : "°F"));

                    updateSingleTemperature(temperatureCelsius, temperatureValue);
                }
            }

            private void updateSingleTemperature(double temperatureCelsius, double temperatureValue) {
                if (binding.singleTemperatureValue != null && binding.singleTemperatureUnit != null) {
                    binding.singleTemperatureValue.setText(String.format("%.2f", temperatureValue));
                    binding.singleTemperatureUnit.setText(isCelsius ? "°C" : "°F");
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

    private void saveTemperatureUnitState(boolean isCelsius) {
        SharedPreferences.Editor editor = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        editor.putBoolean(IS_CELSIUS_KEY, isCelsius);
        editor.apply();
    }

    private boolean getTemperatureUnitState() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(IS_CELSIUS_KEY, false); // Default to false (Fahrenheit)
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
