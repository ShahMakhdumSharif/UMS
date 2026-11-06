package com.example.myapp.repository;

import com.example.myapp.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByEmail(String email);
    Optional<Teacher> findByUserUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
}
