package Http.services.VideoService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import com.sun.net.httpserver.HttpExchange;

import enums.HttpStatus;
import packages.Http.ResponseWriter;
import packages.files.video.VideoStoreManager;

public class VideoService {
    private final HttpExchange exchange;
    final LocalDateTime now;
    private final String videoName;
    HashMap<String,String> responseData = new HashMap<>();


    public VideoService(HttpExchange exchange){
        this.exchange = exchange;

        this.now = LocalDateTime.now();

        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        this.videoName = timestamp + ".mp4";
    }

    public void storeVideo(){
        //get video bytes 
        try {

            VideoStoreManager videoStoreManager = new VideoStoreManager(
                exchange.getRequestBody(), videoName
            );

            videoStoreManager.saveFile();

            responseData.put("name", videoName);

            new ResponseWriter<HashMap<String,String>>(exchange,HttpStatus.OK,"video saved successfully",responseData);

            
        } catch (IOException e) {
            System.out.println("error reading video from request");

            e.printStackTrace();
        }
    }

    public void getVideo(String VideoName){
        //get request  range
        try {
            VideoStoreManager videoStoreManager = new VideoStoreManager(
                exchange.getRequestBody(), VideoName + ".mp4"
            );

            videoStoreManager.readVideo(exchange);

        } catch (IOException e) {
            System.out.println("error writing video to request");

            // new ResponseWriter<String>(exchange, HttpStatus.INTERNAL_SERVER_ERROR, "error returning video", null);

            e.printStackTrace();
        }
    }
}
