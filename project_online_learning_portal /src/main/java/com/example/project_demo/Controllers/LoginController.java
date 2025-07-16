package com.example.project_demo.Controllers;

import com.example.project_demo.Services.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final LoginService loginService;

    @Autowired
    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @RequestMapping ("/")
    public String showIndexPage() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "LoginPage";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            Model model) {

        return loginService.performLogin(email, password, model);
    }
}