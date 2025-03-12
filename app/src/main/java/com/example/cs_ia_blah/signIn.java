package com.example.cs_ia_blah;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signIn extends AppCompatActivity{

    private EditText etEmail, etPassword, etName;
    private Button btnSignUp;
    private TextView tvGoToLogin;

    private FirebaseAuth mAuth;// Firebase Auth instance
    private FirebaseFirestore db; // Firebase Firestore instance
    private android.util.Log Log;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Find views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etSignUpEmail);
        etPassword = findViewById(R.id.etSignUpPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        // Sign Up button listener
        btnSignUp.setOnClickListener(v -> registerUser());

        // Navigate to Login page
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(signIn.this, LogIn.class));
            finish();
        });
    }

    private void registerUser() {
        // Get user input
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Basic validation
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be >= 6 characters");
            etPassword.requestFocus();
            return;
        }

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign-up success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Write user info (including name) to Firestore
                            createUserRecord(user, name);
                        }
                        Toast.makeText(signIn.this,
                                "Sign-up successful!",
                                Toast.LENGTH_SHORT).show();

                        // Move to main screen
                        startActivity(new Intent(signIn.this, MainActivity.class));
                        finish();
                    } else {
                        // If sign-up fails, display a message to the user
                        Toast.makeText(signIn.this,
                                "Sign-up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void createUserRecord(FirebaseUser firebaseUser, String name) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";

        Map<String, Object> userData = new HashMap<>();
        userData.put("displayName", name);
        userData.put("email", email);
        userData.put("createdAt", FieldValue.serverTimestamp());

        // placeholders for Strava tokens
        userData.put("stravaAccessToken", null);
        userData.put("stravaRefreshToken", null);
        userData.put("stravaTokenExpiresAt", null);

        db.collection("users")
                .document(uid)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User record created for " + uid);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error creating user record", e);
                });
    }

}
