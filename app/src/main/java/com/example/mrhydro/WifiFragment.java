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
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class WifiFragment extends Fragment {

    private EditText ssidEditText, passwordEditText;
    private Button addButton;
    private DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi, container, false);

        // Initialize views
        ssidEditText = view.findViewById(R.id.ssidwifi);
        passwordEditText = view.findViewById(R.id.passwordwifi);
        addButton = view.findViewById(R.id.wifiaddbutton);

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
                Toast.makeText(getContext(), "WiFi added successfully", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void saveToFirebase() {
        String ssid = ssidEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!ssid.isEmpty() && !password.isEmpty()) {
            // Create a Map to represent the data
            Map<String, Object> configurationMap = new HashMap<>();
            configurationMap.put("ssid", ssid);
            configurationMap.put("password", password);

            // Push the data to Firebase Database
            databaseReference.setValue(configurationMap);

            // Clear EditText fields after saving
            ssidEditText.setText("");
            passwordEditText.setText("");
        }
    }
}
