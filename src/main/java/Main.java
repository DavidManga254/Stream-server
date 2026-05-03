import Http.Server;
import database.DbConnection;

public class Main {
    public static void main(String[] args){
        //init db
        DbConnection.init();

        //start server
        new Server();
        Server.startServer();


        //Run on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.err.println("shutting down server");

            DbConnection.shutDownConnection();

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