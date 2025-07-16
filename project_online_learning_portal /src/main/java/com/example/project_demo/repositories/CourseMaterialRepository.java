package com.example.project_demo.repositories;

import com.example.project_demo.Models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.project_demo.Models.CourseMaterial;


public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    List<CourseMaterial> findByCourse(Course course);
    CourseMaterial findByCourseAndTitle(Course courseCode, String title);
    void deleteByCourseAndTitle(Course course, String title);
    boolean existsByCourseAndTitle(Course course, String title);
}