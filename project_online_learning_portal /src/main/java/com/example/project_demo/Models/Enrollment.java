package com.example.project_demo.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    @ManyToOne
    private Student student;


    @JoinColumn(name = "course_id", referencedColumnName = "course_id")
    @ManyToOne
    private Course course;

    public void setStudent(Student student) {
        this.student = student;
    }
    public void setCourse(Course course) {
        this.course = course;
    }
    public Long getId() {
        return id;
    }
}
