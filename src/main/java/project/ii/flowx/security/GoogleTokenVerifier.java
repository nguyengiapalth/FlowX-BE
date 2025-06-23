package project.ii.flowx.security;


import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import project.ii.flowx.exceptionhandler.FlowXError;
import project.ii.flowx.exceptionhandler.FlowXException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * GoogleTokenVerifier is a utility class that verifies Google ID tokens.
 * It uses the Google API client library to validate the token against the configured client ID.
 */
@Component
public class GoogleTokenVerifier {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    public GoogleIdToken.Payload verify(String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                JacksonFactory.getDefaultInstance()
        )
        .setAudience(Collections.singletonList(clientId))
        .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken == null) {
            throw new FlowXException(FlowXError.INVALID_CREDENTIALS, "Invalid Google ID token: " + idTokenString);
        }
        return idToken.getPayload();
    }
}
