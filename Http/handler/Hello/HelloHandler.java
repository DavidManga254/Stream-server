package Http.handler.Hello;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import enums.HttpMethod;
import packages.Http.RequestRouter;

public class HelloHandler implements HttpHandler{
    private final HashMap<HttpMethod, Consumer<HttpExchange>> methodHandlers = new HashMap<>();

    //Add api routes here 
    public HelloHandler(){
        methodHandlers.put(HttpMethod.GET, exchange -> handleGet(exchange));

    }
    
    @Override
    public void handle(HttpExchange exchange){
        new RequestRouter(methodHandlers,exchange);
        
    }

    private void handleGet(HttpExchange exchange) {

        try {
            String response = "Hello!";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            
        } catch (Exception e) {
            // TODO: handle exception
            
        }
    }
    
}
