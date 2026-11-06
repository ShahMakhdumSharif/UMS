package com.example.myapp.controller;

import com.example.myapp.dto.ApiResponse;
import com.example.myapp.dto.TeacherDto;
import com.example.myapp.service.TeacherService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public ResponseEntity<List<TeacherDto>> getAllTeachers() {
        return ResponseEntity.ok(teacherService.getAllTeachers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeacherDto> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(teacherService.getTeacherById(id));
    }

    @PostMapping
    public ResponseEntity<TeacherDto> createTeacher(@Valid @RequestBody TeacherDto dto) {
        TeacherDto created = teacherService.createTeacher(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherDto> updateTeacher(@PathVariable Long id, @Valid @RequestBody TeacherDto dto) {
        TeacherDto updated = teacherService.updateTeacher(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Teacher deleted successfully"));
    }
}
