package vn.vnpay.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
@Getter
@Setter
@AllArgsConstructor
public class ServerConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfig.class);

    private int serverPort;
    private String contextPath;
    private String servletUrlPattern;

    public ServerConfig() {
        try {
            loadProperties();
            LOGGER.info("Server configuration loaded successfully.");
        } catch (IOException e) {
            LOGGER.error("Failed to load server.properties file", e);
        }
    }

    private void loadProperties() throws IOException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(Constants.PROPERTIES_SERVER_FILE_PATH)) {
            if (input != null) {
                properties.load(input);

                serverPort = Integer.parseInt(properties.getProperty(Constants.SERVER_PORT));
                contextPath = properties.getProperty(Constants.SERVER_CONTEXT_PATH);
                servletUrlPattern = properties.getProperty(Constants.SERVER_SERVLET_URL_PATTERN);
                LOGGER.info("Server properties loaded successfully.");
            } else {
                LOGGER.error("Unable to find server.properties file.");
                throw new IOException("Unable to find server.properties file.");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read server.properties file", e);
            throw e;
        }
    }

}