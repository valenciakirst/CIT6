package com.example.mrhydro;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private Switch notificationSwitch;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        notificationSwitch = view.findViewById(R.id.notificationSwitch);

        // Set initial state based on the notification filter
        notificationSwitch.setChecked(isNotificationsMuted());

        // Set listener for switch changes
        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Update the notification mute state based on the switch
                updateNotificationMuteState(isChecked);
            }
        });

        return view;
    }

    private void updateNotificationMuteState(boolean isMuted) {
        if (isMuted) {
            muteNotifications();
        } else {
            unmuteNotifications();
        }
    }

    private void muteNotifications() {
        setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
    }

    private void unmuteNotifications() {
        setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
    }

    private void setInterruptionFilter(int filter) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    notificationManager.setInterruptionFilter(filter);
                    showToast("Notifications " + (filter == NotificationManager.INTERRUPTION_FILTER_NONE ? "muted" : "unmuted"));
                } else {
                    showToast("Notification policy access not granted. Redirecting to settings...");
                    redirectToNotificationSettings();
                }
            }
        }
    }

    private void redirectToNotificationSettings() {
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }


    private boolean isNotificationsMuted() {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            return notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_NONE;
        }

        return false;
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
