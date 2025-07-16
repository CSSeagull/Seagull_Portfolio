package com.example.project_demo.repositories;

import com.example.project_demo.Models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByName(String email);
    boolean existsByEmail(String email);
    Optional<Student> findByEmailIgnoreCase(String email);
}
