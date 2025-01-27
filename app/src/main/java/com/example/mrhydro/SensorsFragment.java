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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentSensorsBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SensorsFragment extends Fragment implements View.OnClickListener, ConfigTempHumid.ConfigUpdateListener {
    private static final int UPDATE_INTERVAL = 2000;
    FragmentSensorsBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    private String humidityValue;
    private boolean isCelsius = true;
    private String PREFS_NAME = "MyPrefsFile";
    private String IS_CELSIUS_KEY = "isCelsius";
    private Switch temperatureSwitch;
    private TextView setTempTextView;
    private TextView setHumidityTextView;
    private double lastTemperatureInput = 0.0;
    private boolean isUserInput = false;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSensorsBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        // Initialize views
        temperatureSwitch = rootView.findViewById(R.id.switch2);
        CardView tempCard = rootView.findViewById(R.id.TempCard);
        CardView humidityCard = rootView.findViewById(R.id.HumidityCard);
        setTempTextView = rootView.findViewById(R.id.setTemp);
        setHumidityTextView = rootView.findViewById(R.id.setHumidity);

        // Set listeners
        tempCard.setOnClickListener(this);
        humidityCard.setOnClickListener(this);

        // Initialize temperature switch and listener
        temperatureSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCelsius = isChecked; // Update unit preference
            saveTemperatureUnitState(isCelsius); // Persist the choice
            updateTemperatureBasedOnUnit(); // Recalculate the temperature display
            Toast.makeText(requireContext(),
                    isChecked ? "Switched to Celsius" : "Switched to Fahrenheit",
                    Toast.LENGTH_SHORT).show(); // Notify user
        });

        // Restore the saved unit preference and set the switch accordingly
        isCelsius = getTemperatureUnitState();
        temperatureSwitch.setChecked(isCelsius);

        // Fetch data and periodically update
        readTemperatureData();
        readHumidityData();
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        // Fetch initial Firebase values
        DatabaseReference configRef = FirebaseDatabase.getInstance().getReference("AutoManualSwitch");
        configRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    double tempC = snapshot.child("TemperatureInC").getValue(Double.class);
                    double humidity = snapshot.child("Humidity").getValue(Double.class);

                    setTempTextView.setText("Set Temperature: " + tempC + "°C");
                    setHumidityTextView.setText("Set Humidity: " + humidity + "%");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("SensorsFragment", "Failed to read config data", error.toException());
            }
        });

        return rootView;
    }


    @Override
    public void onConfigUpdated(double temp, double humidity) {
        // Update UI with formatted temperature and humidity
        setTempTextView.setText(String.format("Set Temperature: %.2f°C", temp));
        setHumidityTextView.setText(String.format("Set Humidity: %.2f%%", humidity));
    }




    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.TempCard) {
            openFragment(new TemperatureFragment());
        } else if (v.getId() == R.id.HumidityCard) {
            openFragment(new HumidityFragment());
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void readHumidityData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");
        reference.child("Humidity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Object humidityObject = dataSnapshot.getValue();
                    if (humidityObject != null) {
                        humidityValue = String.valueOf(humidityObject);
                        Log.d("HumidityFragment", "Humidity value from Firebase: " + humidityValue);
                        if (binding.humidityValue != null) {
                            binding.humidityValue.setText(humidityValue + "");
                        }
                    } else {
                        Log.e("HumidityFragment", "Humidity data is null");
                    }
                } else {
                    Log.e("HumidityFragment", "DataSnapshot does not exist for Humidity");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HumidityFragment", "Failed to read humidity data", databaseError.toException());
                Toast.makeText(getContext(), "Failed to read humidity data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void readTemperatureData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");

        // Use the correct key name based on your Firebase structure
        reference.child("TemperatureInC").addListenerForSingleValueEvent(new ValueEventListener() {
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
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
            }
        });
    }




    private void updateTemperatureCard(double celsius, double fahrenheit) {
        if (binding.singleTemperatureValue != null && binding.singleTemperatureUnit != null) {
            if (isCelsius) {
                binding.singleTemperatureValue.setText(String.format("%.2f", celsius));
                binding.singleTemperatureUnit.setText("°C");
            } else {
                binding.singleTemperatureValue.setText(String.format("%.2f", fahrenheit));
                binding.singleTemperatureUnit.setText("°F");
            }
        }
    }


    private void handleUserInput(double inputValue) {
        lastTemperatureInput = inputValue;

        // Display only in Set Temperature TextView
        setTempTextView.setText(
                String.format("Set Temperature: %.2f %s",
                        isCelsius ? inputValue : celsiusToFahrenheit(inputValue),
                        isCelsius ? "°C" : "°F")
        );

    }


    private void updateTemperatureBasedOnUnit() {
        if (isUserInput) {
            double displayedTemp = isCelsius
                    ? lastTemperatureInput
                    : celsiusToFahrenheit(lastTemperatureInput);

            setTempTextView.setText(
                    String.format("Set Temperature: %.2f %s", displayedTemp, isCelsius ? "°C" : "°F")
            );
        }
    }
    private double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    private double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
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

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            readHumidityData();
            readTemperatureData();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(updateRunnable);
        super.onDestroyView();
    }
}