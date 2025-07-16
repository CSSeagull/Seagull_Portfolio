package com.example.project_demo.Controllers;

import com.example.project_demo.Models.*;
import com.example.project_demo.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class QuizController {

    private final CourseService courseService;
    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizResultsService quizResultsService;
    private final StudentService studentService;

    @Autowired
    public QuizController(CourseService courseService, QuizService quizService, QuestionService questionService, QuizResultsService quizResultsService, StudentService studentService) {
        this.courseService = courseService;
        this.quizService = quizService;
        this.questionService = questionService;
        this.quizResultsService = quizResultsService;
        this.studentService = studentService;
    }
    // Staff view
    @GetMapping("/staff/{courseId}/quizzes")
    public String showQuizPage(@PathVariable String courseId, Model model) {
        Course course = courseService.getCourseByCourseId(courseId);
        List<Quiz> quizzes = quizService.getQuizzesByCourse(course);

        model.addAttribute("quizzes", quizzes);
        model.addAttribute("course", course);
        return "QuizPageForStaff";
    }

    @GetMapping("/staff/{courseId}/{quizName}")
    public String showQuizQuestions(@PathVariable String courseId,
                                    @PathVariable String quizName,
                                    Model model) {
        List<Question> questions = questionService.findByQuizName(quizName);
        model.addAttribute("questions", questions);
        model.addAttribute("quizName", quizName);
        return "StaffQuizView";
    }



    @GetMapping("/staff/{courseId}/{quizName}/editquiztitle")
    public String showEditQuizTitleForm(
            @PathVariable String courseId,
            @PathVariable String quizName,
            Model model) {
        model.addAttribute("courseId", courseId);
        model.addAttribute("quizName", quizName);
        return "EditQuizTitle";
    }

    @PutMapping("/staff/{courseId}/{quizName}/editquiztitle")
    public String editQuizTitle(@PathVariable String courseId,
                                @PathVariable String quizName,
                                @RequestParam("newTitle") String newTitle,
                                RedirectAttributes redirectAttributes) {

        boolean isUpdated = quizService.updateQuizTitleIfUnique(courseId, quizName, newTitle);

        if (isUpdated) {
            return "redirect:/staff/" + courseId + "/quizzes";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Quiz title already in use or quiz not found.");
            return "EditQuizTitle";
        }
    }


// don't touch !!!!



    //
    @GetMapping("/staff/{courseId}/quizz/new")
    public String showQuizForm(@PathVariable String courseId, Model model) {
        Course course = courseService.getCourseByCourseId(courseId);
        model.addAttribute("courseId", courseId);
        return "CreateNewQuiz";
    }

//    @GetMapping("/{courseId}/{quizName}")
//    public String showQuizView(@PathVariable String CourseId, @PathVariable String quizName, Model model) {
//        Course course = courseService.getCourseByCourseId(CourseId);
//        model.addAttribute("courseId", CourseId);
//        model.addAttribute("quizName", quizName);
//        model.addAttribute("course", course);
//        return "StaffQuizView";
//    }


//
//
    @PostMapping("/staff/{courseId}/quizz/new")
    public String createQuiz(@PathVariable String courseId, @RequestParam String quizName, Model model) {

        boolean quizExists = quizService.existsByCourseAndName(courseId, quizName);

        if (quizExists) {
            return "CreateNewQuiz";
        } else{
            quizService.createQuiz(courseId, quizName);
            return "CreateNewQuiz";
        }

//        add alert - if possible
    }

    @DeleteMapping("/{courseId}/{quizName}/delete")
    public void deleteQuiz(@PathVariable String courseId, @PathVariable String quizName) {
        quizService.deleteQuizAndQuestions(quizName);
    }

    // Student view
    @GetMapping("/{studentName}/{courseId}/quizzes")
    public String showQuizzesPageStudent(@PathVariable String courseId, @PathVariable String studentName, Model model) {
        Course course = courseService.getCourseByCourseId(courseId);
        List<Quiz> quizzes = quizService.getQuizzesByCourse(course);
        Optional<Student> student = studentService.getStudentByName(studentName);

        if (student.isPresent()) {
            Student stud = student.get();
            List<QuizResults> quizResults = quizResultsService.getQuizResultsByStudent(stud);
            Map<Quiz, QuizResults> quizResultsMap = new HashMap<>(); // {quizId: quizResult} map, using "object" to be able to set null value

            for (QuizResults quizResult : quizResults) {
                quizResultsMap.put(quizResult.getQuiz(), quizResult);
            }
            List<QuizWithResult> quizWithResults = new ArrayList<>();

            for (Quiz quiz : quizzes) {
                if (!quiz.getHidden()) {
                    quizWithResults.add(new QuizWithResult(quiz, quizResultsMap.getOrDefault(quiz, null)));
                }
            }
            model.addAttribute("quizWithResults", quizWithResults);
            model.addAttribute("student", stud);
            model.addAttribute("quizResults", quizResultsMap);

        }
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("course", course);
        return "QuizPageForStudent";
    }
    @GetMapping("/{studentName}/{courseId}/quiz/{quizName}")
    public String showQuizStudent(@PathVariable String courseId, @PathVariable String quizName, @PathVariable String studentName, Model model) {
        Course course = courseService.getCourseByCourseId(courseId);
        List <Question> questions = questionService.findByQuizName(quizName);
        Optional<Student> student = studentService.getStudentByName(studentName);
        if (student.isPresent()) {
            Student stud = student.get();
            model.addAttribute("student", stud);
        }
        model.addAttribute("quiz", quizName);
        model.addAttribute("questions", questions);
        model.addAttribute("course", course);
        return "StudentQuizView";
    }

    @PostMapping("/{studentName}/{courseId}/quiz/{quizName}/submit")
    public String submitQuiz(@PathVariable String courseId, @PathVariable String quizName, @RequestBody Map<String, Integer> requestBody, @PathVariable String studentName, Model model) {
        Course course = courseService.getCourseByCourseId(courseId);
        Quiz quiz = quizService.findQuizByCourseAndName(courseId, quizName);
        int correctAnswers = requestBody.get("correctAnswers");
        Optional<Student> student = studentService.getStudentByName(studentName);
        if (student.isPresent()) {
            Student stud = student.get();
            QuizResults quizResult = new QuizResults(quiz, stud, correctAnswers);
            quizResultsService.save(quizResult);
        }
        String redirectPage = "redirect:/"+ studentName +"/{courseId}/quizzes";
        return redirectPage;
    }

    @PutMapping("/staff/{courseId}/{quizName}/hide")
    public void hideQuiz(@PathVariable String courseId, @PathVariable String quizName) {
        quizService.hideQuiz(courseId, quizName);
    }

    @PutMapping("/staff/{courseId}/{quizName}/unhide")
    public void unhideQuiz(@PathVariable String courseId, @PathVariable String quizName) {
        quizService.unhideQuiz(courseId, quizName);
    }
}

