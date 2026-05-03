package database.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import enums.VideoStatus;

@Entity
@Table(name = "videos")
public class VideoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "videoId", nullable = false)
    private String videoId;

    @Enumerated(EnumType.STRING) 
    @Column(name = "status", nullable = false)
    private VideoStatus videoStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreRemove
    protected void onDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public VideoEntity(){}


    public VideoEntity(String videoId, VideoStatus videoStatus) {
        this.videoId = videoId;
        this.videoStatus = videoStatus;
    }

    

    public record VideoRecordInfo(
        String videoId,
        VideoStatus videoStatus

    ){}

    public VideoRecordInfo getVideoInfo () {
        return new VideoRecordInfo(videoId, videoStatus);
    }

    public void setVideoStatus(VideoStatus status){
        this.videoStatus = status;

    }


    
}
