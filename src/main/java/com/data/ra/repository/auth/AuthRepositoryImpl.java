package com.data.ra.repository.auth;

import com.data.ra.dto.auth.SignUpRequestDTO;
import com.data.ra.entity.auth.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public User findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        }
    }

    @Override
    public User login(String email, String rawPassword) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();

            if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
                return user;
            }

            return null;
        }
    }

    @Override
    public User findByRememberToken(String token) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE rememberToken = :token", User.class)
                    .setParameter("token", token)
                    .uniqueResult();
        }
    }

    @Override
    public void updateRememberToken(Long userId, String token) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, userId);
            if (user != null) {
                user.setRememberToken(token);
                session.update(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public void removeRememberToken(String token) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            User user = findByRememberToken(token);
            if (user != null) {
                user.setRememberToken(null);
                session.update(user);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public boolean register(SignUpRequestDTO request) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            // Check nếu email đã tồn tại
            User existingUser = findByEmail(request.getEmail());
            if (existingUser != null) {
                return false; // Email đã tồn tại
            }

            // Tạo user mới
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());

            // Mã hóa mật khẩu bằng BCrypt
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            newUser.setPassword(encodedPassword);

            newUser.setRole(request.getRole() != null ? request.getRole() : "candidate");
            newUser.setRememberToken(null);

            // Lưu vào DB
            session.save(newUser);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

}
