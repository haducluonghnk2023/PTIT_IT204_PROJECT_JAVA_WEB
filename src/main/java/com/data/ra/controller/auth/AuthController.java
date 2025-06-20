package com.data.ra.controller.auth;

import com.data.ra.dto.auth.ForgotPasswordDTO;
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

    @GetMapping
    public String auth() {
        return "redirect:/auth/login";
    }

    // ✅ Đăng nhập + Form quên mật khẩu dùng chung file
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "forgot", required = false) String forgot,
                            Model model,
                            @ModelAttribute("user") UserDTO userDTO) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserDTO());
        }
        return "auth/sign-in"; // Thymeleaf dùng param.forgot để hiển thị đúng form
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, Model model) {
        User user = authService.findByResetToken(token);
        if (user == null) {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn.");
            return "auth/reset-password-error"; // bạn cần tạo view này nếu muốn
        }
        model.addAttribute("token", token);
        return "auth/reset-password"; // trang chứa form đổi mật khẩu
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      RedirectAttributes redirectAttributes) {

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp");
            return "redirect:/auth/reset-password?token=" + token;
        }

        boolean success = authService.resetPassword(token, password);
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập.");
            return "redirect:/auth/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Token không hợp lệ hoặc đã hết hạn");
            return "redirect:/auth/reset-password?token=" + token;
        }
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

        User user;
        try {
            user = authService.login(userDTO.getEmail(), userDTO.getPassword());
        } catch (IllegalStateException ex) {
            model.addAttribute("errorLock", ex.getMessage());
            return "auth/sign-in";
        }

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

        // Ghi nhớ đăng nhập
        if (rememberMe != null) {
            String token = UUID.randomUUID().toString();
            authService.saveRememberToken(user.getId(), token);

            Cookie cookie = new Cookie("remember-token", token);
            cookie.setMaxAge(7 * 24 * 60 * 60); // 7 ngày
            cookie.setPath("/");
            response.addCookie(cookie);
        }

        // Redirect theo vai trò
        return "admin".equalsIgnoreCase(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/";
    }


    // ✅ Xử lý quên mật khẩu
    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgot") ForgotPasswordDTO forgotPasswordDTO,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Email không hợp lệ.");
            return "redirect:/auth/login?forgot=true";
        }

        User user = authService.findByEmail(forgotPasswordDTO.getEmail());
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng với email này.");
            return "redirect:/auth/login?forgot=true";
        }

        // Tạo token reset
        String token = UUID.randomUUID().toString();
        authService.saveResetToken(user.getEmail(), token); // ⚠️ bạn cần cài thêm method này

        // Gửi email
        String resetLink = "http://localhost:8080/auth/reset-password?token=" + token;
        authService.sendResetPasswordEmail(user.getEmail(), resetLink); // ⚠️ dùng EmailService bên trong AuthService

        redirectAttributes.addFlashAttribute("message", "Vui lòng kiểm tra email để đặt lại mật khẩu.");
        return "redirect:/auth/login";
    }


    // ✅ Đăng ký
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

        if (result.hasErrors()) {
            return "auth/sign-up";
        }

        if (!signUpRequest.getPassword().equals(signUpRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
            return "auth/sign-up";
        }

        boolean isRegistered = authService.register(signUpRequest);
        if (!isRegistered) {
            model.addAttribute("error", "Email đã tồn tại");
            return "auth/sign-up";
        }

        redirectAttributes.addFlashAttribute("user", new UserDTO(signUpRequest.getEmail(), signUpRequest.getPassword()));
        redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");
        return "redirect:/auth/login";
    }

    // ✅ Đăng xuất
    @GetMapping("/logout")
    public String logout(HttpSession session,
                         HttpServletResponse response,
                         @CookieValue(value = "remember-token", required = false) String rememberToken) {

        if (rememberToken != null) {
            authService.removeRememberToken(rememberToken);
        }

        Cookie cookie = new Cookie("remember-token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        if (session != null) {
            session.invalidate();
        }

        return "redirect:/auth/login";
    }
}
