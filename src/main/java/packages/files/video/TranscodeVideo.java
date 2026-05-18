package packages.files.video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import constants.Paths;
import constants.VideoScale;
import enums.VideoResolution;

public class TranscodeVideo {
    private final String videoId;
    private final VideoResolution resolution;
    private String outputPath;
    // private final String VIDEO_480p;
    // private final String VIDEO_360p;
    
    public TranscodeVideo(String videoId, VideoResolution resolution){
        this.videoId = videoId;
        this.resolution = resolution;

        switch(resolution){
            case RESOLUTION_360:
                this.outputPath = Paths.streamPath360(videoId);
                break;
            
            case RESOLUTION_480P:
                this.outputPath = Paths.streamPath480(videoId);
                break;

            case RESOLUTION_720P:
                this.outputPath = Paths.streamPath720(videoId);
                break;

            default:
                this.outputPath = null;
        }
    }

    public void transcode() throws IOException {
        String baseUrl = System.getenv("BASE_URL");
        Path destination = java.nio.file.Paths.get(outputPath);
        Files.createDirectories(destination);

        String inputFile = Paths.VIDEO_STORAGE + "/" + videoId + ".mp4";

        String scale = switch(resolution) {
            case RESOLUTION_720P -> VideoScale.RESOLUTION_720P;
            case RESOLUTION_480P -> VideoScale.RESOLUTION_480P;
            case RESOLUTION_360  -> VideoScale.RESOLUTION_360P;
            default              -> VideoScale.RESOLUTION_720P;
        };

        String resolutionLabel = switch(resolution) {
            case RESOLUTION_720P -> "720";
            case RESOLUTION_480P -> "480";
            case RESOLUTION_360  -> "360";
            default              -> "720";
        };

        String segmentFilename = outputPath + "/%03d.ts";
        String playlistFile    = outputPath + "/playlist.m3u8";

        String hlsBaseUrl = baseUrl + "/streams/watch/" + videoId + "/" + resolutionLabel + "/";

        ProcessBuilder pb = new ProcessBuilder(
            "ffmpeg",
            "-i", inputFile,
            "-map", "0:v", "-map", "0:a",
            "-vf", "scale=" + scale,
            "-c:v", "libx264",
            "-hls_time", "10",
            "-hls_playlist_type", "vod",
            "-hls_flags", "independent_segments",
            "-hls_base_url", hlsBaseUrl,  
            "-hls_segment_filename", segmentFilename,
            playlistFile
        );

        pb.inheritIO();
        Process process = pb.start();

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException("Failed to complete transcoding");
        }
    }
}

// ffmpeg -i storage/videos/20260430230725.mp4 -map 0:v -map 0:a -vf scale=1280:720 -c:v libx264 -hls_time 10 -hls_playlist_type vod -hls_segment_filename storage/stream/20260430230725/720/720p_%03d.ts storage/stream/20260430230725/720/720p.m3u8 -map 0:v -map 0:a -vf scale=854:480 -c:v libx264 -hls_time 10 -hls_playlist_type vod -hls_segment_filename storage/stream/20260430230725/480/480p_%03d.ts storage/stream/20260430230725/480/480p.m3u8 -map 0:v -map 0:a -vf scale=640:360 -c:v libx264 -hls_time 10 -hls_playlist_type vod -hls_segment_filename storage/stream/20260430230725/360/360p_%03d.ts storage/stream/20260430230725/360/360p.m3u8