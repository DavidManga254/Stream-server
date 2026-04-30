package packages.Http;

import com.sun.net.httpserver.HttpExchange;

public class ProcessRequest {
    
    public String getPathParameter(HttpExchange exchange, int position){
        String path = exchange.getRequestURI().getPath();
    
        // split by "/" and grab the last segment
        String[] segments = path.split("/");
        String id = segments[position -1]; // "123"

        return id;
    }
}
