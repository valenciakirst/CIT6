package com.example.mrhydro;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.mrhydro.databinding.FragmentMisterBinding;
import com.example.mrhydro.databinding.FragmentTemperatureBinding;


public class MisterFragment extends Fragment implements View.OnClickListener {

    FragmentMisterBinding binding;
    public MisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMisterBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        MainActivity mainActivity = (MainActivity) requireActivity();
        mainActivity.hideToolbar();


        return view;

    }
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backButton) {
            openFragment(new HomeFragment());
        }
    }

    private void openFragment(HomeFragment homeFragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, homeFragment);  // Use homeFragment instead of fragment
        transaction.addToBackStack(null);
        transaction.commit();
    }

}