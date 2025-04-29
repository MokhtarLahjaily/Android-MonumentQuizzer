package com.example.lahjaily_monumentquiz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.lahjaily_monumentquiz.R;


public class ResultActivity extends BaseActivity {

    TextView resultText;

    View scoreBar;
    Button replayButton, quitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultText = findViewById(R.id.resultText);
        scoreBar = findViewById(R.id.scoreBar);
        replayButton = findViewById(R.id.replayButton);
        quitButton = findViewById(R.id.quitButton);

        // Récupérez les données passées via l'intent
        Intent intent = getIntent();
        int score = intent.getIntExtra("score", 0);
        int totalQuestions = intent.getIntExtra("totalQuestions", 0);

        // Calculez le pourcentage
        int percentage = (int) ((score / (float) totalQuestions) * 100);

        // Affichez le score
        resultText.setText("Votre score : " + score + " / " + totalQuestions);

        // Ajustez la largeur de la barre
        scoreBar.getLayoutParams().width = (int) (percentage * getResources().getDisplayMetrics().widthPixels / 100.0);
        scoreBar.requestLayout();

        // Bouton pour rejouer
        replayButton.setOnClickListener(v -> {
            Intent replayIntent = new Intent(ResultActivity.this, QuizActivity.class);
            startActivity(replayIntent);
            finish();
        });

        // Bouton pour quitter
        quitButton.setOnClickListener(v -> finish());
    }
}