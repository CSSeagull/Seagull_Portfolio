package com.example.project_demo.Services;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Quiz;
import com.example.project_demo.Models.QuizResults;
import com.example.project_demo.Models.Student;
import com.example.project_demo.repositories.QuizResultsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizResultsService {
    @Autowired
    private QuizResultsRepository quizResultsRepository;


    public void save(QuizResults quizResult) {
        quizResultsRepository.save(quizResult);
    }

    public List<QuizResults> getQuizResultsByStudent(Student student) {
        return quizResultsRepository.findByStudent(student);
    }
}
