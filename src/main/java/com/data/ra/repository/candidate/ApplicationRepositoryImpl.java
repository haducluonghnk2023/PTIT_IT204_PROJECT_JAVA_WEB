package com.data.ra.repository.candidate;

import com.data.ra.entity.admin.Application;
import com.data.ra.entity.admin.Progress;
import com.data.ra.entity.admin.RecruitmentPosition;
import com.data.ra.entity.candidate.Candidate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
@Repository
public class ApplicationRepositoryImpl implements  ApplicationRepository {
    @Autowired
    private SessionFactory sessionFactory;


    @Override
    public List<Application> findAll() {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "FROM Application a WHERE a.progress != :rejected", Application.class)
                    .setParameter("rejected", Progress.rejected)
                    .list();
        } finally {
            session.close();
        }
    }


    @Override
    public List<Application> findAllByUser(Integer candidateId) {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "FROM Application a WHERE a.candidate.id = :candidateId AND a.progress != :rejected",
                            Application.class)
                    .setParameter("candidateId", candidateId)
                    .setParameter("rejected", Progress.rejected)
                    .list();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Application> findAllByUsers(Integer candidateId, int page, int size) {
        Session session = sessionFactory.openSession();
        try {
            return session.createQuery(
                            "FROM Application a WHERE a.candidate.id = :candidateId AND a.progress != :rejected",
                            Application.class)
                    .setParameter("candidateId", candidateId)
                    .setParameter("rejected", Progress.rejected)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .list();
        } finally {
            session.close();
        }
    }

    @Override
    public int countAllByUser(Integer candidateId) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery(
                            "SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId AND a.progress != :rejected",
                            Long.class)
                    .setParameter("candidateId", candidateId)
                    .setParameter("rejected", Progress.rejected)
                    .uniqueResult();
            return count != null ? count.intValue() : 0;
        } finally {
            session.close();
        }
    }


    @Override
    public int countApplications() {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery("SELECT COUNT(a) FROM Application a", Long.class).uniqueResult();
            return count != null ? count.intValue() : 0;
        } finally {
            session.close();
        }
    }

    @Override
    public void save(Application application) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            // Nếu là mới thì set ngày tạo
            if (application.getId() == null) {
                application.setCreateAt(new Date());
            }

            // Luôn cập nhật thời gian cập nhật
            application.setUpdateAt(new Date());

            // Lưu hoặc cập nhật
            session.saveOrUpdate(application);

            // Commit giao dịch
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e; // ném lại exception để controller xử lý nếu cần
        } finally {
            session.close(); // đóng session trong mọi trường hợp
        }
    }

    @Override
    public void saveApp(Integer candidateId, Long recruitmentId, String cvUrl) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            // Tạo mới ứng tuyển
            Application application = new Application();
            application.setCandidate(session.get(Candidate.class, candidateId));
            application.setRecruitmentPosition(session.get(RecruitmentPosition.class, recruitmentId));
            application.setCvUrl(cvUrl);
            application.setProgress(Progress.pending);
            application.setCreateAt(new Date());
            application.setUpdateAt(new Date());

            // Lưu ứng tuyển
            session.save(application);

            // Commit giao dịch
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e; // ném lại exception để controller xử lý nếu cần
        } finally {
            session.close(); // đóng session trong mọi trường hợp
        }
    }


    @Override
    public boolean existsByCandidateIdAndRecruitmentId(Integer candidateId, Long recruitmentId) {
        Session session = sessionFactory.openSession();
        try {
            Long count = session.createQuery("SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId AND a.recruitmentPosition.id = :recruitmentId", Long.class)
                    .setParameter("candidateId", candidateId)
                    .setParameter("recruitmentId", recruitmentId)
                    .uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }

    @Override
    public void markHandling(Integer id) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            Application application = session.get(Application.class, id);
            if (application == null) {
                throw new IllegalArgumentException("Application không tồn tại.");
            }

            // Kiểm tra nếu trạng thái không phải là 'pending' thì không cho cập nhật
            if (application.getProgress() != Progress.pending) {
                throw new IllegalStateException("Chỉ được chuyển trạng thái từ 'pending' sang 'handling'.");
            }

            application.setProgress(Progress.handling);
            application.setUpdateAt(new Date());

            session.update(application);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public void scheduleInterview(Integer id, Date interviewDate, String interviewLink) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            Application application = session.get(Application.class, id);
            if (application == null) {
                throw new IllegalArgumentException("Đơn ứng tuyển không tồn tại.");
            }

            // Kiểm tra nếu trạng thái không phải 'handling'
            if (application.getProgress() != Progress.handling) {
                throw new IllegalStateException("Chỉ được lên lịch phỏng vấn khi trạng thái là 'handling'.");
            }

            application.setInterviewTime(interviewDate);
            application.setInterviewLink(interviewLink);
            application.setUpdateAt(new Date());

            session.update(application);
            session.getTransaction().commit();
        } catch (IllegalArgumentException | IllegalStateException e) {
            session.getTransaction().rollback();
            throw e; // ném lại cho controller xử lý
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw new RuntimeException("Lỗi không xác định khi lên lịch phỏng vấn.", e);
        } finally {
            session.close();
        }
    }

    @Override
    public void approveInterview(Integer id) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            Application application = session.get(Application.class, id);
            if (application == null) {
                throw new IllegalArgumentException("Application không tồn tại.");
            }

            if (application.getProgress() != Progress.handling) {
                throw new IllegalStateException("Chỉ có thể duyệt phỏng vấn khi trạng thái là 'handling'.");
            }

            application.setProgress(Progress.interviewing);
            application.setUpdateAt(new Date());

            session.update(application);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public void saveInterviewResult(Integer id, String result, String resultNote) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            Application application = session.get(Application.class, id);
            if (application == null) {
                throw new IllegalArgumentException("Application không tồn tại.");
            }

            if (application.getProgress() != Progress.interviewing) {
                throw new IllegalStateException("Chỉ có thể lưu kết quả phỏng vấn khi trạng thái là 'interviewing'.");
            }

            application.setInterviewResult(result);
            application.setInterviewResultNote(resultNote);
            application.setProgress(Progress.done);
            application.setUpdateAt(new Date());

            session.update(application);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public void destroyInterview(Integer id,String reason) {
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();

            Application application = session.get(Application.class, id);
            if (application == null) {
                throw new IllegalArgumentException("Application không tồn tại.");
            }

            if (application.getProgress() != Progress.handling) {
                throw new IllegalStateException("Chỉ có thể hủy phỏng vấn khi trạng thái là 'handling'.");
            }

            application.setProgress(Progress.rejected); // cập nhật trạng thái thành 'rejected'
            application.setInterviewResultNote(reason); // lưu lý do hủy phỏng vấn
            application.setUpdateAt(new Date());

            session.update(application);
            session.getTransaction().commit();
        } catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public Application findById(Integer id) {
        Session session = sessionFactory.openSession();
        try {
            return session.get(Application.class, id);
        } finally {
            session.close();
        }
    }


}
