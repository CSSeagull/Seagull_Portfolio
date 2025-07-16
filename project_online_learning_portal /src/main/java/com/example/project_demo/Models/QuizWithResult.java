package com.example.project_demo.Models;

public class QuizWithResult {
    private Quiz quiz;
    private QuizResults result;

    public QuizWithResult(Quiz quiz, QuizResults result) {
        this.quiz = quiz;
        this.result = result;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public QuizResults getResult() {
        return result;
    }

    public void setResult(QuizResults result) {
        this.result = result;
    }
}
