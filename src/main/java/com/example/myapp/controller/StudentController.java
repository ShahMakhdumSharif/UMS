package com.example.myapp.controller;

import com.example.myapp.dto.ApiResponse;
import com.example.myapp.dto.StudentDto;
import com.example.myapp.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentDto> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@Valid @RequestBody StudentDto dto) {
        StudentDto created = studentService.createStudent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDto dto) {
        StudentDto updated = studentService.updateStudent(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Student deleted successfully"));
    }
}
