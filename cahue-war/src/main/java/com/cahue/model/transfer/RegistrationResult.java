package com.cahue.model.transfer;

import com.cahue.model.Car;
import com.cahue.model.GoogleUser;
import com.cahue.model.User;

import java.util.List;

/**
 * Created by Francesco on 07/02/2015.
 */
public class RegistrationResult {

    private User user;

    private List<Car> cars;

    private String authToken;

    private String refreshToken;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
