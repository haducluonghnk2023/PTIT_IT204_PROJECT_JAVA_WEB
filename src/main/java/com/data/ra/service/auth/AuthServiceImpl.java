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

    @Override
    public User findByEmail(String email) {
        return authRepository.findByEmail(email);
    }

    @Override
    public User login(String email, String password) {
        User user = authRepository.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }

    @Override
    public User findByRememberToken(String token) {
        return authRepository.findByRememberToken(token);
    }

    @Override
    public void saveRememberToken(Long userId, String token) {
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
}