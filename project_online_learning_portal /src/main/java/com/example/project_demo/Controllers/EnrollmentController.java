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
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class EnrollmentController {
    private final CourseService courseService;
    private final StudentService studentService;
    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(CourseService courseService, StudentService studentService, EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.studentService = studentService;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/enroll/{studentId}")
    public String viewAvailableCourses(@PathVariable Long studentId, Model model) {
        List<Course> courses = courseService.getCourses();
        model.addAttribute("courses", courses);
        model.addAttribute("studentId", studentId);  // Important to keep studentId for forms
        return "EnrollmentPage";
    }

    @GetMapping("/student/courses/{studentId}")
    public String viewEnrolledCourses(@PathVariable Long studentId, Model model) {
        Student student = studentService.getStudentById(studentId);
        List<Enrollment> enrollments = enrollmentService.getEnrollmentsByStudent(student);
        model.addAttribute("enrollments", enrollments);
        return "EnrolledPage";  // Mustache template to show enrolled courses
    }

    @PostMapping("/enroll/{studentId}/{courseId}")
    public String enrollStudent(@PathVariable Long studentId, @PathVariable String courseId, Model model) {
        Student student = studentService.getStudentById(studentId);
        Course course = courseService.getCourseByCourseId(courseId);
        enrollmentService.enrollStudentInCourse(student, course);

        model.addAttribute("message", "Successfully enrolled in course: " + course.getName());
        return "redirect:/student/courses/" + studentId;  // Redirect to view student's enrolled courses
    }

    @PostMapping("/drop/{studentId}/{courseId}")
    public String dropStudent(@PathVariable Long studentId, @PathVariable String courseId, Model model) {
        Student student = studentService.getStudentById(studentId);
        Course course = courseService.getCourseByCourseId(courseId);
        enrollmentService.dropStudentFromCourse(student, course);

        model.addAttribute("message", "Successfully dropped from course: " + course.getName());
        return "redirect:/student/courses/" + studentId;  // Redirect to view student's enrolled courses
    }
}
