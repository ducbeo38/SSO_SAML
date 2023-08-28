package vn.vnpay.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.model.UserRole;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ducpa
 * Created: 22/08/2023
 */
public class AuthorizationManager {
    private final DataSource dataSource;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthorizationManager.class);

    public AuthorizationManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean hasPermission(UserRole userRole) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT role FROM userrole WHERE username = ?")) {

            statement.setString(1, userRole.getUsername());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String roleFromDatabase = resultSet.getString("role");
                    if (userRole.getRole().equals(roleFromDatabase)) {
                        LOGGER.info("User '{}' has the required role: {}", userRole.getUsername(), userRole.getRole());
                        return true;
                    } else {
                        LOGGER.warn("User '{}' does not have the required role: {}", userRole.getUsername(), userRole.getRole());
                    }
                } else {
                    LOGGER.warn("User '{}' not found in the database.", userRole.getUsername());
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error during authorization", e);
        }
        return false;
    }
}