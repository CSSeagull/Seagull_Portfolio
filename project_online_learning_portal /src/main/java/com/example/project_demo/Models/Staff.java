package com.example.project_demo.Models;

import jakarta.persistence.*;

@Entity
@Table(name="staffs")
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JoinColumn(name = "staff_id")
    private Long staffId;

    @Column(name="staff_email", length = 30, nullable = false)
    private String email;

    @Column(name="staff_name", length = 30, nullable = false)
    private String name;

    @Column(name="staff_password", length = 30, nullable = false)
    private String password; //MATH

    public Staff(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
    }

    public Staff() {}

    public Long getStaffId() {
        return staffId;
    }

    public void setStaffId(Long staffId) {
        this.staffId = staffId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}