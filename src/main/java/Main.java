import Http.Server;
import database.DbConnection;
import workflows.TranscodeWorkflow;

public class Main {
    public static void main(String[] args){
        //init db
        DbConnection.init();

        //Start transcoding service
        TranscodeWorkflow.resetStuckJobs();
        TranscodeWorkflow.startWorkers();

        //start server
        new Server();
        Server.startServer();

        
        //Run on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.err.println("shutting down server");
            //Shut down transcoding workers 
            TranscodeWorkflow.shutdownWorkers();

            //close db connection
            DbConnection.shutDownConnection();

            //shut server 
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