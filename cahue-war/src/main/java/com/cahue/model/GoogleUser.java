package com.cahue.model;

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
public class GoogleUser implements Serializable {

    @Id
    private String googleId;
    private Ref<User> user;
    @Index
    private String email;

    public GoogleUser() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
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
        return "GoogleUser{" +
                "email=" + email +
                ", googleId='" + googleId + '\'' +
                '}';
    }

}
