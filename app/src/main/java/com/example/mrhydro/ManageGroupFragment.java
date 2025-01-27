package com.example.mrhydro;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public class ManageGroupFragment extends AppCompatDialogFragment {

    private static final int MAX_GROUPS = 5;
    private ArrayList<String> groupList;
    private ArrayList<String> groupKeys;
    private EditText groupNameInput;
    private Button addSaveButton;
    private Button deleteButton;
    private boolean isEditing = false;
    private int editPosition = -1;
    private DatabaseReference groupsRef;
    private ListView groupListView;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Ensure the dialog is cancelable by tapping outside
        setCancelable(true);

        View view = inflater.inflate(R.layout.fragment_edit_group, container, false);

        groupsRef = FirebaseDatabase.getInstance().getReference("groups");

        groupList = new ArrayList<>();
        groupKeys = new ArrayList<>();
        groupListView = view.findViewById(R.id.group_list_view);

        groupNameInput = view.findViewById(R.id.group_name_input);
        addSaveButton = view.findViewById(R.id.add_save_button);
        deleteButton = view.findViewById(R.id.delete_button);

        // Set up the ArrayAdapter for the ListView
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_activated_1, groupList);
        groupListView.setAdapter(adapter);

        loadGroupsFromFirebase();

        addSaveButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Enter a group name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isEditing) {
                groupList.set(editPosition, groupName);
                groupsRef.child(groupKeys.get(editPosition)).setValue(groupName);
                isEditing = false;
                addSaveButton.setText("Add Group");
            } else {
                if (groupList.size() >= MAX_GROUPS) {
                    Toast.makeText(getContext(), "Maximum of 5 groups reached", Toast.LENGTH_SHORT).show();
                    return;
                }
                String groupKey = groupsRef.push().getKey();
                if (groupKey != null) {
                    groupsRef.child(groupKey).setValue(groupName);
                    groupList.add(groupName);
                    groupKeys.add(groupKey);
                }
            }

            groupNameInput.setText("");
            adapter.notifyDataSetChanged();
        });

        deleteButton.setOnClickListener(v -> deleteGroup());

        groupListView.setOnItemClickListener((parent, view1, position, id) -> editGroup(position));

        return view;
    }


    private void loadGroupsFromFirebase() {
        groupsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                groupList.clear();
                groupKeys.clear();
                for (DataSnapshot groupSnapshot : dataSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getValue(String.class);
                    String groupKey = groupSnapshot.getKey();
                    if (groupName != null) {
                        groupList.add(groupName);
                        groupKeys.add(groupKey);
                        Log.d("FirebaseGroup", "Loaded group: " + groupName + " with key: " + groupKey);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load groups", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Error: " + databaseError.getMessage());
            }
        });
    }


    private void deleteGroup() {
        if (isEditing && editPosition != -1) {
            String groupKey = groupKeys.get(editPosition);
            groupsRef.child(groupKey).removeValue();
            groupList.remove(editPosition);
            groupKeys.remove(editPosition);
            isEditing = false;
            addSaveButton.setText("Add Group");
            adapter.notifyDataSetChanged();
            groupNameInput.setText("");
            Toast.makeText(getContext(), "Group deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Select a group to delete", Toast.LENGTH_SHORT).show();
        }
    }

    private void editGroup(int position) {
        String group = groupList.get(position);
        groupNameInput.setText(group);
        addSaveButton.setText("Save Changes");
        isEditing = true;
        editPosition = position;
    }
}
