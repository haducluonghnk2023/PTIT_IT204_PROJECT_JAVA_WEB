package com.data.ra.repository;

import com.data.ra.entity.auth.User;

public interface AuthRepository {
    User findByEmail(String email);
    User login(String email, String password);
    User findByRememberToken(String token);
    void updateRememberToken(Long userId, String token);
    void removeRememberToken(String token);
}
