package vn.vnpay;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.config.DatabaseConfig;
import vn.vnpay.config.ServerConfig;
import vn.vnpay.controlller.AuthController;

import javax.sql.DataSource;

/**
 * @author ducpa
 * Created: 20/08/2023
 */

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            DataSource dataSource = configureAndCreateDataSource();

            Server server = configureAndCreateJettyServer(dataSource);

            server.join();
            LOGGER.info("Main SUCCESS. Jetty server running.");
        } catch (Exception e) {
            LOGGER.info("Error occurred in the main method");
            LOGGER.error("Error occurred in the main method", e);
        }
    }

    private static Server configureAndCreateJettyServer(DataSource dataSource) {
        try {
            ServerConfig serverConfig = new ServerConfig();
            int serverPort = serverConfig.getServerPort();
            String contextPath = serverConfig.getContextPath();
            String servletUrlPattern = serverConfig.getServletUrlPattern();

            Server server = new Server(serverPort);

            ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            contextHandler.setContextPath(contextPath);

            AuthController feeController = new AuthController(dataSource);
            ResourceConfig resourceConfig = new ResourceConfig().registerInstances(feeController);
            ServletContainer servletContainer = new ServletContainer(resourceConfig);
            contextHandler.addServlet(new ServletHolder(servletContainer), servletUrlPattern);

            server.setHandler(contextHandler);
            server.start();

            return server;
        } catch (Exception e) {
            LOGGER.error("Error occurred while creating Jetty server", e);
            throw new RuntimeException("Error occurred while creating Jetty server", e);
        }
    }

    private static DataSource configureAndCreateDataSource() {
        DatabaseConfig dbConfig = new DatabaseConfig();
        try {
            Class.forName(dbConfig.getDriver());
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error loading database driver", e);
            throw new RuntimeException("Error loading database driver", e);
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dbConfig.getUrl());
        hikariConfig.setUsername(dbConfig.getUsername());
        hikariConfig.setPassword(dbConfig.getPassword());
        hikariConfig.setMinimumIdle(dbConfig.getMinimumIdle());
        hikariConfig.setMaximumPoolSize(dbConfig.getMaximumPoolSize());
        hikariConfig.setConnectionTimeout(dbConfig.getConnectionTimeout());

        return new HikariDataSource(hikariConfig);
    }
}