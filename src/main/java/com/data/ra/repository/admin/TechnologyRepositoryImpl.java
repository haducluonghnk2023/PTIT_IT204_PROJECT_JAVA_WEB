package com.data.ra.repository.admin;

import com.data.ra.entity.admin.Technology;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TechnologyRepositoryImpl implements TechnologyRepository {

    @Autowired
    private SessionFactory sessionFactory;

    private Session getSession() {
        return sessionFactory.openSession();
    }

    @Override
    public List<Technology> findAll(int page, int size, String search) {
        try (Session session = getSession()) {
            String hql = "FROM Technology t WHERE t.is_deleted = false";
            if (search != null && !search.trim().isEmpty()) {
                hql += " AND t.name LIKE :search";
            }

            Query<Technology> query = session.createQuery(hql, Technology.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size);

            if (search != null && !search.trim().isEmpty()) {
                query.setParameter("search", "%" + search.trim() + "%");
            }

            return query.getResultList();
        }
    }

    @Override
    public List<Technology> findAlls() {
        try (Session session = getSession()) {
            String hql = "FROM Technology t WHERE t.is_deleted = false";
            return session.createQuery(hql, Technology.class).getResultList();
        }
    }

    @Override
    public long count(String search) {
        try (Session session = getSession()) {
            String hql = "SELECT COUNT(t) FROM Technology t WHERE t.is_deleted = false";
            if (search != null && !search.trim().isEmpty()) {
                hql += " AND t.name LIKE :search";
            }

            Query<Long> query = session.createQuery(hql, Long.class);
            if (search != null && !search.trim().isEmpty()) {
                query.setParameter("search", "%" + search.trim() + "%");
            }

            return query.uniqueResult();
        }
    }

    @Override
    public Technology findById(Long id) {
        try (Session session = getSession()) {
            return session.get(Technology.class, id);
        }
    }

    @Override
    public Technology findByName(String name) {
        try (Session session = getSession()) {
            return session.createQuery(
                            "FROM Technology t WHERE t.name = :name AND t.is_deleted = false", Technology.class)
                    .setParameter("name", name.trim())
                    .uniqueResult();
        }
    }

    @Override
    public List<Technology> findByIds(List<Long> ids) {
        try (Session session = getSession()) {
            return session.createQuery("FROM Technology t WHERE t.id IN :ids AND t.is_deleted = false", Technology.class)
                    .setParameterList("ids", ids)
                    .getResultList();
        }
    }

    @Override
    public void save(Technology technology) {
        Session session = getSession();
        session.beginTransaction();

        try {
            String originalName = technology.getName().trim();

            // 1. Kiểm tra trùng tên (is_deleted = false)
            Technology existingActive = (Technology) session
                    .createQuery("FROM Technology WHERE name = :name AND is_deleted = false")
                    .setParameter("name", originalName)
                    .uniqueResult();

            if (existingActive != null && (technology.getId() == null || !existingActive.getId().equals(technology.getId()))) {
                throw new RuntimeException("Technology with name '" + originalName + "' already exists.");
            }

            // 2. Tìm bản ghi xóa mềm trùng tên
            String deletedName = originalName + "_deleted";
            Technology deleted = (Technology) session
                    .createQuery("FROM Technology WHERE name = :deletedName AND is_deleted = true")
                    .setParameter("deletedName", deletedName)
                    .uniqueResult();

            if (deleted != null) {
                // Khôi phục bản ghi cũ
                deleted.setName(originalName);
                deleted.setIs_deleted(false);
                session.update(deleted);
            } else {
                // Thêm hoặc cập nhật
                session.saveOrUpdate(technology);
            }

            session.getTransaction().commit();
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        Session session = getSession();
        session.beginTransaction();

        try {
            Technology technology = session.get(Technology.class, id);

            if (technology != null) {
                boolean hasRelation = (technology.getCandidates() != null && !technology.getCandidates().isEmpty()) ||
                        (technology.getRecruitmentPositions() != null && !technology.getRecruitmentPositions().isEmpty());

                if (hasRelation) {
                    technology.setName(technology.getName() + "_deleted");
                    technology.setIs_deleted(true);
                    session.update(technology);
                } else {
                    session.delete(technology);
                }
            }

            session.getTransaction().commit();
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }
}
