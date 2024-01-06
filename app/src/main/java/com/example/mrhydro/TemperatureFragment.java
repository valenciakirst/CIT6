package com.example.mrhydro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.example.mrhydro.databinding.FragmentTemperatureBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TemperatureFragment extends Fragment implements View.OnClickListener{

    FragmentTemperatureBinding binding;
    DatabaseReference reference;
    LineChart celsiusChart;
    String[] daysOfMonth = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
    public TemperatureFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using the generated binding class
        binding = FragmentTemperatureBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        celsiusChart = view.findViewById(R.id.celciusChart);

        MyXAxisFormatter xAxisFormatter = new MyXAxisFormatter(daysOfMonth);
        XAxis xAxis = celsiusChart.getXAxis();
        xAxis.setValueFormatter(xAxisFormatter);

        MyYAxisFormatter yAxisFormatter = new MyYAxisFormatter();
        YAxis leftYAxis = celsiusChart.getAxisLeft();
        leftYAxis.setValueFormatter(yAxisFormatter);

        LineDataSet celsiusDataSet = new LineDataSet(dataValues(), "Temperature Data");
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(celsiusDataSet);
        LineData data = new LineData(dataSets);

// Set data to chart
        celsiusChart.setData(data);

// Refresh and redraw the chart
        celsiusChart.invalidate();

        ImageView backBT = view.findViewById(R.id.backButton);
        backBT.setOnClickListener(this);

        // Read data from Firebase
        readTemperatureData();

        return view;
    }

    private List<Entry> dataValues() {
        ArrayList<Entry> dataValue = new ArrayList<>();
        dataValue.add(new Entry(1f, 28.9f));
        dataValue.add(new Entry(2f, 26.9f));
        dataValue.add(new Entry(3f, 27.9f));
        dataValue.add(new Entry(4f, 30.0f));
        dataValue.add(new Entry(5f, 21.0f));
        dataValue.add(new Entry(6f, 27.9f));
        dataValue.add(new Entry(7f, 27.9f));
        dataValue.add(new Entry(8f, 27.9f));
        dataValue.add(new Entry(9f, 27.9f));
        dataValue.add(new Entry(10f, 27.9f));
        dataValue.add(new Entry(11f, 27.9f));
        dataValue.add(new Entry(12f, 27.9f));
        dataValue.add(new Entry(13f, 27.9f));
        dataValue.add(new Entry(14f, 27.9f));
        dataValue.add(new Entry(15f, 27.9f));
        dataValue.add(new Entry(16f, 27.9f));
        dataValue.add(new Entry(17f, 27.9f));
        dataValue.add(new Entry(18f, 27.9f));
        dataValue.add(new Entry(19f, 27.9f));
        dataValue.add(new Entry(20f, 26.7f));
        dataValue.add(new Entry(21f, 27.9f));
        dataValue.add(new Entry(22f, 27.9f));
        dataValue.add(new Entry(23f, 27.9f));
        dataValue.add(new Entry(24f, 27.9f));
        dataValue.add(new Entry(25f, 27.9f));
        dataValue.add(new Entry(26f, 27.9f));
        dataValue.add(new Entry(27f, 27.9f));
        dataValue.add(new Entry(28f, 27.9f));


        return dataValue;
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
