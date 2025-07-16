package com.example.project_demo.Controllers;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Enrollment;
import com.example.project_demo.Models.Student;
import com.example.project_demo.Services.CourseService;
import com.example.project_demo.Services.EnrollmentService;
import com.example.project_demo.Services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class StudentController {

    private final CourseService courseService;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public StudentController(CourseService courseService, StudentService studentService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/students/{studentName}")
    public String studentDashboard(@PathVariable String studentName, Model model) {
        Optional<Student> optionalStudent = studentService.getStudentByName(studentName);
        if(optionalStudent.isPresent()){
            Student student = optionalStudent.get();
            List<Enrollment> enrollments = enrollmentService.getEnrolledCoursesByStudentId(student.getId());
            model.addAttribute("student", student);
            model.addAttribute("enrollments", enrollments);
            return "StudentHomePage";  // Return the view
        } else {
            model.addAttribute("errorMessage", "No student found with name " + studentName);
            return "error";
        }
    }




}