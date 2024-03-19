package com.example.mrhydro;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    TextView username, email;
    ImageView profilepic;
    FirebaseAuth mAuth;
    FirebaseUser user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        username = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        profilepic = view.findViewById(R.id.profilepic);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if (user != null) {
            email.setText(user.getEmail());

            // Retrieve username from Firebase Realtime Database
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid()).child("username");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String usernameFromDB = dataSnapshot.getValue(String.class);
                    if (usernameFromDB != null && !usernameFromDB.isEmpty()) {
                        username.setText(usernameFromDB);
                    } else {
                        // If username is not set in the database, use email as username
                        username.setText(user.getEmail());
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG, "Failed to read username value.", databaseError.toException());
                }
            });
        }

        return view;
    }


        }
