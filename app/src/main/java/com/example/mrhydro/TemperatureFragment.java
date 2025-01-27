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
import com.google.firebase.auth.FirebaseAuth;
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
    boolean isInitialSetup = true;

    String PREFS_NAME = "MyPrefsFile";
    String IS_CELSIUS_KEY = "isCelsius";
    Switch temperatureSwitch;
    private Spinner dropdownMenu;
    private String lastStoredTimestamp = null;

    private FrameLayout tempChartContainer;
    public TemperatureFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        temperatureSwitch = view.findViewById(R.id.switch1);
        authenticateUser();

        dropdownMenu = view.findViewById(R.id.dropdownMenu);
        tempChartContainer = view.findViewById(R.id.lineChartContainer);


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

                // Only show the toast if this is not the initial setup
                if (!isInitialSetup) {
                    String toastMessage = isChecked ? "Switched to Celsius" : "Switched to Fahrenheit";
                    Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });


        isCelsius = getTemperatureUnitState();
        temperatureSwitch.setChecked(isCelsius);
        readTemperatureData();
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);
        return view;
    }
    private void authenticateUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInAnonymously()
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        Log.d("TemperatureFragment", "signInAnonymously:success");
                        // Authentication succeeded
                        // Now proceed with storing the temperature
                        storeTemperatureData(25.0); // test with a sample temperature for now
                    } else {
                        Log.w("TemperatureFragment", "signInAnonymously:failure", task.getException());
                        Toast.makeText(requireContext(), "Authentication Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void readTemperatureData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");

        // Use the correct key name based on your Firebase structure
        reference.child("TemperatureInC").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getValue() != null) {
                    Object value = dataSnapshot.getValue();

                    double temperatureCelsius = 0.0;
                    if (value instanceof Double) {
                        temperatureCelsius = (Double) value;
                    } else if (value instanceof Long) {
                        temperatureCelsius = ((Long) value).doubleValue();
                    } else {
                        Log.e("TemperatureFragment", "Unexpected data type: " + value);
                        return;
                    }

                    double temperatureValue = isCelsius ? temperatureCelsius : celsiusToFahrenheit(temperatureCelsius);

                    Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureValue +
                            (isCelsius ? "°C" : "°F"));

                    updateSingleTemperature(temperatureValue);
                } else {
                    Log.w("TemperatureFragment", "TemperatureInC data is null or doesn't exist.");
                }
            }

            private void updateSingleTemperature(double temperatureValue) {
                if (binding != null) {
                    binding.singleTemperatureValue.setText(String.format("%.2f", temperatureValue));
                    binding.singleTemperatureUnit.setText(isCelsius ? "°C" : "°F");
                } else {
                    Log.e("TemperatureFragment", "View binding is not initialized.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
            }
        });
    }





    private void storeTemperatureData(double temperatureValue) {
        if (temperatureValue >= 25) {
            DatabaseReference temperatureHistoryRef = FirebaseDatabase.getInstance().getReference("temperatureHistory");

            long currentTimeMillis = System.currentTimeMillis();

            // Store daily data
            long currentDay = currentTimeMillis / (24 * 60 * 60 * 1000); // Calculate current day as an integer
            temperatureHistoryRef.child("daily").child(String.valueOf(currentDay)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Double existingTemp = snapshot.child("temperature").getValue(Double.class);
                        if (existingTemp != null && temperatureValue > existingTemp) {
                            snapshot.getRef().child("temperature").setValue(temperatureValue);
                            snapshot.getRef().child("timestamp").setValue(currentTimeMillis);
                        }
                    } else {
                        snapshot.getRef().child("temperature").setValue(temperatureValue);
                        snapshot.getRef().child("timestamp").setValue(currentTimeMillis);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("TemperatureFragment", "Failed to update daily temperature", error.toException());
                }
            });

            // Store hourly data
            String currentHour = String.format("%02d:00", (currentTimeMillis / (60 * 60 * 1000)) % 24); // Format as "HH:00"
            temperatureHistoryRef.child("hourly").child(currentHour).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        Double existingTemp = snapshot.child("temperature").getValue(Double.class);
                        if (existingTemp != null && temperatureValue > existingTemp) {
                            snapshot.getRef().child("temperature").setValue(temperatureValue);
                            snapshot.getRef().child("timestamp").setValue(currentTimeMillis);
                        }
                    } else {
                        snapshot.getRef().child("temperature").setValue(temperatureValue);
                        snapshot.getRef().child("timestamp").setValue(currentTimeMillis);
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e("TemperatureFragment", "Failed to update hourly temperature", error.toException());
                }
            });
        } else {
            Log.d("TemperatureFragment", "Temperature is below 25°C, not storing.");
        }
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
