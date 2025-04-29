package com.example.lahjaily_monumentquiz.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaRecorder;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;


import com.example.lahjaily_monumentquiz.R;
import com.example.lahjaily_monumentquiz.model.QuizQuestion;
import com.example.lahjaily_monumentquiz.model.SessionData;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizActivity extends BaseActivity {

    private static final String TAG = "QuizActivity";
    private static final int PERMISSION_REQUEST_CODE = 100;

    private Animation slideInRight, slideOutLeft;
    private List<QuizQuestion> questionList = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private int quizRound = 0;

    private TextView questionText;
    private ImageView questionImage;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button nextButton;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private double latitude;
    private double longitude;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private MediaRecorder mediaRecorder;
    private String audioFilePath;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Initialize UI elements
        initializeViews();

        // Initialize animations
        slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right);
        slideOutLeft = AnimationUtils.loadAnimation(this, R.anim.slide_out_left);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Initialize location provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Check permissions based on user status
        if (currentUser == null) {
            signInAnonymously();
        } else {
            checkAndRequestPermissions();
        }

        // Set up button listeners
        nextButton.setOnClickListener(v -> checkAnswer());
    }

    private void initializeViews() {
        questionText = findViewById(R.id.questionText);
        questionImage = findViewById(R.id.questionImage);
        optionsGroup = findViewById(R.id.optionsGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.nextButton);
    }

    private void signInAnonymously() {
        mAuth.signInAnonymously()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInAnonymously:success");
                        currentUser = mAuth.getCurrentUser();
                        checkAndRequestPermissions();
                    } else {
                        Log.w(TAG, "signInAnonymously:failure", task.getException());
                        Toast.makeText(this,
                                "Authentication failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }

    private void checkAndRequestPermissions() {
        String[] permissions = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.RECORD_AUDIO
        };

        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            if (isNewUser()) {
                showPermissionsDialog(permissionsToRequest.toArray(new String[0]));
            } else {
                ActivityCompat.requestPermissions(this,
                        permissionsToRequest.toArray(new String[0]),
                        PERMISSION_REQUEST_CODE);
            }
        } else {
            // All permissions already granted
            onPermissionsGranted();
        }
    }

    private void showPermissionsDialog(String[] permissions) {
        new AlertDialog.Builder(this)
                .setTitle("Permissions nécessaires")
                .setMessage("En tant que nouvel utilisateur, vous devez accorder les permissions pour utiliser cette application.")
                .setPositiveButton("Continuer", (dialog, which) -> {
                    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
                })
                .setNegativeButton("Annuler", (dialog, which) -> {
                    Toast.makeText(this, "Permissions nécessaires pour continuer.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private boolean isNewUser() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userId = currentUser != null ? currentUser.getUid() : "unknown";
        boolean isNew = prefs.getBoolean("isNewUser_" + userId, true);

        if (isNew) {
            // Mark user as existing
            prefs.edit().putBoolean("isNewUser_" + userId, false).apply();
        }

        return isNew;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                onPermissionsGranted();
            } else {
                Toast.makeText(this, "Permissions refusées. Certaines fonctionnalités ne seront pas disponibles.", Toast.LENGTH_SHORT).show();
                // Continue with limited functionality or show dialog explaining why permissions are needed
                loadQuestionsFromRealtimeDB();
            }
        }
    }

    private void onPermissionsGranted() {
        startLocationUpdates();
        startAudioRecording();
        loadQuestionsFromRealtimeDB();
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            LocationRequest locationRequest = new LocationRequest.Builder(10000)
                    .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                    .setMinUpdateIntervalMillis(5000)
                    .build();

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    for (Location location : locationResult.getLocations()) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            Log.d(TAG, "Location updated: Lat: " + latitude + ", Lng: " + longitude);

                            // We have a location, can stop updates if desired
                            // stopLocationUpdates();
                            break;
                        }
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
        } catch (Exception e) {
            Log.e(TAG, "Error starting location updates", e);
        }
    }

    private void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    private void startAudioRecording() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            audioFilePath = getExternalFilesDir(null).getAbsolutePath() + "/audio_record.3gp";

            // For Android 10+ we should use MediaRecorder.Builder, but for backwards compatibility:
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setOutputFile(audioFilePath);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Audio recording started");
        } catch (IOException e) {
            Log.e(TAG, "Failed to start audio recording", e);
            Toast.makeText(this, "Erreur d'enregistrement audio", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in audio recording", e);
        }
    }

    private void stopAudioRecording() {
        if (mediaRecorder != null && isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                isRecording = false;
                Log.d(TAG, "Audio recording stopped. File saved at: " + audioFilePath);
            } catch (Exception e) {
                Log.e(TAG, "Error stopping audio recording", e);
            } finally {
                mediaRecorder = null;
            }
        }
    }

    private void loadQuestionsFromRealtimeDB() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("quizQuestions");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<QuizQuestion> allQuestions = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    QuizQuestion q = child.getValue(QuizQuestion.class);
                    if (q != null) allQuestions.add(q);
                }

                if (allQuestions.isEmpty()) {
                    showErrorAndFinish("Aucune question trouvée dans la base de données.");
                    return;
                }

                if (allQuestions.size() < 5) {
                    showErrorAndFinish("Pas assez de questions dans la base de données.");
                    return;
                }

                // Shuffle all questions
                Collections.shuffle(allQuestions);

                // Split into chunks of 5
                int start = quizRound * 5;
                int end = Math.min(start + 5, allQuestions.size());

                if (start >= allQuestions.size()) {
                    showErrorAndFinish("Pas assez de questions pour la session suivante.");
                    return;
                }

                questionList.clear();
                questionList.addAll(allQuestions.subList(start, end));

                currentIndex = 0;
                score = 0;
                showNextQuestion();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "loadQuestionsFromRealtimeDB:onCancelled", error.toException());
                showErrorAndFinish("Échec chargement : " + error.getMessage());
            }
        });
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(QuizActivity.this, message, Toast.LENGTH_LONG).show();
        new AlertDialog.Builder(this)
                .setTitle("Erreur")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showNextQuestion() {
        if (currentIndex < questionList.size()) {
            // Apply exit animation to the old question
            View contentView = findViewById(R.id.contentLayout);
            if (contentView != null) {
                contentView.startAnimation(slideOutLeft);
            }

            // Wait for animation to end before updating content
            slideOutLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    updateQuestionContent();

                    // Apply enter animation to the new question
                    if (contentView != null) {
                        contentView.startAnimation(slideInRight);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        } else {
            showResult();
        }
    }

    private void updateQuestionContent() {
        if (currentIndex < questionList.size()) {
            QuizQuestion current = questionList.get(currentIndex);

            // Update question text
            questionText.setText(current.getQuestion());

            // Update question image if available
            if (current.getImageUrl() != null && !current.getImageUrl().isEmpty()) {
                Glide.with(QuizActivity.this)
                        .load(current.getImageUrl())
                        .placeholder(R.drawable.placeholder_img)
                        .into(questionImage);
                questionImage.setVisibility(View.VISIBLE);
            } else {
                questionImage.setVisibility(View.GONE);
            }

            // Update options
            optionA.setText(current.getOptionA());
            optionB.setText(current.getOptionB());
            optionC.setText(current.getOptionC());
            optionD.setText(current.getOptionD());

            // Clear previous selection
            optionsGroup.clearCheck();
        }
    }

    private void checkAnswer() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Veuillez sélectionner une réponse.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the selected text
        RadioButton selected = findViewById(selectedId);
        String selectedText = selected.getText().toString().trim();

        // Prepare the correct answer according to the stored format
        QuizQuestion current = questionList.get(currentIndex);
        String rawCorrect = current.getCorrectAnswer().trim();
        String correctText;

        // If rawCorrect is a key A/B/C/D → map to the corresponding option
        if (rawCorrect.equalsIgnoreCase("A") ||
                rawCorrect.equalsIgnoreCase("B") ||
                rawCorrect.equalsIgnoreCase("C") ||
                rawCorrect.equalsIgnoreCase("D")) {
            correctText = current.getOptionFromKey(rawCorrect).trim();
        } else {
            // Otherwise rawCorrect directly contains the text
            correctText = rawCorrect;
        }

        // Case-insensitive and whitespace-insensitive comparison
        if (selectedText.equalsIgnoreCase(correctText)) {
            score++;
            Toast.makeText(this, "Correct !", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Incorrect !", Toast.LENGTH_SHORT).show();
        }

        currentIndex++;
        showNextQuestion();
    }

    private void saveSessionToDatabase(double latitude, double longitude, String audioFilePath) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("sessions");

        // Create a unique ID for each session
        String sessionId = databaseRef.push().getKey();

        String email = (currentUser != null && currentUser.getEmail() != null)
                ? currentUser.getEmail()
                : "Utilisateur anonyme";

        // Create a SessionData object
        SessionData sessionData = new SessionData(latitude, longitude, audioFilePath, score, email);

        if (sessionId != null) {
            // Save data to the database
            databaseRef.child(sessionId).setValue(sessionData)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Session enregistrée avec succès.");
                        } else {
                            Log.e(TAG, "Erreur lors de l'enregistrement : " +
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                        }
                    });
        }
    }

    private void showResult() {
        stopAudioRecording(); // Stop audio recording
        stopLocationUpdates(); // Stop location updates

        saveSessionToDatabase(latitude, longitude, audioFilePath); // Save session

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", questionList.size());
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAudioRecording();
        stopLocationUpdates();
    }
}