package packages.Http;

import java.io.IOException;
import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;

import enums.HttpMethod;
import enums.HttpStatus;


//Responsible for running methods mapped to specific request methods (GET/POST/PUT......)
public class RequestRouter {
    private final HashMap<HttpMethod, Consumer<HttpExchange>> requestsMap;
    private final HttpExchange exchange;
    
    public RequestRouter(HashMap<HttpMethod, Consumer<HttpExchange>> requestMap, HttpExchange exchange){
        this.requestsMap = requestMap;
        this.exchange = exchange;

        this.resolve();
    }

    private void resolve(){

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Range");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            try {
                exchange.sendResponseHeaders(204, -1); 
                exchange.getResponseBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return; 
        }
        
        //exchange.getRequestMethod tells the request method :: GET/POST/PUT/UPDATE etc
        String requestType = this.exchange.getRequestMethod();
        
        Consumer<HttpExchange> methodToRun = requestsMap.get(HttpMethod.from(requestType));

        if(methodToRun != null){
            methodToRun.accept(this.exchange);
        } else {
            try {
                exchange.sendResponseHeaders(HttpStatus.METHOD_NOT_ALLOWED.code, -1); // Method Not Allowed
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
