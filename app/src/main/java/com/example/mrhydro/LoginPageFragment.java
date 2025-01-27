package com.example.mrhydro;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginPageFragment extends Fragment {

    EditText inputEmail, inputPassword;
    Button loginBT;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView registerText;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Navigate to MainActivity if user is already logged in
            Intent intent = new Intent(getContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_login_page, container, false);
        mAuth = FirebaseAuth.getInstance();
        inputEmail = view.findViewById(R.id.LoginUsernameInput);
        inputPassword = view.findViewById(R.id.LoginPasswordInput);
        loginBT = view.findViewById(R.id.LoginBtn);
        progressBar = view.findViewById(R.id.progressBar);
        registerText = view.findViewById(R.id.RegisterText);

        mAuth.useAppLanguage();

        loginBT.setOnClickListener(v -> {
            if (!validateEmail() || !validatePassword()) {
                return;
            }
            // Check user credentials and handle reCAPTCHA within this method
            checkUser();
        });

        registerText.setOnClickListener(view1 -> {
            Intent intent = new Intent(getContext(), RegisterPage.class);
            startActivity(intent);
        });

        return view;
    }

    public Boolean validateEmail() {
        String val = inputEmail.getText().toString();
        if (val.isEmpty()) {
            inputEmail.setError("Email cannot be empty");
            return false;
        } else {
            inputEmail.setError(null);
            return true;
        }
    }

    public Boolean validatePassword() {
        String val = inputPassword.getText().toString();
        if (val.isEmpty()) {
            inputPassword.setError("Password cannot be empty");
            return false;
        } else {
            inputPassword.setError(null);
            return true;
        }
    }

    private void checkUser() {
        String userEmail = inputEmail.getText().toString().trim();
        String userPassword = inputPassword.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(requireContext(), "Authentication failed. Invalid email or password.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
