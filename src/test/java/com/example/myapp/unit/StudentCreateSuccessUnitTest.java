package com.example.myapp.unit;

import com.example.myapp.dto.StudentDto;
import com.example.myapp.model.Role;
import com.example.myapp.model.Student;
import com.example.myapp.model.User;
import com.example.myapp.repository.StudentRepository;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.service.StudentService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentCreateSuccessUnitTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Unit Test 4: createStudent encodes password, saves user and student, and returns DTO")
    void testCreateStudent_WhenValidDto_SuccessfullyCreatesAndReturnsDto() {
        StudentDto inputDto = new StudentDto();
        inputDto.setUsername("newstudent");
        inputDto.setPassword("plainSecret");
        inputDto.setFullName("Jane Doe");
        inputDto.setEmail("jane@university.edu");
        inputDto.setStudentId("ST67890");
        inputDto.setDepartment("Software Engineering");
        inputDto.setSemester("Semester 1");
        inputDto.setCgpa(3.90);

        when(userRepository.existsByUsername("newstudent")).thenReturn(false);
        when(studentRepository.existsByEmail("jane@university.edu")).thenReturn(false);
        when(studentRepository.existsByStudentId("ST67890")).thenReturn(false);
        when(passwordEncoder.encode("plainSecret")).thenReturn("hashedSecret");

        User savedUser = new User("newstudent", "hashedSecret", Role.ROLE_STUDENT);
        savedUser.setId(2L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        Student savedStudent = new Student(
                savedUser,
                inputDto.getFullName(),
                inputDto.getEmail(),
                inputDto.getStudentId(),
                inputDto.getDepartment(),
                inputDto.getSemester(),
                inputDto.getCgpa()
        );
        savedStudent.setId(2L);
        when(studentRepository.save(any(Student.class))).thenReturn(savedStudent);

        StudentDto created = studentService.createStudent(inputDto);

        assertNotNull(created);
        assertEquals(2L, created.getId());
        assertEquals("newstudent", created.getUsername());
        assertEquals("Jane Doe", created.getFullName());
        verify(userRepository, times(1)).save(any(User.class));
        verify(studentRepository, times(1)).save(any(Student.class));
    }
}
