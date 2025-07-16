package com.example.project_demo.repositories;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByCourseId(String courseId);
    List<Course> findByStaff_StaffId(Long staffId);
    List<Course> findByStaff(Staff staff);
}
