package com.example.project_demo.Services;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Question;
import com.example.project_demo.Models.Quiz;
import com.example.project_demo.repositories.QuestionRepository;
import com.example.project_demo.repositories.QuizRepository;
import com.example.project_demo.repositories.QuizResultsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {
    private final CourseService courseService;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizResultsRepository quizResultsRepository;

    @Autowired
    public QuizService(CourseService courseService, QuizRepository quizRepository, QuestionRepository questionRepository, QuizResultsRepository quizResultsRepository) {
        this.courseService = courseService;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizResultsRepository = quizResultsRepository;
    }

    public List<Quiz> getQuizzesByCourse(Course course) {
        return quizRepository.findAllByCourse(course);
    }

    public Quiz createQuiz(Course course) {
        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setIsHidden(false);
        return quizRepository.save(quiz);
    }

    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }


    @Transactional
    public Quiz saveQuiz(Quiz quiz) {
        // Perform any needed logic before saving
        return quizRepository.save(quiz);
    }




    public boolean updateQuizTitleIfUnique(String courseId, String currentQuizName, String newTitle) {
        // Check if the new title already exists for the given course
        boolean titleInUse = quizRepository.existsByCourse_CourseIdAndQuizName(courseId, newTitle);

        if (!titleInUse) {
            // Proceed to find and update the quiz if the new title is not in use
            Quiz quiz = quizRepository.findByCourse_CourseIdAndQuizName(courseId, currentQuizName);
            if (quiz != null) {
                quiz.setQuizName(newTitle);
                quizRepository.save(quiz);
                return true; // Indicate success
            }
        }
        return false; // Indicate failure
    }


    public Quiz findQuizByCourseAndName(String courseId, String quizName) {
        return quizRepository.findByCourse_CourseIdAndQuizName(courseId, quizName);
    }

    public boolean existsByCourseAndName(String courseId, String quizName) {
        return quizRepository.existsByCourse_CourseIdAndQuizName(courseId, quizName);
    }

    public void createQuiz(String courseId, String quizName) {
        Course course = courseService.getCourseByCourseId(courseId);

        Quiz quiz = new Quiz();
        quiz.setCourse(course);
        quiz.setQuizName(quizName);
        quizRepository.save(quiz);
    }


    @Transactional
    public void deleteQuizAndQuestions(String quizName) {
        Optional<Quiz> quizOptional = quizRepository.findByQuizName(quizName);

        if (!quizOptional.isPresent()) {
            throw new IllegalArgumentException("Quiz not found");
        }
        Quiz quiz = quizOptional.get();
        Long quizId = quiz.getQuizId();
        quizResultsRepository.deleteByQuizQuizId(quizId);
        questionRepository.deleteByQuizQuizId(quizId);
        quizRepository.delete(quiz);
    }

    public void hideQuiz(String courseId, String quizName) {
        Quiz quiz = quizRepository.findByCourseCourseIdAndQuizName(courseId, quizName);
        if (quiz != null) {
            quiz.setHidden(true);
            quizRepository.save(quiz);
        }
    }

    public void unhideQuiz(String courseId, String quizName) {
        Quiz quiz = quizRepository.findByCourseCourseIdAndQuizName(courseId, quizName);
        if (quiz != null) {
            quiz.setHidden(false);
            quizRepository.save(quiz);
        }
    }

}


