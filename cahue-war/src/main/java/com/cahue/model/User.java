package com.cahue.model;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.Ref;
import com.googlecode.objectify.annotation.*;

import javax.xml.bind.annotation.XmlTransient;
import java.util.*;

/**
 * Created by Francesco on 07/02/2015.
 */
@Cache
@Entity
public class User {

    @Id
    private Long id;

    @Load
    private Ref<GoogleUser> googleUser;

    private Date creationDate = new Date();

    private String refreshToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    @XmlTransient
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public GoogleUser getGoogleUser() {
        return googleUser.get();
    }

    public void setGoogleUser(GoogleUser googleUser) {
        this.googleUser = Ref.create(googleUser);
    }

    public Key<User> createKey() {
        return Key.create(User.class, id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", googleUser=" + googleUser +
                ", creationDate=" + creationDate +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
