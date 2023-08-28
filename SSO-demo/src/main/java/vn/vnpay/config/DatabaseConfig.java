package vn.vnpay.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.constant.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author ducpa
 * Created: 21/08/2023
 */
@Data
@AllArgsConstructor
public class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);

    private String url;
    private String username;
    private String password;
    private String driver;

    private int minimumIdle;
    private int maximumPoolSize;
    private int connectionTimeout;

    public DatabaseConfig() {
        try {
            loadProperties();
        } catch (IOException e) {
            LOGGER.error("Error while loading database.properties file.", e);
        } catch (NumberFormatException e) {
            LOGGER.error("Error while parsing numeric properties in database.properties file.", e);
        }
    }

    private void loadProperties() throws IOException, NumberFormatException {
        Properties properties = new Properties();
        InputStream input = getClass().getClassLoader().getResourceAsStream(Constants.PROPERTIES_FILE_PATH);

        if (input != null) {
            properties.load(input);

            url = properties.getProperty(Constants.URL);
            username = properties.getProperty(Constants.USERNAME);
            password = properties.getProperty(Constants.PASSWORD);
            driver = properties.getProperty(Constants.DRIVE);
            minimumIdle = Integer.parseInt(properties.getProperty(Constants.MINIMUM_IDLE));
            maximumPoolSize = Integer.parseInt(properties.getProperty(Constants.MAXIMUM_POOL_SIZE));
            connectionTimeout = Integer.parseInt(properties.getProperty(Constants.CONNECTION_TIMEOUT));

            LOGGER.info("Database configuration loaded successfully.");
        } else {
            LOGGER.error("Unable to find database.properties file.");
        }
    }
}