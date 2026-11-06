package com.example.myapp.repository;

import com.example.myapp.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUserUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByStudentId(String studentId);
    boolean existsByStudentIdAndIdNot(String studentId, Long id);
}
