package Http.handler.Hello;

import java.io.IOException;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class HelloHandler implements HttpHandler{
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "Hello!";
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
    
}
