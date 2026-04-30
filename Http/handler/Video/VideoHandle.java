package Http.handler.Video;

import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import Http.services.VideoService.VideoService;
import enums.HttpMethod;
import packages.Http.RequestRouter;

public class VideoHandle implements HttpHandler {
    private final HashMap<HttpMethod, Consumer<HttpExchange>> methodHandlers = new HashMap<>();

    public VideoHandle(){
        methodHandlers.put(HttpMethod.POST, exchange -> this.postHandler(exchange));
    }
    
    @Override
    public void handle(HttpExchange exchange){
        new RequestRouter(methodHandlers, exchange);
    }

    private void postHandler(HttpExchange exchange){
        new VideoService(exchange).storeVideo();
    }
    
}
