package com.example.project_demo.Services;

import com.example.project_demo.Models.Student;
import com.example.project_demo.repositories.StaffRepository;
import com.example.project_demo.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }


    public Optional<Student> getStudentByEmail(String email) {
        return studentRepository.findByEmailIgnoreCase(email);
    }


    public Optional<Student> getStudentByName(String name) {
        return studentRepository.findByName(name);
    }


    public Student getStudentById(Long studentId) {
        return studentRepository.findById(Math.toIntExact(studentId))
                .orElseThrow(() -> new RuntimeException("Student not found with ID: " + studentId));
    }

}