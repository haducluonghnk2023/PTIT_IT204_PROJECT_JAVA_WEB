package com.data.ra.service;

import com.data.ra.entity.auth.User;
import com.data.ra.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthRepository authRepository;

    @Override
    public User findByEmail(String email) {
        return authRepository.findByEmail(email);
    }

    @Override
    public User login(String email, String password) {
        User user = authRepository.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
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
}