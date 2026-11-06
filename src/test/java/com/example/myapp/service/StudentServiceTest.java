package com.example.myapp.service;

import com.example.myapp.dto.StudentDto;
import com.example.myapp.exception.BadRequestException;
import com.example.myapp.exception.ResourceNotFoundException;
import com.example.myapp.model.Role;
import com.example.myapp.model.Student;
import com.example.myapp.model.User;
import com.example.myapp.repository.StudentRepository;
import com.example.myapp.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    private User sampleUser;
    private Student sampleStudent;

    @BeforeEach
    void setUp() {
        sampleUser = new User("johndoe", "encodedPass", Role.ROLE_STUDENT);
        sampleUser.setId(1L);

        sampleStudent = new Student(
                sampleUser,
                "John Doe",
                "john@university.edu",
                "ST12345",
                "Computer Science",
                "Semester 4",
                3.85
        );
        sampleStudent.setId(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Unit Test 1: getAllStudents returns mapped list of StudentDto")
    void testGetAllStudents_ReturnsAllStudentDtos() {
        when(studentRepository.findAll()).thenReturn(List.of(sampleStudent));

        List<StudentDto> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals("john@university.edu", result.get(0).getEmail());
        assertEquals("ST12345", result.get(0).getStudentId());
        verify(studentRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Unit Test 2: getStudentById returns StudentDto when student exists")
    void testGetStudentById_WhenStudentExists_ReturnsStudentDto() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));

        StudentDto result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("johndoe", result.getUsername());
        assertEquals("John Doe", result.getFullName());
        verify(studentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Unit Test 3: getStudentById throws ResourceNotFoundException when student does not exist")
    void testGetStudentById_WhenNotFound_ThrowsResourceNotFoundException() {
        when(studentRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.getStudentById(999L)
        );

        assertTrue(exception.getMessage().contains("Student not found with id: 999"));
        verify(studentRepository, times(1)).findById(999L);
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

    @Test
    @DisplayName("Unit Test 5: createStudent throws BadRequestException when username already exists")
    void testCreateStudent_WhenUsernameAlreadyExists_ThrowsBadRequestException() {
        StudentDto inputDto = new StudentDto();
        inputDto.setUsername("johndoe");
        inputDto.setEmail("john.unique@university.edu");
        inputDto.setStudentId("ST99999");

        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> studentService.createStudent(inputDto)
        );

        assertTrue(exception.getMessage().contains("Username already exists"));
        verify(userRepository, never()).save(any(User.class));
        verify(studentRepository, never()).save(any(Student.class));
    }

    @Test
    @DisplayName("Unit Test 6: updateStudent throws AccessDeniedException when student attempts to edit another profile")
    void testUpdateStudent_WhenStudentRoleModifiesDifferentProfile_ThrowsAccessDeniedException() {
        // Authenticated as student 'habil'
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "habil",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Target profile belongs to 'johndoe'
        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));

        StudentDto updateDto = new StudentDto();
        updateDto.setFullName("Unauthorized Update");
        updateDto.setEmail("updated@university.edu");
        updateDto.setStudentId("ST12345");

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> studentService.updateStudent(1L, updateDto)
        );

        assertTrue(exception.getMessage().contains("Students can only modify their own profile"));
        verify(studentRepository, never()).save(any(Student.class));
    }
}

