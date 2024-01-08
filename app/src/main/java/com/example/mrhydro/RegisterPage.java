package com.example.mrhydro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterPage extends AppCompatActivity {

    EditText inputEmail, inputPassword;
    Button registerBT;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView loginText;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register_page);
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.RegisterPasswordInput);
        registerBT = findViewById(R.id.RegisterBtn);
        progressBar = findViewById(R.id.progressBar);
        loginText = findViewById(R.id.ToLogin);

        loginText.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), LoginPage.class);
            startActivity(intent);
            finish();
        });

        registerBT.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(inputEmail.getText());
            password = String.valueOf(inputPassword.getText());

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(RegisterPage.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(RegisterPage.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Registration successful, set display name
                            setDisplayName("John Doe");
                            Toast.makeText(RegisterPage.this, "Registered Successfully",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(RegisterPage.this, "Registration Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

        });
    }

    private void setDisplayName(String displayName) {
        FirebaseUser user = mAuth.getCurrentUser();
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                // You can also set other profile information here like photo URL
                .build();

        if (user != null) {
            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Display name updated successfully
                            // Handle any further actions if needed
                        }
                    });
        }
    }
}