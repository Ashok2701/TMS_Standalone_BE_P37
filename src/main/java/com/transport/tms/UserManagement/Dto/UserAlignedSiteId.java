package com.transport.tms.UserManagement.Dto;

import com.transport.tms.UserManagement.Entity.User;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;


public class UserAlignedSiteId implements Serializable {
    private String user;
    private String fcy;

    public UserAlignedSiteId() {
    }
    public UserAlignedSiteId(String user, String fcy) {
        this.user = user;
        this.fcy    = fcy;
    }
    // Getters and Setters
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getFcy() { return fcy; }
    public void setFcy(String fcy) { this.fcy = fcy; }

    // Override equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAlignedSiteId that = (UserAlignedSiteId) o;
        return Objects.equals(user, that.user) && Objects.equals(fcy, that.fcy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, fcy);
    }
    // Getters, setters, equals, and hashCode methods
}
