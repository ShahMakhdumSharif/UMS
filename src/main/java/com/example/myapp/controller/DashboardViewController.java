package com.example.myapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/ums")
public class DashboardViewController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "forward:/dashboard.html";
    }

    @GetMapping("/dashboard/admin")
    public String admin() {
        return "forward:/admin.html";
    }

    @GetMapping("/dashboard/teacher")
    public String teacher() {
        return "forward:/teacher.html";
    }

    @GetMapping("/dashboard/student")
    public String student() {
        return "forward:/student.html";
    }
}
