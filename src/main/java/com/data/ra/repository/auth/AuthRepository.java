package com.data.ra.repository.auth;

import com.data.ra.dto.auth.SignUpRequestDTO;
import com.data.ra.entity.auth.User;

public interface AuthRepository {
    User findByEmail(String email);
    User findById(Integer id);
    User login(String email, String password);
    User findByRememberToken(String token);
    void updateRememberToken(Integer userId, String token);
    void removeRememberToken(String token);
    boolean register(SignUpRequestDTO request);
    void save(User user);
    void saveResetToken(String email, String token);
    User findByResetToken(String token);
    boolean resetPassword(String token, String password);
}
