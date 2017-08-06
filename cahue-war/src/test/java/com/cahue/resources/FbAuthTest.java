package com.cahue.resources;

import com.cahue.auth.UserAuthenticationService;
import org.junit.Test;

/**
 * Date: 05.02.15
 *
 * @author francesco
 */
public class FbAuthTest {

    @Test
    public void testFbAuthToken() {
        UserAuthenticationService authenticationService = new UserAuthenticationService();
        authenticationService.retrieveFacebookUser("EAAMZCEaxaDUEBALtQYGKrdva5klX2c8gMtXjKlpmdEcszehQwe2EIwaURxZCgegIagVj4cua7DW3dxptiBreNzNlT6VMj25Q3bgSDnwl9v4d9k35X4J5r5tTPCtHme7ZBCvvYZBK0SX1qEIHnQPM0TKwucQbwAaIXUsrQwZC9ZBHmAvtgck9tndKg3AIZA79WcUIgGavZCYhKGqKk4SEl5OUQ8bcliAyWZCUZD");
    }
}
