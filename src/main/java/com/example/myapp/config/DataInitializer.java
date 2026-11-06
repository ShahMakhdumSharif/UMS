package com.example.myapp.config;

import com.example.myapp.model.Admin;
import com.example.myapp.model.Role;
import com.example.myapp.model.Student;
import com.example.myapp.model.Teacher;
import com.example.myapp.model.User;
import com.example.myapp.repository.AdminRepository;
import com.example.myapp.repository.StudentRepository;
import com.example.myapp.repository.TeacherRepository;
import com.example.myapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           AdminRepository adminRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            // Seed Admin
            User adminUser = new User("admin", passwordEncoder.encode("password"), Role.ROLE_ADMIN);
            userRepository.save(adminUser);
            Admin admin = new Admin(
                    adminUser,
                    "Dr. Mashud",
                    "admin@university.edu",
                    "University Administration",
                    "+8801866666666",
                    "Admin Building 201"
            );
            adminRepository.save(admin);

            // Seed Teacher 1
            User teacherUser1 = new User("prof.hashem", passwordEncoder.encode("password"), Role.ROLE_TEACHER);
            userRepository.save(teacherUser1);
            Teacher teacher1 = new Teacher(
                    teacherUser1,
                    "Prof. MMA Hashem",
                    "mma.hashem@university.edu",
                    "Computer Science & Engineering",
                    "Professor",
                    "+8801822223333"
            );
            teacherRepository.save(teacher1);

            // Seed Teacher 2
            User teacherUser2 = new User("prof.rahim", passwordEncoder.encode("password"), Role.ROLE_TEACHER);
            userRepository.save(teacherUser2);
            Teacher teacher2 = new Teacher(
                    teacherUser2,
                    "Prof. Rahim",
                    "rahim.prof@university.edu",
                    "Electrical & Electronic Engineering",
                    "Professor",
                    "+8801822223334"
            );
            teacherRepository.save(teacher2);

            // Seed Student 1
            User studentUser1 = new User("habil", passwordEncoder.encode("password"), Role.ROLE_STUDENT);
            userRepository.save(studentUser1);
            Student student1 = new Student(
                    studentUser1,
                    "Habil",
                    "habil.student@university.edu",
                    "2207089",
                    "Computer Science & Engineering",
                    "Semester 6",
                    3.13
            );
            studentRepository.save(student1);

            // Seed Student 2
            User studentUser2 = new User("kabil", passwordEncoder.encode("password"), Role.ROLE_STUDENT);
            userRepository.save(studentUser2);
            Student student2 = new Student(
                    studentUser2,
                    "Kabil",
                    "kabil.student@university.edu",
                    "2207088",
                    "Computer Science & Engineering",
                    "Semester 6",
                    3.70
            );
            studentRepository.save(student2);
        }
    }
}
