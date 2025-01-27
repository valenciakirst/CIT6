package com.example.mrhydro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.Switch;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.mrhydro.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Map;

public class HomeFragment extends Fragment implements View.OnClickListener {

    FragmentHomeBinding binding;
    private DatabaseReference groupsRef;
    private ArrayList<String> groupList = new ArrayList<>();
    Spinner sensorMenu;
    Switch autoManualSwitch;
    DatabaseReference misterStatusRef;
    FirebaseAuth mAuth;
    boolean isToggleInProgress = false;
    private static final String SHARED_PREFS = "mySharedPrefs";
    private static final String SELECTED_GROUP_KEY = "selectedGroup";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        CardView TempCard = view.findViewById(R.id.TempCard);
        CardView HumidityCard = view.findViewById(R.id.HumidityCard);


        // Firebase authentication check
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // User is not logged in
            showToast("User not logged in!");
            return view; // Exit early or handle user login flow here
        }

        // Existing Firebase initialization
        groupsRef = FirebaseDatabase.getInstance().getReference("groups");
        DatabaseReference temperatureRef = FirebaseDatabase.getInstance().getReference("DHT/TemperatureInC");
        temperatureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double temperature = snapshot.getValue(Double.class);
                if (temperature != null) {
                    handleTemperatureChange(temperature);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Failed to read temperature: " + error.getMessage());
            }
        });
        if (user != null) {
            String userId = user.getUid();
            misterStatusRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Mister Status");
        }

        if (user != null) {
            String userId = user.getUid();
            misterStatusRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Mister Status");
        }


        // Initialize the Switch and set listener
        autoManualSwitch = view.findViewById(R.id.switch5);
        autoManualSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> handleAutoManualSwitch(isChecked));
        Button manageGroupButton = view.findViewById(R.id.add_group_button);
        manageGroupButton.setOnClickListener(v -> openManageGroupFragment());

        if (misterStatusRef != null) {
            misterStatusRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Object status = task.getResult().getValue();

                    boolean isAuto = false;
                    boolean isMisterOn = false;
                    boolean isRedColor = false;

                    if (status instanceof Map) {
                        Map<String, Object> statusMap = (Map<String, Object>) status;
                        isAuto = "AUTO".equals(statusMap.get("mode"));
                        isMisterOn = Boolean.TRUE.equals(statusMap.get("toggleState"));
                        isRedColor = Boolean.TRUE.equals(statusMap.get("isRedColor"));
                    }

                    autoManualSwitch.setChecked(isAuto);
                    autoManualSwitch.setText(isAuto ? "AUTO" : "MANUAL");

                    binding.MisterToggle.setEnabled(isAuto);
                    binding.MisterToggle.setChecked(isMisterOn);

                    if (isMisterOn && isRedColor) {
                        binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                    }
                } else {
                    showToast("Failed to retrieve status from Firebase");
                }
            });
        }

        FrameLayout frameLayout = view.findViewById(R.id.sensorWidgets);
        if (frameLayout != null && savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.sensorWidgets, new SensorsFragment())
                    .commit();
        }

        CardView mistercard = view.findViewById(R.id.MisterCard);
        sensorMenu = view.findViewById(R.id.sensormenu);

        ToggleButton misterToggle = binding.MisterToggle;
        misterToggle.setOnClickListener(this);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showToolbar();

        mistercard.setOnClickListener(this);

        Button configureTempButton = view.findViewById(R.id.configureTempButton);
        configureTempButton.setOnClickListener(v -> {
            boolean isAutoMode = autoManualSwitch.isChecked();
            ConfigTempHumid configTempHumidDialog = new ConfigTempHumid(isAutoMode);
            configTempHumidDialog.show(getParentFragmentManager(), "ConfigTempHumidDialog");
        });

        loadGroupsForDropdown();
        setupDropdownMenu();

        return view;
    }

    private void handleTemperatureChange(double temperature) {
        if (autoManualSwitch.isChecked()) { // Check if in AUTO mode
            if (temperature >= 35) {
                // Turn the mister ON
                misterStatusRef.child("toggleState").setValue(true);
                misterStatusRef.child("isRedColor").setValue(true);
                binding.MisterToggle.setChecked(true);
                binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                showToast("Temperature exceeded 35°C. Mister turned ON.");
            } else {
                // Turn the mister OFF
                misterStatusRef.child("toggleState").setValue(false);
                misterStatusRef.child("isRedColor").setValue(false);
                binding.MisterToggle.setChecked(false);
                binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                showToast("Temperature below 35°C. Mister turned OFF.");
            }
        }
    }

    private void loadGroupsForDropdown() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupList.clear();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getValue(String.class);
                    groupList.add(groupName);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, groupList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                sensorMenu.setAdapter(adapter);

                SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                String selectedGroup = sharedPreferences.getString(SELECTED_GROUP_KEY, null);
                if (selectedGroup != null && groupList.contains(selectedGroup)) {
                    int position = groupList.indexOf(selectedGroup);
                    sensorMenu.setSelection(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupDropdownMenu() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.DHT22,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sensorMenu.setAdapter(adapter);
        sensorMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedGroup = parentView.getItemAtPosition(position).toString();

                SharedPreferences sharedPreferences = requireContext().getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SELECTED_GROUP_KEY, selectedGroup);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void handleAutoManualSwitch(boolean isChecked) {
        DatabaseReference switchValueRef = FirebaseDatabase.getInstance()
                .getReference().child("AutoManualSwitch").child("SwitchValue");

        Button configureTempButton = getView().findViewById(R.id.configureTempButton); // Reference the button

        if (isChecked) {
            autoManualSwitch.setText("AUTO");
            showToast("Switched to AUTO mode");

            misterStatusRef.child("mode").setValue("AUTO");
            switchValueRef.setValue(false);

            binding.MisterToggle.setEnabled(true);

            // Disable the button and change its appearance to make it look disabled
            configureTempButton.setEnabled(false); // Disable the button
            configureTempButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray)); // Change background color to gray
            configureTempButton.setTextColor(getResources().getColor(android.R.color.darker_gray)); // Change text color to gray for disabled appearance

            misterStatusRef.child("toggleState").get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Boolean toggleState = task.getResult().getValue(Boolean.class);
                    if (toggleState != null) {
                        binding.MisterToggle.setChecked(toggleState);

                        if (toggleState) {
                            binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                            misterStatusRef.child("isRedColor").setValue(true);
                        } else {
                            binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                        }
                    }
                }
            });
        } else {
            autoManualSwitch.setText("MANUAL");
            showToast("Switched to MANUAL mode");

            misterStatusRef.child("mode").setValue("MANUAL");
            switchValueRef.setValue(true);

            binding.MisterToggle.setEnabled(false);
            binding.MisterToggle.setChecked(false);

            // Enable the button and restore its appearance
            configureTempButton.setEnabled(true); // Enable the button
            configureTempButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light)); // Change to the original color
            configureTempButton.setTextColor(getResources().getColor(android.R.color.white)); // Restore text color

            binding.MisterToggle.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            misterStatusRef.child("isRedColor").setValue(false);
        }
    }



    private void openManageGroupFragment() {
        ManageGroupFragment manageGroupFragment = new ManageGroupFragment();
        manageGroupFragment.show(getParentFragmentManager(), "ManageGroupFragment");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.MisterToggle) {
            if (isToggleInProgress) return;
            isToggleInProgress = true;
            ToggleButton misterToggle = (ToggleButton) v;
            handleMisterSwitch(misterToggle.isChecked());
            isToggleInProgress = false;
        }
    }

    private void handleMisterSwitch(boolean isChecked) {
        ToggleButton misterToggle = binding.MisterToggle;

        // Update the Firebase database based on the toggle state
        DatabaseReference switchStateRef = FirebaseDatabase.getInstance()
                .getReference("DHT").child("Switch");

        if (isChecked) {
            showToast("Mister turned ON");
            misterToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));

            // Update Firebase values
            misterStatusRef.child("toggleState").setValue(true);
            misterStatusRef.child("isRedColor").setValue(true);
            switchStateRef.setValue(true); // Update "Switch" value to true
        } else {
            showToast("Mister turned OFF");
            misterToggle.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            // Update Firebase values
            misterStatusRef.child("toggleState").setValue(false);
            misterStatusRef.child("isRedColor").setValue(false);
            switchStateRef.setValue(false); // Update "Switch" value to false
        }
    }


    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}