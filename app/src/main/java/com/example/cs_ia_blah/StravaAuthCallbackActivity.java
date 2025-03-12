package com.example.cs_ia_blah;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class StravaAuthCallbackActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(StravaAuthCallbackActivity.this, "successful strava callback", Toast.LENGTH_SHORT).show();


        Uri uri = getIntent().getData(); // e.g. myapp://auth?code=XYZ
        if (uri != null && uri.toString().startsWith("myapp://auth")) {
            String code = uri.getQueryParameter("code");
            if (code != null) {
                // Exchange 'code' for an access/refresh token
                exchangeCodeForToken(code);
            }
        }

        // Close this activity or navigate the user onward
        startActivity(new Intent(StravaAuthCallbackActivity.this, MainActivity.class));
        finish();
    }

    private void exchangeCodeForToken(String code) {
        String clientId = "146563";
        String clientSecret = "5c746a6e787a12e9707d2ffe7ffb1b18d8235fdb";
        String tokenUrl = "https://www.strava.com/oauth/token";

        // Create the request body
        RequestBody requestBody = new FormBody.Builder()
                .add("client_id", clientId)
                .add("client_secret", clientSecret)
                .add("code", code)
                .add("grant_type", "authorization_code")
                .build();

        // Build the request
        Request request = new Request.Builder()
                .url(tokenUrl)
                .post(requestBody)
                .build();

        // Make an async call
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // handling errors
                Log.e("StravaToken", "Token exchange failed", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("StravaToken", "Unexpected response code: " + response.code());
                    return;
                }
                // Parse the JSON
                String responseBody = response.body().string();
                // e.g., use JSONObject or GSON to extract "access_token", "refresh_token", "expires_in", etc.

                // Example with JSONObject
                try {
                    JSONObject json = new JSONObject(responseBody);
                    String accessToken = json.optString("access_token");
                    String refreshToken = json.optString("refresh_token");
                    int expiresIn = json.optInt("expires_in", 0);
                    long expiresAt = json.optLong("expires_at", 0);
                    storeStravaTokens(accessToken, refreshToken, expiresAt);


                    // Store them in your app (e.g., Firestore, SharedPreferences)
                    // Then you can fetch user activities using "accessToken"
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void storeStravaTokens(String accessToken, String refreshToken, long expiresAt) {
        // Here, update your Firestore user document or secure storage with the tokens.
        // Example:
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String, Object> tokenData = new HashMap<>();
            tokenData.put("stravaAccessToken", accessToken);
            tokenData.put("stravaRefreshToken", refreshToken);
            tokenData.put("stravaTokenExpiresAt", expiresAt);

            db.collection("users").document(uid)
                    .set(tokenData, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("StravaToken", "Tokens stored successfully"))
                    .addOnFailureListener(e -> Log.e("StravaToken", "Error storing tokens", e));
        }
    }

}


