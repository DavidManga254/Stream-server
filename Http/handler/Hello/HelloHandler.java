package Http.handler.Hello;

import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import enums.HttpMethod;
import enums.HttpStatus;
import packages.Http.RequestRouter;
import packages.Http.ResponseWriter;

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

        new ResponseWriter<String>(exchange, HttpStatus.OK, null, "Hello");
    
    }
    
}
