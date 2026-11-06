package com.example.myapp.unit;

import com.example.myapp.dto.StudentDto;
import com.example.myapp.model.Role;
import com.example.myapp.model.Student;
import com.example.myapp.model.User;
import com.example.myapp.repository.StudentRepository;
import com.example.myapp.repository.UserRepository;
import com.example.myapp.service.StudentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentGetAllUnitTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("Unit Test 1: getAllStudents returns mapped list of StudentDto")
    void testGetAllStudents_ReturnsAllStudentDtos() {
        User user = new User("johndoe", "encodedPass", Role.ROLE_STUDENT);
        user.setId(1L);

        Student student = new Student(
                user,
                "John Doe",
                "john@university.edu",
                "ST12345",
                "Computer Science",
                "Semester 4",
                3.85
        );
        student.setId(1L);

        when(studentRepository.findAll()).thenReturn(List.of(student));

        List<StudentDto> result = studentService.getAllStudents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals("john@university.edu", result.get(0).getEmail());
        assertEquals("ST12345", result.get(0).getStudentId());
        verify(studentRepository, times(1)).findAll();
    }
}
