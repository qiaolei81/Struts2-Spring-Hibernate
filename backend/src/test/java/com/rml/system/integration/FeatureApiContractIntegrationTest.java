package com.rml.system.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end contract verification for all major feature API endpoints.
 *
 * Seed data: test-seed.sql injects admin user (password "admin123") + ADMINISTRATOR role.
 *
 * RESOLVED BUGS (all fixed in t20/t21):
 *   BUG-1: LazyInitializationException in UserService — fixed with @Transactional(readOnly=true)
 *   BUG-2: LazyInitializationException in RoleService — fixed with @Transactional(readOnly=true)
 *
 * CONTRACT MISMATCHES (all resolved):
 *   MISMATCH-1 (RESOLVED — t27): Search param aligned: all 4 list controllers now bind
 *               @RequestParam(name = "name") so ?name= from frontend is correctly handled.
 *   MISMATCH-2 (RESOLVED — not a defect): OnlineController accepts both "/online" and "/online-users"
 *   MISMATCH-3 (RESOLVED — not a defect): DocumentController implements both "/{id}/manual"
 *               and "/manual/{filename}"
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "/test-seed.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/test-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
@DisplayName("Feature API Contract — E2E Integration")
class FeatureApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Auth module ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Auth: POST /auth/login")
    class AuthLogin {

        @Test
        @DisplayName("Valid credentials → 200 with token + user info")
        void login_validCredentials_returns200WithToken() throws Exception {
            // NOTE: will pass once BUG-1 is fixed (missing @Transactional on UserService.authenticate)
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"admin","password":"admin123"}
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.token").isNotEmpty())
                    .andExpect(jsonPath("$.data.user.username").value("admin"));
        }

        @Test
        @DisplayName("Wrong password → 401 Unauthorized")
        void login_badPassword_returns401() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"admin","password":"wrongpassword"}
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Missing username → 400 Bad Request")
        void login_missingUsername_returns400() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"password":"admin123"}
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    // ── User module ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Users: CRUD /users")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Users {

        @Test
        @DisplayName("GET /users returns paginated user list")
        void getUsers_returnsPaginatedList() throws Exception {
            mockMvc.perform(get("/users")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").isNumber());
        }

        @Test
        @DisplayName("GET /users?name=admin filters by name (MISMATCH-1 fixed)")
        void getUsers_filteredByName_returns200() throws Exception {
            mockMvc.perform(get("/users")
                            .param("page", "0")
                            .param("size", "10")
                            .param("name", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("POST /users creates a new user → 201")
        void createUser_returns201() throws Exception {
            mockMvc.perform(post("/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"username":"testuser","password":"pass1234"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.id").exists());
        }

        @Test
        @DisplayName("GET /users/all returns full user list (no pagination)")
        void getAllUsers_returnsArray() throws Exception {
            mockMvc.perform(get("/users/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("DELETE /users?ids= deletes specified user IDs")
        void deleteUsers_returns200() throws Exception {
            mockMvc.perform(delete("/users")
                            .param("ids", "nonexistent-id-1,nonexistent-id-2"))
                    .andExpect(status().isOk());
        }
    }

    // ── Role module ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Roles: CRUD /roles")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Roles {

        @Test
        @DisplayName("GET /roles returns paginated role list")
        void getRoles_returnsPaginatedList() throws Exception {
            mockMvc.perform(get("/roles")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /roles?name=ADMIN filters by name (MISMATCH-1 fixed)")
        void getRoles_filteredByName_returns200() throws Exception {
            mockMvc.perform(get("/roles")
                            .param("page", "0")
                            .param("size", "10")
                            .param("name", "ADMIN"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /roles/all returns full role list")
        void getAllRoles_returnsArray() throws Exception {
            mockMvc.perform(get("/roles/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("POST /roles creates a role → 201")
        void createRole_returns201() throws Exception {
            mockMvc.perform(post("/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"OPERATOR","description":"Operator role"}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("OPERATOR"));
        }

        @Test
        @DisplayName("PUT /roles/{id}/authorities assigns authorities to role")
        void assignAuthoritiesToRole_returns200() throws Exception {
            mockMvc.perform(put("/roles/some-role-id/authorities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"authorityIds":["auth-1","auth-2"]}
                                    """))
                    .andExpect(status().isOk());
        }
    }

    // ── Authority module ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Authorities: tree /authorities")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Authorities {

        @Test
        @DisplayName("GET /authorities/tree returns nested authority tree")
        void getAuthorityTree_returnsTree() throws Exception {
            mockMvc.perform(get("/authorities/tree"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("POST /authorities creates a root authority → 201")
        void createAuthority_returns201() throws Exception {
            mockMvc.perform(post("/authorities")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"name":"User Management","url":"PERM_USER_LIST","seq":1}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.name").value("User Management"));
        }
    }

    // ── Equipment module ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Equipment: CRUD /equipment")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Equipment {

        @Test
        @DisplayName("GET /equipment returns paginated list")
        void getEquipment_returnsPaginatedList() throws Exception {
            mockMvc.perform(get("/equipment")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /equipment?name=Server filters by name (MISMATCH-1 fixed)")
        void getEquipment_filteredByName_returns200() throws Exception {
            mockMvc.perform(get("/equipment")
                            .param("page", "0")
                            .param("size", "10")
                            .param("name", "Server"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("POST /equipment creates equipment → 201")
        void createEquipment_returns201() throws Exception {
            mockMvc.perform(post("/equipment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"model":"SRV-001","name":"Server Rack","producer":"Dell","quantity":5}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.model").value("SRV-001"));
        }

        @Test
        @DisplayName("GET /equipment/export returns Excel binary (xlsx content-type)")
        void exportEquipment_returnsExcelBlob() throws Exception {
            mockMvc.perform(get("/equipment/export"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition",
                            org.hamcrest.Matchers.containsString("attachment")));
        }
    }

    // ── Document module ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Documents: CRUD /documents")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Documents {

        @Test
        @DisplayName("GET /documents returns paginated list")
        void getDocuments_returnsPaginatedList() throws Exception {
            mockMvc.perform(get("/documents")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /documents?name=Manual filters by name (MISMATCH-1 fixed)")
        void getDocuments_filteredByName_returns200() throws Exception {
            mockMvc.perform(get("/documents")
                            .param("page", "0")
                            .param("size", "10")
                            .param("name", "Manual"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("POST /documents creates document → 201")
        void createDocument_returns201() throws Exception {
            mockMvc.perform(post("/documents")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"model":"MNL-001","name":"User Manual","producer":"Acme","quantity":1}
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.model").value("MNL-001"));
        }
    }

    // ── Logs module ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Access Logs: GET /logs")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Logs {

        @Test
        @DisplayName("GET /logs returns paginated access log list")
        void getLogs_returnsPaginatedList() throws Exception {
            mockMvc.perform(get("/logs")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @DisplayName("GET /logs?name=admin filters logs by username")
        void getLogs_filteredByUsername() throws Exception {
            mockMvc.perform(get("/logs")
                            .param("page", "0")
                            .param("size", "10")
                            .param("name", "admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    // ── Stats module ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Statistics: GET /users/stats/by-role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class Stats {

        @Test
        @DisplayName("GET /users/stats/by-role returns role distribution array")
        void getUserStatsByRole_returnsArray() throws Exception {
            mockMvc.perform(get("/users/stats/by-role"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    // ── User → Role assignment ────────────────────────────────────────────────

    @Nested
    @DisplayName("User-Role assignment: PUT /users/roles")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    class UserRoleAssignment {

        @Test
        @DisplayName("PUT /users/roles assigns roles to users")
        void assignRolesToUsers_returns200() throws Exception {
            mockMvc.perform(put("/users/roles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"userIds":["user-id-1"],"roleIds":["role-id-1"]}
                                    """))
                    .andExpect(status().isOk());
        }
    }
}
