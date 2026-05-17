package Http.handler.Video;

import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import Http.services.VideoService.VideoService;
import enums.HttpMethod;
import packages.Http.ProcessRequest;
import packages.Http.RequestRouter;

public class StreamSegmentHandle implements HttpHandler {
    private final HashMap<HttpMethod, Consumer<HttpExchange>> methodHandlers = new HashMap<>();

    public StreamSegmentHandle(){
        methodHandlers.put(HttpMethod.GET, exchange -> this.getSegment(exchange));
    }
    
    @Override
    public void handle(HttpExchange exchange){
        new RequestRouter(methodHandlers, exchange);
    }

    // GET: /streams/watch/{id}/{quality}/{segment}
    private void getSegment(HttpExchange exchange) {
        String id = new ProcessRequest().getPathParameter(exchange, 4);
        String quality = new ProcessRequest().getPathParameter(exchange, 5);
        String segment = new ProcessRequest().getPathParameter(exchange, 6);

        System.err.println(id + " " + quality + " " + segment);

        new VideoService(exchange).getSegmentStream(id, quality, segment);
    }
    
}
