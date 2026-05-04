package enums;

import constants.VideoScale;

public enum VideoResolution {

    RESOLUTION_720P(VideoScale.RESOLUTION_720P, "1280x720", 1_400_000),
    RESOLUTION_480P(VideoScale.RESOLUTION_480P, "854x480", 1_200_000),
    RESOLUTION_360(VideoScale.RESOLUTION_360P, "640x360", 800_000);

    private final String ffmpegScale;
    private final String hlsResolution;
    private final int bitrate;

    VideoResolution(String ffmpegScale, String hlsResolution, int bitrate) {
        this.ffmpegScale = ffmpegScale;
        this.hlsResolution = hlsResolution;
        this.bitrate = bitrate;
    }

    public String getFfmpegScale() {
        return ffmpegScale;
    }

    public String getHlsResolution() {
        return hlsResolution;
    }

    public int getBitrate() {
        return bitrate;
    }

    public String getFolder() {
        return switch (this) {
            case RESOLUTION_720P -> "720p";
            case RESOLUTION_480P -> "480p";
            case RESOLUTION_360 -> "360p";
    };
}
}