package lk.sliit.hometutor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.*;
import java.nio.file.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.*;
import org.springframework.test.web.servlet.*;

@SpringBootTest
@AutoConfigureMockMvc
class WebIntegrationTest {
  static final Path DATA;

  static {
    try {
      DATA = Files.createTempDirectory("tutor-http-");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @DynamicPropertySource
  static void config(DynamicPropertyRegistry r) {
    r.add("app.data-dir", () -> DATA.toString());
    r.add("app.demo", () -> true);
  }

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper mapper;

  record Client(MockHttpSession session, String token) {}

  Client client() throws Exception {
    MvcResult r = mvc.perform(get("/api/session")).andExpect(status().isOk()).andReturn();
    return new Client(
        (MockHttpSession) r.getRequest().getSession(false),
        mapper.readTree(r.getResponse().getContentAsString()).get("csrfToken").asText());
  }

  JsonNode login(Client c, String email) throws Exception {
    return json(
        mvc.perform(
                post("/api/auth/login")
                    .session(c.session())
                    .header("X-CSRF-TOKEN", c.token())
                    .contentType("application/json")
                    .content(
                        mapper.writeValueAsString(
                            java.util.Map.of("email", email, "password", "Demo123!"))))
            .andExpect(status().isOk())
            .andReturn());
  }

  JsonNode json(MvcResult r) throws Exception {
    return mapper.readTree(r.getResponse().getContentAsString());
  }

  @Test
  void browserSessionBookingAndAuthorization() throws Exception {
    Client student = client(), tutor = client();
    login(student, "student@hometutor.test");
    login(tutor, "tutor@hometutor.test");
    var me =
        json(
            mvc.perform(get("/api/tutors/mine").session(tutor.session()))
                .andExpect(status().isOk())
                .andReturn());
    long tid = me.get("id").asLong();
    var slots =
        json(
            mvc.perform(get("/api/tutors/" + tid + "/slots"))
                .andExpect(status().isOk())
                .andReturn());
    long sid = slots.get(0).get("id").asLong();
    var booking =
        json(
            mvc.perform(
                    post("/api/bookings")
                        .session(student.session())
                        .header("X-CSRF-TOKEN", student.token())
                        .contentType("application/json")
                        .content("{\"slotId\":" + sid + "}"))
                .andExpect(status().isOk())
                .andReturn());
    long bid = booking.get("id").asLong();
    mvc.perform(
            post("/api/bookings")
                .session(student.session())
                .header("X-CSRF-TOKEN", student.token())
                .contentType("application/json")
                .content("{\"slotId\":" + sid + "}"))
        .andExpect(status().isConflict());
    mvc.perform(
            put("/api/bookings/" + bid + "/status")
                .session(student.session())
                .header("X-CSRF-TOKEN", student.token())
                .contentType("application/json")
                .content("{\"status\":\"CONFIRMED\"}"))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/bookings/" + bid + "/status")
                .session(tutor.session())
                .header("X-CSRF-TOKEN", tutor.token())
                .contentType("application/json")
                .content("{\"status\":\"CONFIRMED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"));
    mvc.perform(get("/api/admin/users").session(student.session()))
        .andExpect(status().isForbidden());
    mvc.perform(
            put("/api/bookings/" + bid + "/status")
                .session(student.session())
                .header("X-CSRF-TOKEN", student.token())
                .contentType("application/json")
                .content("{\"status\":\"CANCELLED\"}"))
        .andExpect(status().isOk());
    mvc.perform(
            delete("/api/bookings/" + bid)
                .session(student.session())
                .header("X-CSRF-TOKEN", student.token()))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/auth/logout")
                .session(student.session())
                .header("X-CSRF-TOKEN", student.token()))
        .andExpect(status().isOk());
    mvc.perform(get("/api/bookings")).andExpect(status().isUnauthorized());
  }

  @Test
  void csrfAndValidation() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"student@hometutor.test\",\"password\":\"Demo123!\"}"))
        .andExpect(status().isForbidden());
    Client c = client();
    mvc.perform(
            post("/api/auth/register")
                .session(c.session())
                .header("X-CSRF-TOKEN", c.token())
                .contentType("application/json")
                .content(
                    "{\"name\":\"Test\",\"email\":\"t@test.com\",\"password\":\"Password1\",\"role\":\"ADMIN\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/tutors/not-a-number")).andExpect(status().isBadRequest());
    mvc.perform(get("/api/tutors/999999")).andExpect(status().isNotFound());
  }

  @Test
  void staticUiAndPublicData() throws Exception {
    mvc.perform(get("/index.html"))
        .andExpect(status().isOk())
        .andExpect(header().exists("Content-Security-Policy"));
    mvc.perform(get("/js/router.js")).andExpect(status().isOk());
    mvc.perform(get("/api/tutors"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].subject").value("Chemistry"))
        .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
  }
}
