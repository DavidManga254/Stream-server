package packages.Http;

import java.util.HashMap;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;

public class RequestRouter {
    private final HashMap<String, Consumer<HttpExchange>> requestsMap;
    private final HttpExchange exchange;
    
    public RequestRouter(HashMap<String, Consumer<HttpExchange>> requestMap, HttpExchange exchange){
        this.requestsMap = requestMap;
        this.exchange = exchange;

        this.resolve();
    }

    private void resolve(){
        //exchange.getRequestMethod tells the request method :: GET/POST/PUT/UPDATE etc
        String requestType = this.exchange.getRequestMethod();
        
        Consumer<HttpExchange> methodToRun = requestsMap.get(requestType);

        if(methodToRun != null){
            methodToRun.accept(this.exchange);
        } else {
            try {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                exchange.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
