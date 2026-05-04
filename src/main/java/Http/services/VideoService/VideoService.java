package Http.services.VideoService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.UUID;

import com.sun.net.httpserver.HttpExchange;

import enums.HttpStatus;
import packages.Http.ResponseWriter;
import packages.files.video.VideoStoreManager;
import workflows.TranscodeWorkflow;

public class VideoService {
    private final HttpExchange exchange;
    final LocalDateTime now;
    private final String videoName;
    private final String id;
    HashMap<String,String> responseData = new HashMap<>();


    public VideoService(HttpExchange exchange){
        this.exchange = exchange;

        this.now = LocalDateTime.now();

        this.id = UUID.randomUUID().toString();

        this.videoName = id + ".mp4";
    }

    public void storeVideo(){
        //get video bytes 
        try {

            VideoStoreManager videoStoreManager = new VideoStoreManager(
                exchange.getRequestBody(), videoName
            );

            videoStoreManager.saveFile();

            responseData.put("name", videoName);

            //store records 
            TranscodeWorkflow.storeVideoAndJobsRecords(id);

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
