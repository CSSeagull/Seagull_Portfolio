package com.example.project_demo.Services;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Enrollment;
import com.example.project_demo.Models.Student;
import com.example.project_demo.repositories.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    // Get all enrollments by student
    public List<Enrollment> getEnrollmentsByStudent(Student student) {
        return enrollmentRepository.findByStudent(student);
    }

    // Check if a student is already enrolled in a course
    public boolean isStudentEnrolledInCourse(Student student, Course course) {
        return enrollmentRepository.findByStudentAndCourse(student, course).isPresent();
    }

    // Enroll a student in a course
    public void enrollStudentInCourse(Student student, Course course) {
        if (!isStudentEnrolledInCourse(student, course)) {
            Enrollment enrollment = new Enrollment();
            enrollment.setStudent(student);
            enrollment.setCourse(course);
            enrollmentRepository.save(enrollment);
        }
    }

    // Drop a student from a course
    public void dropStudentFromCourse(Student student, Course course) {
        Optional<Enrollment> enrollmentOptional = enrollmentRepository.findByStudentAndCourse(student, course);
        enrollmentOptional.ifPresent(enrollmentRepository::delete);
    }

    public List<Enrollment> getEnrolledCoursesByStudentId(long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

}
