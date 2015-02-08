package com.cahue.model.transfer;

/**
 * Created by francesco on 19.01.2015.
 */
public class RegistrationRequestBean {

    private String googleAuthToken;

    public String getGoogleAuthToken() {
        return googleAuthToken;
    }

    public void setGoogleAuthToken(String googleAuthToken) {
        this.googleAuthToken = googleAuthToken;
    }

    private String deviceRegId;

    public String getDeviceRegId() {
        return deviceRegId;
    }

    public void setDeviceRegId(String deviceRegId) {
        this.deviceRegId = deviceRegId;
    }
}
