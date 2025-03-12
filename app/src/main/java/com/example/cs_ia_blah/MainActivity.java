package com.example.cs_ia_blah;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Button btnLogout;
    private FloatingActionButton fabCreate;
    private RecyclerView challengesRecyclerView;
    private ChallengeAdapter adapter;
    private List<ChallengeAdapter.Challenge> challengeList = new ArrayList<ChallengeAdapter.Challenge>();
    private Button btnConnectStrava;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Firebase references
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            // Not logged in, redirect to LoginActivity
            startActivity(new Intent(MainActivity.this, LogIn.class));
            finish();
            return;
        }

        // 3. Find views
        btnLogout = findViewById(R.id.btnLogOut);
        fabCreate = findViewById(R.id.fabCreateChallenge);
        challengesRecyclerView = findViewById(R.id.challengesRecyclerView);
        btnConnectStrava = findViewById(R.id.btnConnectStrava);


        // 4. Setup RecyclerView
        challengesRecyclerView = findViewById(R.id.challengesRecyclerView);
        challengesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChallengeAdapter(this, challengeList);
        challengesRecyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadChallenges();

        // 6. Logout button logic
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(MainActivity.this, LogIn.class));
            finish();
        });

        // 7. Create Challenge FAB
        fabCreate.setOnClickListener(v -> {
            // Navigate to a CreateChallengeActivity or open a dialog
            startActivity(new Intent(MainActivity.this, CreateChallengeActivity.class));
            finish();
        });

        btnConnectStrava.setOnClickListener(v -> launchStravaAuth());
    }




    private void loadChallenges() {
        db.collection("challenges")
                .whereEqualTo("completed", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ChallengeAdapter.Challenge> activeChallenges = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String challengeId = doc.getId();
                        String title = doc.getString("title");
                        String description = doc.getString("description");
                        Double goalDistance = doc.getDouble("goalDistanceKm");
                        Double pledgeAmount = doc.getDouble("pledgeAmount");
                        Timestamp startTimestamp = doc.getTimestamp("startDate");
                        Timestamp endTimestamp = doc.getTimestamp("endDate");
                        Boolean completed = doc.getBoolean("completed");

                        if (title != null && description != null && goalDistance != null && pledgeAmount != null &&
                                startTimestamp != null && endTimestamp != null && (completed != null && !completed)) {
                            ChallengeAdapter.Challenge challenge = new ChallengeAdapter.Challenge(
                                    challengeId, title, description, goalDistance, pledgeAmount, startTimestamp, endTimestamp, completed);
                            activeChallenges.add(challenge);
                        }
                    }
                    challengeList.clear();
                    challengeList.addAll(activeChallenges);
                    adapter.setChallengeList(challengeList);
                })
                .addOnFailureListener(e -> {
                    Log.e("MainActivity", "Error loading challenges", e);
                });
    }




    private void launchStravaAuth() {
        // Build your Strava authorization URL
        String clientId = "146563";  // from my personal Strava Dev settings
        String redirectUri = "myapp://auth";   // scheme/host from my intent-filter
        String scope = "activity:read_all";

        // Building the full authorize URL
        String authUrl = "https://www.strava.com/oauth/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + redirectUri +
                "&scope=" + scope +
                "&approval_prompt=auto";

        // Create a Custom Tabs intent and open it
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder.build();

        // Launch the Custom Tab
        customTabsIntent.launchUrl(this, Uri.parse(authUrl));
    }
}


