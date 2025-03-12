//package com.example.cs_ia_blah;
//
//import com.google.firebase.Timestamp;
//import java.util.Date;
//
//public class Challenge {
//    private String challengeId
//    private String title;
//    private String description;
//    private double goalDistanceKm;
//    private double pledgeAmount;
//    private Timestamp startDate;  // Firestore Timestamp for start date
//    private Timestamp endDate; // Firestore Timestamp for end date
//    private boolean completed;
//
//    // Default constructor required for Firestore serialization
//    public Challenge() {}
//
//    // Full constructor
//    public Challenge(String title, String description, double goalDistanceKm, double pledgeAmount,
//                     Timestamp startDate, Timestamp endDate) {
//        this.title = title;
//        this.description = description;
//        this.goalDistanceKm = goalDistanceKm;
//        this.pledgeAmount = pledgeAmount;
//        this.startDate = startDate;
//        this.endDate = endDate;
//        this.completed = false;
//    }
//
//    public Challenge(String challengeId, String title, String description, Double goalDistance, Double pledgeAmount, Timestamp startDate, Timestamp endDate) {
//        this.challengeId = challengeId;
//        this.title = title;
//        this.description = description;
//        this.goalDistanceKm = goalDistance;
//        this.pledgeAmount = pledgeAmount;
//        this.startDate = startDate;
//        this.endDate = endDate;
//
//    }
//
//    // Getters and setters
//    public String getTitle() {
//        return title;
//    }
//    public void setTitle(String title) {
//        this.title = title;
//    }
//    public String getDescription() {
//        return description;
//    }
//    public void setDescription(String description) {
//        this.description = description;
//    }
//    public double getGoalDistanceKm() {
//        return goalDistanceKm;
//    }
//    public void setGoalDistanceKm(double goalDistanceKm) {
//        this.goalDistanceKm = goalDistanceKm;
//    }
//    public double getPledgeAmount() {
//        return pledgeAmount;
//    }
//    public void setPledgeAmount(double pledgeAmount) {
//        this.pledgeAmount = pledgeAmount;
//    }
//    public Timestamp getStartDate() {
//        return startDate;
//    }
//    public void setStartDate(Timestamp startDate) {
//        this.startDate = startDate;
//    }
//    public Timestamp getEndDate() {
//        return endDate;
//    }
//    public void setEndDate(Timestamp endDate) {
//        this.endDate = endDate;
//    }
//}
