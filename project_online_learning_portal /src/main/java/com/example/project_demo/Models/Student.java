package com.example.project_demo.Models;

import jakarta.persistence.*;

@Entity
@Table(name="students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="student_id")
    private Long studentId;

    @Column(name="student_email", length = 30, nullable = false)
    private String email;

    @Column(name="student_name", length = 30, nullable = false)
    private String name;

    @Column(name="student_password", length = 30, nullable = false)
    private String password;


    public Student(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public Student() {}

    public Long getStudentId() {return this.studentId;}

    public String getEmail() {
        return this.email;
    }

    public String getName() {
        return this.name;
    }

    public String getPassword() {
        return this.password;
    }

    public void setStudentId(Long id) {this.studentId = id;}

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getId() {
        return this.studentId;
    }
}
