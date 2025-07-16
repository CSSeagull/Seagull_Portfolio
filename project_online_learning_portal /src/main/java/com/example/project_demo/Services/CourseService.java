package com.example.project_demo.Services;

import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.Staff;
import com.example.project_demo.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseByCourseId(String courseId) {
        return courseRepository.findByCourseId(courseId);
    }

    public List<Course> getCoursesByStaffId(Long staffId) {
        return courseRepository.findByStaff_StaffId(staffId);
    }

    public List<Course> getCoursesByStaff(Staff staff) {
        return courseRepository.findByStaff(staff);
    }
}