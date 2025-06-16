package com.data.ra.service.auth;

import com.data.ra.dto.auth.SignUpRequestDTO;
import com.data.ra.entity.auth.User;

public interface AuthService {
    User findByEmail(String email);
    User login(String email, String password);
    User findByRememberToken(String token);
    void saveRememberToken(Long userId, String token);
    void removeRememberToken(String token);
    boolean register(SignUpRequestDTO request);
}
