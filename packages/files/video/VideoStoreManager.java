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
    private final Path destination;
    private  OutputStream  videoOutputStream; 

    public VideoStoreManager(InputStream videoStream, String videoName, OutputStream videooOutputStream){
        this.videoStream = videoStream;

        this.destination  = java.nio.file.Paths.get(Paths.VIDEO_STORAGE + "/" + videoName);

        this.videoOutputStream = videooOutputStream;
    }

    public void saveFile() throws IOException {
        
        Files.createDirectories(destination.getParent());

        try (OutputStream out = Files.newOutputStream(destination)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = videoStream.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    public void readVideo() throws IOException {
        try(InputStream in = Files.newInputStream(destination)){
            byte[] buffer = new byte[8192];

            int bytesRead;

            while((bytesRead = in.read(buffer)) != -1){
                videoOutputStream.write(buffer,0,bytesRead);
            }
        }

    }
}