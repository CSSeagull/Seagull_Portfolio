package com.example.project_demo.Controllers;

import com.example.project_demo.Models.Question;
import com.example.project_demo.Services.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @GetMapping("/{courseId}/quiz/{quizName}/addquestion")
    public String showAddQuestionForm(
            @PathVariable String courseId,
            @PathVariable String quizName,
            Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("courseId", courseId);
        model.addAttribute("quizName", quizName);
        return "addQuestion";
    }

    @PostMapping("/{courseId}/quiz/{quizName}/addquestion")
    public String addQuestion(
            @PathVariable String courseId,
            @PathVariable String quizName,
            @ModelAttribute Question question) {
        questionService.saveQuestion(courseId, quizName, question);
        return "redirect:/" + courseId + "/quiz/" + quizName + "/addquestion";
    }

    @DeleteMapping("/delete/{questionId}")
    public String deleteQuestion(@PathVariable Long questionId) {
        try {
            questionService.deleteQuestion(questionId);
            return "StaffQuizView"; // Return the view name after successful deletion
        } catch (IllegalArgumentException e) {
            return "redirect:/404"; // Redirect to a not-found page or handle as needed
        } catch (Exception e) {
            return "redirect:/500"; // Redirect to an error page or handle as needed
        }
    }

}