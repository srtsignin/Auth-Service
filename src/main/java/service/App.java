package service;

import com.google.gson.Gson;
import edu.rosehulman.csse.rosefire.server.AuthData;
import edu.rosehulman.csse.rosefire.server.RosefireTokenVerifier;
import exceptions.DatabaseDriverException;
import exceptions.InvalidTokenException;
import exceptions.MissingTokenException;
import models.CheckResponse;
import models.RolesResponse;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static spark.Spark.get;
import static spark.Spark.port;

public class App {

    private static final Path PROPERTIES = Paths.get(".", "secrets", "auth-keys.properties");

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
            e.printStackTrace(System.out);
            System.out.println(sw.getBuffer().toString());
        });
    }

    private static void createEndpoints() {
        System.out.println("Starting App on http://localhost:80/");
        port(80);
        get("/test", ((request, response) -> "Welcome to the Auth Server"));
        get("/roles", (App::createRolesResponse), getJsonTransformer());
        get("/", (App::checkRole), getJsonTransformer());
    }

    private static ResponseTransformer getJsonTransformer() {
        return new Gson()::toJson;
    }

    private static RolesResponse createRolesResponse(Request request, Response response) {
        try {
            return new RolesResponse(getRoles(request), "Successful");
        } catch (MissingTokenException error) {
            response.status(400);
            return new RolesResponse("RosefireToken Header not present");
        } catch (InvalidTokenException error) {
            response.status(200);
            return new RolesResponse("Authorization Failed");
        } catch (DatabaseDriverException error) {
            response.status(503);
            return new RolesResponse("Unable to Validate Roles");
        }
    }

    private static CheckResponse checkRole(Request request, Response response) {
        String roleToCheck = request.queryParamOrDefault("role","Student");
        try {
            String[] roles = getRoles(request);
            return new CheckResponse(Arrays.asList(roles).contains(roleToCheck),roleToCheck,"Successful");
        } catch (MissingTokenException error) {
            response.status(400);
            return new CheckResponse(roleToCheck, "RosefireToken Header not present");
        } catch (InvalidTokenException error) {
            response.status(200);
            return new CheckResponse(roleToCheck,"Authorization Failed");
        } catch (DatabaseDriverException error) {
            response.status(503);
            return new CheckResponse(roleToCheck,"Unable to Validate Roles");
        }
    }

    private static String[] getRoles(Request request) {
        String username = getUserFromRosefire(request);
        try {
            System.out.println("Attempting to get roles");
            String[] roles = Neo4JDriver.getInstance().getRoles(username);
            if (roles.length == 0) {
                roles = new String[]{"Student"};
            }
            System.out.println("Found Roles: " + Arrays.toString(roles));
            return roles;
        } catch (Exception error) {
            throw new DatabaseDriverException("Neo4J error", error);
        }
    }

    private static String getUserFromRosefire(Request request) {
        String token = request.headers("RosefireToken");

        if (token == null || token.isEmpty()) {
            throw new MissingTokenException("Missing Rosefire token");
        }

        System.out.println("Attempting to verify token");
        RosefireTokenVerifier verifier = new RosefireTokenVerifier(System.getProperty("rosefire.secret"));

        AuthData decodedToken;
        try {
            System.out.println("Decoding Token");
            decodedToken = verifier.verify(token);
            System.out.println("Decoded Token: " + decodedToken);
        } catch (Exception error) {
            System.out.println("Exception occured attempting to validate token " + error.getMessage());
            error.printStackTrace(System.out);
            throw new InvalidTokenException("Invalid Rosefire token", error);
        }
        return decodedToken.getUsername();
    }
}
