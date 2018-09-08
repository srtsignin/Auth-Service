package models;

import java.util.Arrays;

public class RolesResponse {
    private String[] roles;
    private String message;

    public RolesResponse(String[] roles, String message) {
        this(message);
        this.roles = roles;
    }

    public RolesResponse(String message) {
        this();
        this.message = message;
    }

    public RolesResponse() {
        roles = new String[0];
        message = "";
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
                '}';
    }
}
