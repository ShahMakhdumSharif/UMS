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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentGetByIdSuccessUnitTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    @DisplayName("Unit Test 2: getStudentById returns StudentDto when student exists")
    void testGetStudentById_WhenStudentExists_ReturnsStudentDto() {
        User user = new User("isaac", "password", Role.ROLE_STUDENT);
        user.setId(1L);

        Student student = new Student(
                user,
                "Isaac Aneek Sarker",
                "isaac@university.edu",
                "2207095",
                "Computer Science",
                "Semester 6",
                3.85
        );
        student.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentDto result = studentService.getStudentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("isaac", result.getUsername());
        assertEquals("Isaac Aneek Sarker", result.getFullName());
        verify(studentRepository, times(1)).findById(1L);
    }
}
