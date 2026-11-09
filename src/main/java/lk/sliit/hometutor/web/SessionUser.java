package lk.sliit.hometutor.web;

import jakarta.servlet.http.*;
import lk.sliit.hometutor.service.AppException;

final class SessionUser {
    private SessionUser() {}

    static long uid(HttpServletRequest r) {
        HttpSession s = r.getSession(false);
        if (s == null || !(s.getAttribute("uid") instanceof Long id))
            throw new AppException(401, "Please sign in.");
        return id;
    }
}
