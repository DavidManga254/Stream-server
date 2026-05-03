package packages.files.video;

import java.nio.file.Files;
import java.nio.file.Path;

import constants.Paths;

public class TranscodeVideo {
    private final String videoId;

    private final String VIDEO_720p;
    private final String VIDEO_480p;
    private final String VIDEO_360p;
    
    public TranscodeVideo(String videoId){
        this.videoId = videoId;

        VIDEO_720p = Paths.streamPath720(videoId) + "/" + videoId;
        VIDEO_480p = Paths.streamPath480(videoId) + "/" + videoId;
        VIDEO_360p = Paths.streamPath360(videoId) + "/" + videoId;
    }

    public void transcode(){
        Path destination720 = java.nio.file.Paths.get(VIDEO_720p);
        Path destination480 = java.nio.file.Paths.get(VIDEO_480p);
        Path destination360 = java.nio.file.Paths.get(VIDEO_360p);

        // Files.createDirectories(destination720.getParent());
        // Files.createDirectories(destination480.getParent());
        // Files.createDirectories(destination360.getParent());


        // ProcessBuilder pb = new ProcessBuilder(null)

    }
}

// ffmpeg -i storage/videos/20260430230725.mp4 -map 0:v -map 0:a -vf scale=1280:720 -c:v libx264 -hls_time 10 -hls_playlist_type vod -hls_segment_filename storage/stream/20260430230725/720/720p_%03d.ts storage/stream/20260430230725/720/720p.m3u8 -map 0:v -map 0:a -vf scale=854:480 -c:v libx264 -hls_time 10 -hls_playlist_type vod -hls_segment_filename storage/stream/20260430230725/480/480p_%03d.ts storage/stream/20260430230725/480/480p.m3u8 -map 0:v -map 0:a -vf scale=640:360 -c:v libx264 -hls_time 10 -hls_playlist_type vod -hls_segment_filename storage/stream/20260430230725/360/360p_%03d.ts storage/stream/20260430230725/360/360p.m3u8