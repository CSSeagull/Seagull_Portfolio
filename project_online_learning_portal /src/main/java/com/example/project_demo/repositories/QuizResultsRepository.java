package com.example.project_demo.repositories;

import com.example.project_demo.Models.Quiz;
import com.example.project_demo.Models.QuizResults;
import com.example.project_demo.Models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizResultsRepository extends JpaRepository<QuizResults, Long> {
    List<QuizResults> findByStudent(Student student);
    void deleteByQuizQuizId(Long quizId);
}
