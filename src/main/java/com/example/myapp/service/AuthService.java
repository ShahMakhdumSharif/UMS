package com.example.myapp.service;

import com.example.myapp.dto.AuthRequest;
import com.example.myapp.dto.AuthResponse;
import com.example.myapp.exception.ResourceNotFoundException;
import com.example.myapp.model.Admin;
import com.example.myapp.model.Student;
import com.example.myapp.model.Teacher;
import com.example.myapp.model.User;
import com.example.myapp.repository.AdminRepository;
import com.example.myapp.repository.StudentRepository;
import com.example.myapp.repository.TeacherRepository;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserRepository userRepository,
                       AdminRepository adminRepository,
                       TeacherRepository teacherRepository,
                       StudentRepository studentRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        String fullName = user.getUsername();
        String email = "";

        switch (user.getRole()) {
            case ROLE_ADMIN -> {
                Optional<Admin> admin = adminRepository.findByUserUsername(user.getUsername());
                if (admin.isPresent()) {
                    fullName = admin.get().getFullName();
                    email = admin.get().getEmail();
                }
            }
            case ROLE_TEACHER -> {
                Optional<Teacher> teacher = teacherRepository.findByUserUsername(user.getUsername());
                if (teacher.isPresent()) {
                    fullName = teacher.get().getFullName();
                    email = teacher.get().getEmail();
                }
            }
            case ROLE_STUDENT -> {
                Optional<Student> student = studentRepository.findByUserUsername(user.getUsername());
                if (student.isPresent()) {
                    fullName = student.get().getFullName();
                    email = student.get().getEmail();
                }
            }
        }

        return new AuthResponse(jwt, user.getUsername(), user.getRole().name(), fullName, email);
    }

    public AuthResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("No authenticated user");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        String fullName = user.getUsername();
        String email = "";

        switch (user.getRole()) {
            case ROLE_ADMIN -> {
                Optional<Admin> admin = adminRepository.findByUserUsername(user.getUsername());
                if (admin.isPresent()) {
                    fullName = admin.get().getFullName();
                    email = admin.get().getEmail();
                }
            }
            case ROLE_TEACHER -> {
                Optional<Teacher> teacher = teacherRepository.findByUserUsername(user.getUsername());
                if (teacher.isPresent()) {
                    fullName = teacher.get().getFullName();
                    email = teacher.get().getEmail();
                }
            }
            case ROLE_STUDENT -> {
                Optional<Student> student = studentRepository.findByUserUsername(user.getUsername());
                if (student.isPresent()) {
                    fullName = student.get().getFullName();
                    email = student.get().getEmail();
                }
            }
        }

        return new AuthResponse(null, user.getUsername(), user.getRole().name(), fullName, email);
    }
}
