package models;

import java.lang.reflect.Array;

public class UserAndRoles {
    /**
     * I know what you're thinking, this class is dumb. Well let me tell ya, Java is dumb sometimes.
     */

    private String[] roles;
    private User user;

    public UserAndRoles(String[] roles, User user) {
        this.roles = roles;
        this.user = user;
    }

    public UserAndRoles() {
        this.roles = new String[]{};
        this.user = new User();
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
