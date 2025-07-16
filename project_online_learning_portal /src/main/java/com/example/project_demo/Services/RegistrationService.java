package com.example.project_demo.Services;

import com.example.project_demo.repositories.StaffRepository;
import com.example.project_demo.repositories.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    @Autowired
    public RegistrationService(StudentRepository studentRepository, StaffRepository staffRepository) {

        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
    }

    public boolean isEmailRegistered(String email) {
        return studentRepository.existsByEmail(email);
    }
    public boolean isEamilStaffRegistered(String email) {
        return staffRepository.existsByEmail(email);
    }
}