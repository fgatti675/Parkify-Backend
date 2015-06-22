package com.cahue.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.Cache;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;


/**
 * Date: 14.01.15
 *
 * @author francesco
 */
@Cache
@Entity
public class FacebookUser implements Serializable {

    public FacebookUser() {
    }

    @Id
    private String facebookId;

    private Ref<User> user;

    @Index
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }

    @XmlTransient
    public User getUser() {
        return user.get();
    }

    public void setUser(User user) {
        this.user = Ref.create(user);
    }

    @Override
    public String toString() {
        return "FacebookUser{" +
                "email=" + email +
                ", facebookId='" + facebookId + '\'' +
                '}';
    }

    public static Key<FacebookUser> createGoogleUserKey(String googleId) {
        return Key.create(FacebookUser.class, googleId);
    }

}
