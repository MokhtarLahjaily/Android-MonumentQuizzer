package com.example.lahjaily_monumentquiz.model;

public class SessionData {
    private double latitude;
    private double longitude;
    private String audioFilePath;

    private int score;
    private String email;


    // Constructeur par défaut requis par Firebase
    public SessionData() { }

    public SessionData(double latitude, double longitude, String audioFilePath, int score, String email) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.audioFilePath = audioFilePath;
        this.score = score;
        this.email = email;
    }

    // Getters et setters
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getAudioFilePath() { return audioFilePath; }
    public void setAudioFilePath(String audioFilePath) { this.audioFilePath = audioFilePath; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


}
