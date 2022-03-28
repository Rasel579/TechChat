package server;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LifeCycle;
import org.apache.logging.log4j.core.config.*;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfigurationFactory;
import org.apache.logging.log4j.core.lookup.StrLookup;
import server.chat.MyServer;
import java.io.IOException;
import java.sql.SQLException;

import static org.apache.logging.log4j.core.config.Property.*;

public class ServerApp {
    private final  static  int DEFAULT_PORT = 8887;
    private final static Logger LOGGER = LogManager.getLogger(ServerApp.class);

    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        if(args.length != 0){
            port = Integer.parseInt(args[0]);
        }

        try {
            new MyServer(port).start();
        }catch (IOException | SQLException | ClassNotFoundException e){
            LOGGER.log(Level.getLevel("Error"),"Ошибка создания сервера " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
