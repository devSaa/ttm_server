package launcher;

import application.ApplicationContext;
import dao.jdbc.ConnectionManager;
import network.SocketListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Scanner;

/**
 * Application starter. Initialize ApplicationContext and start SocketListener.
 */
public class Launcher {
    private static final Logger infoLogger = LogManager.getLogger("infoLogger");
    private static SocketListener socketListener;

    public static void main(String[] args) {
        infoLogger.info("Application started");

        ApplicationContext.init();

        socketListener = new SocketListener();
        socketListener.start();

        System.out.println("To stop application enter \"shutdown\"");

        while (true) {
            Scanner scanner = new Scanner(System.in);
            if (scanner.nextLine().equals("shutdown")) {
                stopApplication();
                scanner.close();
                break;
            }
        }
    }

    private static void stopApplication() {
        socketListener.close();
        ConnectionManager.close();
        infoLogger.info("Application stopped");
    }
}
