package database.Repositories;

import java.util.List;

import database.DbConnection;
import database.entities.TranscodeJobEntity;
import enums.TranscodeJobStatus;
import enums.VideoResolution;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class TranscodeJobRepository {

    public static void storeJob(String videoId, VideoResolution resolution) {
        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(new TranscodeJobEntity(videoId, resolution, TranscodeJobStatus.PENDING));
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
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

    public static void updateJobStatus(String videoId, VideoResolution resolution, TranscodeJobStatus status) {
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
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}