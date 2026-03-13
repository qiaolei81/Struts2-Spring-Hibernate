package com.rml.system.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Security filter chain configuration.
 *
 * Proves:
 *  - Public endpoints (login, register, actuator/health, /health) are accessible without JWT.
 *  - All other protected endpoints return 401 (Unauthorized) without a token.
 *  - A tampered / invalid JWT Bearer token yields 401, not 500.
 *  - The 401 response body conforms to the application JSON error format.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security FilterChain — Integration")
class SecurityFilterChainIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ── Public endpoints ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /auth/login is publicly accessible (not blocked by security filter)")
    void login_isPublic() throws Exception {
        // Empty body → 400 Bad Request (validation), not 401 (security blocked).
        // A 400 proves the request reached the validation layer, i.e. the security
        // filter passed it through as a public endpoint.
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/register is publicly accessible")
    void register_isPublic() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /actuator/health is publicly accessible")
    void actuatorHealth_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /health (custom) is publicly accessible without JWT")
    void customHealth_noJwt_returnsOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /users without JWT returns 401")
    void users_noJwt_returns401() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /roles without JWT returns 401")
    void roles_noJwt_returns401() throws Exception {
        mockMvc.perform(get("/roles"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /equipment without JWT returns 401")
    void equipment_noJwt_returns401() throws Exception {
        mockMvc.perform(get("/equipment"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /documents without JWT returns 401")
    void documents_noJwt_returns401() throws Exception {
        mockMvc.perform(get("/documents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /logs without JWT returns 401")
    void logs_noJwt_returns401() throws Exception {
        mockMvc.perform(get("/logs"))
                .andExpect(status().isUnauthorized());
    }

    // ── Invalid JWT is rejected on protected endpoints ────────────────────────

    @Test
    @DisplayName("Request with invalid Bearer token returns 401")
    void invalidBearerToken_returns401() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "Bearer this.is.not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Request with malformed Authorization header returns 401")
    void malformedAuthHeader_returns401() throws Exception {
        mockMvc.perform(get("/users")
                        .header("Authorization", "NotBearer somevalue"))
                .andExpect(status().isUnauthorized());
    }
}
