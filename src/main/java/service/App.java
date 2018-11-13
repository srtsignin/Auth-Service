package service;

import cardfire.CardfireVerifier;
import com.google.gson.Gson;
import edu.rosehulman.csse.rosefire.server.AuthData;
import edu.rosehulman.csse.rosefire.server.RosefireTokenVerifier;
import exceptions.DatabaseDriverException;
import exceptions.InvalidTokenException;
import exceptions.MissingTokenException;
import models.*;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;
import spark.Spark;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static spark.Spark.*;

public class App {

    private static final Path PROPERTIES = Paths.get(".", "secrets", "auth-keys.properties");
    private static final Logger log = Logger.getLogger(App.class);

    public static void main(String[] args) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);
        PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
        rootLogger.addAppender(new ConsoleAppender(layout));

        PropertiesLoader.LoadFromFile(PROPERTIES);
        addExceptionConsoleLogger();
        createEndpoints();
    }

    private static void addExceptionConsoleLogger() {
        Spark.exception(Exception.class, (e, request, response) -> {
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw, true);
            e.printStackTrace(pw);
            log.error(e);
        });
    }

    private static void createEndpoints() {
        log.warn("Starting App on http://localhost:80/");
        port(8080);
        get("/test", ((request, response) -> "Welcome to the Auth Server"));
        get("/roles", App::createRolesResponse, getJsonTransformer());

        // Admin Endpoints
        get("/all_roles", App::getAllRoles, getJsonTransformer());
        post("/users", ((request, response) -> "Welcome!"));
        get("/users", App::getAllUsersAndRoles, getJsonTransformer());
        delete("/users", ((request, response) -> "Yes"));

        get("/", App::checkRole, getJsonTransformer());
    }

    private static ResponseTransformer getJsonTransformer() {
        return new Gson()::toJson;
    }

    private static UserResponse getAllUsersAndRoles(Request request, Response response) {
        try {
            UserAndRoles userRoles = getUserWithRoles(request);
            checkAdminPermission(userRoles);

            List<UserAndRoles> users = Neo4JDriver.getInstance().getAll();
            return new UserResponse("Successful", users);
        } catch (MissingTokenException e) {
            log.error(e);
            response.status(400);
            return new UserResponse("No AuthToken provided");
        } catch (DatabaseDriverException e) {
            log.error(e);
            response.status(500);
            return new UserResponse("Unable to reach the server");
        } catch (Exception e) {
            log.error(e);
            response.status(403);
            return new UserResponse("Insufficient Permissions to complete request");
        }
    }

    private static RolesResponse getAllRoles(Request request, Response response) {
        try {
            UserAndRoles userRoles = getUserWithRoles(request);
            checkAdminPermission(userRoles);

            String[] roles = Neo4JDriver.getInstance().getAllRoles();
            return new RolesResponse(new UserAndRoles(roles, null), "Successful");
        } catch (MissingTokenException e) {
            log.error(e);
            response.status(400);
            return new RolesResponse("No AuthToken provided");
        } catch (DatabaseDriverException e) {
            log.error(e);
            response.status(500);
            return new RolesResponse("Unable to reach the server");
        } catch (Exception e) {
            log.error(e);
            response.status(403);
            return new RolesResponse("Insufficient Permissions to complete request");
        }
    }

    private static void checkAdminPermission(UserAndRoles userRoles) {
        for (String role : userRoles.getRoles()) {
            if (role.equals("Admin")) {
                return;
            }
        }
        throw new RuntimeException("Insufficient Permissions");
    }

    private static RolesResponse createRolesResponse(Request request, Response response) {
        try {
            return new RolesResponse(getUserWithRoles(request), "Successful");
        } catch (MissingTokenException error) {
            log.error(error);
            response.status(400);
            return new RolesResponse("AuthToken Header not present");
        } catch (InvalidTokenException error) {
            log.error(error);
            response.status(200);
            return new RolesResponse("Authorization Failed");
        } catch (DatabaseDriverException error) {
            log.error("It broke", error);
            response.status(503);
            return new RolesResponse("Unable to Validate Roles");
        }
    }

    private static CheckResponse checkRole(Request request, Response response) {
        log.warn("Recieved Request at /");
        String roleToCheck = request.queryParamOrDefault("role", "Student");
        try {
            UserAndRoles userAndRoles = getUserWithRoles(request);
            return new CheckResponse(Arrays.asList(userAndRoles.getRoles()).contains(roleToCheck), roleToCheck, "Successful");
        } catch (MissingTokenException error) {
            log.error(error);
            response.status(400);
            return new CheckResponse(roleToCheck, "AuthToken Header not present");
        } catch (InvalidTokenException error) {
            log.error(error);
            response.status(200);
            return new CheckResponse(roleToCheck, "Authorization Failed");
        } catch (DatabaseDriverException error) {
            log.error(error);
            response.status(503);
            return new CheckResponse(roleToCheck, "Unable to Validate Roles");
        }
    }

    private static UserAndRoles getUserWithRoles(Request request) {
        User user = getUser(request);
        try {
            log.debug("Attempting to get roles");
            String[] roles = Neo4JDriver.getInstance().getRoles(user.getUsername());
            log.debug(Arrays.toString(roles));
            if (roles.length == 0) {
                roles = new String[]{"Student"};
            }
            log.debug("Found Roles: " + Arrays.toString(roles));
            return new UserAndRoles(roles, user);
        } catch (Exception error) {
            log.error("Error occured getting roles", error);
            throw new DatabaseDriverException("Neo4J error", error);
        }
    }

    private static User getUser(Request request) {
        String token = request.headers("AuthToken");

        if (token == null || token.isEmpty()) {
            throw new MissingTokenException("Missing AuthToken token");
        }

        log.debug("Attempting to verify token");
        RosefireTokenVerifier verifier = new RosefireTokenVerifier(System.getProperty("rosefire.secret"));

        AuthData decodedToken;
        try {
            log.debug("Decoding Token");
            decodedToken = verifier.verify(token);
            log.debug("Decoded Token: " + decodedToken);
        } catch (Exception error) {
            log.warn("Exception occured attempting to validate Rosefire token " + error.getMessage());
            log.error(error);
            return getUserFromCardfire(token);
        }
        return new User(decodedToken.getUsername(), decodedToken.getName());
    }

    private static User getUserFromCardfire(String token) {
        CardfireVerifier verifier = new CardfireVerifier();
        User user = verifier.verifyCardfireToken(token);
        log.debug(user);
        return user;
    }
}
