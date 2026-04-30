package packages.files.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import constants.Paths;

public class VideoStoreManager {
    private final InputStream videoStream;
    private final String videoName;

    public VideoStoreManager(InputStream videoStream, String videoName){
        this.videoStream = videoStream;
        this.videoName = videoName;
    }

    public void saveFile() throws IOException {
        Path destination = java.nio.file.Paths.get(Paths.VIDEO_STORAGE + "/" + videoName);
        Files.createDirectories(destination.getParent());

        try (OutputStream out = Files.newOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = videoStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
}