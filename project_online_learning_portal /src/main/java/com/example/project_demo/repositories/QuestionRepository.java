package com.example.project_demo.repositories;

import com.example.project_demo.Models.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuiz_QuizName(String quizName);

    void deleteByQuizQuizId(Long quizId);}
