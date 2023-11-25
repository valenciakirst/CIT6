package com.example.mrhydro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private CardView temperature;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        //temperature = (CardView) view.findViewById(R.id.temperature);

        return view;
    }
       //temperature.setOnClickListener(new View.OnClickListener() {
            public void onLinearLayoutClick(View view) {
                Intent i = new Intent(getActivity(), Temperature.class);
                startActivity(i);
            }
        }



