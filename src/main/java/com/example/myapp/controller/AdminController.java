package com.example.myapp.controller;

import com.example.myapp.dto.AdminDto;
import com.example.myapp.dto.ApiResponse;
import com.example.myapp.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public ResponseEntity<List<AdminDto>> getAllAdmins() {
        return ResponseEntity.ok(adminService.getAllAdmins());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdminDto> getAdminById(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getAdminById(id));
    }

    @PostMapping
    public ResponseEntity<AdminDto> createAdmin(@Valid @RequestBody AdminDto dto) {
        AdminDto created = adminService.createAdmin(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminDto> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminDto dto) {
        AdminDto updated = adminService.updateAdmin(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdmin(@PathVariable Long id) {
        adminService.deleteAdmin(id);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Admin deleted successfully"));
    }
}
