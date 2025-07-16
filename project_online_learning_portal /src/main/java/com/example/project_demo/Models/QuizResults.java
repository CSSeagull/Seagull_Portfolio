package com.example.project_demo.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "quiz_results")
public class QuizResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_result_id")
    private Long quizResultId;

    @JoinColumn(name="quiz_id")
    @ManyToOne
    private Quiz quiz;

    @JoinColumn(name = "student_id")
    @ManyToOne
    private Student student;

    @Column(name="result")
    private int result;

    public QuizResults(Quiz quiz, Student student, int result) {
        this.quiz = quiz;
        this.student = student;
        this.result = result;
    }
    public QuizResults() {}

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
