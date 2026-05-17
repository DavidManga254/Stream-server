package constants;

import enums.VideoResolution;

public final class Paths {
    private Paths(){}

    private static final String ROOT_PATH = "storage/";
    public static final String VIDEO_STORAGE = ROOT_PATH + "videos";
    public static final String STREAM_VIDEO_STORAGE = ROOT_PATH + "stream";
    
    public static String streamPath720(String videoId){
        return STREAM_VIDEO_STORAGE + "/" + videoId + "/720";
    }

    public static String streamPath480(String videoId){
        return STREAM_VIDEO_STORAGE + "/" + videoId + "/480";
    }

    public static String streamPath360(String videoId){
        return STREAM_VIDEO_STORAGE + "/" + videoId + "/360";
    }

    public static String getMasterPlaylist(String videoId){
        return STREAM_VIDEO_STORAGE + "/" + videoId + "/master.m3u8"; 
    }

    public static String getQualityPlaylist(String videoId,VideoResolution resolution){
        String quality = switch(resolution){
            case RESOLUTION_360 -> "360";
            case RESOLUTION_480P -> "480";
            case RESOLUTION_720P -> "720";
            default -> null;
        };

        return STREAM_VIDEO_STORAGE + "/" + videoId + "/" + quality + "/playlist.m3u8"; 
    }

    public static String getSegmentPath(String videoId,VideoResolution resolution, String segment){
        String quality = switch(resolution){
            case RESOLUTION_360 -> "360";
            case RESOLUTION_480P -> "480";
            case RESOLUTION_720P -> "720";
            default -> null;
        };

        return STREAM_VIDEO_STORAGE + "/" + videoId + "/" + quality + "/" + segment; 
    }
}
