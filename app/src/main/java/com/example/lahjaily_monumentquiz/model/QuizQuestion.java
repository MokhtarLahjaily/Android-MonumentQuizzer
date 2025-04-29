package com.example.lahjaily_monumentquiz.model;

public class QuizQuestion {
    private String question;
    private String imageUrl;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;

    // Constructeur par défaut requis par Firebase
    public QuizQuestion() { }

    // Getters
    public String getQuestion()      { return question; }
    public String getImageUrl()      { return imageUrl; }
    public String getOptionA()       { return optionA; }
    public String getOptionB()       { return optionB; }
    public String getOptionC()       { return optionC; }
    public String getOptionD()       { return optionD; }
    public String getCorrectAnswer(){ return correctAnswer; }

    /**
     * Retourne le texte de l'option correspondant à la clé ("A","B","C","D").
     */
    public String getOptionFromKey(String key) {
        switch (key) {
            case "A": return optionA;
            case "B": return optionB;
            case "C": return optionC;
            case "D": return optionD;
            default:  return "";
        }
    }
}
