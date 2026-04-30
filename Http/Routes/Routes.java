package Http.Routes;

import com.sun.net.httpserver.HttpServer;

import Http.handler.Hello.HelloHandler;
import Http.handler.Video.VideoHandle;

public class Routes {

    private final HttpServer server;

    public Routes(HttpServer server){
        this.server = server;
    } 

    //Add api Routes here 

    public void mapRoutesToServer(){
        server.createContext("/hello-test",new HelloHandler());

        //video
        server.createContext("/video/upload", new VideoHandle());
        server.createContext("/video/", new VideoHandle());
    }
}
