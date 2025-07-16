package com.example.project_demo.Models;

import jakarta.persistence.*;

@Entity
@Table(name="courses")
public class Course {
    @Id
    @Column(name="course_id", length=10, nullable = false)
    private String courseId;

    @Column(name="course_name", length = 30, nullable = false)
    private String name;

    @JoinColumn(name = "staff_id")
    @ManyToOne
    private Staff staff;

    public Course() {}
    public Course(String courseId, String name, Staff staff) {
        this.courseId = courseId;
        this.name = name;
        this.staff = staff;
    }


    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Staff getStaffId() {
        return staff;
    }

    public void setStaffId(Staff staff) {
        this.staff = staff;
    }
}