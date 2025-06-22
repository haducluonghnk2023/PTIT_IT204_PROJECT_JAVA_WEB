package com.data.ra.repository.admin;

import com.data.ra.entity.candidate.Candidate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Repository
public class CandidateRepositoryImpl implements CandidateRepository {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<Candidate> findAllCandidates(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Candidate";
            return session.createQuery(hql, Candidate.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();
        }
    }

    @Override
    @Transactional
    public void updateStatusById(Long id, boolean status) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Candidate candidate = session.get(Candidate.class, id.intValue());
            if (candidate != null) {
                candidate.setStatus(status ? "Active" : "Deactive");
                session.update(candidate);
            }
            session.getTransaction().commit();
        }
    }

    @Override
    @Transactional
    public String resetPasswordById(Long id) {
        String newPassword = generateRandomPassword();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            Candidate candidate = session.get(Candidate.class, id.intValue());
            if (candidate != null) {
                candidate.setPassword(newPassword);
                session.update(candidate);
            }
            session.getTransaction().commit();
        }
        return newPassword;
    }

    @Override
    public List<Candidate> filterCandidates(String name, Integer experience, int minAge, int maxAge, String gender, String technology, int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder("SELECT DISTINCT c FROM Candidate c LEFT JOIN c.technologies t WHERE 1=1");

            if (name != null && !name.isEmpty()) {
                hql.append(" AND LOWER(c.name) LIKE :name");
            }
            if (experience != null) {
                hql.append(" AND c.experience >= :experience");
            }
            if (minAge > 0 && maxAge > 0) {
                hql.append(" AND FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dob) BETWEEN :minAge AND :maxAge");
            }
            if (gender != null && !gender.isEmpty()) {
                hql.append(" AND c.gender = :gender");
            }
            if (technology != null && !technology.isEmpty()) {
                hql.append(" AND LOWER(t.name) LIKE :technology");
            }

            Query<Candidate> query = session.createQuery(hql.toString(), Candidate.class);

            if (name != null && !name.isEmpty()) {
                query.setParameter("name", "%" + name.toLowerCase() + "%");
            }
            if (experience != null) {
                query.setParameter("experience", experience);
            }
            if (minAge > 0 && maxAge > 0) {
                query.setParameter("minAge", minAge);
                query.setParameter("maxAge", maxAge);
            }
            if (gender != null && !gender.isEmpty()) {
                query.setParameter("gender", gender);
            }
            if (technology != null && !technology.isEmpty()) {
                query.setParameter("technology", "%" + technology.toLowerCase() + "%");
            }

            return query.setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();
        }
    }

    @Override
    public Optional<Candidate> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Candidate candidate = session.get(Candidate.class, id);
            return Optional.ofNullable(candidate);
        }
    }

    @Override
    public Candidate findByPhone(String phone) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Candidate c WHERE c.phone = :phone";
            Query<Candidate> query = session.createQuery(hql, Candidate.class);
            query.setParameter("phone", phone);

            List<Candidate> results = query.setMaxResults(1).list();
            return results.isEmpty() ? null : results.get(0);
        }
    }


    @Override
    public int countFilteredCandidates(String name, Integer experience, int minAge, int maxAge, String gender, String technology) {
        try (Session session = sessionFactory.openSession()) {
            StringBuilder hql = new StringBuilder("SELECT COUNT(DISTINCT c) FROM Candidate c LEFT JOIN c.technologies t WHERE 1=1");

            if (name != null && !name.isEmpty()) {
                hql.append(" AND LOWER(c.name) LIKE :name");
            }
            if (experience != null) {
                hql.append(" AND c.experience >= :experience");
            }
            if (minAge > 0 && maxAge > 0) {
                hql.append(" AND FUNCTION('YEAR', CURRENT_DATE) - FUNCTION('YEAR', c.dob) BETWEEN :minAge AND :maxAge");
            }
            if (gender != null && !gender.isEmpty()) {
                hql.append(" AND c.gender = :gender");
            }
            if (technology != null && !technology.isEmpty()) {
                hql.append(" AND LOWER(t.name) LIKE :technology");
            }

            Query<Long> query = session.createQuery(hql.toString(), Long.class);

            if (name != null && !name.isEmpty()) {
                query.setParameter("name", "%" + name.toLowerCase() + "%");
            }
            if (experience != null) {
                query.setParameter("experience", experience);
            }
            if (minAge > 0 && maxAge > 0) {
                query.setParameter("minAge", minAge);
                query.setParameter("maxAge", maxAge);
            }
            if (gender != null && !gender.isEmpty()) {
                query.setParameter("gender", gender);
            }
            if (technology != null && !technology.isEmpty()) {
                query.setParameter("technology", "%" + technology.toLowerCase() + "%");
            }

            Long count = query.uniqueResult();
            return count != null ? count.intValue() : 0;
        }
    }

    @Override
    public Candidate findByUserId(Integer userId) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Candidate c WHERE c.user.id = :userId";
            Query<Candidate> query = session.createQuery(hql, Candidate.class);
            query.setParameter("userId", userId);
            return query.uniqueResult();
        }
    }

    @Override
    public void save(Candidate candidate) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(candidate);
            session.getTransaction().commit();
        }
    }

    @Override
    public Long countAll() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(c) FROM Candidate c";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        }
    }

    private String generateRandomPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
