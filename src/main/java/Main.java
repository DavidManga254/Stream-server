import Http.Server;

public class Main {
    public static void main(String[] args){
        //start server
        new Server();
        Server.startServer();


        //Run on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.err.println("shutting down server");

            Server.stopServer();
        }));

        // keep program alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.err.println("error  keeping server alive");
            e.printStackTrace();
        }
    }
}