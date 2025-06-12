package com.data.ra.repository;

import com.data.ra.entity.auth.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AuthRepositoryImpl implements AuthRepository {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public User findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE email = :email", User.class)
                    .setParameter("email", email)
                    .uniqueResult();
        }
    }

    @Override
    public User login(String email, String password) {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User WHERE email = :email AND password = :password", User.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .uniqueResult();
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
}
