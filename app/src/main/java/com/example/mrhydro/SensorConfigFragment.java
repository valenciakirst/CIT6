package com.example.mrhydro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class SensorConfigFragment extends Fragment {

    private static final String TAG = "SensorConfigFragment";

    private EditText groupname, type, pin;
    private Button addButton;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor, container, false);

        // Initialize views
        groupname = view.findViewById(R.id.sensorgroupname);
        type = view.findViewById(R.id.sensortype);
        pin = view.findViewById(R.id.pin);
        addButton = view.findViewById(R.id.sensorbutton);

        // Initialize Firebase
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Assuming 'users' is your top-level node
            databaseReference = firebaseDatabase.getReference().child("users").child(currentUser.getUid()).child("configuration");
        }

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToFirebase();
                Toast.makeText(getContext(), "Sensor added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void saveToFirebase() {
        try {
            String sensorgroup = groupname.getText().toString().trim();
            String sensorpin = pin.getText().toString().trim();
            String sensortype = type.getText().toString().trim();

            if (!sensorgroup.isEmpty() && !sensorpin.isEmpty()) {
                // Create a Map to represent the data
                Map<String, Object> configurationMap = new HashMap<>();
                configurationMap.put("sensorgroup", sensorgroup);
                configurationMap.put("sensorpin", sensorpin);
                configurationMap.put("sensortype", sensortype);

                // Push the data to Firebase Database
                if (databaseReference != null) {
                    databaseReference.setValue(configurationMap);
                } else {
                    Log.e(TAG, "Database reference is null");
                }

                // Clear EditText fields after saving
                groupname.setText("");
                pin.setText("");
                type.setText("");
            } else {
                Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in saving data to Firebase: " + e.getMessage());
        }
    }
}
