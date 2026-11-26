package com.shopsphere.userservice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

/** End-to-end API tests on an in-memory H2 database. Emails are random so tests never clash. */
@SpringBootTest
@AutoConfigureMockMvc
class UserApiTest {

    // the super admin created at startup from src/test/resources/application.properties
    private static final String SUPER_EMAIL = "superadmin@test.com";
    private static final String PASSWORD = "Admin@12345";
    private static final String USER_PASSWORD = "secret123";

    @Autowired
    MockMvc mvc;

    @Autowired
    JwtDecoder jwtDecoder;

    // ---------- helpers ----------

    private String email() {
        return "u" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    }

    private String createBody(String email, String password) {
        return "{\"name\":\"Test User\",\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private long idOf(String body) {
        Number n = JsonPath.read(body, "$.id");
        return n.longValue();
    }

    /** Registers a customer and returns the new id. */
    private long register(String email) throws Exception {
        String body = mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(email, USER_PASSWORD)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return idOf(body);
    }

    private String login(String email, String password) throws Exception {
        String body = mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String superToken() throws Exception {
        return login(SUPER_EMAIL, PASSWORD);
    }

    /** Super admin creates an admin; returns that admin's email. */
    private String createAdmin() throws Exception {
        String email = email();
        mvc.perform(post("/users/admins").header(HttpHeaders.AUTHORIZATION, bearer(superToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(createBody(email, USER_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        return email;
    }

    private long meId(String token) throws Exception {
        String body = mvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return idOf(body);
    }

    private String statusBody(boolean active) {
        return "{\"active\":" + active + "}";
    }

    // ---------- register ----------

    @Test
    void registerCreatesACustomerAndNeverShowsThePassword() throws Exception {
        String email = email();
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(email, USER_PASSWORD)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerCannotChooseARole() throws Exception {
        // an extra "role" field in the body is ignored
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Sneaky\",\"email\":\"" + email() + "\",\"password\":\"secret123\",\"role\":\"SUPER_ADMIN\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void duplicateEmailIsRejectedIgnoringCase() throws Exception {
        String email = email();
        register(email);
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(email.toUpperCase(), USER_PASSWORD)))
                .andExpect(status().isConflict());
    }

    @Test
    void registerValidation() throws Exception {
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody(email(), "short")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(createBody("not-an-email", USER_PASSWORD)))
                .andExpect(status().isBadRequest());
    }

    // ---------- login ----------

    @Test
    void loginReturnsATokenWithTheContractTheOtherServicesExpect() throws Exception {
        String email = email();
        long id = register(email);

        String token = login(email, USER_PASSWORD);
        Jwt jwt = jwtDecoder.decode(token);   // verified with the same secret the other services use

        Assertions.assertEquals(String.valueOf(id), jwt.getSubject());
        Assertions.assertEquals("CUSTOMER", jwt.getClaimAsString("role"));
        Assertions.assertNotNull(jwt.getExpiresAt());
    }

    @Test
    void wrongPasswordAndUnknownEmailGiveTheSameAnswer() throws Exception {
        String email = email();
        register(email);

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"wrongpass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email() + "\",\"password\":\"wrongpass1\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Invalid email or password"));
    }

    // ---------- my own account ----------

    @Test
    void meNeedsATokenAndReturnsTheCallersOwnAccount() throws Exception {
        String email = email();
        register(email);

        mvc.perform(get("/users/me")).andExpect(status().isUnauthorized());

        mvc.perform(get("/users/me").header(HttpHeaders.AUTHORIZATION, bearer(login(email, USER_PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void changePassword() throws Exception {
        String email = email();
        register(email);
        String token = login(email, USER_PASSWORD);

        // wrong current password
        mvc.perform(put("/users/me/password").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrongpass1\",\"newPassword\":\"newsecret456\"}"))
                .andExpect(status().isBadRequest());

        // correct change
        mvc.perform(put("/users/me/password").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"" + USER_PASSWORD + "\",\"newPassword\":\"newsecret456\"}"))
                .andExpect(status().isNoContent());

        // the new password works, the old one does not
        login(email, "newsecret456");
        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + USER_PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- admins ----------

    @Test
    void onlyTheSuperAdminCanCreateAdmins() throws Exception {
        // the super admin can
        createAdmin();

        // a customer cannot
        String customerEmail = email();
        register(customerEmail);
        mvc.perform(post("/users/admins").header(HttpHeaders.AUTHORIZATION, bearer(login(customerEmail, USER_PASSWORD)))
                        .contentType(MediaType.APPLICATION_JSON).content(createBody(email(), USER_PASSWORD)))
                .andExpect(status().isForbidden());

        // an admin cannot create another admin
        String adminToken = login(createAdmin(), USER_PASSWORD);
        mvc.perform(post("/users/admins").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(createBody(email(), USER_PASSWORD)))
                .andExpect(status().isForbidden());
    }

    @Test
    void userListIsForAdminsOnly() throws Exception {
        String customerEmail = email();
        register(customerEmail);
        String customerToken = login(customerEmail, USER_PASSWORD);
        String adminToken = login(createAdmin(), USER_PASSWORD);

        mvc.perform(get("/users").header(HttpHeaders.AUTHORIZATION, bearer(customerToken)))
                .andExpect(status().isForbidden());

        mvc.perform(get("/users").param("role", "CUSTOMER").header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].role").exists());
    }

    // ---------- blocking ----------

    @Test
    void adminBlocksACustomerWhoThenCannotLogIn() throws Exception {
        String customerEmail = email();
        long customerId = register(customerEmail);
        String adminToken = login(createAdmin(), USER_PASSWORD);

        mvc.perform(put("/users/" + customerId + "/status").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + customerEmail + "\",\"password\":\"" + USER_PASSWORD + "\"}"))
                .andExpect(status().isForbidden());

        // unblock: login works again
        mvc.perform(put("/users/" + customerId + "/status").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(true)))
                .andExpect(status().isOk());
        login(customerEmail, USER_PASSWORD);
    }

    @Test
    void blockingRulesProtectAdminsAndTheSuperAdmin() throws Exception {
        String adminEmail = createAdmin();
        String adminToken = login(adminEmail, USER_PASSWORD);
        long adminId = meId(adminToken);
        long superId = meId(superToken());
        long otherAdminId = meId(login(createAdmin(), USER_PASSWORD));

        // nobody blocks the super admin
        mvc.perform(put("/users/" + superId + "/status").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(false)))
                .andExpect(status().isForbidden());

        // an admin cannot block themselves
        mvc.perform(put("/users/" + adminId + "/status").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(false)))
                .andExpect(status().isForbidden());

        // an admin cannot block another admin
        mvc.perform(put("/users/" + otherAdminId + "/status").header(HttpHeaders.AUTHORIZATION, bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(false)))
                .andExpect(status().isForbidden());

        // the super admin can block an admin
        mvc.perform(put("/users/" + otherAdminId + "/status").header(HttpHeaders.AUTHORIZATION, bearer(superToken()))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void customersCannotChangeStatus() throws Exception {
        String customerEmail = email();
        register(customerEmail);
        long otherId = register(email());

        mvc.perform(put("/users/" + otherId + "/status")
                        .header(HttpHeaders.AUTHORIZATION, bearer(login(customerEmail, USER_PASSWORD)))
                        .contentType(MediaType.APPLICATION_JSON).content(statusBody(false)))
                .andExpect(status().isForbidden());
    }
}
