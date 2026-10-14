package lk.sliit.hometutor.web;

import java.nio.file.Path;
import java.time.*;
import lk.sliit.hometutor.repository.FileStateRepository;
import lk.sliit.hometutor.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class AppConfig {
  @Bean
  TutorService tutorService(
      @Value("${app.data-dir}") String directory, @Value("${app.demo}") boolean demo) {
    TutorService s =
        new TutorService(
            new FileStateRepository(Path.of(directory)), Clock.system(ZoneId.of("Asia/Colombo")));
    if (demo) s.seedDemo();
    return s;
  }

  @Bean
  AccountService accounts(TutorService app) {
    return app.accounts();
  }

  @Bean
  TutorProfileService profiles(TutorService app) {
    return app.profiles();
  }

  @Bean
  AvailabilityService availability(TutorService app) {
    return app.availability();
  }

  @Bean
  BookingService bookingsModule(TutorService app) {
    return app.bookingsModule();
  }

  @Bean
  ReviewService reviewsModule(TutorService app) {
    return app.reviewsModule();
  }

  @Bean
  AdminService admin(TutorService app) {
    return app.admin();
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    // Session identity is checked by the application service. Spring supplies CSRF and headers.
    http.authorizeHttpRequests(a -> a.anyRequest().permitAll())
        .formLogin(f -> f.disable())
        .httpBasic(b -> b.disable())
        .logout(l -> l.disable())
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; script-src 'self'; style-src 'self'; img-src"
                                + " 'self' data:; connect-src 'self'; frame-ancestors 'none';"
                                + " base-uri 'self'; form-action 'self'")));
    return http.build();
  }
}
