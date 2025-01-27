package com.example.mrhydro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfigTempHumid extends AppCompatDialogFragment {

    public interface ConfigUpdateListener {
        void onConfigUpdated(double temp, double humidity);
    }

    private ConfigUpdateListener listener;
    private DatabaseReference configRef;
    private double targetTemperature;
    private double targetHumidity;

    public ConfigTempHumid(boolean isAutoMode) {
        // Optionally handle auto mode (not used here)
    }

    public void setConfigUpdateListener(ConfigUpdateListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_config_temp_dialog, container, false);

        configRef = FirebaseDatabase.getInstance().getReference().child("AutoManualSwitch");
        EditText tempEditText = rootView.findViewById(R.id.Temp);
        EditText humidEditText = rootView.findViewById(R.id.Humid);
        Button setThresholdButton = rootView.findViewById(R.id.set_threshold_button);

        // Set threshold button click listener
        setThresholdButton.setOnClickListener(v -> {
            String temperatureInput = tempEditText.getText().toString();
            String humidityInput = humidEditText.getText().toString();

            if (!temperatureInput.isEmpty() && !humidityInput.isEmpty()) {
                targetTemperature = Double.parseDouble(temperatureInput);
                targetHumidity = Double.parseDouble(humidityInput);

                // Save configuration to Firebase
                saveConfigToFirebase(targetTemperature, targetHumidity);

                if (listener != null) {
                    listener.onConfigUpdated(targetTemperature, targetHumidity);
                }
            } else {
                Toast.makeText(getContext(), "Please enter valid inputs", Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }

    private void saveConfigToFirebase(double temperature, double humidity) {
        configRef.child("TemperatureInC").setValue(temperature);
        configRef.child("Humidity").setValue(humidity);

        // Display a toast message to confirm threshold has been set
        Toast.makeText(getContext(),
                String.format("Threshold set: %.2f°C and %.2f%% humidity", temperature, humidity),
                Toast.LENGTH_SHORT).show();
    }
}
