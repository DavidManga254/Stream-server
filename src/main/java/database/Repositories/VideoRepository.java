package database.Repositories;

import database.DbConnection;
import database.entities.VideoEntity;
import enums.VideoStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public class VideoRepository {
    

    public static VideoEntity.VideoRecordInfo getVideoRecord(String videoId) {
        EntityManager em = DbConnection.getDbConnection();
        try {
            VideoEntity video = em.createQuery(
                "SELECT v FROM VideoEntity v WHERE v.videoId = :videoId", VideoEntity.class)
                .setParameter("videoId", videoId)
                .getSingleResult();
            return video.getVideoInfo();
        } finally {
            em.close();
        }
    }

    public static void storeVideoRecord(String videoId){
        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            em.persist(new VideoEntity(videoId,VideoStatus.PROCESSING));

            tx.commit();
            
        } catch (Exception e) {
            tx.rollback();

            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public static void updateVideoStatus(String videoId, VideoStatus status) {
        EntityManager em = DbConnection.getDbConnection();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            VideoEntity video = em.createQuery(
                "SELECT v FROM VideoEntity v WHERE v.videoId = :videoId", VideoEntity.class)
                .setParameter("videoId", videoId)
                .getSingleResult();
            video.setVideoStatus(status);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
