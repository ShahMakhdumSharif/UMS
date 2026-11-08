package com.example.myapp.unit;

import com.example.myapp.exception.ResourceNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentGetByIdNotFoundUnitTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

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
}

