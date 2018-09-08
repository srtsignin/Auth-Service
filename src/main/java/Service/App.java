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
import java.nio.file.Path;
import java.nio.file.Paths;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {

    private static Path PROPERTIES = Paths.get(".", "secrets", "auth-keys.properties");
    private static Neo4JDriver driver;

    public static void main(String[] args) {
        PropertiesLoader.LoadFromFile(PROPERTIES);
        addExceptionConsoleLogger();

        driver = new Neo4JDriver();
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
        get("/api/auth/", ((request, response) -> "Welcome to the Auth Server"));
        get("/api/auth/role", (App::getRole), getJsonTransformer());
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

        String[] roles = driver.getRoles(decodedToken.getUsername());
        RolesResponse rolesResponse = new RolesResponse(roles, "Successful");

        if(roles.length == 0) {
            rolesResponse.setRoles(new String[] { "Student" });
        }

        return rolesResponse;
    }
}
