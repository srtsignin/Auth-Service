package Service;

import edu.rosehulman.csse.rosefire.server.RosefireTokenVerifier;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class App {

    public static void main(String[] args) {
        System.out.println("Started App on http://localhost:4567/");

        get("/", ((request, response) -> "Welcome to the Auth Server"));
        get("/role", ((request, response) -> getRole(request, response)));
    }

    private static String[] getRole(Request request, Response response) {
        String[] roles = {"NotImplemented"};
        String token = request.headers("Auth");

//        RosefireTokenVerifier verifier = new RosefireTokenVerifier(SECRET);


        return roles;
    }
}
