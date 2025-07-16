package com.example.project_demo.Services;

import com.example.project_demo.Models.Question;
import com.example.project_demo.Models.Quiz;
import com.example.project_demo.repositories.QuestionRepository;
import com.example.project_demo.repositories.QuizRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;


    @Autowired
    private QuizRepository quizRepository;

    public List<Question> findByQuizName(String quizName) {
        return questionRepository.findByQuiz_QuizName(quizName);
    }

    public void saveQuestion(String courseId, String quizName, Question question) {
        Quiz quiz = quizRepository.findByCourse_CourseIdAndQuizName(courseId, quizName);
        if (quiz != null) {
            question.setQuiz(quiz);
            questionRepository.save(question);
        } else {
            throw new RuntimeException("Quiz not found for the given course and quiz name");
        }
    }

    @Transactional
    public void deleteQuestion(Long questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Question not found");
        }
        questionRepository.deleteById(questionId);
    }

}


