package vn.vnpay.indentity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ducpa
 * Created: 20/08/2023
 */
public class AuthManager {
    private final DataSource dataSource;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthManager.class);

    public AuthManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean authenticate(User user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT password FROM user WHERE username = ?")) {

            statement.setString(1, user.getUsername());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String storedPassword = resultSet.getString("password");
                    boolean isAuthenticated = storedPassword.equals(user.getPassword());
                    if (isAuthenticated) {
                        LOGGER.info("User '{}' logged in successfully.", user.getUsername());
                    } else {
                        LOGGER.warn("Failed login attempt for user '{}'.", user.getUsername());
                    }
                    return isAuthenticated;
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error during authentication", e);
        }
        return false;
    }
}
