package com.example.myapp.service;

import com.example.myapp.dto.StudentDto;
import com.example.myapp.exception.BadRequestException;
import com.example.myapp.exception.ResourceNotFoundException;
import com.example.myapp.model.Role;
import com.example.myapp.model.Student;
import com.example.myapp.model.User;
import com.example.myapp.repository.StudentRepository;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentService(StudentRepository studentRepository,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<StudentDto> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentDto getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return toDto(student);
    }

    @Transactional
    public StudentDto createStudent(StudentDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            throw new AccessDeniedException("Students cannot create other student profiles");
        }

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new BadRequestException("Username already exists: " + dto.getUsername());
        }
        if (studentRepository.existsByEmail(dto.getEmail().trim())) {
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }
        if (studentRepository.existsByStudentId(dto.getStudentId().trim())) {
            throw new BadRequestException("Student ID already exists: " + dto.getStudentId());
        }

        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "password123";
        }

        User user = new User(
                dto.getUsername().trim(),
                passwordEncoder.encode(rawPassword),
                Role.ROLE_STUDENT
        );
        user = userRepository.save(user);

        Student student = new Student(
                user,
                dto.getFullName(),
                dto.getEmail(),
                dto.getStudentId(),
                dto.getDepartment(),
                dto.getSemester(),
                dto.getCgpa()
        );
        student = studentRepository.save(student);

        return toDto(student);
    }

    @Transactional
    public StudentDto updateStudent(Long id, StudentDto dto) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            String currentUsername = auth.getName();
            if (student.getUser() == null || !student.getUser().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("Students can only modify their own profile");
            }
        }

        if (studentRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new BadRequestException("Email already in use by another student: " + dto.getEmail());
        }
        if (studentRepository.existsByStudentIdAndIdNot(dto.getStudentId(), id)) {
            throw new BadRequestException("Student ID already in use by another student: " + dto.getStudentId());
        }

        student.setFullName(dto.getFullName());
        student.setEmail(dto.getEmail());
        student.setStudentId(dto.getStudentId());
        student.setDepartment(dto.getDepartment());
        student.setSemester(dto.getSemester());
        student.setCgpa(dto.getCgpa());

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            student.getUser().setPassword(passwordEncoder.encode(dto.getPassword().trim()));
            userRepository.save(student.getUser());
        }

        Student updated = studentRepository.save(student);
        return toDto(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"))) {
            String currentUsername = auth.getName();
            if (student.getUser() == null || !student.getUser().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("Students can only delete their own profile");
            }
        }
        studentRepository.delete(student);
    }

    public StudentDto toDto(Student student) {
        StudentDto dto = new StudentDto();
        dto.setId(student.getId());
        dto.setUsername(student.getUser() != null ? student.getUser().getUsername() : null);
        dto.setFullName(student.getFullName());
        dto.setEmail(student.getEmail());
        dto.setStudentId(student.getStudentId());
        dto.setDepartment(student.getDepartment());
        dto.setSemester(student.getSemester());
        dto.setCgpa(student.getCgpa());
        dto.setRole(student.getUser() != null ? student.getUser().getRole().name() : Role.ROLE_STUDENT.name());
        return dto;
    }
}
