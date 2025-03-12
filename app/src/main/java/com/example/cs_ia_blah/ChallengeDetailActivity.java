package com.example.cs_ia_blah;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ChallengeDetailActivity extends AppCompatActivity {

    private TextView tvChallengeTitle, tvChallengeDescription, tvChallengeGoal, tvChallengePledge, tvChallengeTimeframe;
    private TextView tvUserDistance, tvProgressPercentage;
    private Button btnJoin, btnSyncStrava, btnBackToMain;
    private ProgressBar progressBarChallenge;
    private Toolbar toolbar;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String challengeId, currentUserId;
    private double challengeGoalDistance; // in kilometers

    // Timeframe dates retrieved from Firestore
    private Date challengeStartDate, challengeEndDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge_detail);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LogIn.class));
            finish();
            return;
        }
        currentUserId = currentUser.getUid();
        challengeId = getIntent().getStringExtra("CHALLENGE_ID");
        if (challengeId == null) {
            Toast.makeText(this, "No challenge selected.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvChallengeTitle = findViewById(R.id.tvChallengeTitle);
        tvChallengeDescription = findViewById(R.id.tvChallengeDescription);
        tvChallengeGoal = findViewById(R.id.tvChallengeGoal);
        tvChallengePledge = findViewById(R.id.tvChallengePledge);
        tvChallengeTimeframe = findViewById(R.id.tvChallengeTimeframe);
        tvUserDistance = findViewById(R.id.tvUserDistance);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);
        btnJoin = findViewById(R.id.btnJoin);
        btnSyncStrava = findViewById(R.id.btnSyncStrava);
        progressBarChallenge = findViewById(R.id.progressBarChallenge);
        btnBackToMain = findViewById(R.id.btnBackToMain);

        loadChallengeDetails();
        checkIfParticipant();

        btnJoin.setOnClickListener(v -> joinChallenge());
        btnSyncStrava.setOnClickListener(v -> syncStravaData());
        btnBackToMain.setOnClickListener(v -> {
             startActivity(new Intent(ChallengeDetailActivity.this, MainActivity.class));
             finish();
        });

    }



    // Loads challenge details from Firestore, including timeframe and goal
    private void loadChallengeDetails() {
        db.collection("challenges")
                .document(challengeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String title = documentSnapshot.getString("title");
                        String description = documentSnapshot.getString("description");
                        challengeGoalDistance = documentSnapshot.getDouble("goalDistanceKm") != null ?
                                documentSnapshot.getDouble("goalDistanceKm") : 0.0;
                        Double pledgeAmount = documentSnapshot.getDouble("pledgeAmount") != null ?
                                documentSnapshot.getDouble("pledgeAmount") : 0.0;

                        tvChallengeTitle.setText(title != null ? title : "No Title");
                        tvChallengeDescription.setText(description != null ? description : "No Description");
                        tvChallengeGoal.setText("Goal: " + challengeGoalDistance + " km");
                        tvChallengePledge.setText("Pledge: $" + pledgeAmount + " per km");

                        // Retrieve and display the challenge timeframe
                        Timestamp startTimestamp = documentSnapshot.getTimestamp("startDate");
                        Timestamp endTimestamp = documentSnapshot.getTimestamp("endDate");
                        if (startTimestamp != null && endTimestamp != null) {
                            challengeStartDate = startTimestamp.toDate();
                            challengeEndDate = endTimestamp.toDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                            String timeframe = "Timeframe: " + sdf.format(challengeStartDate) + " - " + sdf.format(challengeEndDate);
                            tvChallengeTimeframe.setText(timeframe);
                        } else {
                            tvChallengeTimeframe.setText("Timeframe not set");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ChallengeDetailActivity.this, "Error loading challenge details.", Toast.LENGTH_SHORT).show()
                );
    }

    // Checks if the current user is a participant and updates UI with challenge progress (based on the aggregated total)
    private void checkIfParticipant() {
        // For individual participant UI, we still update the participant record (without a completed flag)
        db.collection("challenges")
                .document(challengeId)
                .collection("participants")
                .document(currentUserId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.w("ChallengeDetail", "Listen failed.", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        btnJoin.setVisibility(View.GONE);
                        btnSyncStrava.setVisibility(View.VISIBLE);
                        // Show individual user's distance if needed
                        Double distance = snapshot.getDouble("distanceCovered");
                        double userDistance = distance != null ? distance : 0.0;
                        tvUserDistance.setText("Your Distance: " + userDistance + " km");
                    } else {
                        btnJoin.setVisibility(View.VISIBLE);
                        btnSyncStrava.setVisibility(View.GONE);
                        tvUserDistance.setText("Your Distance: 0 km");
                    }
                });

        // Listen for challenge-level total distance updates to update the progress bar
        db.collection("challenges")
                .document(challengeId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;
                    Double totalDistance = snapshot.getDouble("totalDistance");
                    double aggregatedDistance = totalDistance != null ? totalDistance : 0.0;
                    int progressPercent = challengeGoalDistance > 0 ? (int)((aggregatedDistance / challengeGoalDistance) * 100) : 0;
                    progressBarChallenge.setProgress(progressPercent);
                    tvProgressPercentage.setText("Progress: " + progressPercent + "%");
                });
    }

    // When joining a challenge, add a participant record (without a completed flag)
    private void joinChallenge() {
        Map<String, Object> participantData = new HashMap<>();
        participantData.put("distanceCovered", 0.0);
        participantData.put("joinedAt", FieldValue.serverTimestamp());
        participantData.put("userId", currentUserId);
        db.collection("challenges")
                .document(challengeId)
                .collection("participants")
                .document(currentUserId)
                .set(participantData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ChallengeDetailActivity.this, "You joined this challenge!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(ChallengeDetailActivity.this, "Error joining challenge", Toast.LENGTH_SHORT).show()
                );
    }

    // When syncing Strava data, update the individual participant record, then recalc the challenge's total distance
    private void syncStravaData() {
        getUserStravaToken(token -> {
            if (token == null) {
                runOnUiThread(() -> Toast.makeText(ChallengeDetailActivity.this,
                        "No Strava token found. Please connect your Strava account.", Toast.LENGTH_SHORT).show());
                return;
            }

            OkHttpClient client = new OkHttpClient();
            String url = "https://www.strava.com/api/v3/athlete/activities?per_page=30";
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + token)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChallengeDetailActivity.this,
                            "Failed to fetch Strava data", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        runOnUiThread(() -> Toast.makeText(ChallengeDetailActivity.this,
                                "Error: " + response.code(), Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String jsonResponse = response.body().string();
                    try {
                        JSONArray activities = new JSONArray(jsonResponse);
                        double totalDistanceKm = 0.0;

                        // Define a SimpleDateFormat to parse the ISO 8601 date returned by Strava.
                        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        // Ensure that challengeStartDate and challengeEndDate are set
                        if (challengeStartDate == null || challengeEndDate == null) {
                            runOnUiThread(() -> Toast.makeText(ChallengeDetailActivity.this,
                                    "Challenge timeframe not set.", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        // Iterate through each activity
                        for (int i = 0; i < activities.length(); i++) {
                            JSONObject activity = activities.getJSONObject(i);
                            if ("Run".equalsIgnoreCase(activity.getString("type"))) {
                                // Parse the activity's start date
                                String runStartStr = activity.getString("start_date");
                                Date runStartDate = isoFormat.parse(runStartStr);

                                // Only add the distance if the run's start date falls within the challenge timeframe.
                                if (runStartDate != null &&
                                        !runStartDate.before(challengeStartDate) &&
                                        !runStartDate.after(challengeEndDate)) {
                                    double distanceMeters = activity.getDouble("distance");
                                    totalDistanceKm += distanceMeters / 1000.0;
                                }
                            }
                        }
                        // Update Firestore with the new total distance for this participant and recalc aggregated distance.
                        updateParticipantDistance(totalDistanceKm);
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void updateParticipantDistance(double newDistance) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("distanceCovered", newDistance);

        db.collection("challenges")
                .document(challengeId)
                .collection("participants")
                .document(currentUserId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    // After updating the participant's own record, recalculate the aggregated total distance
                    recalcChallengeTotalDistance();
                })
                .addOnFailureListener(e ->
                        runOnUiThread(() -> Toast.makeText(ChallengeDetailActivity.this, "Error updating your distance", Toast.LENGTH_SHORT).show())
                );
    }


    // Recalculate the total distance of all participants for this challenge
    private void recalcChallengeTotalDistance() {
        db.collection("challenges")
                .document(challengeId)
                .collection("participants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalDistance = 0.0;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double dist = doc.getDouble("distanceCovered");
                        if (dist != null) {
                            totalDistance += dist;
                        }
                    }
                    // Update the challenge document with the total distance
                    double finalTotalDistance = totalDistance;
                    db.collection("challenges")
                            .document(challengeId)
                            .update("totalDistance", totalDistance)
                            .addOnSuccessListener(aVoid -> {
                                if (finalTotalDistance >= challengeGoalDistance) {
                                    db.collection("challenges")
                                            .document(challengeId)
                                            .update("completed", true);
                                }
                            });

                })
                .addOnFailureListener(e ->
                        runOnUiThread(() -> Toast.makeText(ChallengeDetailActivity.this, "Error recalculating total distance", Toast.LENGTH_SHORT).show())
                );
    }

    private void getUserStravaToken(final TokenCallback callback) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            callback.onTokenReceived(null);
            return;
        }
        String uid = currentUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Retrieve the token from the user document.
                        String token = documentSnapshot.getString("stravaAccessToken");
                        callback.onTokenReceived(token);
                    } else {
                        callback.onTokenReceived(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ChallengeDetail", "Error retrieving Strava token", e);
                    callback.onTokenReceived(null);
                });
    }

    public interface TokenCallback {
        void onTokenReceived(String token);
    }

}
