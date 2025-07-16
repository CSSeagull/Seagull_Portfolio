package com.example.project_demo.repositories;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Enrollment;
import com.example.project_demo.Models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(Student student);
    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);
    List<Enrollment> findByStudentId(long studentId);

}
