package com.example.myapp.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UniversityManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String obtainJwtToken(String username, String password) throws Exception {
        String loginPayload = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.token");
    }

    @Test
    @DisplayName("Integration Test 1: POST /api/auth/login authenticates seeded user and returns JWT token")
    void testAuthLogin_ValidCredentials_ReturnsJwtToken() throws Exception {
        String loginPayload = """
                {
                    "username": "admin",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @DisplayName("Integration Test 2: GET /api/students returns seeded student records for authenticated user")
    void testGetAllStudents_WithAuthenticatedUser_ReturnsStudentList() throws Exception {
        String adminToken = obtainJwtToken("admin", "password");

        mockMvc.perform(get("/api/students")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$[0].fullName", notNullValue()))
                .andExpect(jsonPath("$[0].email", notNullValue()));
    }

    @Test
    @DisplayName("Integration Test 3: Unauthenticated access returns 401, and unauthorized role access returns 403")
    void testRbacEndpointSecurity_UnauthenticatedAndForbiddenScenarios() throws Exception {
        // Scenario A: Accessing protected endpoint without authentication returns 401 Unauthorized
        mockMvc.perform(get("/api/admins")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // Scenario B: Student attempting to create an Admin record returns 403 Forbidden
        String studentToken = obtainJwtToken("habil", "password");
        String newAdminPayload = """
                {
                    "username": "rogueadmin",
                    "fullName": "Rogue Admin",
                    "email": "rogue@university.edu",
                    "department": "Security",
                    "phone": "+8801700000000",
                    "officeRoom": "Room 404"
                }
                """;

        mockMvc.perform(post("/api/admins")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newAdminPayload))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }
}
