package com.example.project_demo.repositories;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByCourse(Course course);

    Quiz findByCourse_CourseIdAndQuizName(String courseId, String quizName);

    boolean existsByCourse_CourseIdAndQuizName(String courseId, String quizName);

    Optional<Quiz> findByQuizName(String quizName);
    Quiz findByCourseCourseIdAndQuizName(String courseId, String quizName);

}
