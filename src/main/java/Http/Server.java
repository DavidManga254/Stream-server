package Http;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import Http.Routes.Routes;

public class Server {;
    private static final int PORT = 4221;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    
    private  static HttpServer server;

    public Server(){
        try {
            server = HttpServer.create(new InetSocketAddress(PORT),0);

            server.setExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE));

            //map the routes 
            Routes routes = new Routes(server);
            routes.mapRoutesToServer();
            
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    public static void startServer(){
        System.out.println("Server starting on port :: " + server.getAddress());
        server.start();    
    }

    public static void stopServer(){
        server.stop(0);
    }

    
}
