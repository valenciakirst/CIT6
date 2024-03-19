package com.example.mrhydro;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ConfigurationActivity extends AppCompatActivity {

    private static final String TAG = "ConfigurationActivity";

    TextView toWiFiConfig;
    TextView toSensorConfig;
    ImageView back;

    // Flags to track visibility state of fragments
    boolean isWifiConfigVisible = false;
    boolean isSensorConfigVisible = false;

    // Drawables for the arrows
    Drawable arrowDown;
    Drawable arrowUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        toWiFiConfig = findViewById(R.id.ConfigureWifiText);
        toSensorConfig = findViewById(R.id.ConfigureSensorText);

        // Load arrow drawables
        arrowDown = ContextCompat.getDrawable(this, R.drawable.baseline_keyboard_arrow_down_24);
        arrowUp = ContextCompat.getDrawable(this, R.drawable.baseline_keyboard_arrow_up_24); // Load different drawable for arrowUp

        // Set arrow down as compound drawable end for initial state
        setArrowDrawable(toWiFiConfig, arrowDown);
        setArrowDrawable(toSensorConfig, arrowDown);

        toWiFiConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!isWifiConfigVisible) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.wifi_container, new WifiFragment())
                                .commit();
                        isWifiConfigVisible = true;
                        setArrowDrawable(toWiFiConfig, arrowUp); // Change arrow to up
                    } else {
                        // If the fragment is already visible, hide it
                        getSupportFragmentManager().beginTransaction()
                                .remove(getSupportFragmentManager().findFragmentById(R.id.wifi_container))
                                .commit();
                        isWifiConfigVisible = false;
                        setArrowDrawable(toWiFiConfig, arrowDown); // Change arrow to down
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in replacing WiFi fragment: " + e.getMessage());
                }
            }
        });

        toSensorConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!isSensorConfigVisible) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.sensor_container, new SensorConfigFragment())
                                .commit();
                        isSensorConfigVisible = true;
                        setArrowDrawable(toSensorConfig, arrowUp); // Change arrow to up
                    } else {
                        // If the fragment is already visible, hide it
                        getSupportFragmentManager().beginTransaction()
                                .remove(getSupportFragmentManager().findFragmentById(R.id.sensor_container))
                                .commit();
                        isSensorConfigVisible = false;
                        setArrowDrawable(toSensorConfig, arrowDown); // Change arrow to down
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in replacing SensorConfigFragment: " + e.getMessage());
                }
            }
        });
    }

    private void setArrowDrawable(TextView textView, Drawable drawable) {
        // Set compound drawable end for the TextView
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
    }

    public void onBackButtonClick(View view) {
        onBackPressed();
    }
}
