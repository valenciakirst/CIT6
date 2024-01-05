package com.example.mrhydro;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentTemperatureBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TemperatureFragment extends Fragment implements View.OnClickListener{

    private static final int UPDATE_INTERVAL = 2000;

    FragmentTemperatureBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());


    public TemperatureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the generated binding class
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);
        // Read data from Firebase
        readTemperatureData();

        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);

        return view;
    }

    private void readTemperatureData() {
        reference = FirebaseDatabase.getInstance().getReference("DHT");

        reference.child("Temperature in C").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if the value is a Double
                if (dataSnapshot.exists() && dataSnapshot.getValue() instanceof Double) {
                    // Convert the value to a String
                    String temperatureValue = String.valueOf(dataSnapshot.getValue());
                    Log.d("TemperatureFragment", "Temperature value from Firebase: " + temperatureValue);

                    // Update your UI or perform actions with the temperature data
                    if (temperatureValue != null) {
                        // Assuming you have a TextView inside the layout associated with TemperatureFragment,
                        // replace R.id.TempCard with its actual ID
                        binding.temperatureValue.setText(temperatureValue);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors here
                Log.e("TemperatureFragment", "Failed to read temperature data", databaseError.toException());
            }
        });
    }

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            readTemperatureData();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };
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
}
