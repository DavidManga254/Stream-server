package constants;

public final class Paths {
    private Paths(){

    }

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
}
