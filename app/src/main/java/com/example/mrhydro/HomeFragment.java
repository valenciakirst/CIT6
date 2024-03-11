package com.example.mrhydro;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mrhydro.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment implements View.OnClickListener {


    FragmentHomeBinding binding;
    DatabaseReference reference;
    Handler handler = new Handler(Looper.getMainLooper());
    Spinner sensorMenu;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

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
            openFragment(new MisterFragment());
        }
    }

    private void handleMisterSwitch(boolean isChecked) {
        if (isChecked) {
            showToast("Mister turned ON");
            // Perform any other actions you need when the mister is turned ON
        } else {
            showToast("Mister turned OFF");
            // Perform any other actions you need when the mister is turned OFF
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
    }
}