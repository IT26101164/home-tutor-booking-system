package lk.sliit.hometutor.web;

import static lk.sliit.hometutor.web.SessionUser.uid;

import jakarta.servlet.http.*;
import java.util.*;
import lk.sliit.hometutor.service.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AdminController {
    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/admin/users")
    public Object users(HttpServletRequest r) {
        return service.users(uid(r));
    }

    @DeleteMapping("/admin/users/{id}")
    public void deactivate(@PathVariable long id, HttpServletRequest r) {
        service.deactivate(uid(r), id);
    }

    @GetMapping("/admin/stats")
    public Object stats(HttpServletRequest r) {
        return service.stats(uid(r));
    }

    public record AdminCreateInput(String name, String email, String password, String role) {}

    public record AdminEditInput(String name, String email) {}

    @PostMapping("/admin/users")
    public Object create(@RequestBody AdminCreateInput i, HttpServletRequest r) {
        return service.createUser(uid(r), i.name(), i.email(), i.password(), i.role());
    }

    @PutMapping("/admin/users/{id}")
    public Object edit(@PathVariable long id, @RequestBody AdminEditInput i, HttpServletRequest r) {
        return service.updateUser(uid(r), id, i.name(), i.email());
    }

    @PutMapping("/admin/users/{id}/activate")
    public Object activate(@PathVariable long id, HttpServletRequest r) {
        return service.reactivate(uid(r), id);
    }
}
