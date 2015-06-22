package com.cahue.auth;

import com.cahue.model.User;
import com.google.inject.Provider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

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

        if (token != null && authService.validateToken(token)) {

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