package com.cahue.users;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Person;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Date: 13.01.15
 *
 * @author francesco
 */
public class UserAuthService {

    private Credential credential;

    public static final void main(String[] args) throws Exception {
        new UserAuthService("ya29.-gBs2W-JFt0fMdxcZ93TO-5WS1g1GKQAAub4Z0Oqo1irr6Mo6wMhlkg4Du24AAxXJnpSijwv1CUYHQ");
    }

    public UserAuthService(final String accessToken) {
//        AuthorizationCodeFlow authFlow = null;
        try {
//            authFlow = initializeFlow();
//            Credential credential = authFlow.loadCredential(getUserId(req));
            credential = new GoogleCredential.Builder()
                    .setRequestInitializer((new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest request)
                                throws IOException {
                            request.getHeaders().setAuthorization("Bearer " + accessToken);
                        }
                    }))
                    .build();

            Oauth2 oauth2 = new Oauth2.Builder(UserAuthUtils.HTTP_TRANSPORT, UserAuthUtils.JSON_FACTORY, credential.getRequestInitializer()).build();

            Userinfoplus userinfoplus = oauth2.userinfo().get().execute();
            System.out.println(userinfoplus);

            // Build the Plus object using the credentials
            Plus plus = new Plus.Builder(
                    UserAuthUtils.HTTP_TRANSPORT, UserAuthUtils.JSON_FACTORY, credential).setApplicationName("").build();
            // Make the API call
            Person profile = plus.people().get("me").execute();
            System.out.println(profile);
        }
//        catch (ServletException e) {
//            e.printStackTrace();
//        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
        return UserAuthUtils.initializeFlow();
    }


}
