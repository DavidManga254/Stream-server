package packages.Http;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;


import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import enums.HttpStatus;

//Responsible for writing response for api
public class ResponseWriter<T> {
    private final HttpExchange exchange;
    private final HttpStatus responseHttpstatus;
    private T data;
    private String Message;
    
    public ResponseWriter(HttpExchange exchange, HttpStatus responseHttpstatus, String message, T data){
        this.exchange = exchange;
        this.responseHttpstatus = responseHttpstatus;
        this.data = data;
        this.Message = message;

        resolveResponse();
    }

    private void resolveResponse(){
        HashMap<String, Object> response = new HashMap<>();

        try {
            response.put("status", this.responseHttpstatus.code);

            if(this.Message != null){
                response.put("message", this.Message);
            }

            if(this.data != null){
                response.put("data", this.data);
            }

            Gson gson = new Gson();
            String json = gson.toJson(response);
            byte[] bytes = json.getBytes(StandardCharsets.UTF_8);


            //headers
            exchange.sendResponseHeaders(this.responseHttpstatus.code, bytes.length);

            //data
            exchange.getResponseBody().write(bytes);

            exchange.getResponseBody().close();
        } catch (IOException e) {
            //TODO i don't think this is ok, double try catch
            try {
                exchange.sendResponseHeaders(HttpStatus.INTERNAL_SERVER_ERROR.code, 0);

                response.clear();

                response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.code);
                response.put("message", "server error while returning response");
            } catch (Exception err) {
                err.printStackTrace();
            }
            e.printStackTrace();
        }
    }
}
