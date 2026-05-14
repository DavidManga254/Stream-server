package Http.handler.Video;

import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import Http.services.VideoService.VideoService;
import enums.HttpMethod;
import packages.Http.ProcessRequest;
import packages.Http.RequestRouter;

public class StreamHandle implements HttpHandler {
    private final HashMap<HttpMethod, Consumer<HttpExchange>> methodHandlers = new HashMap<>();

    public StreamHandle(){
        methodHandlers.put(HttpMethod.GET, exchange -> this.getPlaylistHandler(exchange));
    }
    
    @Override
    public void handle(HttpExchange exchange){
        new RequestRouter(methodHandlers, exchange);
    }

    // GET: /streams/{id}/{quality}
    private void getPlaylistHandler(HttpExchange exchange) {
        String id = new ProcessRequest().getPathParameter(exchange, 3);
        String quality = new ProcessRequest().getPathParameter(exchange, 4);


        new VideoService(exchange).getQualityPlaylist(id, quality);
    }

    // GET: /streams/{id}/{quality}/{segment}
    // private void getSegmentHandler(HttpExchange exchange) {
    //     String id = new ProcessRequest().getPathParameter(exchange, 2);
    //     String quality = new ProcessRequest().getPathParameter(exchange, 3);
    //     String segment = new ProcessRequest().getPathParameter(exchange, 4);

    //     new StreamService(exchange).getSegment(id, quality, segment);
    // }
    // private void getHandler(HttpExchange exchange){
    //     String videoId = new ProcessRequest().getPathParameter(exchange, 3);

    //     new VideoService(exchange).getVideo(videoId);

    // }
}
