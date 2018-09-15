package models;

public class CheckResponse {
    private Boolean isAuthorized;
    private String role;
    private String message;


    public CheckResponse(Boolean isAuthorized, String role, String message) {
        this.role = role;
        this.message = message;
        this.isAuthorized = isAuthorized;
    }

    public CheckResponse(String role, String message) {
        this(false, role, message);
    }

    public Boolean getAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(Boolean authorized) {
        isAuthorized = authorized;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "CheckResponse{" +
                "isAuthorized=" + isAuthorized +
                ", role='" + role + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}