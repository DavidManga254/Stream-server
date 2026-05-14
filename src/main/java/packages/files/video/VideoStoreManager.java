package packages.files.video;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import constants.Paths;
import enums.HttpStatus;
import enums.VideoResolution;
import packages.Http.AddResponsePartialHeader;
import packages.Http.ResponseWriter;

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

    public static void serveMasterPlaylist(HttpExchange exchange, String videoId){
        Path masterFilePath = java.nio.file.Paths.get(Paths.getMasterPlaylist(videoId));

        streamPlaylistFile(exchange, masterFilePath);
    }

    public static void serveQualityPlaylist(HttpExchange exchange, String videoId, VideoResolution resolution){
        Path playListFile = java.nio.file.Paths.get(Paths.getQualityPlaylist(videoId, resolution));


        streamPlaylistFile(exchange, playListFile);
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

    private static void streamPlaylistFile(HttpExchange exchange, Path filePath){

        if (!Files.exists(filePath)){
            new ResponseWriter<>(exchange, HttpStatus.NOT_FOUND, "Requested playlist not found", null);

        }

        try {
            long fileSize = Files.size(filePath);

            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "application/vnd.apple.mpegurl");
            headers.set("Cache-Control", "no-cache");
            headers.set("Access-Control-Allow-Origin", "*");

            exchange.sendResponseHeaders(HttpStatus.OK.code, fileSize);


            
        } catch (IOException e) {
            new ResponseWriter<>(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Failed sending response headers", null);
        }

        
        try (InputStream in = Files.newInputStream(filePath)){
            
            OutputStream fileOutputStream = exchange.getResponseBody();

            byte[] buffer = new byte[8192];

            int bytesRead;

            while((bytesRead = in.read(buffer)) != -1){

                fileOutputStream.write(buffer,0,bytesRead);

            }

            fileOutputStream.close();

        } catch (IOException e){
            e.printStackTrace();

            new ResponseWriter<>(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "Error returning file", null);
        }
        
        
    }
}