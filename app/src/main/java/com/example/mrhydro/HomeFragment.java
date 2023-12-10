package com.example.mrhydro;

import android.content.Intent; // Add this import statement
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mrhydro.databinding.ActivityMainBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment implements View.OnClickListener {
    // ...

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CardView tempcard = view.findViewById(R.id.TempCard);
        CardView humiditycard = view.findViewById(R.id.HumidityCard);
        CardView mistercard = view.findViewById(R.id.MisterCard);
        ImageView notificationbt = view.findViewById(R.id.notificationIcon);

        tempcard.setOnClickListener(this);
        humiditycard.setOnClickListener(this);
        mistercard.setOnClickListener(this);
        notificationbt.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.TempCard) {
            openFragment(new TemperatureFragment());
        } else if (v.getId() == R.id.HumidityCard) {
            openFragment(new HumidityFragment());
        } else if (v.getId() == R.id.MisterCard) {
            openFragment(new MisterFragment());
        } else if (v.getId() == R.id.notificationIcon) {
            // Start a new activity
            startActivity(new Intent(getActivity(), NotificationsActivity.class));
        }
    }

    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}

