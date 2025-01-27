package com.example.mrhydro;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.work.WorkManager;
import androidx.work.PeriodicWorkRequest;
import androidx.work.ExistingPeriodicWorkPolicy;


import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class TemperatureAnalysisWorker extends Worker {
    public TemperatureAnalysisWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Perform background task here (e.g., temperature analysis)
        return Result.success();
    }

    public class TemperatureFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_temperature_charts, container, false);

            // Schedule the periodic work
            PeriodicWorkRequest analyzeTemperatureWork =
                    new PeriodicWorkRequest.Builder(TemperatureAnalysisWorker.class, 1, TimeUnit.DAYS)
                            .build();
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                    "TemperatureAnalysis",
                    ExistingPeriodicWorkPolicy.REPLACE,
                    analyzeTemperatureWork);

            return view;
        }
    }
}
