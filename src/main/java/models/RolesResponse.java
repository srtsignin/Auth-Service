package models;

import java.util.Arrays;

public class RolesResponse {
    private User user;
    private String[] roles;
    private String message;

    public RolesResponse(UserAndRoles userAndRoles, String message) {
        this(message);
        this.user = userAndRoles.getUser();
        this.roles = userAndRoles.getRoles();
    }

    public RolesResponse(String message) {
        this();
        this.message = message;
    }

    public RolesResponse() {
        user = new User();
        roles = new String[]{};
        message = "";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RolesResponse{" +
                "roles=" + Arrays.toString(roles) +
                ", message='" + message + '\'' +
                ", user={" +
                "username='" + user.getUsername() + '\'' +
                ",name='" + user.getName() + '\'' +
                "}}";
    }
}
