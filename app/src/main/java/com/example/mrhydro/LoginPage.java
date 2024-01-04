package com.example.mrhydro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {
    EditText inputEmail, inputPassword;
    Button loginBT;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView registerText;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        mAuth = FirebaseAuth.getInstance();
        inputEmail = findViewById(R.id.LoginUsernameInput);
        inputPassword = findViewById(R.id.LoginPasswordInput);
        loginBT  = findViewById(R.id.LoginBtn);
        progressBar = findViewById(R.id.progressBar);
        registerText= findViewById(R.id.RegisterText);
        registerText.setOnClickListener(view -> {

            Intent intent = new Intent(LoginPage.this, RegisterPage.class);
            startActivity(intent);
        });

        loginBT.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, password;
            email = String.valueOf(inputEmail.getText());
            password = String.valueOf(inputPassword.getText());

            if (TextUtils.isEmpty(email)){
                Toast.makeText(LoginPage.this, "Enter Email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)){
                Toast.makeText(LoginPage.this, "Enter Password", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            Log.d("LoginPage", "Login successful.");
                            Toast.makeText(LoginPage.this, "Login successful.",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("LoginPage", "Login failed.", task.getException());
                            Toast.makeText(LoginPage.this, "Login failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}