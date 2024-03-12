package com.example.mrhydro;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationHistory extends Fragment {

    private View view; // Declare view here

    private List<NotificationItem> notificationHistoryList;
    private NotificationAdapter historyAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_notification_history, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        notificationHistoryList = new ArrayList<>();
        historyAdapter = new NotificationAdapter(notificationHistoryList);
        RecyclerView historyRecyclerView = view.findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        historyRecyclerView.setAdapter(historyAdapter);

        // Load notification history data here (you can fetch it from Firebase)
        loadNotificationHistory();
    }

    private void loadNotificationHistory() {
        // Read notifications from Firebase for history
        historyAdapter.readNotificationsFromFirebase();
    }
}
