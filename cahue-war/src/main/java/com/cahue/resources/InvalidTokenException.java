package com.cahue.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * This exception is thrown when an error occurs while exchanging Google auth token
 * Date: 20.01.15
 *
 * @author francesco
 */
public class InvalidTokenException extends WebApplicationException {

    public InvalidTokenException(int statusCode, String message) {
        super(Response
                .status(statusCode)
                .entity("Google Auth: " + message)
                .build());
    }
}
