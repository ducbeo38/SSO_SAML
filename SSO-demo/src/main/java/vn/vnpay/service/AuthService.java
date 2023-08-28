package vn.vnpay.service;

import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Subject;
import org.opensaml.xml.security.credential.BasicCredential;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureConstants;
import org.opensaml.xml.signature.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vn.vnpay.authorization.AuthorizationManager;
import vn.vnpay.constant.Constants;
import vn.vnpay.indentity.AuthManager;
import vn.vnpay.model.User;
import vn.vnpay.model.UserRole;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author ducpa
 * Created: 20/08/2023
 */

public class AuthService {

    private final AuthManager authManager;
    private final AuthorizationManager authorizationManager;
    private final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

    public AuthService(DataSource dataSource) {
        try {
            DefaultBootstrap.bootstrap();
        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap OpenSAML", e);
        }
        authManager = new AuthManager(dataSource);
        authorizationManager = new AuthorizationManager(dataSource);
    }

    public AuthnRequest createAndSignAuthnRequest(User user, UserRole userRole) throws Exception {
        if (authManager.authenticate(user) && authorizationManager.hasPermission(userRole)) {
            LOGGER.info("Authentication and authorization successful for user: " + user.getUsername());
            AuthnRequest authnRequest = createAuthnRequest();
            signSAMLObject(authnRequest);
            LOGGER.info("AuthnRequest successfully created and signed.");
            return authnRequest;
        } else {
            LOGGER.warn("Authentication or authorization failed for user: " + user.getUsername());
            return null;
        }
    }

    private AuthnRequest createAuthnRequest() {
        AuthnRequest authnRequest = (AuthnRequest) Configuration.getBuilderFactory().getBuilder(AuthnRequest.DEFAULT_ELEMENT_NAME).buildObject(AuthnRequest.DEFAULT_ELEMENT_NAME);

        // Thiết lập các thuộc tính cho AuthnRequest
        authnRequest.setID(Constants.ID_AUTH); // Đặt ID cho AuthnRequest
        authnRequest.setVersion(SAMLVersion.VERSION_20); // Thiết lập phiên bản SAML
        authnRequest.setIssueInstant(new DateTime()); // Thiết lập thời điểm tạo AuthnRequest

        // Thiết lập Issuer (Service Provider)
        Issuer issuer = (Issuer) Configuration.getBuilderFactory().getBuilder(Issuer.DEFAULT_ELEMENT_NAME).buildObject(Issuer.DEFAULT_ELEMENT_NAME);
        issuer.setValue(Constants.URL_SERVICE_PROVIDER); // URL của Service Provider
        authnRequest.setIssuer(issuer);

        Subject subject = (Subject) Configuration.getBuilderFactory().getBuilder(Subject.DEFAULT_ELEMENT_NAME).buildObject(Subject.DEFAULT_ELEMENT_NAME);
        authnRequest.setSubject(subject);

        // Thiết lập RequestedAuthnContext (Yêu cầu xác thực người dùng)
        RequestedAuthnContext requestedAuthnContext = (RequestedAuthnContext) Configuration.getBuilderFactory().getBuilder(RequestedAuthnContext.DEFAULT_ELEMENT_NAME).buildObject(RequestedAuthnContext.DEFAULT_ELEMENT_NAME);
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);

        AuthnContextClassRef authnContextClassRef = (AuthnContextClassRef) Configuration.getBuilderFactory().getBuilder(AuthnContextClassRef.DEFAULT_ELEMENT_NAME).buildObject(AuthnContextClassRef.DEFAULT_ELEMENT_NAME);
        authnContextClassRef.setAuthnContextClassRef(AuthnContext.PPT_AUTHN_CTX);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

        authnRequest.setRequestedAuthnContext(requestedAuthnContext);

        return authnRequest;
    }

    private void signSAMLObject(SignableSAMLObject samlObject) throws Exception {
        Credential signingCredential = createSigningCredential();

        // Tạo Signature
        Signature signature = (Signature) Configuration.getBuilderFactory().getBuilder(Signature.DEFAULT_ELEMENT_NAME).buildObject(Signature.DEFAULT_ELEMENT_NAME);

        signature.setSigningCredential(signingCredential);
        signature.setSignatureAlgorithm(SignatureConstants.ALGO_ID_SIGNATURE_RSA_SHA256);
        signature.setCanonicalizationAlgorithm(SignatureConstants.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);

        samlObject.setSignature(signature);

        LOGGER.info("Creating SAML signature");

        // Marshalling và ký dữ liệu
        Configuration.getMarshallerFactory().getMarshaller(samlObject).marshall(samlObject);
        Signer.signObject(signature);

        LOGGER.info("SAML object signed");
    }

    private Credential createSigningCredential() throws NoSuchAlgorithmException {
        BasicCredential credential = new BasicCredential();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Constants.KEY);
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        credential.setPrivateKey(privateKey);
        credential.setPublicKey(publicKey);
        return credential;
    }
}