package com.data.ra.repository.candidate;

import com.data.ra.entity.admin.RecruitmentPosition;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public class RecruitmentPositionRepositoryImpl implements  RecruitmentPositionRepository {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<RecruitmentPosition> findAlls(int page, int size) {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "SELECT DISTINCT rp FROM RecruitmentPosition rp " +
                                    "JOIN rp.technologies t " +
                                    "WHERE t.name NOT LIKE :deletedPattern " +
                                    "AND NOT EXISTS (" +
                                    "    SELECT 1 FROM RecruitmentPosition rp2 " +
                                    "    JOIN rp2.technologies t2 " +
                                    "    WHERE rp2.id = rp.id AND t2.name LIKE :deletedPattern" +
                                    ")",
                            RecruitmentPosition.class)
                    .setParameter("deletedPattern", "%_deleted")
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();
        } finally {
            session.close();
        }
    }


    @Override
    public long countAll() {
        Session session = sessionFactory.openSession();
        try {
            return (Long) session.createQuery("SELECT COUNT(rp) FROM RecruitmentPosition rp").uniqueResult();
        } finally {
            session.close();
        }
    }

    @Override
    public List<RecruitmentPosition> findAll() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "SELECT DISTINCT rp FROM RecruitmentPosition rp " +
                                    "JOIN rp.technologies t " +
                                    "WHERE rp.name NOT LIKE :deletedPattern " +
                                    "AND NOT EXISTS (" +
                                    "    SELECT 1 FROM RecruitmentPosition rp2 " +
                                    "    JOIN rp2.technologies t2 " +
                                    "    WHERE rp2.id = rp.id AND t2.name LIKE :deletedPattern" +
                                    ")",
                            RecruitmentPosition.class)
                    .setParameter("deletedPattern", "%_deleted")
                    .list();
        } finally {
            session.close();
        }
    }

    @Override
    public List<RecruitmentPosition> findByNameContainingIgnoreCase(String name) {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery("FROM RecruitmentPosition WHERE LOWER(name) LIKE LOWER(:name)", RecruitmentPosition.class)
                    .setParameter("name", "%" + name + "%")
                    .list();
        } finally {
            session.close();
        }
    }

    @Override
    public List<RecruitmentPosition> filterByLocationTypeAndKeyword(String location, String type, String keyword) {
        Session session = sessionFactory.openSession();
        try {
            StringBuilder hql = new StringBuilder("FROM RecruitmentPosition WHERE 1=1");
            if (location != null && !location.isEmpty()) {
                hql.append(" AND LOWER(location) = LOWER(:location)");
            }
            if (type != null && !type.isEmpty()) {
                hql.append(" AND LOWER(type) = LOWER(:type)");
            }
            if (keyword != null && !keyword.isEmpty()) {
                hql.append(" AND LOWER(name) LIKE LOWER(:keyword)");
            }

            Query query = session.createQuery(hql.toString(), RecruitmentPosition.class);
            if (location != null && !location.isEmpty()) {
                query.setParameter("location", location);
            }
            if (type != null && !type.isEmpty()) {
                query.setParameter("type", type);
            }
            if (keyword != null && !keyword.isEmpty()) {
                query.setParameter("keyword", "%" + keyword + "%");
            }

            return query.list();
        } finally {
            session.close();
        }
    }

    @Override
    public RecruitmentPosition findById(Long id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(RecruitmentPosition.class, id);
        } finally {
            session.close();
        }
    }

    @Override
    public void save(RecruitmentPosition position) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            session.saveOrUpdate(position);
            session.getTransaction().commit();
        } catch (Exception e) {
            if (session.getTransaction() != null) {
                session.getTransaction().rollback();
            }
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            RecruitmentPosition position = session.get(RecruitmentPosition.class, id);
            if (position != null) {
                session.delete(position); // cố gắng xóa
            }

            tx.commit();
        } catch (Exception ex) {
            // Nếu bị lỗi do ràng buộc khóa ngoại (FK), thì cập nhật tên
            if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
                if (tx != null) tx.rollback();

                // mở lại transaction để update tên
                Transaction tx2 = session.beginTransaction();
                RecruitmentPosition position = session.get(RecruitmentPosition.class, id);

                if (position != null && !position.getName().endsWith("_deleted")) {
                    position.setName(position.getName() + "_deleted");
                    session.update(position);
                }

                tx2.commit();
            } else {
                if (tx != null) tx.rollback();
                throw ex; // rethrow lỗi không phải Constraint
            }
        } finally {
            session.close();
        }
    }

    @Override
    public List<RecruitmentPosition> searchByName(String keyword, int page, int size) {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "FROM RecruitmentPosition r WHERE LOWER(r.name) LIKE LOWER(:keyword) AND r.name NOT LIKE :deletedPattern",
                            RecruitmentPosition.class)
                    .setParameter("keyword", "%" + keyword + "%")
                    .setParameter("deletedPattern", "%_deleted")
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();
        } finally {
            session.close();
        }
    }

    @Override
    public long countSearchByName(String keyword) {
    Session session = sessionFactory.openSession();
        try {
            return (Long) session.createQuery(
                            "SELECT COUNT(r) FROM RecruitmentPosition r WHERE LOWER(r.name) LIKE LOWER(:keyword) AND r.name NOT LIKE :deletedPattern")
                    .setParameter("keyword", "%" + keyword + "%")
                    .setParameter("deletedPattern", "%_deleted")
                    .uniqueResult();
        } finally {
            session.close();
        }
    }

    @Override
    public RecruitmentPosition findByName(String name) {
    Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "FROM RecruitmentPosition r WHERE LOWER(r.name) = LOWER(:name) AND r.name NOT LIKE :deletedPattern",
                            RecruitmentPosition.class)
                    .setParameter("name", name)
                    .setParameter("deletedPattern", "%_deleted")
                    .uniqueResult();
        } finally {
            session.close();
        }
    }


}
