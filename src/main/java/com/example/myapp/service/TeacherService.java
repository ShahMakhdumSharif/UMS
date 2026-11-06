package com.example.myapp.service;

import com.example.myapp.dto.TeacherDto;
import com.example.myapp.exception.BadRequestException;
import com.example.myapp.exception.ResourceNotFoundException;
import com.example.myapp.model.Role;
import com.example.myapp.model.Teacher;
import com.example.myapp.model.User;
import com.example.myapp.repository.TeacherRepository;
import com.example.myapp.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherService(TeacherRepository teacherRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<TeacherDto> getAllTeachers() {
        return teacherRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeacherDto getTeacherById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return toDto(teacher);
    }

    @Transactional
    public TeacherDto createTeacher(TeacherDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
            throw new AccessDeniedException("Teachers cannot create other teacher profiles");
        }

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new BadRequestException("Username already exists: " + dto.getUsername());
        }
        if (teacherRepository.existsByEmail(dto.getEmail().trim())) {
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }

        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "password123";
        }

        User user = new User(
                dto.getUsername().trim(),
                passwordEncoder.encode(rawPassword),
                Role.ROLE_TEACHER
        );
        user = userRepository.save(user);

        Teacher teacher = new Teacher(
                user,
                dto.getFullName(),
                dto.getEmail(),
                dto.getDepartment(),
                dto.getDesignation(),
                dto.getPhone()
        );
        teacher = teacherRepository.save(teacher);

        return toDto(teacher);
    }

    @Transactional
    public TeacherDto updateTeacher(Long id, TeacherDto dto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
            String currentUsername = auth.getName();
            if (teacher.getUser() == null || !teacher.getUser().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("Teachers can only modify their own profile");
            }
        }

        if (teacherRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new BadRequestException("Email already in use by another teacher: " + dto.getEmail());
        }

        teacher.setFullName(dto.getFullName());
        teacher.setEmail(dto.getEmail());
        teacher.setDepartment(dto.getDepartment());
        teacher.setDesignation(dto.getDesignation());
        teacher.setPhone(dto.getPhone());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            teacher.getUser().setPassword(passwordEncoder.encode(dto.getPassword().trim()));
            userRepository.save(teacher.getUser());
        }

        Teacher updated = teacherRepository.save(teacher);
        return toDto(updated);
    }

    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"))) {
            String currentUsername = auth.getName();
            if (teacher.getUser() == null || !teacher.getUser().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("Teachers can only delete their own profile");
            }
        }
        teacherRepository.delete(teacher);
    }

    public TeacherDto toDto(Teacher teacher) {
        TeacherDto dto = new TeacherDto();
        dto.setId(teacher.getId());
        dto.setUsername(teacher.getUser() != null ? teacher.getUser().getUsername() : null);
        dto.setFullName(teacher.getFullName());
        dto.setEmail(teacher.getEmail());
        dto.setDepartment(teacher.getDepartment());
        dto.setDesignation(teacher.getDesignation());
        dto.setPhone(teacher.getPhone());
        dto.setRole(teacher.getUser() != null ? teacher.getUser().getRole().name() : Role.ROLE_TEACHER.name());
        return dto;
    }
}
