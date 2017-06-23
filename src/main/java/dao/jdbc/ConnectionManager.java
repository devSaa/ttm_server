package dao.jdbc;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import application.ApplicationContext;
import application.exception.PropertyNotFoundException;
import application.exception.InternalServerErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Contains pool of connections to specified database and provide connection on demand.
 */
public class ConnectionManager {
    private static ComboPooledDataSource dataSource;

    private static final Logger infoLogger = LogManager.getLogger("infoLogger");
    private static final Logger errorLogger = LogManager.getLogger("errorLogger");

    static {
        Locale.setDefault(Locale.ENGLISH);

        try {
            String driverClass = ApplicationContext.getProperty("driverClass");
            String jdbcUrl = ApplicationContext.getProperty("jdbcUrl");
            String user = ApplicationContext.getProperty("user");
            String password = ApplicationContext.getProperty("password");

            dataSource = new ComboPooledDataSource();
            // db
            dataSource.setDriverClass(driverClass);
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setUser(user);
            dataSource.setPassword(password);
            // connection pool
            dataSource.setMinPoolSize(10);
            dataSource.setAcquireIncrement(10);
            dataSource.setMaxPoolSize(50);
            // statement pool
            dataSource.setMaxStatements(180);
            dataSource.setMaxStatementsPerConnection(10);

            infoLogger.info("Connection manager initialized");
        } catch (PropertyVetoException e) {
            errorLogger.error("Property can't be set to specified value", e);
            throw new RuntimeException("Property can't be set to specified value", e);
        } catch (PropertyNotFoundException e) {
            errorLogger.error("Property for ConnectionManager not found", e);
            throw new RuntimeException("Property for ConnectionManager not found", e);
        }
    }

    public static Connection getConnection() throws SQLException, InternalServerErrorException {
        if (dataSource != null)
            return dataSource.getConnection();
        else
            throw new InternalServerErrorException("ConnectionManager not initialized properly");
    }

    public static void close() {
        dataSource.close();
        infoLogger.info("Connection manager closed");
    }
}
