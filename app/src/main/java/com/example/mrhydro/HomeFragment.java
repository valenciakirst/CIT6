package com.example.mrhydro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton; // Import ToggleButton instead of Switch
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.mrhydro.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment implements View.OnClickListener {

    FragmentHomeBinding binding;
    Spinner sensorMenu;
    DatabaseReference misterStatusRef; // Reference to the Mister status in the database
    FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();

        // Initialize database reference
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            misterStatusRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId).child("Mister Status");
        }

        FrameLayout frameLayout = view.findViewById(R.id.sensorWidgets);

        if (frameLayout != null) {
            // Initially, replace the FrameLayout with HomeFragment content
            if (savedInstanceState == null) {
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.sensorWidgets, new SensorsFragment())
                        .commit();
            }
        }

        CardView mistercard = view.findViewById(R.id.MisterCard);
        sensorMenu = view.findViewById(R.id.sensormenu);

        // Binding MisterToggle
        ToggleButton misterToggle = binding.MisterSwitch; // Update to ToggleButton
        misterToggle.setOnClickListener(this); // Register onClickListener to handle toggle

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.showToolbar();

        mistercard.setOnClickListener(this);

        setupDropdownMenu();

        return view;
    }

    private void openSensorsFragment() {
        SensorsFragment sensorsFragment = new SensorsFragment();

        // Replace the existing fragment with SensorsFragment
        getChildFragmentManager().beginTransaction()
                .replace(R.id.sensorWidgets, sensorsFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.MisterCard) {
            handleMisterSwitch(binding.MisterSwitch.isChecked()); // Pass current toggle state
        } else if (v.getId() == R.id.MisterSwitch) { // Added to handle toggle click
            handleMisterSwitch(((ToggleButton) v).isChecked()); // Cast to ToggleButton and get state
        }
    }

    private void handleMisterSwitch(boolean isChecked) {
        ToggleButton misterToggle = binding.MisterSwitch;
        if (isChecked) {
            showToast("Mister turned ON");
            misterToggle.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light)); // Change background color to red
            misterStatusRef.setValue(true); // Update database with true
        } else {
            showToast("Mister turned OFF");
            misterToggle.setBackgroundColor(getResources().getColor(android.R.color.darker_gray)); // Set background color to grey
            misterStatusRef.setValue(false); // Update database with false
        }
    }


    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void setupDropdownMenu() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.DHT22,  // Add a string array resource for options
                android.R.layout.simple_spinner_item
        );

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sensorMenu.setAdapter(adapter);

        sensorMenu.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedItem = parentView.getItemAtPosition(position).toString();
                // Handle item selection here if needed
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here
            }
        });
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
