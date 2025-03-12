package com.example.cs_ia_blah;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CreateChallengeActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etGoalDistance, etPledgeAmount;
    private TextView tvStartDate, tvEndDate;
    private Date selectedStartDate, selectedEndDate;
    private Button btnSaveChallenge;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_challenge);

        // 1. Initialize FirebaseAuth & Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Find UI elements
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etGoalDistance = findViewById(R.id.etGoalDistance);
        etPledgeAmount = findViewById(R.id.etPledgeAmount);
        btnSaveChallenge = findViewById(R.id.btnSaveChallenge);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);

        // Set OnClickListener for Start Date TextView
        tvStartDate.setOnClickListener(v -> showDatePickerDialog(true));

        // Similarly, set OnClickListener for End Date TextView
        tvEndDate.setOnClickListener(v -> showDatePickerDialog(false));

        // 3. Set onClickListener for the save button
        btnSaveChallenge.setOnClickListener(view -> saveChallenge());
    }

    private void showDatePickerDialog(final boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog with current date as default
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                CreateChallengeActivity.this,
                (DatePicker view, int yearPicked, int monthPicked, int dayOfMonth) -> {
                    calendar.set(yearPicked, monthPicked, dayOfMonth);
                    Date selectedDate = calendar.getTime();
                    // Format the date
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    String formattedDate = sdf.format(selectedDate);

                    if (isStartDate) {
                        tvStartDate.setText("Start Date: " + formattedDate);
                        selectedStartDate = selectedDate;
                    } else {
                        tvEndDate.setText("End Date: " + formattedDate);
                        selectedEndDate = selectedDate;
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }


    private void saveChallenge() {
        String challengeId = null;
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        double goalDistance = Double.parseDouble(etGoalDistance.getText().toString().trim());
        double pledgeAmount = Double.parseDouble(etPledgeAmount.getText().toString().trim());

        Timestamp startTimestamp = new Timestamp(selectedStartDate);
        Timestamp endTimestamp = new Timestamp(selectedEndDate);

        ChallengeAdapter.Challenge challenge = new ChallengeAdapter.Challenge(challengeId, title, description, goalDistance, pledgeAmount, startTimestamp, endTimestamp, false);

        // Write the challenge to Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("challenges")
                .add(challenge)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Challenge created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateChallengeActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating challenge: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}


