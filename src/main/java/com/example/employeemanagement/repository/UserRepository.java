package com.example.employeemanagement.repository;

import java.util.Optional;

import com.example.employeemanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
