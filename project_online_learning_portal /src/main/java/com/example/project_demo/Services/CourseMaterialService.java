package com.example.project_demo.Services;
import com.example.project_demo.Models.Course;
import com.example.project_demo.Models.CourseMaterial;
import com.example.project_demo.repositories.CourseMaterialRepository;
import com.example.project_demo.repositories.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class CourseMaterialService {

    private final CourseMaterialRepository repository;

    @Autowired
    public CourseMaterialService(CourseMaterialRepository repository, CourseRepository courseRepository) {
        this.repository = repository;
    }

    public void saveCourseMaterial(String title, Course course, MultipartFile file) throws IOException {
        CourseMaterial material = new CourseMaterial();
        material.setTitle(title);
        material.setCourse(course);
        material.setFileData(file.getBytes());

        repository.save(material);
    }

    public List<CourseMaterial> findByCourse(Course course) {
        return repository.findByCourse(course);
    }

    public CourseMaterial findByCourseAndTitle(Course course, String title) {
        return repository.findByCourseAndTitle(course, title);
    }

    @Transactional
    public void deleteByCourseAndTitle(Course course, String title) {
        repository.deleteByCourseAndTitle(course, title);
    }

    @Transactional
    public void updateTitle(Course course, String oldTitle, String newTitle) {
        CourseMaterial material = repository.findByCourseAndTitle(course, oldTitle);
        material.setTitle(newTitle);
        repository.save(material);
    }

    public boolean existsByCourseAndTitle(Course course, String title) {
        return repository.existsByCourseAndTitle(course, title);
    }
}