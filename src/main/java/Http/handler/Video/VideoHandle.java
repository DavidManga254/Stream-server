package Http.handler.Video;

import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import Http.services.VideoService.VideoService;
import enums.HttpMethod;
import packages.Http.ProcessRequest;
import packages.Http.RequestRouter;

public class VideoHandle implements HttpHandler {
    private final HashMap<HttpMethod, Consumer<HttpExchange>> methodHandlers = new HashMap<>();

    public VideoHandle(){
        methodHandlers.put(HttpMethod.POST, exchange -> this.postHandler(exchange));
        methodHandlers.put(HttpMethod.GET, exchange -> this.getHandler(exchange));
    }
    
    @Override
    public void handle(HttpExchange exchange){
        new RequestRouter(methodHandlers, exchange);
    }

    private void postHandler(HttpExchange exchange){
        new VideoService(exchange).storeVideo();
    }

    //GET: video/{id}
    private void getHandler(HttpExchange exchange){
        String videoId = new ProcessRequest().getPathParameter(exchange, 3);

        new VideoService(exchange).getMasterPlayList(videoId);

    }
    
}
