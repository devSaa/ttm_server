package application;

import application.exception.PropertyNotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Class which contains application properties,
 * like currently using interface implementations
 * and database connection parameters
 */
public class ApplicationContext {
    private static String propertyFilePath = "persistence.properties";
    static Properties prop;

    private static final Logger infoLogger = LogManager.getLogger("infoLogger");
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");

    public static void init() {
        prop = new Properties();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(propertyFilePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                prop.setProperty(line.split("=")[0], line.split("=")[1]);
            }

//            prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(propertyFilePath));

            infoLogger.info("Application context initialized");
        } catch (IOException e) {
            errorLogger.error("Can't read property file. Application context initialization failed", e);
            throw new RuntimeException("Can't read property file");
        }
    }

    public static String getProperty(String propertyName) throws PropertyNotFoundException {
        String propertyValue = prop.getProperty(propertyName);
        if (propertyValue == null) {
            throw new PropertyNotFoundException("Can't find property " + propertyName);
        }
        return propertyValue;
    }
}
