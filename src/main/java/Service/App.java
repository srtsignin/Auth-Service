package Service;

import com.google.gson.Gson;
import edu.rosehulman.csse.rosefire.server.AuthData;
import edu.rosehulman.csse.rosefire.server.RosefireError;
import edu.rosehulman.csse.rosefire.server.RosefireTokenVerifier;
import io.jsonwebtoken.MalformedJwtException;
import models.RolesResponse;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {

    private static String PROPERTIES = "auth-keys.properties";

    public static void main(String[] args) {
        PropertiesLoader.LoadFromFile(PROPERTIES);
        addExceptionConsoleLogger();
        createEndpoints();
    }

    private static void addExceptionConsoleLogger() {
        Spark.exception(Exception.class, (e, request, response) -> {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            System.err.println(sw.getBuffer().toString());
        });
    }

    private static void createEndpoints() {
        System.out.println("Starting App on http://localhost:80/");
        port(80);
        get("/", ((request, response) -> "Welcome to the Auth Server"));
        get("/role", (App::getRole), getJsonTransformer());
    }

    private static ResponseTransformer getJsonTransformer() {
        return object -> new Gson().toJson(object);
    }


    private static RolesResponse getRole(Request request, Response response) {
        String token = request.headers("RosefireToken");

        if (token == null || token.isEmpty()) {
            response.status(400);
            return new RolesResponse("RosefireToken Header not present");
        }

        RosefireTokenVerifier verifier = new RosefireTokenVerifier(System.getProperty("rosefire.secret"));

        AuthData decodedToken;
        try {
            decodedToken = verifier.verify(token);
        } catch (RosefireError | MalformedJwtException error) {
            response.status(200);
            return new RolesResponse("Authorization Failed");
        }

        System.out.println(decodedToken.getUsername());

        return new RolesResponse();
    }
}
