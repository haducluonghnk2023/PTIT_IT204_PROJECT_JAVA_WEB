package com.data.ra.config;

import com.data.ra.entity.auth.User;
import com.data.ra.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    private AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // ✅ Bỏ qua nếu đang truy cập trang auth/login hoặc auth/**
        String uri = request.getRequestURI();
        if (uri.startsWith(request.getContextPath() + "/auth")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session != null &&
                (session.getAttribute("currentAdmin") != null || session.getAttribute("currentCandidate") != null)) {
            return true;
        }

        // ✅ Check cookie "remember-token"
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("remember-token".equals(cookie.getName())) {
                    String token = cookie.getValue();

                    User user = authService.findByRememberToken(token);
                    if (user != null) {
                        // ✅ Gắn lại session
                        session = request.getSession(true);
                        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                            session.setAttribute("currentAdmin", user);
                        } else if ("CANDIDATE".equalsIgnoreCase(user.getRole())) {
                            session.setAttribute("currentCandidate", user);
                        }
                        return true; // Cho phép đi tiếp
                    }
                }
            }
        }

        // ✅ Nếu không có session hoặc không tìm thấy user từ token thì redirect
        response.sendRedirect(request.getContextPath() + "/auth/login");
        return false;
    }
}
