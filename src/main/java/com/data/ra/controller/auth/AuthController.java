package com.data.ra.controller.auth;

import com.data.ra.dto.auth.UserDTO;
import com.data.ra.entity.auth.User;
import com.data.ra.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("user", new UserDTO());
        return "auth/sign-in";
    }

    @GetMapping
    public String auth(Model model) {
        return "redirect:/auth/login";
    }

    @PostMapping("/login")
    public String signIn(@Valid @ModelAttribute("user") UserDTO userDTO,
                         BindingResult result,
                         @RequestParam(value = "remember", required = false) String rememberMe,
                         HttpSession session,
                         HttpServletResponse response,
                         Model model) {

        if (result.hasErrors()) {
            return "auth/sign-in";
        }
        User user = authService.login(userDTO.getEmail(), userDTO.getPassword());

        if (user == null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng");
            return "auth/sign-in";
        }

        // Lưu session theo role
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("currentAdmin", user);
        } else if ("CANDIDATE".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("currentCandidate", user);
        }

        // Remember me
        if (rememberMe != null) {
            String token = UUID.randomUUID().toString();
            authService.saveRememberToken(user.getId(), token);

            Cookie cookie = new Cookie("remember-token", token);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        // Chuyển trang sau login
        return "ADMIN".equalsIgnoreCase(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/candidate";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session,
                         HttpServletResponse response,
                         @CookieValue(value = "remember-token", required = false) String rememberToken) {

        // Xoá token trong DB nếu có
        if (rememberToken != null) {
            authService.removeRememberToken(rememberToken);
        }

        // Xoá cookie
        Cookie cookie = new Cookie("remember-token", null);
        cookie.setMaxAge(0); // Xóa ngay
        cookie.setPath("/");
        response.addCookie(cookie);

        // Invalidate session
        if (session != null) {
            session.invalidate();
        }

        // Redirect về login
        return "redirect:/auth/login";
    }

}
