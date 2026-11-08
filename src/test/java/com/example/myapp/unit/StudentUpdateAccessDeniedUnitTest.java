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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StudentUpdateAccessDeniedUnitTest {

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
    @DisplayName("Unit Test 6: updateStudent throws AccessDeniedException when student attempts to edit another profile")
    void testUpdateStudent_WhenStudentRoleModifiesDifferentProfile_ThrowsAccessDeniedException() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "habil",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        User sampleUser = new User("isaac", "password", Role.ROLE_STUDENT);
        sampleUser.setId(1L);

        Student sampleStudent = new Student(
                sampleUser,
                "Isaac Aneek Sarker",
                "isaac@university.edu",
                "2207095",
                "Computer Science",
                "Semester 6",
                3.85
        );
        sampleStudent.setId(1L);

        when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));

        StudentDto updateDto = new StudentDto();
        updateDto.setFullName("Unauthorized Update");
        updateDto.setEmail("updated@university.edu");
        updateDto.setStudentId("2207096");

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> studentService.updateStudent(1L, updateDto)
        );

        assertTrue(exception.getMessage().contains("Students can only modify their own profile"));
        verify(studentRepository, never()).save(any(Student.class));
    }
}

