package com.example.mrhydro;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterPage extends AppCompatActivity {

    EditText inputEmail, inputPassword, inputUsername, inputName;
    Button registerBT;
    FirebaseAuth mAuth;
    DatabaseReference reference;

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
        setContentView(R.layout.activity_register_page);

        mAuth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference("users");

        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.RegisterPasswordInput);
        inputUsername = findViewById(R.id.Username);
        inputName = findViewById(R.id.Name);
        registerBT = findViewById(R.id.RegisterBtn);
        TextView toLogin = findViewById(R.id.ToLogin); // Find "login here" TextView

        registerBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = inputName.getText().toString().trim();
                String username = inputUsername.getText().toString().trim();
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check if any field is empty
                if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterPage.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call the registerUser method
                registerUser(name, username, email, password);
            }
        });


        // Set onClickListener for the "login here" text
        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterPage.this, LoginPageFragment.class);
                startActivity(intent);
                finish();
            }
        });
    }


    private void registerUser(String name, String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterPage.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                // Hash the password before storing it
                                String hashedPassword = PasswordUtils.hashPassword(password);

                                // Create a HashMap to store user data
                                HashMap<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("username", username);
                                userData.put("email", email);
                                userData.put("password", hashedPassword); // Store hashed password

                                // Store user data in the database under the user's unique ID
                                reference.child(uid).setValue(userData);

                                Toast.makeText(RegisterPage.this, "You have registered successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterPage.this, LoginPageFragment.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(RegisterPage.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
