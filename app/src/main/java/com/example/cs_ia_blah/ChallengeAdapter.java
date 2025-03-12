package com.example.cs_ia_blah;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChallengeAdapter extends RecyclerView.Adapter<ChallengeAdapter.ChallengeViewHolder> {

    // The Challenge model representing each challenge item
    public static class Challenge {
        private String challengeId;
        private String title;
        private String description;
        private double goalDistanceKm;
        private double pledgeAmount;
        private Date startDate;
        private Date endDate;
        private boolean completed;

        // Default constructor (needed for Firestore deserialization, if used)
        public Challenge() { }

        // Full constructor that accepts Timestamps for start and end dates and converts them to Date objects.
        public Challenge(String challengeId, String title, String description, double goalDistanceKm, double pledgeAmount,
                         Timestamp startTimestamp, Timestamp endTimestamp, boolean completed) {
            this.challengeId = challengeId;
            this.title = title;
            this.description = description;
            this.goalDistanceKm = goalDistanceKm;
            this.pledgeAmount = pledgeAmount;
            this.startDate = (startTimestamp != null) ? startTimestamp.toDate() : null;
            this.endDate = (endTimestamp != null) ? endTimestamp.toDate() : null;
            this.completed = completed;
        }

        // Getters
        public String getChallengeId() {
            return challengeId;
        }
        public String getTitle() {
            return title;
        }
        public String getDescription() {
            return description;
        }
        public double getGoalDistanceKm() {
            return goalDistanceKm;
        }
        public double getPledgeAmount() {
            return pledgeAmount;
        }
        public Date getStartDate() {
            return startDate;
        }
        public Date getEndDate() {
            return endDate;
        }
        public boolean isCompleted() {
            return completed;
        }
    }

    private Context context;
    private List<Challenge> challengeList;

    public ChallengeAdapter(Context context, List<Challenge> challengeList) {
        this.context = context;
        this.challengeList = challengeList;
    }

    @NonNull
    @Override
    public ChallengeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_challenge, parent, false);
        return new ChallengeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChallengeViewHolder holder, int position) {
        Challenge challenge = challengeList.get(position);
        holder.tvTitle.setText(challenge.getTitle());
        holder.tvDescription.setText(challenge.getDescription());
        holder.tvGoal.setText("Goal: " + challenge.getGoalDistanceKm() + " km");
        holder.tvPledge.setText("Pledge: $" + challenge.getPledgeAmount() + " per km");

        // Format the start and end dates for display
        Date start = challenge.getStartDate();
        Date end = challenge.getEndDate();
        String timeframe;
        if (start != null && end != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            timeframe = "Timeframe: " + sdf.format(start) + " - " + sdf.format(end);
        } else {
            timeframe = "Timeframe not set";
        }
        holder.tvTimeframe.setText(timeframe);

        // Set a click listener to open the challenge detail activity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChallengeDetailActivity.class);
            intent.putExtra("CHALLENGE_ID", challenge.getChallengeId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return challengeList.size();
    }

    public void setChallengeList(List<Challenge> challengeList) {
        this.challengeList = challengeList;
        notifyDataSetChanged();
    }

    // ViewHolder for challenge items
    public static class ChallengeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvGoal, tvPledge, tvTimeframe;

        public ChallengeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvChallengeTitle);
            tvDescription = itemView.findViewById(R.id.tvChallengeDescription);
            tvGoal = itemView.findViewById(R.id.tvChallengeGoal);
            tvPledge = itemView.findViewById(R.id.tvChallengePledge);
            tvTimeframe = itemView.findViewById(R.id.tvChallengeTimeframe);
        }
    }
}
