package com.cahue.util;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * Created by Francesco on 11/02/2015.
 */
public class AuthenticationException extends WebApplicationException {

    public AuthenticationException() {
        super(Response
                .status(Response.Status.FORBIDDEN)
                .entity("Authorization header required")
                .build());
    }
}
