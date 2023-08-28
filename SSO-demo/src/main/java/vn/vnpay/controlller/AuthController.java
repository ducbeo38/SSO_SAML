package vn.vnpay.controlller;

import org.opensaml.Configuration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.model.response.AuthResponse;
import vn.vnpay.model.User;
import vn.vnpay.model.UserRole;
import vn.vnpay.service.AuthService;

import javax.sql.DataSource;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author ducpa
 * Created: 20/08/2023
 */
@Path("/sso")
public class AuthController {

    private final AuthService authService;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    public AuthController(DataSource dataSource) {
        authService = new AuthService(dataSource);
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @QueryParam("username") String username,
            @QueryParam("password") String password,
            @QueryParam("role") String userRole) {

        User user = new User(username, password);
        UserRole role = new UserRole(username, userRole);

        try {
            AuthnRequest authnRequest = authService.createAndSignAuthnRequest(user, role);
            if (authnRequest != null) {
                String authnRequestXML = XMLHelper.nodeToString(Configuration.getMarshallerFactory().getMarshaller(authnRequest).marshall(authnRequest));
                LOGGER.info("Login successful for user: " + user.getUsername());
                AuthResponse<String> authResponse = new AuthResponse<>(200, "Login successful", authnRequestXML);
                return Response.ok(authResponse, MediaType.APPLICATION_JSON).build();
            } else {
                LOGGER.warn("Login failed for user: " + user.getUsername());
                AuthResponse<String> authResponse = new AuthResponse<>(401, "Authentication failed", null);
                return Response.status(Response.Status.UNAUTHORIZED).entity(authResponse).build();
            }
        } catch (Exception e) {
            LOGGER.error("Error creating AuthnRequest", e);
            AuthResponse<String> authResponse = new AuthResponse<>(500, "Error creating AuthnRequest", null);
            return Response.serverError().entity(authResponse).build();
        }
    }
}