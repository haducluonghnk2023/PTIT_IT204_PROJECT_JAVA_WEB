package com.data.ra.service.auth;

import com.data.ra.dto.auth.SignUpRequestDTO;
import com.data.ra.entity.auth.User;
import com.data.ra.repository.auth.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Override
    public User findByEmail(String email) {
        return authRepository.findByEmail(email);
    }

    @Override
    public User findById(Integer id) {
        return authRepository.findById(id);
    }

    @Override
    public User login(String email, String password) {
        return authRepository.login(email, password);
    }

    @Override
    public User findByRememberToken(String token) {
        return authRepository.findByRememberToken(token);
    }

    @Override
    public void saveRememberToken(Integer userId, String token) {
        authRepository.updateRememberToken(userId, token);
    }

    @Override
    public void removeRememberToken(String token) {
        User user = authRepository.findByRememberToken(token);
        if (user != null) {
            user.setRememberToken(null);
            authRepository.updateRememberToken(user.getId(), null);
        }
    }

    @Override
    public boolean register(SignUpRequestDTO request) {
        return authRepository.register(request);
    }

    @Override
    public void save(User user) {
        authRepository.save(user);
    }

    // ✅ Lưu reset token vào DB
    @Override
    public void saveResetToken(String email, String token) {
        authRepository.saveResetToken(email, token);
    }

    // ✅ Gửi email có chứa link đặt lại mật khẩu
    @Override
    public void sendResetPasswordEmail(String email, String resetLink) {
        emailService.sendResetPasswordEmail(email, resetLink);
    }

    @Override
    public User findByResetToken(String token) {
        User user = authRepository.findByResetToken(token);
        if (user != null && user.getResetToken() != null) {
            return user;
        }
        return null;
    }

    @Override
    public boolean resetPassword(String token, String password) {
       return authRepository.resetPassword(token, password);
    }

}