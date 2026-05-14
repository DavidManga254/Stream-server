package database.Repositories;

import java.util.List;

import database.DbConnection;
import database.entities.TranscodeJobEntity;
import database.entities.TranscodeJobEntity.TranscodeJobEntityInfo;
import enums.TranscodeJobStatus;
import enums.VideoResolution;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TranscodeJobRepository {

    public static void storeJobs(String videoId, List<VideoResolution> resolutions) {

        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            for (VideoResolution resolution : resolutions) {
                em.persist(new TranscodeJobEntity(
                        videoId,
                        resolution,
                        TranscodeJobStatus.PENDING
                ));
            }

            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public static List<TranscodeJobEntityInfo> getBatchPendingJobs(int limit) {
        EntityManager em = DbConnection.getDbConnection();

        try {
            return em.createQuery("""
                    SELECT new database.entities.TranscodeJobEntity$TranscodeJobEntityInfo(
                        t.videoId,
                        t.resolution,
                        t.transcodeJobStatus
                    )
                    FROM TranscodeJobEntity t
                    WHERE t.transcodeJobStatus = :status
                    """, TranscodeJobEntity.TranscodeJobEntityInfo.class)
                    .setParameter("status", TranscodeJobStatus.PENDING)
                    .setMaxResults(limit)
                    .getResultList();

        } finally {
            em.close();
        }
    }

    public static TranscodeJobEntity.TranscodeJobEntityInfo getJob(String videoId, VideoResolution resolution) {
        EntityManager em = DbConnection.getDbConnection();
        try {
            TranscodeJobEntity job = em.createQuery(
                "SELECT j FROM TranscodeJobEntity j WHERE j.videoId = :videoId AND j.resolution = :resolution",
                TranscodeJobEntity.class)
                .setParameter("videoId", videoId)
                .setParameter("resolution", resolution)
                .getSingleResult();
            return job.getJobInfo();
        } finally {
            em.close();
        }
    }

    public static List<TranscodeJobEntity.TranscodeJobEntityInfo> getJobsByVideoId(String videoId) {
        EntityManager em = DbConnection.getDbConnection();
        try {
            return em.createQuery(
                "SELECT j FROM TranscodeJobEntity j WHERE j.videoId = :videoId",
                TranscodeJobEntity.class)
                .setParameter("videoId", videoId)
                .getResultList()
                .stream()
                .map(TranscodeJobEntity::getJobInfo)
                .toList();
        } finally {
            em.close();
        }
    }

    public static List<TranscodeJobEntity.TranscodeJobEntityInfo> getJobsByVideoIdAndStatus(String videoId, TranscodeJobStatus status) {
        EntityManager em = DbConnection.getDbConnection();
        try {
            return em.createQuery(
                "SELECT j FROM TranscodeJobEntity j WHERE j.videoId = :videoId AND j.transcodeJobStatus = :status",
                TranscodeJobEntity.class)
                .setParameter("videoId", videoId)
                .setParameter("status", status)
                .getResultList()
                .stream()
                .map(TranscodeJobEntity::getJobInfo)
                .toList();
        } finally {
            em.close();
        }
    }

    public static boolean updateJobStatus(String videoId, VideoResolution resolution, TranscodeJobStatus status) {
        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TranscodeJobEntity job = em.createQuery(
                "SELECT j FROM TranscodeJobEntity j WHERE j.videoId = :videoId AND j.resolution = :resolution",
                TranscodeJobEntity.class)
                .setParameter("videoId", videoId)
                .setParameter("resolution", resolution)
                .getSingleResult();
            job.setTranscodeJobStatus(status);
            tx.commit();
            return true;
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            em.close();
        }
    }

    public static TranscodeJobEntity.TranscodeJobEntityInfo getNextPendingJob() {
        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            List<TranscodeJobEntity> jobs = em.createQuery(
                "SELECT j FROM TranscodeJobEntity j WHERE j.transcodeJobStatus = :status",
                TranscodeJobEntity.class)
                .setParameter("status", TranscodeJobStatus.PENDING)
                .setMaxResults(1)
                .getResultList();

            if (jobs.isEmpty()) return null;

            TranscodeJobEntity job = jobs.get(0);
            job.setTranscodeJobStatus(TranscodeJobStatus.PROCESSING);
            tx.commit();
            return job.getJobInfo();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
            return null;
        } finally {
            em.close();
        }
    }

    public static void resetProcessingJobs() {
        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery(
                "UPDATE TranscodeJobEntity j SET j.transcodeJobStatus = :pending WHERE j.transcodeJobStatus = :processing")
                .setParameter("pending", TranscodeJobStatus.PENDING)
                .setParameter("processing", TranscodeJobStatus.PROCESSING)
                .executeUpdate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

}