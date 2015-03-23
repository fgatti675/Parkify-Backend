package com.cahue.auth;

import com.cahue.auth.UserAuthenticationService;
import com.cahue.auth.UserService;
import com.cahue.model.User;
import com.google.inject.Provider;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

@Singleton
public final class AuthenticationFilter implements Filter
{
    public static final String DEVICE_HEADER = "Device";

    @Inject
    private UserAuthenticationService authService;

    @Inject
    private Provider<UserService> userServiceProvider;

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)request;

        String token = req.getHeader(UserAuthenticationService.AUTH_HEADER);
        String deviceId = req.getHeader(DEVICE_HEADER);

        if (authService.validateToken(token)) {

            User user = authService.retrieveUser(token);
            UserService userService = userServiceProvider.get();
            userService.setCurrentUser(user);
            userService.setDeviceId(deviceId);

            try {
                chain.doFilter(request, response);
            } finally {
                userService.setCurrentUser(null);
                userService.setDeviceId(null);
            }

        } else {
            // TODO auth exception
//            throw new RuntimeException("Not authenticated");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // TODO Auto-generated method stub

    }
}