package com.example.project_demo.Services;

import com.example.project_demo.Models.Staff;
import com.example.project_demo.Models.Student;
import com.example.project_demo.repositories.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StaffService {

    private final StaffRepository staffRepository;

    @Autowired
    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }


    public Optional<Staff> getStaffByEmail(String email) {
        return staffRepository.findAll().stream()
                .filter(staff -> staff.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    public Optional<Staff> getStaffByName(String staffName) {
        return staffRepository.findByName(staffName);
    }

    public Staff findById(long staffId) {
        return staffRepository.findById((int) staffId)
                .orElseThrow(() -> new RuntimeException("Staff not found with id: " + staffId));
    }


}
