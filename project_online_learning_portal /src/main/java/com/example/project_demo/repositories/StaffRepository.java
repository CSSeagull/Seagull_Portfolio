package com.example.project_demo.repositories;

import com.example.project_demo.Models.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByName(String name);

    boolean existsByEmail(String email);

}
