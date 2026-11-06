package com.example.myapp.unit;

import com.example.myapp.dto.StudentDto;
import com.example.myapp.exception.BadRequestException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentCreateDuplicateUsernameUnitTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

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
        verify(userRepository, never()).save(any());
        verify(studentRepository, never()).save(any());
    }
}
