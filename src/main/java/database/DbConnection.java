package database;

import jakarta.persistence.EntityManagerFactory;

import java.util.HashMap;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Persistence;


public class DbConnection {
    private static final EntityManagerFactory managerFactory;

    static {
        HashMap<String, String> properties = new HashMap<>();
        properties.put("jakarta.persistence.jdbc.url", 
            "jdbc:mysql://db:" + System.getenv("DB_PORT") + "/" + System.getenv("MYSQL_DATABASE"));
        properties.put("jakarta.persistence.jdbc.user", System.getenv("MYSQL_USER"));
        properties.put("jakarta.persistence.jdbc.password", System.getenv("MYSQL_PASSWORD"));
        
        managerFactory = Persistence.createEntityManagerFactory("serverPersistence", properties);
    }


    public static void init(){}

    public static EntityManager getDbConnection (){

        return managerFactory.createEntityManager();
    }

    public static void shutDownConnection(){
        managerFactory.close();
    }
}
