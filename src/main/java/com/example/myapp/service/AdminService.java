package com.example.myapp.service;

import com.example.myapp.dto.AdminDto;
import com.example.myapp.exception.BadRequestException;
import com.example.myapp.exception.ResourceNotFoundException;
import com.example.myapp.model.Admin;
import com.example.myapp.model.Role;
import com.example.myapp.model.User;
import com.example.myapp.repository.AdminRepository;
import com.example.myapp.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(AdminRepository adminRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminDto> getAllAdmins() {
        return adminRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminDto getAdminById(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));
        return toDto(admin);
    }

    @Transactional
    public AdminDto createAdmin(AdminDto dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username is required");
        }
        if (userRepository.existsByUsername(dto.getUsername().trim())) {
            throw new BadRequestException("Username already exists: " + dto.getUsername());
        }
        if (adminRepository.existsByEmail(dto.getEmail().trim())) {
            throw new BadRequestException("Email already registered: " + dto.getEmail());
        }

        String rawPassword = dto.getPassword();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "password123"; // default fallback password
        }

        User user = new User(
                dto.getUsername().trim(),
                passwordEncoder.encode(rawPassword),
                Role.ROLE_ADMIN
        );
        user = userRepository.save(user);

        Admin admin = new Admin(
                user,
                dto.getFullName(),
                dto.getEmail(),
                dto.getDepartment(),
                dto.getPhone(),
                dto.getOfficeRoom()
        );
        admin = adminRepository.save(admin);

        return toDto(admin);
    }

    @Transactional
    public AdminDto updateAdmin(Long id, AdminDto dto) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            String currentUsername = auth.getName();
            if (admin.getUser() == null || !admin.getUser().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("Admins can only modify their own profile");
            }
        }

        if (adminRepository.existsByEmailAndIdNot(dto.getEmail(), id)) {
            throw new BadRequestException("Email already in use by another admin: " + dto.getEmail());
        }

        admin.setFullName(dto.getFullName());
        admin.setEmail(dto.getEmail());
        admin.setDepartment(dto.getDepartment());
        admin.setPhone(dto.getPhone());
        admin.setOfficeRoom(dto.getOfficeRoom());

        // Update password if provided
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            admin.getUser().setPassword(passwordEncoder.encode(dto.getPassword().trim()));
            userRepository.save(admin.getUser());
        }

        Admin updated = adminRepository.save(admin);
        return toDto(updated);
    }

    @Transactional
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            String currentUsername = auth.getName();
            if (admin.getUser() == null || !admin.getUser().getUsername().equals(currentUsername)) {
                throw new AccessDeniedException("Admins can only delete their own profile");
            }
        }
        adminRepository.delete(admin);
    }

    public AdminDto toDto(Admin admin) {
        AdminDto dto = new AdminDto();
        dto.setId(admin.getId());
        dto.setUsername(admin.getUser() != null ? admin.getUser().getUsername() : null);
        dto.setFullName(admin.getFullName());
        dto.setEmail(admin.getEmail());
        dto.setDepartment(admin.getDepartment());
        dto.setPhone(admin.getPhone());
        dto.setOfficeRoom(admin.getOfficeRoom());
        dto.setRole(admin.getUser() != null ? admin.getUser().getRole().name() : Role.ROLE_ADMIN.name());
        return dto;
    }
}
