package models;

import java.util.ArrayList;
import java.util.List;

public class UserResponse {

    private List<UserAndRoles> users;
    private String message;

    public UserResponse() {
        this("Failed");
    }

    public UserResponse(String message){
        this(message, new ArrayList<>());
    }

    public UserResponse(String message, List<UserAndRoles> users){
        this.message = message;
        this.users = users;
    }

    public List<UserAndRoles> getUsers() {
        return users;
    }

    public void setUsers(List<UserAndRoles> users) {
        this.users = users;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "users=" + users +
                ", message='" + message + '\'' +
                '}';
    }
}
