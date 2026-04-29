package Http.Routes;

import com.sun.net.httpserver.HttpServer;

import Http.handler.Hello.HelloHandler;

public class Routes {

    private final HttpServer server;

    public Routes(HttpServer server){
        this.server = server;
    } 

    //Add api Routes here 

    public void mapRoutesToServer(){
        server.createContext("/hello-test",new HelloHandler());
    }
}
