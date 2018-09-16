package service;

import com.google.gson.Gson;
import edu.rosehulman.csse.rosefire.server.AuthData;
import edu.rosehulman.csse.rosefire.server.RosefireTokenVerifier;
import exceptions.DatabaseDriverException;
import exceptions.InvalidTokenException;
import exceptions.MissingTokenException;
import models.CheckResponse;
import models.RolesResponse;
import org.apache.log4j.*;
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
    private static final Logger log = Logger.getLogger(App.class);

    public static void main(String[] args) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.DEBUG);
        PatternLayout layout = new PatternLayout("%d{ISO8601} [%t] %-5p %c %x - %m%n");
        rootLogger.addAppender( new ConsoleAppender(layout));

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
        log.debug("Starting App on http://localhost:80/");
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
        log.debug("Recieved Request at /");
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
            log.error("DBDriverException Occured. Sending 503");
            response.status(503);
            return new CheckResponse(roleToCheck,"Unable to Validate Roles");
        }
    }

    private static String[] getRoles(Request request) {
        String username = getUserFromRosefire(request);
        try {
            log.debug("Attempting to get roles");
            String[] roles = Neo4JDriver.getInstance().getRoles(username);
            if (roles.length == 0) {
                roles = new String[]{"Student"};
            }
            log.debug("Found Roles: " + Arrays.toString(roles));
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

        log.debug("Attempting to verify token");
        RosefireTokenVerifier verifier = new RosefireTokenVerifier(System.getProperty("rosefire.secret"));

        AuthData decodedToken;
        try {
            log.debug("Decoding Token");
            decodedToken = verifier.verify(token);
            log.debug("Decoded Token: " + decodedToken);
        } catch (Exception error) {
            log.warn("Exception occured attempting to validate token " + error.getMessage());
            log.error(error);
            throw new InvalidTokenException("Invalid Rosefire token", error);
        }
        return decodedToken.getUsername();
    }
}
