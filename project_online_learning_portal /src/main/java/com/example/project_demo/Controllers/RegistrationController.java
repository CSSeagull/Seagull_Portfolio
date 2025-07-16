package com.example.project_demo.Controllers;

import com.example.project_demo.Models.Student;
import com.example.project_demo.Services.RegistrationService;
import com.example.project_demo.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private StudentRepository studentRepository;
    private final RegistrationService registrationService;

    public RegistrationController(StudentRepository studentRepository, RegistrationService registrationService) {
        this.studentRepository = studentRepository;
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, @RequestParam(value = "email", required = false) String email) {
        Student student = new Student();
        student.setEmail(email);
        model.addAttribute("student", student);
        return "RegistrationPage";
    }

    @PostMapping("/register")
    public String registerStudent(@RequestParam("name") String name,
                                  @RequestParam("email") String email,
                                  @RequestParam("password") String password,
                                  Model model) {

        if (registrationService.isEmailRegistered(email)) {
            model.addAttribute("registration_message", "email_is_already_in_use");
            System.out.println("email_is_already_in_use");
            return "RegistrationPage";
        }
        if (registrationService.isEamilStaffRegistered(email)) {
            model.addAttribute("registration_message_staff", "email_is_already_in_use_as_staff");
            System.out.println("email_is_already_in_use_as_staff");
            return "RegistrationPage";
        }

//        add alert to email_is_already_in_use_as_staff and email_is_already_in_use

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPassword(password);


        studentRepository.save(student);



        model.addAttribute("student", student);
        return "StudentHomePage";
    }

}