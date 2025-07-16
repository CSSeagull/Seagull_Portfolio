package com.example.project_demo.Services;

import com.example.project_demo.Models.Staff;
import com.example.project_demo.Models.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.Optional;

@Service
public class LoginService {

    private final StudentService studentService;
    private final StaffService staffService;

    @Autowired
    public LoginService(StudentService studentService, StaffService staffService) {
        this.studentService = studentService;
        this.staffService = staffService;
    }

    public String performLogin(String email, String password, Model model) {
        Optional<Student> optionalStudent = studentService.getStudentByEmail(email);
        if (optionalStudent.isPresent()) {
            return handleStudentLogin(optionalStudent.get(), password, model);
        }

        Optional<Staff> optionalStaff = staffService.getStaffByEmail(email);
        if (optionalStaff.isPresent()) {
            return handleStaffLogin(optionalStaff.get(), password, model);
        }

        model.addAttribute("error", "User not found");
        return "LoginPage";
    }

    private String handleStudentLogin(Student student, String password, Model model) {
        if (student.getPassword() != null && student.getPassword().equals(password)) {
            model.addAttribute("student", student);
            return "redirect:/students/" + student.getName();
        } else {
            model.addAttribute("error", "Invalid password");
            return "LoginPage";
        }
    }

    private String handleStaffLogin(Staff staff, String password, Model model) {
        // Assuming staff would also require password validation
        if (staff.getPassword() != null && staff.getPassword().equals(password)) {
            model.addAttribute("staff", staff);
            return "redirect:/staff/" + staff.getName();
        } else {
            model.addAttribute("error", "Invalid password");
            return "LoginPage";
        }
    }

}