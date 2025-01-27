package com.example.mrhydro;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentHumidityBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class HumidityFragment extends Fragment implements View.OnClickListener {
    private static final int UPDATE_INTERVAL = 2000;

    FragmentHumidityBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());

    private Spinner dropdownMenu;
    private FrameLayout lineChartContainer;
    private String humidityValue; // Declare the humidityValue variable

    public HumidityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHumidityBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        readHumidityData();
        dropdownMenu = view.findViewById(R.id.dropdownMenu);
        lineChartContainer = view.findViewById(R.id.lineChartContainer);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.hideToolbar();

        setupDropdownMenu();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            openFragment(new HomeFragment());
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
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
                    Object value = dataSnapshot.getValue();
                    if (value != null) {
                        humidityValue = String.valueOf(value);
                        Log.d("HumidityFragment", "Humidity value from Firebase: " + humidityValue);

                        // Update the UI
                        binding.humidityValue.setText(humidityValue);

                        // Store the humidity value
                        storeHumidityData(humidityValue);
                    } else {
                        Log.e("HumidityFragment", "Humidity value is null");
                    }
                } else {
                    Log.e("HumidityFragment", "Humidity value does not exist in Firebase");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HumidityFragment", "Failed to read humidity data", databaseError.toException());
                Toast.makeText(getContext(), "Failed to read humidity data", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            readHumidityData();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };
    private void storeHumidityData(String humidity) {
        // Convert humidity to a float for comparison
        float currentHumidity = Float.parseFloat(humidity);

        // Reference to the "humidityHistory" node in Firebase
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference("humidityHistory");

        // Access the "hourly" and "daily" nodes
        DatabaseReference hourlyRef = historyRef.child("hourly");
        DatabaseReference dailyRef = historyRef.child("daily");

        // Get current time in milliseconds
        long timestamp = System.currentTimeMillis();  // Current time in milliseconds (Unix timestamp)

        // Check and store the highest humidity for the current hour
        String currentHour = getCurrentHour(); // Format as "HH:00"
        hourlyRef.child(currentHour).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HumidityData existingData = dataSnapshot.getValue(HumidityData.class);
                float maxHourlyHumidity = existingData != null ? existingData.getHumidity() : 0;
                if (currentHumidity > maxHourlyHumidity) {
                    // Store humidity along with the timestamp
                    hourlyRef.child(currentHour).setValue(new HumidityData(currentHumidity, timestamp))
                            .addOnSuccessListener(aVoid -> Log.d("HumidityFragment", "Max hourly humidity stored"))
                            .addOnFailureListener(e -> Log.e("HumidityFragment", "Failed to store max hourly humidity", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HumidityFragment", "Failed to access hourly humidity", databaseError.toException());
            }
        });

        // Check and store the highest humidity for the current day
        String currentDay = getCurrentDay(); // Get the current day (e.g., "6", "7", ...)
        dailyRef.child(currentDay).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HumidityData existingData = dataSnapshot.getValue(HumidityData.class);
                float maxDailyHumidity = existingData != null ? existingData.getHumidity() : 0;
                if (currentHumidity > maxDailyHumidity) {
                    // Store humidity along with the timestamp
                    dailyRef.child(currentDay).setValue(new HumidityData(currentHumidity, timestamp))
                            .addOnSuccessListener(aVoid -> Log.d("HumidityFragment", "Max daily humidity stored"))
                            .addOnFailureListener(e -> Log.e("HumidityFragment", "Failed to store max daily humidity", e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("HumidityFragment", "Failed to access daily humidity", databaseError.toException());
            }
        });
    }


    private String getCurrentHour() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY); // Get the current hour (0-23)
        return String.format("%02d:00", hour); // Format it as "HH:00" (e.g., "13:00")
    }

    private String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH); // Get the current day of the month
        return String.valueOf(dayOfMonth); // Return as string (e.g., "6", "7", ...)
    }

    private void setupDropdownMenu() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.line_chart_options,
                android.R.layout.simple_spinner_item
        );

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
        HumidityHoursFragment lineChartFragment = new HumidityHoursFragment();

        Bundle bundle = new Bundle();
        bundle.putString("selectedOption", selectedOption);
        lineChartFragment.setArguments(bundle);

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.lineChartContainer, lineChartFragment);
        transaction.commit();
    }
}
