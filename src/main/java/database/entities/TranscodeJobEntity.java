package database.entities;

import java.time.LocalDateTime;

import enums.TranscodeJobStatus;
import enums.VideoResolution;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;


@Entity
@Table(name = "transcode_jobs")
public class TranscodeJobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "videoId", nullable = false)
    private String videoId;

    @Enumerated(EnumType.STRING) 
    @Column(name = "resolution", nullable = false)
    private VideoResolution resolution;

    @Enumerated(EnumType.STRING) 
    @Column(name = "status", nullable = false)
    private TranscodeJobStatus videoStatus;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreRemove
    protected void onDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public TranscodeJobEntity(){}

    public TranscodeJobEntity(String videoId, VideoResolution resolution, TranscodeJobStatus videoStatus){
        this.videoId = videoId;
        this.resolution = resolution;
        this.videoStatus = videoStatus;
    }

    public record TranscodeJobEntityInfo(
        String videoId, 
        VideoResolution resolution, 
        TranscodeJobStatus videoStatus
    ) {}

    public TranscodeJobEntityInfo getJobInfo(){
        return new TranscodeJobEntityInfo(videoId, resolution, videoStatus);
    }
}
