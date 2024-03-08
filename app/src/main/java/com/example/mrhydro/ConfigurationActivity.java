package com.example.mrhydro;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class ConfigurationActivity extends AppCompatActivity {

    private static final String TAG = "ConfigurationActivity";

    TextView toWiFiConfig;
    TextView toSensorConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        toWiFiConfig = findViewById(R.id.ConfigureWifiText);
        toSensorConfig = findViewById(R.id.ConfigureSensorText);

        toWiFiConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.wifi_container, new WifiFragment())
                            .commit();
                } catch (Exception e) {
                    Log.e(TAG, "Error in replacing WiFi fragment: " + e.getMessage());
                }
            }
        });

        toSensorConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.sensor_container, new SensorConfigFragment())
                            .commit();
                } catch (Exception e) {
                    Log.e(TAG, "Error in replacing SensorConfigFragment: " + e.getMessage());
                }
            }
        });
    }
}
