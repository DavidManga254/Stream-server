package packages.files.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.HttpExchange;

import constants.Paths;
import packages.Http.AddResponsePartialHeader;

public class VideoStoreManager {
    private final InputStream videoStream;
    private final Path destination;

    public VideoStoreManager(InputStream videoStream, String videoName){
        this.videoStream = videoStream;

        this.destination  = java.nio.file.Paths.get(Paths.VIDEO_STORAGE + "/" + videoName);
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

    public void readVideo(HttpExchange exchange) throws IOException {
        

        long fileSize = Files.size(destination);

        AddResponsePartialHeader partialHeadermaker = new AddResponsePartialHeader(exchange, fileSize);

        AddResponsePartialHeader.RequestedBytesInfo requestedBytesInfo = partialHeadermaker.parseBytesHeader(exchange.getRequestHeaders().getFirst("Range"));

        long sizeToSend = requestedBytesInfo.end() - requestedBytesInfo.start() + 1;

        partialHeadermaker.addPartialHeaderInfo(sizeToSend, requestedBytesInfo.start(), requestedBytesInfo.end(), "video/mp4", requestedBytesInfo.end() == fileSize - 1);

        OutputStream videoOutputStream = exchange.getResponseBody();

        try(InputStream in = Files.newInputStream(destination)){

            in.skip(requestedBytesInfo.start());

            byte[] buffer = new byte[8192];


            //start at chunksize and go down as we send
            long remainToSend = sizeToSend;

            int bytesRead;

            while(remainToSend > 0 && (bytesRead = in.read(buffer,0, (int) Math.min(buffer.length, remainToSend))) != -1){
                try {
                    videoOutputStream.write(buffer, 0, bytesRead);
                    remainToSend -= bytesRead;
                } catch (IOException e) {
                    // client disconnected, stop trying to write
                    System.out.println("client disconnected mid stream, stopping");
                    return;
                }
            }
        }

        videoOutputStream.close();

    }
}