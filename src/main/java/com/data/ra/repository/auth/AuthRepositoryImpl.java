package com.data.ra.repository.auth;

import com.data.ra.dto.auth.SignUpRequestDTO;
import com.data.ra.entity.auth.User;
import com.data.ra.entity.candidate.Candidate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

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
    public User findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(User.class, id);
        }
    }

    @Override
    public User login(String email, String rawPassword) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();

            if (user != null && passwordEncoder.matches(rawPassword, user.getPassword())) {

                // Tìm Candidate theo user_id
                Candidate candidate = session.createQuery("FROM Candidate WHERE user.id = :userId", Candidate.class)
                        .setParameter("userId", user.getId())
                        .uniqueResult();

                // Nếu có Candidate và trạng thái là deactive thì return null (tài khoản bị khóa)
                if (candidate != null && "deactive".equalsIgnoreCase(candidate.getStatus())) {
                    throw new IllegalStateException("Tài khoản đã bị khóa.");
                }

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
    public void updateRememberToken(Integer userId, String token) {
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

            // 1. Check email đã tồn tại chưa
            User existingUser = findByEmail(request.getEmail());
            if (existingUser != null) {
                return false;
            }

            // 2. Tạo User mới
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setEmail(request.getEmail());

            // Mã hóa mật khẩu
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            newUser.setPassword(encodedPassword);
            newUser.setRole(request.getRole() != null ? request.getRole() : "candidate");
            newUser.setRememberToken(null);

            // 3. Lưu User
            session.save(newUser);

            // 4. Tạo Candidate tương ứng
            Candidate candidate = new Candidate();
            candidate.setUser(newUser);
            candidate.setName(newUser.getUsername());
            candidate.setEmail(newUser.getEmail());
            candidate.setStatus("active");

            // 5. Lưu Candidate
            session.save(candidate);

            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }


    @Override
    public void save(User user) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(user);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public void saveResetToken(String email, String token) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User user = session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();

            if (user != null) {
                user.setResetToken(token);
                user.setTokenExpiry(new Date(System.currentTimeMillis() + 3600_000)); // 1 giờ sau
                session.update(user);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

    @Override
    public User findByResetToken(String token) {
        try (Session session = sessionFactory.openSession()) {
            User user = session.createQuery("FROM User WHERE resetToken = :token", User.class)
                    .setParameter("token", token)
                    .uniqueResult();

            if (user != null) {
                Date now = new Date();
                if (user.getTokenExpiry() != null && user.getTokenExpiry().after(now)) {
                    return user;
                }
            }

            return null;
        }
    }

    @Override
    public boolean resetPassword(String token, String password) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            User user = session.createQuery("FROM User WHERE resetToken = :token", User.class)
                    .setParameter("token", token)
                    .uniqueResult();

            if (user == null || user.getTokenExpiry() == null || user.getTokenExpiry().before(new Date())) {
                return false; // Token không hợp lệ hoặc hết hạn
            }

            // Mã hóa mật khẩu mới
            String encodedPassword = passwordEncoder.encode(password);
            user.setPassword(encodedPassword);
            user.setResetToken(null); // Xóa token reset sau khi đổi mật khẩu
            user.setTokenExpiry(null); // Xóa thời gian hết hạn

            session.saveOrUpdate(user);
            session.flush();
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw e;
        }
    }

}
