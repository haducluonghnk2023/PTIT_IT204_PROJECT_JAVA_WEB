package com.data.ra.controller.auth;

import com.data.ra.dto.auth.SignUpRequestDTO;
import com.data.ra.dto.auth.UserDTO;
import com.data.ra.entity.auth.User;
import com.data.ra.service.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public String loginPage(Model model, @ModelAttribute("user") UserDTO userDTO) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
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
        if ("admin".equalsIgnoreCase(user.getRole())) {
            session.setAttribute("currentAdmin", user);
        } else if ("candidate".equalsIgnoreCase(user.getRole())) {
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
        return "admin".equalsIgnoreCase(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/candidate/information";
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

    @GetMapping("/signup")
    public String signUpPage(Model model) {
        model.addAttribute("signUpRequest", new SignUpRequestDTO());
        return "auth/sign-up";
    }

    @PostMapping("/signup")
    public String signUp(@Valid @ModelAttribute("signUpRequest") SignUpRequestDTO signUpRequest,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {

        // 🔴 BƯỚC 1: Check các lỗi @NotBlank, @Email trước
        if (result.hasErrors()) {
            return "auth/sign-up";
        }

        // 🔴 BƯỚC 2: Check confirm password sau khi các trường khác OK
        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
            return "auth/sign-up";
        }

        // 🔴 BƯỚC 3: Kiểm tra email trùng
        boolean isRegistered = authService.register(signUpRequest);
        if (!isRegistered) {
            model.addAttribute("error", "Email đã tồn tại");
            return "auth/sign-up";
        }

        redirectAttributes.addFlashAttribute("user", new UserDTO(signUpRequest.getEmail(),""));
        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        // Thành công
        return "redirect:/auth/login";
    }


}
