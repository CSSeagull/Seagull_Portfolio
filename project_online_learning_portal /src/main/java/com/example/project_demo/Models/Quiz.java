package com.example.project_demo.Models;

import jakarta.persistence.*;

@Entity
@Table(name="quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course; // FK to course

    @Column(name = "quiz_name")
    private String quizName;

    @Column(name = "is_hidden")
    private Boolean isHidden = true;


    public void setCourse(Course course) {
        this.course = course;
    }

    public void setIsHidden(boolean b) {
        this.isHidden = b;
    }

    public String getQuizName() {
        return quizName;
    }

    public void setQuizName(String quizName) {
        this.quizName = quizName;
    }

    public Long getQuizId() {
        return quizId;
    }

    public Long getId() {
        return quizId;
    }

    public Boolean getHidden() {
        return isHidden;
    }

    public void setHidden(Boolean hidden) {
        isHidden = hidden;
    }
}
